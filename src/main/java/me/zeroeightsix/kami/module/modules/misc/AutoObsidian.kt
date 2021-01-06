package me.zeroeightsix.kami.module.modules.misc

import baritone.api.pathing.goals.Goal
import baritone.api.pathing.goals.GoalNear
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.event.Phase
import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.OnUpdateWalkingPlayerEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.process.AutoObsidianProcess
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.EntityUtils.getDroppedItem
import me.zeroeightsix.kami.util.WorldUtils.placeBlock
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.math.RotationUtils.getRotationTo
import me.zeroeightsix.kami.util.math.VectorUtils
import me.zeroeightsix.kami.util.math.VectorUtils.toVec3dCenter
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.onMainThreadSafe
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.block.BlockShulkerBox
import net.minecraft.block.state.IBlockState
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.inventory.GuiShulkerBox
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.Blocks
import net.minecraft.init.Enchantments
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.ClickType
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.extension.ceilToInt
import org.kamiblue.commons.interfaces.DisplayEnum
import org.kamiblue.event.listener.listener

@Module.Info(
    name = "AutoObsidian",
    category = Module.Category.MISC,
    description = "Breaks down Ender Chests to restock obsidian"
)
object AutoObsidian : Module() {
    private val fillMode by setting("FillMode", FillMode.TARGET_STACKS)
    private val searchShulker by setting("SearchShulker", false)
    private val leaveEmptyShulkers by setting("LeaveEmptyShulkers", true, { searchShulker })
    private val autoRefill by setting("AutoRefill", false, { fillMode != FillMode.INFINITE })
    private val threshold by setting("RefillThreshold", 8, 1..56, 1, { autoRefill && fillMode != FillMode.INFINITE })
    private val targetStacks by setting("TargetStacks", 1, 1..20, 1, { fillMode == FillMode.TARGET_STACKS })
    private val delayTicks by setting("DelayTicks", 5, 0..10, 1)
    private val rotationMode by setting("RotationMode", RotationMode.SPOOF)
    private val maxReach by setting("MaxReach", 4.5f, 2.0f..6.0f, 0.1f)

    private enum class FillMode(override val displayName: String, val message: String) : DisplayEnum {
        TARGET_STACKS("Target Stacks", "Target stacks reached"),
        FILL_INVENTORY("Fill Inventory", "Inventory filled"),
        INFINITE("Infinite", "")
    }

    enum class State(override val displayName: String) : DisplayEnum {
        SEARCHING("Searching"),
        PLACING("Placing"),
        PRE_MINING("Pre Mining"),
        MINING("Mining"),
        COLLECTING("Collecting"),
        DONE("Done")
    }

    private enum class SearchingState(override val displayName: String) : DisplayEnum {
        PLACING("Placing"),
        OPENING("Opening"),
        PRE_MINING("Pre Mining"),
        MINING("Mining"),
        COLLECTING("Collecting"),
        DONE("Done")
    }

    @Suppress("UNUSED")
    private enum class RotationMode(override val displayName: String) : DisplayEnum {
        OFF("Off"),
        SPOOF("Spoof"),
        VIEW_LOCK("View Lock")
    }

    var goal: Goal? = null; private set
    var state = State.SEARCHING; private set
    private var searchingState = SearchingState.PLACING

    private var active = false
    private var placingPos = BlockPos(0, -1, 0)
    private var shulkerBoxId = 0
    private var lastHitVec: Vec3d? = null

    private val tickTimer = TickTimer(TimeUnit.TICKS)
    private val rotateTimer = TickTimer(TimeUnit.TICKS)
    private val shulkerOpenTimer = TickTimer(TimeUnit.TICKS)
    private val renderer = ESPRenderer().apply { aFilled = 33; aOutline = 233 }

    override fun isActive(): Boolean {
        return isEnabled && active
    }

    override fun onEnable() {
        state = State.SEARCHING
    }

    override fun onDisable() {
        reset()
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END || !tickTimer.tick(delayTicks.toLong())) return@safeListener

            updateState()
            when (state) {
                State.SEARCHING -> {
                    searchingState()
                }
                State.PLACING -> {
                    placeEnderChest(placingPos)
                }
                State.PRE_MINING -> {
                    mineBlock(placingPos, true)
                }
                State.MINING -> {
                    mineBlock(placingPos, false)
                }
                State.COLLECTING -> {
                    collectDroppedItem(Blocks.OBSIDIAN.id)
                }
                State.DONE -> {
                    if (!autoRefill) {
                        MessageSendHelper.sendChatMessage("$chatName ${fillMode.message}, disabling.")
                        disable()
                    } else {
                        if (active) MessageSendHelper.sendChatMessage("$chatName ${fillMode.message}, stopping.")
                        reset()
                    }
                }
            }
        }

        listener<RenderWorldEvent> {
            if (state != State.DONE) renderer.render(clear = false, cull = true)
        }

        safeListener<OnUpdateWalkingPlayerEvent> { event ->
            if (event.phase != Phase.PRE || rotateTimer.tick(20L, false)) return@safeListener
            val rotation = lastHitVec?.let { getRotationTo(it) } ?: return@safeListener

            when (rotationMode) {
                RotationMode.SPOOF -> {
                    val packet = PlayerPacketManager.PlayerPacket(rotating = true, rotation = rotation)
                    PlayerPacketManager.addPacket(this@AutoObsidian, packet)
                }
                RotationMode.VIEW_LOCK -> {
                    player.rotationYaw = rotation.x
                    player.rotationPitch = rotation.y
                }
                else -> {
                    // Rotation off
                }
            }
        }
    }

    private fun SafeClientEvent.updateState() {
        if (state != State.DONE) {
            updatePlacingPos()

            if (!active) {
                active = true
                BaritoneUtils.primary?.pathingControlManager?.registerProcess(AutoObsidianProcess)
            }

            if (state != State.COLLECTING && searchingState != SearchingState.COLLECTING) {
                goal = if (player.getDistanceSqToCenter(placingPos) > 2.0) {
                    GoalNear(placingPos, 2)
                } else {
                    null
                }
            }
        }

        updateMainState()
        updateSearchingState()
    }

    private fun SafeClientEvent.updatePlacingPos() {
        val eyePos = player.getPositionEyes(1f)
        if (isPositionValid(placingPos, world.getBlockState(placingPos), eyePos)) return

        val posList = VectorUtils.getBlockPosInSphere(eyePos, maxReach)
            .sortedBy { it.distanceSqToCenter(eyePos.x, eyePos.y, eyePos.z) }
            .map { it to world.getBlockState(it) }
            .toList()

        val pair = posList.find { it.second.block == Blocks.ENDER_CHEST || it.second.block is BlockShulkerBox }
            ?: posList.find { isPositionValid(it.first, it.second, eyePos) }

        if (pair != null) {
            placingPos = pair.first
            renderer.clear()
            renderer.add(pair.first, ColorHolder(64, 255, 64))
        } else {
            MessageSendHelper.sendChatMessage("$chatName No valid position for placing shulker box / ender chest nearby, disabling.")
            mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
            disable()
        }
    }

    private fun SafeClientEvent.isPositionValid(pos: BlockPos, blockState: IBlockState, eyePos: Vec3d) =
        !world.getBlockState(pos.down()).material.isReplaceable
            && (blockState.block.let { it == Blocks.ENDER_CHEST || it is BlockShulkerBox }
            || WorldUtils.isPlaceable(pos))
            && world.isAirBlock(pos.up())
            && world.rayTraceBlocks(eyePos, pos.toVec3dCenter())?.let { it.typeOfHit == RayTraceResult.Type.MISS } ?: true

    private fun SafeClientEvent.updateMainState() {
        val passCountCheck = checkObbyCount()

        state = when {
            state == State.DONE && autoRefill && InventoryUtils.countItemAll(Blocks.OBSIDIAN.id) < threshold -> {
                State.SEARCHING
            }
            state == State.COLLECTING && (!canPickUpObby() || getDroppedItem(Blocks.OBSIDIAN.id, 8.0f) == null) -> {
                State.DONE
            }
            state != State.DONE && world.isAirBlock(placingPos) && !passCountCheck -> {
                State.COLLECTING
            }
            state == State.MINING && world.isAirBlock(placingPos) -> {
                State.PLACING
            }
            state == State.PLACING && !world.isAirBlock(placingPos) -> {
                State.PRE_MINING
            }
            state == State.SEARCHING && searchingState == SearchingState.DONE && passCountCheck -> {
                State.PLACING
            }
            else -> {
                state
            }
        }
    }

    /**
     * Check if we can pick up more obsidian:
     * There must be at least one slot which is either empty, or contains a stack of obsidian less than 64
     */
    private fun SafeClientEvent.canPickUpObby(): Boolean {
        return fillMode == FillMode.INFINITE || player.inventory?.mainInventory?.any {
            it.isEmpty || it.item.id == Blocks.OBSIDIAN.id && it.count < 64
        } ?: false
    }

    /**
     * @return `true` if can still place more ender chest
     */
    private fun SafeClientEvent.checkObbyCount(): Boolean {
        val inventory = InventoryUtils.countItemAll(Blocks.OBSIDIAN.id)
        val dropped = EntityUtils.getDroppedItems(Blocks.OBSIDIAN.id, 8.0f).sumBy { it.item.count }

        return when (fillMode) {
            FillMode.TARGET_STACKS -> {
                ((inventory + dropped) / 8.0f).ceilToInt() / 8 <= targetStacks
            }
            FillMode.FILL_INVENTORY -> {
                countEmptySlots() - dropped >= 8
            }
            FillMode.INFINITE -> {
                true
            }
        }
    }

    private fun SafeClientEvent.countEmptySlots(): Int {
        return player.inventory?.mainInventory?.sumBy {
            when {
                it.isEmpty -> 64
                it.item.id == Blocks.OBSIDIAN.id -> 64 - it.count
                else -> 0
            }
        } ?: 0
    }

    private fun SafeClientEvent.updateSearchingState() {
        if (state == State.SEARCHING) {
            if (searchingState != SearchingState.DONE) {
                searchingState = when {
                    searchingState == SearchingState.PLACING && InventoryUtils.countItemAll(Blocks.ENDER_CHEST.id) > 0 -> {
                        SearchingState.DONE
                    }
                    searchingState == SearchingState.COLLECTING && getDroppedItem(shulkerBoxId, 8.0f) == null -> {
                        SearchingState.DONE
                    }
                    searchingState == SearchingState.MINING && world.isAirBlock(placingPos) -> {
                        if (InventoryUtils.countItemAll(Blocks.ENDER_CHEST.id) > 0) {
                            SearchingState.COLLECTING
                        } else {
                            // In case if the shulker wasn't placed due to server lag
                            SearchingState.PLACING
                        }
                    }
                    searchingState == SearchingState.OPENING && (InventoryUtils.countItemAll(Blocks.ENDER_CHEST.id) > 0
                        || InventoryUtils.getSlots(0, 35, 0) == null) -> {
                        SearchingState.PRE_MINING
                    }
                    searchingState == SearchingState.PLACING && !world.isAirBlock(placingPos) -> {
                        if (world.getBlockState(placingPos).block is BlockShulkerBox) {
                            SearchingState.OPENING
                        } else {
                            // In case if the shulker wasn't placed due to server lag
                            SearchingState.PRE_MINING
                        }
                    }
                    else -> {
                        searchingState
                    }
                }
            }
        } else {
            searchingState = SearchingState.PLACING
        }
    }

    private fun SafeClientEvent.searchingState() {
        if (searchShulker) {
            when (searchingState) {
                SearchingState.PLACING -> {
                    placeShulker(placingPos)
                }
                SearchingState.OPENING -> {
                    openShulker(placingPos)
                }
                SearchingState.PRE_MINING -> {
                    mineBlock(placingPos, true)
                }
                SearchingState.MINING -> {
                    mineBlock(placingPos, false)
                }
                SearchingState.COLLECTING -> {
                    collectDroppedItem(shulkerBoxId)
                }
                SearchingState.DONE -> {
                    updatePlacingPos()
                }
            }
        } else {
            searchingState = SearchingState.DONE
        }
    }

    /**
     * @return The id of a shulker found in the hotbar, else returns -1
     */
    private fun getShulkerIdInHotbar(): Int {
        for (shulkerId in 219..234) {
            if (InventoryUtils.getSlotsHotbar(shulkerId) != null) return shulkerId
        }
        return -1
    }

    /**
     * @return the id of a shulker found in a non-hotbar slot, else returns -1
     */
    private fun getShulkerIdNotInHotBar(): Int {
        for (shulkerId in 219..234) {
            if (InventoryUtils.getSlotsNoHotbar(shulkerId) != null) return shulkerId
        }
        return -1
    }

    private fun SafeClientEvent.placeShulker(pos: BlockPos) {
        shulkerBoxId = getShulkerIdInHotbar()
        val shulkerIdNotInHotbar = getShulkerIdNotInHotBar()
        if (shulkerBoxId == -1 && shulkerIdNotInHotbar != -1) {
            InventoryUtils.moveToHotbar(shulkerIdNotInHotbar, Items.DIAMOND_PICKAXE.id)
            shulkerBoxId = shulkerIdNotInHotbar
            return
        } else if (shulkerBoxId == -1) {
            MessageSendHelper.sendChatMessage("$chatName No shulker box was found in hotbar, disabling.")
            mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
            disable()
            return
        }

        InventoryUtils.swapSlotToItem(shulkerBoxId)
        if (world.getBlockState(pos).block !is BlockShulkerBox) {
            placeBlock(pos)
        }
    }

    private fun SafeClientEvent.placeEnderChest(pos: BlockPos) {
        /* Case where we need to move ender chests into the hotbar */
        if (InventoryUtils.getSlotsHotbar(Blocks.ENDER_CHEST.id) == null && InventoryUtils.getSlotsNoHotbar(Blocks.ENDER_CHEST.id) != null) {
            InventoryUtils.moveToHotbar(Blocks.ENDER_CHEST.id, Items.DIAMOND_PICKAXE.id)
            return
        } else if (InventoryUtils.getSlots(0, 35, Blocks.ENDER_CHEST.id) == null) {
            /* Case where we are out of ender chests */
            if (searchShulker) {
                state = State.SEARCHING
            } else {
                MessageSendHelper.sendChatMessage("$chatName No ender chest was found in inventory, disabling.")
                mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                disable()
                return
            }
        }

        /* Else, we already have ender chests in the hotbar */
        InventoryUtils.swapSlotToItem(Blocks.ENDER_CHEST.id)

        placeBlock(pos)
    }

    private fun SafeClientEvent.openShulker(pos: BlockPos) {
        if (mc.currentScreen is GuiShulkerBox) {
            val container = player.openContainer
            val slot = container.inventory.subList(0, 27).indexOfFirst { it.item.id == Blocks.ENDER_CHEST.id }

            if (slot != -1) {
                InventoryUtils.inventoryClick(container.windowId, slot, 0, ClickType.QUICK_MOVE)
                player.closeScreen()
            } else if (shulkerOpenTimer.tick(100, false)) { // Wait for maximum of 5 seconds
                if (leaveEmptyShulkers && container.inventory.subList(0, 27).indexOfFirst { it.item.id != Items.AIR.id } == -1) {
                    searchingState = SearchingState.PRE_MINING
                    player.closeScreen()
                } else {
                    MessageSendHelper.sendChatMessage("$chatName No ender chest was found in shulker, disabling.")
                    mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                    disable()
                }
            }
        } else {
            val side = EnumFacing.getDirectionFromEntityLiving(pos, player)
            val hitVecOffset = WorldUtils.getHitVecOffset(side)

            lastHitVec = WorldUtils.getHitVec(pos, side)
            rotateTimer.reset()

            if (shulkerOpenTimer.tick(50)) {
                defaultScope.launch {
                    delay(10L)
                    onMainThreadSafe {
                        connection.sendPacket(CPacketPlayerTryUseItemOnBlock(pos, side, EnumHand.MAIN_HAND, hitVecOffset.x.toFloat(), hitVecOffset.y.toFloat(), hitVecOffset.z.toFloat()))
                        player.swingArm(EnumHand.MAIN_HAND)
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.placeBlock(pos: BlockPos) {
        val pair = WorldUtils.getNeighbour(pos, 1, 6.5f)
            ?: run {
                MessageSendHelper.sendChatMessage("$chatName Can't find neighbour block")
                return
            }

        lastHitVec = WorldUtils.getHitVec(pair.second, pair.first)
        rotateTimer.reset()

        connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING))

        defaultScope.launch {
            delay(10L)
            onMainThreadSafe {
                placeBlock(pair.second, pair.first)
            }

            delay(10L)
            onMainThreadSafe {
                connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))
            }
        }
    }

    /**
     * Gets the first hotbar slot of a diamond pickaxe that does not have the silk touch enchantment.
     * @return The position of the pickaxe. -1 if there is no match.
     */
    private fun SafeClientEvent.getHotbarNonSilkTouchPick(): Int {
        val slotsWithPickaxes = InventoryUtils.getSlotsHotbar(Items.DIAMOND_PICKAXE.id)
            ?: return -1

        for (slot in slotsWithPickaxes) {
            if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.inventory.getStackInSlot(slot)) == 0) {
                return slot
            }
        }

        return -1
    }

    /**
     * Gets the first non-hotbar slot of a diamond pickaxe that does not have the silk touch enchantment.
     * @return The position of the pickaxe. -1 if there is no match.
     */
    private fun SafeClientEvent.getInventoryNonSilkTouchPick(): Int {
        val slotsWithPickaxes = InventoryUtils.getSlotsNoHotbar(Items.DIAMOND_PICKAXE.id)
            ?: return -1

        for (slot in slotsWithPickaxes) {
            if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.inventory.getStackInSlot(slot)) == 0) {
                return slot
            }
        }

        return -1
    }

    /**
     * Swaps the active hotbar slot to one which has a valid pickaxe (i.e. non-silk touch). If there is no valid pickaxe,
     * disable the module.
     */
    private fun SafeClientEvent.swapToValidPickaxe() {
        var hotbarPickaxeSlot = getHotbarNonSilkTouchPick()
        val inventoryPickaxeSlot = getInventoryNonSilkTouchPick()

        if ((hotbarPickaxeSlot == -1) && (inventoryPickaxeSlot != -1)) {
            /* Note that slot 36 in windowId 0 is the same as slot 0 in the hotbar */
            InventoryUtils.moveToSlot(0, inventoryPickaxeSlot, 36)
            hotbarPickaxeSlot = 0
        } else if (hotbarPickaxeSlot == -1) {
            MessageSendHelper.sendChatMessage("No valid pickaxe was found in inventory.")
            mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
            disable()
            return
        }

        InventoryUtils.swapSlot(hotbarPickaxeSlot)
    }

    private fun SafeClientEvent.mineBlock(pos: BlockPos, pre: Boolean) {
        if (pre) swapToValidPickaxe()

        val side = EnumFacing.getDirectionFromEntityLiving(pos, player)
        lastHitVec = WorldUtils.getHitVec(pos, side)
        rotateTimer.reset()

        defaultScope.launch {
            delay(5L)
            onMainThreadSafe {
                if (pre) {
                    connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, side))
                    if (state != State.SEARCHING) state = State.MINING else searchingState = SearchingState.MINING
                } else {
                    connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, side))
                }
                player.swingArm(EnumHand.MAIN_HAND)
            }
        }
    }

    private fun collectDroppedItem(itemId: Int) {
        goal = if (getDroppedItem(itemId, 8.0f) != null) {
            GoalNear(getDroppedItem(itemId, 8.0f), 0)
        } else {
            null
        }
    }

    private fun reset() {
        active = false
        goal = null
        searchingState = SearchingState.PLACING
        placingPos = BlockPos(0, -1, 0)
        lastHitVec = null
    }
}

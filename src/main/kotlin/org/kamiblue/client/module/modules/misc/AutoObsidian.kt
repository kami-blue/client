package org.kamiblue.client.module.modules.misc

import baritone.api.pathing.goals.Goal
import baritone.api.pathing.goals.GoalNear
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.RenderWorldEvent
import org.kamiblue.client.manager.managers.PlayerPacketManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.process.AutoObsidianProcess
import org.kamiblue.client.util.*
import org.kamiblue.client.util.EntityUtils.getDroppedItem
import org.kamiblue.client.util.WorldUtils.getNeighbour
import org.kamiblue.client.util.WorldUtils.isPlaceable
import org.kamiblue.client.util.WorldUtils.placeBlock
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.ESPRenderer
import org.kamiblue.client.util.items.*
import org.kamiblue.client.util.math.RotationUtils.getRotationTo
import org.kamiblue.client.util.math.VectorUtils
import org.kamiblue.client.util.math.VectorUtils.toVec3dCenter
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.defaultScope
import org.kamiblue.client.util.threads.onMainThreadSafe
import org.kamiblue.client.util.threads.safeListener
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
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemShulkerBox
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

internal object AutoObsidian : Module(
    name = "AutoObsidian",
    category = Category.MISC,
    description = "Breaks down Ender Chests to restock obsidian",
    modulePriority = 15
) {
    private val fillMode by setting("Fill Mode", FillMode.TARGET_STACKS)
    private val searchShulker by setting("Search Shulker", false)
    private val leaveEmptyShulkers by setting("Leave Empty Shulkers", true, { searchShulker })
    private val autoRefill by setting("Auto Refill", false, { fillMode != FillMode.INFINITE })
    private val threshold by setting("Refill Threshold", 8, 1..63, 1, { autoRefill && fillMode != FillMode.INFINITE })
    private val targetStacks by setting("Target Stacks", 1, 1..20, 1, { fillMode == FillMode.TARGET_STACKS })
    private val delayTicks by setting("Delay Ticks", 5, 0..10, 1)
    private val rotationMode by setting("Rotation Mode", RotationMode.SPOOF)
    private val maxReach by setting("Max Reach", 4.5f, 2.0f..6.0f, 0.1f)

    private enum class FillMode(override val displayName: String, val message: String) : DisplayEnum {
        TARGET_STACKS("Target Stacks", "Target stacks reached"),
        FILL_INVENTORY("Fill Inventory", "Inventory filled"),
        INFINITE("Infinite", "")
    }

    enum class State(override val displayName: String) : DisplayEnum {
        SEARCHING("Searching"),
        PLACING("Placing"),
        MINING("Mining"),
        COLLECTING("Collecting"),
        DONE("Done")
    }

    enum class SearchingState(override val displayName: String) : DisplayEnum {
        PLACING("Placing"),
        OPENING("Opening"),
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
    var searchingState = SearchingState.PLACING; private set

    private var active = false
    private var placingPos = BlockPos(0, -1, 0)
    private var shulkerID = 0
    private var lastHitVec: Vec3d? = null

    private val tickTimer = TickTimer(TimeUnit.TICKS)
    private val rotateTimer = TickTimer(TimeUnit.TICKS)
    private val shulkerOpenTimer = TickTimer(TimeUnit.TICKS)
    private val renderer = ESPRenderer().apply { aFilled = 33; aOutline = 233 }

    override fun isActive(): Boolean {
        return isEnabled && active
    }

    init {
        onEnable {
            state = State.SEARCHING
        }

        onDisable {
            reset()
        }

        safeListener<TickEvent.ClientTickEvent>(69) {
            if (it.phase != TickEvent.Phase.START) return@safeListener

            runAutoObby()
            doRotation()
        }

        listener<RenderWorldEvent> {
            if (state != State.DONE) renderer.render(clear = false, cull = true)
        }
    }

    private fun SafeClientEvent.doRotation() {
        if (rotateTimer.tick(20L, false)) return

        val rotation = lastHitVec?.let { getRotationTo(it) } ?: return

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

    private fun SafeClientEvent.runAutoObby() {
        if (!tickTimer.tick(delayTicks.toLong())) return

        updateState()
        when (state) {
            State.SEARCHING -> {
                searchingState()
            }
            State.PLACING -> {
                placeEnderChest(placingPos)
            }
            State.MINING -> {
                mineBlock(placingPos)
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
            || isPlaceable(pos))
            && world.isAirBlock(pos.up())
            && world.rayTraceBlocks(eyePos, pos.toVec3dCenter())?.let { it.typeOfHit == RayTraceResult.Type.MISS } ?: true

    private fun SafeClientEvent.updateMainState() {
        val passCountCheck = checkObbyCount()

        state = when {
            state == State.DONE && autoRefill && player.inventorySlots.countBlock(Blocks.OBSIDIAN) < threshold -> {
                State.SEARCHING
            }
            state == State.COLLECTING && (!canPickUpObby() || getDroppedItem(Blocks.OBSIDIAN.id, 8.0f) == null) -> {
                State.DONE
            }
            state != State.DONE && world.isAirBlock(placingPos) && !passCountCheck -> {
                State.COLLECTING
            }
            state == State.MINING && world.isAirBlock(placingPos) -> {
                startPlacing()
            }
            state == State.PLACING && !world.isAirBlock(placingPos) -> {
                State.MINING
            }
            state == State.SEARCHING && searchingState == SearchingState.DONE && passCountCheck -> {
                startPlacing()
            }
            else -> {
                state
            }
        }
    }

    private fun SafeClientEvent.startPlacing() =
        if (player.inventorySlots.countBlock(Blocks.ENDER_CHEST) == 0) {
            State.SEARCHING
        } else {
            State.PLACING
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
        val inventory = player.allSlots.countBlock(Blocks.OBSIDIAN)
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
            val enderChestCount = player.inventorySlots.countBlock(Blocks.ENDER_CHEST)

            if (searchingState != SearchingState.DONE) {
                searchingState = when {
                    searchingState == SearchingState.PLACING && enderChestCount > 0 -> {
                        SearchingState.DONE
                    }
                    searchingState == SearchingState.COLLECTING && getDroppedItem(shulkerID, 8.0f) == null -> {
                        SearchingState.DONE
                    }
                    searchingState == SearchingState.MINING && world.isAirBlock(placingPos) -> {
                        if (enderChestCount > 0) {
                            SearchingState.COLLECTING
                        } else {
                            // In case if the shulker wasn't placed due to server lag
                            SearchingState.PLACING
                        }
                    }
                    searchingState == SearchingState.OPENING
                        && (enderChestCount > 0 || player.inventorySlots.firstEmpty() == null) -> {
                        SearchingState.MINING
                    }
                    searchingState == SearchingState.PLACING && !world.isAirBlock(placingPos) -> {
                        if (world.getBlockState(placingPos).block is BlockShulkerBox) {
                            SearchingState.OPENING
                        } else {
                            // In case if the shulker wasn't placed due to server lag
                            SearchingState.MINING
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
                SearchingState.MINING -> {
                    mineBlock(placingPos)
                }
                SearchingState.COLLECTING -> {
                    collectDroppedItem(shulkerID)
                }
                SearchingState.DONE -> {
                    updatePlacingPos()
                }
            }
        } else {
            searchingState = SearchingState.DONE
        }
    }

    private fun SafeClientEvent.placeShulker(pos: BlockPos) {
        val hotbarSlot = player.hotbarSlots.firstItem<ItemShulkerBox, HotbarSlot>()

        if (hotbarSlot != null) {
            shulkerID = hotbarSlot.stack.item.id
            swapToSlot(hotbarSlot)
        } else {
            if (!swapToItemOrMove<ItemShulkerBox>()) {
                MessageSendHelper.sendChatMessage("$chatName No shulker box was found in inventory, disabling.")
                mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                disable()
            }

            return
        }

        if (world.getBlockState(pos).block !is BlockShulkerBox) {
            placeBlock(pos)
        }
    }

    private fun SafeClientEvent.placeEnderChest(pos: BlockPos) {
        if (!swapToBlock(Blocks.ENDER_CHEST)) {
            if (!swapToBlockOrMove(Blocks.ENDER_CHEST)) {
                MessageSendHelper.sendChatMessage("$chatName No ender chest was found in inventory, disabling.")
                mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                disable()
            }

            return
        }

        placeBlock(pos)
    }

    private fun SafeClientEvent.openShulker(pos: BlockPos) {
        if (mc.currentScreen is GuiShulkerBox) {
            val container = player.openContainer
            val slot = container.getSlots(0..27).firstBlock(Blocks.ENDER_CHEST)

            if (slot != null) {
                clickSlot(container.windowId, slot, 0, ClickType.QUICK_MOVE)
                player.closeScreen()
            } else if (shulkerOpenTimer.tick(100, false)) { // Wait for maximum of 5 seconds
                if (leaveEmptyShulkers && container.inventory.subList(0, 27).all { it.isEmpty }) {
                    searchingState = SearchingState.MINING
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
                    delay(20L)
                    onMainThreadSafe {
                        connection.sendPacket(CPacketPlayerTryUseItemOnBlock(pos, side, EnumHand.MAIN_HAND, hitVecOffset.x.toFloat(), hitVecOffset.y.toFloat(), hitVecOffset.z.toFloat()))
                        player.swingArm(EnumHand.MAIN_HAND)
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.placeBlock(pos: BlockPos) {
        val pair = getNeighbour(pos, 1, 6.5f)
            ?: run {
                MessageSendHelper.sendChatMessage("$chatName Can't find neighbour block")
                return
            }

        lastHitVec = WorldUtils.getHitVec(pair.second, pair.first)
        rotateTimer.reset()

        val isBlackListed = WorldUtils.blackList.contains(world.getBlockState(pair.second).block)

        if (isBlackListed) {
            connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING))
        }

        defaultScope.launch {
            delay(20L)
            onMainThreadSafe {
                placeBlock(pair.second, pair.first)
            }

            if (isBlackListed) {
                delay(20L)
                onMainThreadSafe {
                    connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))
                }
            }
        }
    }

    private fun SafeClientEvent.mineBlock(pos: BlockPos) {
        swapToValidPickaxe()

        val side = EnumFacing.getDirectionFromEntityLiving(pos, player)
        lastHitVec = WorldUtils.getHitVec(pos, side)
        rotateTimer.reset()

        defaultScope.launch {
            delay(20L)
            onMainThreadSafe {
                connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, side))
                connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, side))
                player.swingArm(EnumHand.MAIN_HAND)
            }
        }
    }

    /**
     * Swaps the active hotbar slot to one which has a valid pickaxe (i.e. non-silk touch). If there is no valid pickaxe,
     * disable the module.
     */
    private fun SafeClientEvent.swapToValidPickaxe() {
        val swapped = swapToItem(Items.DIAMOND_PICKAXE) {
            EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, it) == 0
        }

        if (!swapped) {
            val slotFrom = getInventoryNonSilkTouchPick()
                ?: run {
                    MessageSendHelper.sendChatMessage("No valid pickaxe was found in inventory.")
                    mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                    disable()
                    return
                }

            val slotTo = player.hotbarSlots.firstEmpty()?.hotbarSlot ?: 0

            moveToHotbar(slotFrom.slotNumber, slotTo)
        }
    }

    /**
     * Gets the first non-hotbar slot of a diamond pickaxe that does not have the silk touch enchantment.
     * @return The position of the pickaxe. -1 if there is no match.
     */
    private fun SafeClientEvent.getInventoryNonSilkTouchPick(): Slot? {
        return player.storageSlots.firstItem(Items.DIAMOND_PICKAXE) {
            EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, it) == 0
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

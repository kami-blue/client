package org.kamiblue.client.module.modules.misc

import baritone.api.pathing.goals.Goal
import baritone.api.pathing.goals.GoalNear
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.block.BlockEnderChest
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
import net.minecraft.item.ItemShulkerBox
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.server.SPacketBlockChange
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.EnumDifficulty
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.BlockBreakEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.event.events.RenderWorldEvent
import org.kamiblue.client.manager.managers.PlayerPacketManager.sendPlayerPacket
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.process.AutoObsidianProcess
import org.kamiblue.client.process.PauseProcess
import org.kamiblue.client.util.*
import org.kamiblue.client.util.EntityUtils.getDroppedItem
import org.kamiblue.client.util.EntityUtils.getDroppedItems
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.ESPRenderer
import org.kamiblue.client.util.items.*
import org.kamiblue.client.util.math.RotationUtils.getRotationTo
import org.kamiblue.client.util.math.VectorUtils
import org.kamiblue.client.util.math.VectorUtils.toVec3dCenter
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.*
import org.kamiblue.client.util.world.*
import org.kamiblue.commons.interfaces.DisplayEnum
import org.kamiblue.event.listener.asyncListener
import org.kamiblue.event.listener.listener
import kotlin.math.ceil

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
    private val instantMining by setting("Instant Mining", true)
    private val instantMiningDelay by setting("Instant Mining Delay", 10, 1..20, 1, { instantMining })
    private val threshold by setting("Refill Threshold", 32, 1..64, 1, { autoRefill && fillMode != FillMode.INFINITE })
    private val targetStacks by setting("Target Stacks", 1, 1..20, 1, { fillMode == FillMode.TARGET_STACKS })
    private val delayTicks by setting("Delay Ticks", 4, 1..10, 1)
    private val rotationMode by setting("Rotation Mode", RotationMode.SPOOF)
    private val maxReach by setting("Max Reach", 4.9f, 2.0f..6.0f, 0.1f)

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

    enum class SearchingState(override val displayName: String) : DisplayEnum {
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
    var searchingState = SearchingState.PLACING; private set

    private var active = false
    private var placingPos = BlockPos(0, -1, 0)
    private var shulkerID = 0
    private var lastHitVec: Vec3d? = null
    private var lastMiningSide = EnumFacing.UP
    private var canInstantMine = false

    private val delayTimer = TickTimer(TimeUnit.TICKS)
    private val rotateTimer = TickTimer(TimeUnit.TICKS)
    private val shulkerOpenTimer = TickTimer(TimeUnit.TICKS)
    private val miningTimer = TickTimer(TimeUnit.TICKS)
    private val miningTimeoutTimer = TickTimer(TimeUnit.SECONDS)

    private val miningMap = HashMap<BlockPos, Pair<Int, Long>>() // <BlockPos, <Breaker ID, Last Update Time>>

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

        safeListener<BlockBreakEvent> {
            if (it.breakerID != player.entityId) {
                miningMap[it.position] = it.breakerID to System.currentTimeMillis()
            }
        }

        asyncListener<PacketEvent.PostSend> {
            if (!instantMining || it.packet !is CPacketPlayerDigging) return@asyncListener

            if (it.packet.position != placingPos || it.packet.facing != lastMiningSide) {
                canInstantMine = false
            }
        }

        safeAsyncListener<PacketEvent.Receive> {
            if (!instantMining || it.packet !is SPacketBlockChange) return@safeAsyncListener
            if (it.packet.blockPosition != placingPos) return@safeAsyncListener

            val prevBlock = world.getBlockState(it.packet.blockPosition).block
            val newBlock = it.packet.blockState.block

            if (prevBlock != newBlock) {
                if (prevBlock != Blocks.AIR && newBlock == Blocks.AIR) {
                    canInstantMine = true
                }
                miningTimer.reset()
                miningTimeoutTimer.reset()
            }
        }

        listener<RenderWorldEvent> {
            if (state != State.DONE) renderer.render(clear = false, cull = true)
        }

        safeListener<TickEvent.ClientTickEvent>(69) {
            if (it.phase != TickEvent.Phase.START || PauseProcess.isActive ||
                (world.difficulty == EnumDifficulty.PEACEFUL &&
                    player.dimension == 1 &&
                    @Suppress("UNNECESSARY_SAFE_CALL")
                    player.serverBrand?.contains("2b2t") == true)) return@safeListener

            updateMiningMap()
            runAutoObby()
            doRotation()
        }
    }

    private fun updateMiningMap() {
        val removeTime = System.currentTimeMillis() - 5000L
        miningMap.values.removeIf { it.second < removeTime }
    }

    private fun SafeClientEvent.doRotation() {
        if (rotateTimer.tick(20L, false)) return

        val rotation = lastHitVec?.let { getRotationTo(it) } ?: return

        when (rotationMode) {
            RotationMode.SPOOF -> {
                sendPlayerPacket {
                    rotate(rotation)
                }
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
        if (!delayTimer.tick(delayTicks.toLong())) return

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

    private fun SafeClientEvent.updateState() {
        if (state != State.DONE) {
            updatePlacingPos()

            if (!active) {
                active = true
                BaritoneUtils.primary?.pathingControlManager?.registerProcess(AutoObsidianProcess)
            }

            if (state != State.COLLECTING && searchingState != SearchingState.COLLECTING) {
                goal = if (player.getDistanceSqToCenter(placingPos) > 4.0) {
                    GoalNear(placingPos, 2)
                } else {
                    null
                }
            }
        }

        updateSearchingState()
        updateMainState()
    }

    private fun SafeClientEvent.updatePlacingPos() {
        val eyePos = player.getPositionEyes(1f)
        if (isPositionValid(placingPos, world.getBlockState(placingPos), eyePos)) return

        val posList = VectorUtils.getBlockPosInSphere(eyePos, maxReach).asSequence()
            .filter { !miningMap.contains(it) }
            .map { it to world.getBlockState(it) }
            .sortedBy { it.first.distanceSqToCenter(eyePos.x, eyePos.y, eyePos.z) }
            .toList()

        val pair = posList.find { it.second.block == Blocks.ENDER_CHEST || it.second.block is BlockShulkerBox }
            ?: posList.find { isPositionValid(it.first, it.second, eyePos) }

        if (pair != null) {
            if (pair.first != placingPos) {
                placingPos = pair.first
                canInstantMine = false

                renderer.clear()
                renderer.add(pair.first, ColorHolder(64, 255, 64))
            }
        } else {
            MessageSendHelper.sendChatMessage("$chatName No valid position for placing shulker box / ender chest nearby, disabling.")
            mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
            disable()
        }
    }

    private fun SafeClientEvent.isPositionValid(pos: BlockPos, blockState: IBlockState, eyePos: Vec3d) =
        !world.getBlockState(pos.down()).isReplaceable
            && (blockState.block.let { it == Blocks.ENDER_CHEST || it is BlockShulkerBox }
            || world.isPlaceable(pos))
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
                State.PRE_MINING
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
        if (searchShulker && player.inventorySlots.countBlock(Blocks.ENDER_CHEST) == 0) {
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
    private fun SafeClientEvent.checkObbyCount() =
        when (fillMode) {
            FillMode.TARGET_STACKS -> {
                val empty = countEmptySlots()
                val dropped = countDropped()
                val total = countInventory() + dropped

                val hasEmptySlots = empty - dropped >= 8
                val belowTarget = ceil(total / 8.0f) / 8.0f < targetStacks
                hasEmptySlots && belowTarget
            }
            FillMode.FILL_INVENTORY -> {
                countEmptySlots() - countDropped() >= 8
            }
            FillMode.INFINITE -> {
                true
            }
        }

    private fun SafeClientEvent.countInventory() =
        player.inventorySlots.countBlock(Blocks.OBSIDIAN)

    private fun SafeClientEvent.countDropped() =
        getDroppedItems(Blocks.OBSIDIAN.id, 8.0f).sumBy { it.item.count }

    private fun SafeClientEvent.countEmptySlots(): Int {
        return player.inventorySlots.sumBy {
            val stack = it.stack
            when {
                stack.isEmpty -> 64
                stack.item.block == Blocks.OBSIDIAN -> 64 - stack.count
                else -> 0
            }
        }
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
            val moved = swapToItemOrMove<ItemShulkerBox>(
                predicateSlot = {
                    val item = it.item
                    val block = item.block
                    item != Items.DIAMOND_PICKAXE && block !is BlockEnderChest
                }
            )

            if (!moved) {
                MessageSendHelper.sendChatMessage("$chatName No shulker box was found in inventory, disabling.")
                mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                disable()
            }

            onInventoryOperation()
            return
        }

        if (world.getBlockState(pos).block !is BlockShulkerBox) {
            placeBlock(pos)
        }
    }

    private fun SafeClientEvent.placeEnderChest(pos: BlockPos) {
        if (!swapToBlock(Blocks.ENDER_CHEST)) {
            val moved = swapToBlockOrMove(
                Blocks.ENDER_CHEST,
                predicateSlot = {
                    val item = it.item
                    val block = item.block
                    item != Items.DIAMOND_PICKAXE && block !is BlockShulkerBox
                }
            )
            if (!moved) {
                MessageSendHelper.sendChatMessage("$chatName No ender chest was found in inventory, disabling.")
                mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                disable()
            }

            onInventoryOperation()
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
                    searchingState = SearchingState.PRE_MINING
                    player.closeScreen()
                } else {
                    MessageSendHelper.sendChatMessage("$chatName No ender chest was found in shulker, disabling.")
                    mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                    disable()
                }
            }
        } else {
            val center = pos.toVec3dCenter()
            val diff = player.getPositionEyes(1.0f).subtract(center)
            val normalizedVec = diff.normalize()

            val side = EnumFacing.getFacingFromVector(normalizedVec.x.toFloat(), normalizedVec.y.toFloat(), normalizedVec.z.toFloat())
            val hitVecOffset = getHitVecOffset(side)

            lastHitVec = getHitVec(pos, side)
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
        val placeInfo = getNeighbour(pos, 1, 6.5f)
            ?: run {
                MessageSendHelper.sendChatMessage("$chatName Can't find neighbour block")
                return
            }

        lastHitVec = placeInfo.hitVec
        rotateTimer.reset()

        val isBlackListed = world.getBlockState(placeInfo.pos).isBlacklisted

        if (isBlackListed) {
            connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING))
        }

        defaultScope.launch {
            delay(20L)
            onMainThreadSafe {
                placeBlock(placeInfo)
            }

            if (isBlackListed) {
                delay(20L)
                onMainThreadSafe {
                    connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))
                }
            }
        }
    }

    private fun SafeClientEvent.mineBlock(pos: BlockPos, pre: Boolean) {
        if (!swapToValidPickaxe()) return

        val center = pos.toVec3dCenter()
        val diff = player.getPositionEyes(1.0f).subtract(center)
        val normalizedVec = diff.normalize()
        var side = EnumFacing.getFacingFromVector(normalizedVec.x.toFloat(), normalizedVec.y.toFloat(), normalizedVec.z.toFloat())

        lastHitVec = center
        rotateTimer.reset()

        if (instantMining && canInstantMine) {
            if (!miningTimer.tick(instantMiningDelay.toLong(), false)) return

            if (!miningTimeoutTimer.tick(2L, false)) {
                side = side.opposite
            } else {
                canInstantMine = false
            }
        }

        defaultScope.launch {
            delay(20L)
            onMainThreadSafe {
                if (pre || miningTimeoutTimer.tick(8L)) {
                    connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, side))
                    if (state != State.SEARCHING) state = State.MINING else searchingState = SearchingState.MINING
                } else {
                    connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, side))
                }
                player.swingArm(EnumHand.MAIN_HAND)
                lastMiningSide = side
            }
        }
    }

    /**
     * Swaps the active hotbar slot to one which has a valid pickaxe (i.e. non-silk touch). If there is no valid pickaxe,
     * disable the module.
     */
    private fun SafeClientEvent.swapToValidPickaxe(): Boolean {
        val swapped = swapToItem(Items.DIAMOND_PICKAXE) {
            EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, it) == 0
        }

        if (!swapped) {
            val moved = swapToItemOrMove(
                Items.DIAMOND_PICKAXE,
                predicateItem = {
                    EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, it) == 0
                },
                predicateSlot = {
                    val item = it.item
                    val block = item.block
                    block !is BlockShulkerBox && block !is BlockEnderChest
                }
            )

            if (!moved) {
                MessageSendHelper.sendChatMessage("No valid pickaxe was found in inventory.")
                mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                disable()
            }

            onInventoryOperation()
            return false
        }

        return true
    }

    private fun SafeClientEvent.onInventoryOperation() {
        delayTimer.reset(20L)
        playerController.updateController()
    }

    private fun SafeClientEvent.collectDroppedItem(itemId: Int) {
        val droppedItem = getDroppedItem(itemId, 8.0f)
        goal = if (droppedItem != null) {
            GoalNear(droppedItem, 0)
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
        lastMiningSide = EnumFacing.UP
        canInstantMine = false

        runBlocking {
            onMainThread {
                miningMap.clear()
            }
        }
    }
}

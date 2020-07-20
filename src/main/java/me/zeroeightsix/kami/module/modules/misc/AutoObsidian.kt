package me.zeroeightsix.kami.module.modules.misc

import baritone.api.BaritoneAPI
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.process.AutoObsidianProcess
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BlockUtils.isPlaceableForChest
import me.zeroeightsix.kami.util.EntityUtils.getDroppedItem
import me.zeroeightsix.kami.util.EntityUtils.getRotationFromVec3d
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.InventoryUtils.countItem
import me.zeroeightsix.kami.util.InventoryUtils.getSlots
import me.zeroeightsix.kami.util.InventoryUtils.getSlotsHotbar
import me.zeroeightsix.kami.util.InventoryUtils.getSlotsNoHotbar
import me.zeroeightsix.kami.util.InventoryUtils.moveToHotbar
import me.zeroeightsix.kami.util.InventoryUtils.swapSlotToItem
import me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage
import net.minecraft.block.BlockShulkerBox
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.inventory.GuiShulkerBox
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.ClickType
import net.minecraft.item.Item.getIdFromItem
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Created by Xiaro on 7/12/20
 *
 * Thanks to CorruptedSeal's help with dropped item scanning <3
 */
@Module.Info(
        name = "AutoObsidian",
        category = Module.Category.MISC,
        description = "Mines ender chest automatically to fill inventory with obsidian"
)
class AutoObsidian : Module() {
    private val searchShulker = register(Settings.b("SearchShulker", false))
    private val autoRefill = register(Settings.b("AutoRefill", false))
    private val thresold = register(Settings.integerBuilder("RefillThresold").withValue(8).withRange(1, 56).withVisibility { autoRefill.value }.build())
    private val targetStacks = register(Settings.integerBuilder("TargetStacks").withValue(1).withRange(1, 20).build())
    private val delayTicks = register(Settings.integerBuilder("DelayTicks").withValue(5).withRange(0, 10).build())

    enum class State {
        SEARCHING, PLACING, PRE_MINING, MINING, COLLECTING, DONE
    }

    enum class SearchingState {
        PLACING, OPENING, PRE_MINING, MINING, COLLECTING, DONE
    }

    var active = false
    var pathing = false
    var goal: BlockPos? = null
    var state = State.SEARCHING

    private var searchingState = SearchingState.PLACING
    private var playerPos = BlockPos(0, -1, 0)
    private var placingPos = BlockPos(0, -1, 0)
    private var shulkerBoxId = 0
    private var enderChestCount = -1
    private var obsidianCount = -1
    private var tickCount = 0
    private var openTime = 0L

    override fun onEnable() {
        InventoryUtils.inProgress = false
        if (mc.player == null) return
        state = State.SEARCHING
    }

    override fun onUpdate() {
        if (mc.playerController == null) return

        /* Just a delay */
        if (tickCount < delayTicks.value) {
            tickCount++
            return
        } else tickCount = 0

        updateState()
        when (state) {

            /* Searching states */
            State.SEARCHING -> {
                if (searchShulker.value) {
                    when (searchingState) {
                        SearchingState.PLACING -> placeShulker(placingPos)
                        SearchingState.OPENING -> openShulker(placingPos)
                        SearchingState.PRE_MINING -> mineBlock(placingPos, true)
                        SearchingState.MINING -> mineBlock(placingPos, false)
                        SearchingState.COLLECTING -> collectDroppedItem(shulkerBoxId)
                        SearchingState.DONE -> {
                            /* Positions need to be updated after moving while collecting dropped shulker box */
                            val currentPos = BlockPos(floor(mc.player.posX).toInt(), floor(mc.player.posY).toInt(), floor(mc.player.posZ).toInt())
                            playerPos = currentPos
                            setPlacingPos()
                        }
                    }
                } else searchingState = SearchingState.DONE
            }

            /* Main states */
            State.PLACING -> placeEnderChest(placingPos)
            State.PRE_MINING -> mineBlock(placingPos, true)
            State.MINING -> mineBlock(placingPos, false)
            State.COLLECTING -> collectDroppedItem(49)
            State.DONE -> {
                if (!autoRefill.value) {
                    sendChatMessage("$chatName Reached target stacks, disabling.")
                    this.disable()
                } else {
                    if (active) sendChatMessage("$chatName Reached target stacks, stopping.")
                    reset()
                }
            }
        }
    }

    override fun onDisable() {
        InventoryUtils.inProgress = false
        val baritoneProcess = BaritoneAPI.getProvider().primaryBaritone.pathingControlManager.mostRecentInControl()
        if (baritoneProcess.isPresent && baritoneProcess.get() is AutoObsidianProcess) {
            baritoneProcess.get().onLostControl()
        }
        reset()
    }

    private fun updateState() {
        val currentPos = BlockPos(floor(mc.player.posX).toInt(), floor(mc.player.posY).toInt(), floor(mc.player.posZ).toInt())
        if (state != State.DONE && placingPos.y == -1) {
            playerPos = currentPos
            setPlacingPos()
        }

        if (!active && state != State.DONE) {
            active = true
            BaritoneAPI.getProvider().primaryBaritone.pathingControlManager.registerProcess(KamiMod.autoObsidianProcess)
        }

        /* Tell baritone to get you back to position */
        if (state != State.DONE && state != State.COLLECTING && searchingState != SearchingState.COLLECTING) {
            if (currentPos.x != playerPos.x || currentPos.z != playerPos.z) {
                pathing = true
                goal = playerPos
                return
            } else {
                pathing = false
            }
        }

        /* Updates ender chest and obsidian counts before placing and mining ender chest */
        if (state == State.SEARCHING) {
            enderChestCount = countItem(0, 35, 130)
            obsidianCount = countObsidian()
        }

        /* Updates main state */
        val placedEnderChest = enderChestCount - countItem(0, 35, 130)
        val targetEnderChest = (targetStacks.value * 64 - obsidianCount) / 8
        state = when {
            state == State.DONE && autoRefill.value && countItem(0, 35, 49) <= thresold.value -> {
                State.SEARCHING
            }
            state == State.COLLECTING && getDroppedItem(49, 16.0f) == null -> {
                State.DONE
            }
            state != State.DONE && mc.world.isAirBlock(placingPos) && placedEnderChest >= targetEnderChest -> {
                State.COLLECTING
            }
            state == State.MINING && mc.world.isAirBlock(placingPos) -> {
                State.PLACING
            }
            state == State.PLACING && !mc.world.isAirBlock(placingPos) -> {
                State.PRE_MINING
            }
            state == State.SEARCHING && searchingState == SearchingState.DONE && placedEnderChest < targetEnderChest -> {
                State.PLACING
            }
            else -> state
        }

        /* Updates searching state */
        if (state == State.SEARCHING && searchingState != SearchingState.DONE) {
            searchingState = when {
                searchingState == SearchingState.PLACING && countItem(0, 35, 130) > 0 -> {
                    SearchingState.DONE
                }
                searchingState == SearchingState.COLLECTING && getDroppedItem(shulkerBoxId, 16.0f) == null -> {
                    SearchingState.DONE
                }
                searchingState == SearchingState.MINING && mc.world.isAirBlock(placingPos) -> {
                    if (countItem(0, 35, 130) > 0) {
                        SearchingState.COLLECTING
                    } else { /* In case if the shulker wasn't placed due to server lag */
                        SearchingState.PLACING
                    }
                }
                searchingState == SearchingState.OPENING && (countItem(0, 35, 130) >= 64 || getSlots(0, 35, 0) == null) -> {
                    SearchingState.PRE_MINING
                }
                searchingState == SearchingState.PLACING && !mc.world.isAirBlock(placingPos) -> {
                    if (mc.world.getBlockState(placingPos).block is BlockShulkerBox) {
                        SearchingState.OPENING
                    } else { /* In case if the shulker wasn't placed due to server lag */
                        SearchingState.PRE_MINING
                    }
                }
                else -> searchingState
            }
        } else if (state != State.SEARCHING) searchingState = SearchingState.PLACING

    }

    private fun countObsidian(): Int {
        return ceil(countItem(0, 35, 49).toDouble() / 8.0).toInt() * 8
    }

    private fun setPlacingPos() {
        if (getPlacingPos().y != -1) {
            placingPos = getPlacingPos()
        } else {
            sendChatMessage("$chatName No valid position for placing shulker box / ender chest nearby, disabling.")
            mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
            this.disable()
            return
        }
    }

    private fun getPlacingPos(): BlockPos {
        val pos = playerPos
        var facing = EnumFacing.NORTH
        for (i in 1..4) {
            val posOffset = pos.offset(facing)
            val posOffsetDiagonal = posOffset.offset(facing.rotateY())
            when {
                isPlaceableForChest(posOffset) -> return posOffset
                isPlaceableForChest(posOffset.up()) -> return posOffset.up()
                isPlaceableForChest(posOffsetDiagonal) -> return posOffsetDiagonal
                isPlaceableForChest(posOffsetDiagonal.up()) -> return posOffsetDiagonal.up()
                else -> facing = facing.rotateY()
            }
        }
        return BlockPos(0, -1, 0)
    }

    private fun lookAtBlock(pos: BlockPos) {
        val vec3d = Vec3d((pos.x + 0.5) - mc.player.posX, pos.y - (mc.player.eyeHeight + mc.player.posY), (pos.z + 0.5) - mc.player.posZ)
        val lookAt = getRotationFromVec3d(vec3d)
        mc.player.rotationYaw = lookAt[0].toFloat()
        mc.player.rotationPitch = lookAt[1].toFloat()
    }

    /* Tasks */
    private fun placeShulker(pos: BlockPos) {
        for (i in 219..234) {
            if (getSlotsHotbar(i) == null) {
                if (i != 234) continue else {
                    sendChatMessage("$chatName No shulker box was found in hotbar, disabling.")
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                    this.disable()
                    return
                }
            }
            shulkerBoxId = i
            swapSlotToItem(i)
            break
        }

        if (mc.world.getBlockState(pos).block !is BlockShulkerBox) {
            lookAtBlock(pos)
            mc.player.isSneaking = true
            mc.playerController.processRightClickBlock(mc.player, mc.world, pos.down(), EnumFacing.UP, mc.objectMouseOver.hitVec, EnumHand.MAIN_HAND)
            mc.player.swingArm(EnumHand.MAIN_HAND)
            mc.rightClickDelayTimer = 4
        }
    }

    private fun placeEnderChest(pos: BlockPos) {
        if (getSlotsHotbar(130) == null && getSlotsNoHotbar(130) != null) {
            moveToHotbar(130, 278, (delayTicks.value * 16).toLong())
            return
        } else if (getSlots(0, 35, 130) == null) {
            if (searchShulker.value) {
                state = State.SEARCHING
            } else {
                sendChatMessage("$chatName No ender chest was found in inventory, disabling.")
                mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                this.disable()
                return
            }
        }
        swapSlotToItem(130)

        lookAtBlock(pos)
        mc.player.isSneaking = true
        mc.playerController.processRightClickBlock(mc.player, mc.world, pos.down(), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec, EnumHand.MAIN_HAND)
        mc.player.swingArm(EnumHand.MAIN_HAND)
        mc.rightClickDelayTimer = 4
    }


    private fun openShulker(pos: BlockPos) {
        lookAtBlock(pos)
        if (mc.currentScreen !is GuiShulkerBox) {
            /* Added a delay here so it doesn't spam right click and get you kicked */
            if (System.currentTimeMillis() >= openTime + 2000L) {
                openTime = System.currentTimeMillis()
                mc.playerController.processRightClickBlock(mc.player, mc.world, pos, mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec, EnumHand.MAIN_HAND)
            }
        } else {
            /* Extra delay here to wait for the item list to be loaded */
            Executors.newSingleThreadScheduledExecutor().schedule({
                val currentContainer = mc.player.openContainer
                var enderChestSlot = -1
                for (i in 0..26) {
                    if (getIdFromItem(currentContainer.inventory[i].getItem()) == 130) {
                        enderChestSlot = i
                    }
                }
                if (enderChestSlot != -1) {
                    mc.playerController.windowClick(currentContainer.windowId, enderChestSlot, 0, ClickType.QUICK_MOVE, mc.player)
                } else {
                    sendChatMessage("$chatName No ender chest was found in shulker, disabling.")
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                    this.disable()
                }
            }, delayTicks.value * 50L, TimeUnit.MILLISECONDS)
        }
    }

    private fun mineBlock(pos: BlockPos, pre: Boolean) {
        if (getSlotsHotbar(278) == null && getSlotsNoHotbar(278) != null) {
            moveToHotbar(278, 130, (delayTicks.value * 16).toLong())
            return
        } else if (getSlots(0, 35, 278) == null) {
            sendChatMessage("$chatName No pickaxe was found in inventory, disabling.")
            mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
            this.disable()
            return
        }
        swapSlotToItem(278)
        lookAtBlock(pos)

        /* Packet mining lol */
        if (pre) {
            mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, mc.objectMouseOver.sideHit))
            if (state != State.SEARCHING) state = State.MINING else searchingState = SearchingState.MINING
        } else {
            mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, mc.objectMouseOver.sideHit))
        }
        mc.player.swingArm(EnumHand.MAIN_HAND)
    }

    private fun collectDroppedItem(itemId: Int) {
        pathing = if (getDroppedItem(itemId, 16.0f) != null) {
            goal = getDroppedItem(itemId, 16.0f)
            true
        } else false
    }

    private fun reset() {
        active = false
        pathing = false
        searchingState = SearchingState.PLACING
        playerPos = BlockPos(0, -1, 0)
        placingPos = BlockPos(0, -1, 0)
        enderChestCount = -1
        obsidianCount = -1
        tickCount = 0
    }
    /* End of tasks */
}
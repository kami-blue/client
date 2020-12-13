package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.mixin.client.entity.MixinEntity
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.combat.Surround
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BlockUtils
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.block.Block
import net.minecraft.block.BlockContainer
import net.minecraft.block.BlockFalling
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.util.math.BlockPos
import net.minecraftforge.client.event.InputUpdateEvent
import me.zeroeightsix.kami.util.InventoryUtils.swapSlot
import kotlin.math.round

/**
 * @see MixinEntity.isSneaking
 *
 * TODO: Fix chest scaffold
 * Modified by TopiasL for NECRON Client
 */
@Module.Info(
        name = "Scaffold",
        category = Module.Category.PLAYER,
        description = "Places blocks under you"
)
object Scaffold : Module() {
    private val placeBlocks = register(Settings.b("PlaceBlocks", true))
    val safeWalk = register(Settings.b("SafeWalk", true))
    private val tower = register(Settings.b("Tower", true))
    private val onlyUseSolidBlocks = register(Settings.b("OnlyUseSolidBlocks", true))
    private val useBlackList = register(Settings.b("UseBlackList", true))
    private val jumpMotion = register(Settings.doubleBuilder("JumpMotion").withValue(0.42).withRange(0.34, 0.6).withStep(0.02).withVisibility { tower.value })
    private val swapMode = register(Settings.e<SwapMode>("HotbarSwapMode", SwapMode.SPOOF))
    private val modeSetting = register(Settings.e<Mode>("Mode", Mode.NORMAL))
    private val randomDelay = register(Settings.booleanBuilder("RandomDelay").withValue(false).withVisibility { modeSetting.value == Mode.LEGIT })
    private val delayRange = register(Settings.integerBuilder("DelayRange").withValue(6).withRange(0, 10).withVisibility { modeSetting.value == Mode.LEGIT && randomDelay.value })
    private val ticks = register(Settings.integerBuilder("Ticks").withValue(2).withRange(0, 30).withStep(1).withVisibility { modeSetting.value == Mode.NORMAL })
    private val maxBlocksPerOperation = register(Settings.integerBuilder("MaxBlocksPerOperation").withValue(2).withRange(1, 3).withStep(1))

    private var shouldSlow = false
    private var towerStart = 0.0
    private var holding = false

    private enum class SwapMode {
        NO, SWAP, SPOOF
    }

    private enum class Mode {
        NORMAL, LEGIT
    }

    init {
        listener<InputUpdateEvent> {
            if (modeSetting.value == Mode.LEGIT && shouldSlow) {
                if (randomDelay.value) {
                    it.movementInput.moveStrafe *= 0.2f + randomInRange
                    it.movementInput.moveForward *= 0.2f + randomInRange
                } else {
                    it.movementInput.moveStrafe *= 0.2f
                    it.movementInput.moveForward *= 0.2f
                }
            }
        }

        listener<SafeTickEvent> {
            shouldSlow = false

            val towering = mc.gameSettings.keyBindJump.isKeyDown && tower.value
            var vec3d = EntityUtils.getInterpolatedPos(mc.player, ticks.value.toFloat())

            if (modeSetting.value == Mode.LEGIT) vec3d = EntityUtils.getInterpolatedPos(mc.player, 0f)

            val blockPos = BlockPos(vec3d).down()
            val belowBlockPos = blockPos.down()
            val legitPos = BlockPos(EntityUtils.getInterpolatedPos(mc.player, 2f))

            /* when legitBridge is enabled */
            /* check if block behind player is air or other replaceable block and if it is, make the player crouch */
            if (modeSetting.value == Mode.LEGIT && mc.world.getBlockState(legitPos.down()).material.isReplaceable && mc.player.onGround && !towering) {
                shouldSlow = true
                mc.player.movementInput.sneak = true
                mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING))
            }

            if (towering) {
                if (mc.player.posY <= blockPos.y + 1.0f) {
                    return@listener
                }
            }

            if (!mc.world.getBlockState(blockPos).material.isReplaceable) {
                return@listener
            }
            val oldSlot = mc.player.inventory.currentItem

            if (swapMode.value == SwapMode.SWAP || swapMode.value == SwapMode.SPOOF ) {
                if (!setSlotToBlocks(belowBlockPos)) return@listener
            }

            /* check if we don't have a block adjacent to the blockPos */
            val neighbor = BlockUtils.getNeighbour(blockPos, attempts = maxBlocksPerOperation.value) ?: return@listener

            /* place the block */
            if (placeBlocks.value) BlockUtils.placeBlock(neighbor.second, neighbor.first)

            /* Reset the slot */
            if (!holding) { swapSlot(oldSlot); PlayerPacketManager.resetHotbar() }

            if (towering) {
                val motion = jumpMotion.value // jump motion
                if (mc.player.onGround) {
                    towerStart = mc.player.posY
                    mc.player.motionY = motion
                }
                if (mc.player.posY > towerStart + motion) {
                    mc.player.setPosition(mc.player.posX, round(mc.player.posY), mc.player.posZ)
                    mc.player.motionY = motion
                    towerStart = mc.player.posY
                }
            } else {
                towerStart = 0.0
            }

            if (shouldSlow) {
                mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING))
                shouldSlow = false
            }
        }
    }

    private val randomInRange: Float
        get() = 0.11f + Math.random().toFloat() * (delayRange.value / 10.0f - 0.11f)

    private fun setSlotToBlocks(belowBlockPos: BlockPos) : Boolean {
        if (isBlock(mc.player.heldItemMainhand, belowBlockPos)) {
            holding = true
            return true
        }
        holding = false

        /* search blocks in hotbar */
        var newSlot = -1
        for (i in 0..8) {
            /* filter out non-block items */
            val stack = mc.player.inventory.getStackInSlot(i)

            if (isBlock(stack, belowBlockPos)) {
                newSlot = i
                break
            }
        }

        /* check if any blocks were found, and if they were then set the slot */
        if (newSlot != -1) {
            if (swapMode.value == SwapMode.SWAP) swapSlot(newSlot)
            else if (swapMode.value == SwapMode.SPOOF) PlayerPacketManager.spoofHotbar(newSlot)
        }
        else return false
        return true
    }

    private fun isBlock(stack: ItemStack, belowBlockPos: BlockPos): Boolean {
        /* filter out non-block items */
        if (stack == ItemStack.EMPTY || stack.getItem() !is ItemBlock) return false

        val block = (stack.getItem() as ItemBlock).block
        if ((BlockUtils.blackList.contains(block) || block is BlockContainer) && useBlackList.value) return false

        /* filter out non-solid blocks */
        if (!Block.getBlockFromItem(stack.getItem()).defaultState.isFullBlock && onlyUseSolidBlocks.value) return false

        /* don't use falling blocks if it'd fall */
        if ((stack.getItem() as ItemBlock).block is BlockFalling) {
            if (mc.world.getBlockState(belowBlockPos).material.isReplaceable) return false
        }

        return true
    }
}
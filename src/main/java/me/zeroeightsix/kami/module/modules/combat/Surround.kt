package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.player.NoBreakAnimation
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BlockUtils
import me.zeroeightsix.kami.util.CenterPlayer
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.block.Block
import net.minecraft.block.BlockObsidian
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.sqrt

@Module.Info(
        name = "Surround",
        category = Module.Category.COMBAT,
        description = "Surrounds you with obsidian to take less damage"
)
object Surround : Module() {
    val autoDisable: Setting<Boolean> = register(Settings.b("DisableOnPlace", true))
    private val spoofRotations = register(Settings.b("SpoofRotations", true))
    private val spoofHotbar = register(Settings.b("SpoofHotbar", false))
    private val blockPerTick = register(Settings.doubleBuilder("BlocksPerTick").withMinimum(1.0).withValue(4.0).withMaximum(10.0).build())
    private val debugMsgs = register(Settings.e<DebugMsgs>("DebugMessages", DebugMsgs.IMPORTANT))
    private val autoCenter = register(Settings.b("AutoCenter", true))
    private val placeAnimation = register(Settings.b("PlaceAnimation", false))

    private val surroundTargets = arrayOf(Vec3d(0.0, 0.0, 0.0), Vec3d(1.0, 1.0, 0.0), Vec3d(0.0, 1.0, 1.0), Vec3d(-1.0, 1.0, 0.0), Vec3d(0.0, 1.0, -1.0), Vec3d(1.0, 0.0, 0.0), Vec3d(0.0, 0.0, 1.0), Vec3d(-1.0, 0.0, 0.0), Vec3d(0.0, 0.0, -1.0), Vec3d(1.0, 1.0, 0.0), Vec3d(0.0, 1.0, 1.0), Vec3d(-1.0, 1.0, 0.0), Vec3d(0.0, 1.0, -1.0))

    private var basePos: BlockPos? = null
    private var offsetStep = 0
    private var playerHotbarSlot = -1
    private var lastHotbarSlot = -1

    @Suppress("UNUSED")
    private enum class DebugMsgs {
        NONE, IMPORTANT, ALL
    }

    override fun onUpdate() {
        if (autoCenter.value && (mc.player.posX != CenterPlayer.getPosX(1.0f) || mc.player.posZ != CenterPlayer.getPosZ(1.0f))) {
            CenterPlayer.centerPlayer(1.0f)
            if (debugMsgs.value == DebugMsgs.ALL) MessageSendHelper.sendChatMessage("$chatName Auto centering. Player position is " + mc.player.positionVector.toString())
        } else {
            if (offsetStep == 0) {
                basePos = BlockPos(mc.player.positionVector).down()
                playerHotbarSlot = mc.player.inventory.currentItem
                if (debugMsgs.value == DebugMsgs.ALL) {
                    MessageSendHelper.sendChatMessage("$chatName Starting Loop, current Player Slot: $playerHotbarSlot")
                }
                if (!spoofHotbar.value) {
                    lastHotbarSlot = mc.player.inventory.currentItem
                }
            }
            for (i in 0 until floor(blockPerTick.value).toInt()) {
                if (debugMsgs.value == DebugMsgs.ALL) {
                    MessageSendHelper.sendChatMessage("$chatName Loop iteration: $offsetStep")
                }
                if (offsetStep >= surroundTargets.size) {
                    endLoop()
                    return
                }
                val offset = surroundTargets[offsetStep]
                placeBlock(BlockPos(basePos!!.add(offset.x, offset.y, offset.z)))
                ++offsetStep
            }
        }
    }

    public override fun onEnable() {
        if (mc.player == null) return

        if (autoCenter.value) {
            CenterPlayer.centerPlayer(0.5f)
            if (debugMsgs.value == DebugMsgs.ALL) MessageSendHelper.sendChatMessage("$chatName Auto centering. Player position is " + mc.player.positionVector.toString())
        }

        playerHotbarSlot = mc.player.inventory.currentItem
        lastHotbarSlot = -1

        if (debugMsgs.value == DebugMsgs.ALL) {
            MessageSendHelper.sendChatMessage("$chatName Saving initial Slot = $playerHotbarSlot")
        }
    }

    public override fun onDisable() {
        if (mc.player != null) {
            if (debugMsgs.value == DebugMsgs.ALL) {
                MessageSendHelper.sendChatMessage("$chatName Disabling")
            }

            if (lastHotbarSlot != playerHotbarSlot && playerHotbarSlot != -1) {
                if (spoofHotbar.value) {
                    mc.player.connection.sendPacket(CPacketHeldItemChange(playerHotbarSlot))
                } else {
                    mc.player.inventory.currentItem = playerHotbarSlot
                }
            }

            playerHotbarSlot = -1
            lastHotbarSlot = -1
        }
    }

    private fun endLoop() {
        offsetStep = 0

        if (debugMsgs.value == DebugMsgs.ALL) {
            MessageSendHelper.sendChatMessage("$chatName Ending Loop")
        }

        if (lastHotbarSlot != playerHotbarSlot && playerHotbarSlot != -1) {
            if (debugMsgs.value == DebugMsgs.ALL) {
                MessageSendHelper.sendChatMessage("$chatName Setting Slot back to  = $playerHotbarSlot")
            }
            if (spoofHotbar.value) {
                mc.player.connection.sendPacket(CPacketHeldItemChange(playerHotbarSlot))
            } else {
                mc.player.inventory.currentItem = playerHotbarSlot
            }
            lastHotbarSlot = playerHotbarSlot
        }

        if (autoDisable.value) {
            disable()
        }
    }

    private fun placeBlock(blockPos: BlockPos) {
        if (!mc.world.getBlockState(blockPos).material.isReplaceable) {
            if (debugMsgs.value == DebugMsgs.ALL) {
                MessageSendHelper.sendChatMessage("$chatName Block is already placed, skipping")
            }
        } else if (!BlockUtils.checkForNeighbours(blockPos) && debugMsgs.value == DebugMsgs.ALL) {
            MessageSendHelper.sendChatMessage("$chatName !checkForNeighbours(blockPos), disabling! ")
        } else {
            if (placeAnimation.value) mc.player.connection.sendPacket(CPacketAnimation(mc.player.activeHand))
            placeBlockExecute(blockPos)
        }

        if (NoBreakAnimation.isEnabled) NoBreakAnimation.resetMining()
    }

    private fun findObiInHotbar(): Int {
        var slot = -1
        for (i in 0..8) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (stack != ItemStack.EMPTY && stack.getItem() is ItemBlock) {
                val block = (stack.getItem() as ItemBlock).block
                if (block is BlockObsidian) {
                    slot = i
                    break
                }
            }
        }
        return slot
    }

    private fun placeBlockExecute(pos: BlockPos) {
        val eyesPos = Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight().toDouble(), mc.player.posZ)
        val var3 = EnumFacing.values()
        for (side in var3) {
            val neighbor = pos.offset(side)
            val side2 = side.opposite

            if (!canBeClicked(neighbor)) {
                if (debugMsgs.value == DebugMsgs.ALL) {
                    MessageSendHelper.sendChatMessage("$chatName No neighbor to click at!")
                }
            } else {
                val hitVec = Vec3d(neighbor).add(0.5, 0.5, 0.5).add(Vec3d(side2.directionVec).scale(0.5))

                if (eyesPos.squareDistanceTo(hitVec) <= 18.0625) {
                    if (spoofRotations.value) {
                        faceVectorPacketInstant(hitVec)
                    }

                    var needSneak = false
                    val blockBelow = mc.world.getBlockState(neighbor).block

                    if (BlockUtils.blackList.contains(blockBelow) || BlockUtils.shulkerList.contains(blockBelow)) {
                        if (debugMsgs.value == DebugMsgs.IMPORTANT) {
                            MessageSendHelper.sendChatMessage("$chatName Sneak enabled!")
                        }
                        needSneak = true
                    }

                    if (needSneak) {
                        mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING))
                    }

                    val obiSlot = findObiInHotbar()

                    if (obiSlot == -1) {
                        if (debugMsgs.value == DebugMsgs.IMPORTANT) {
                            MessageSendHelper.sendChatMessage("$chatName No obsidian in hotbar, disabling!")
                        }
                        disable()
                        return
                    }

                    if (lastHotbarSlot != obiSlot) {
                        if (debugMsgs.value == DebugMsgs.ALL) {
                            MessageSendHelper.sendChatMessage("$chatName Setting Slot to obsidian at  = $obiSlot")
                        }

                        if (spoofHotbar.value) {
                            mc.player.connection.sendPacket(CPacketHeldItemChange(obiSlot))
                        } else {
                            mc.player.inventory.currentItem = obiSlot
                        }
                        lastHotbarSlot = obiSlot
                    }

                    mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, side2, hitVec, EnumHand.MAIN_HAND)
                    mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))

                    if (needSneak) {
                        if (debugMsgs.value == DebugMsgs.IMPORTANT) {
                            MessageSendHelper.sendChatMessage("$chatName Sneak disabled!")
                        }
                        mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING))
                    }
                    return
                }

                if (debugMsgs.value == DebugMsgs.ALL) {
                    MessageSendHelper.sendChatMessage("$chatName Distance > 4.25 blocks!")
                }
            }
        }
    }

    private fun canBeClicked(pos: BlockPos): Boolean {
        return getBlock(pos).canCollideCheck(getState(pos), false)
    }

    fun getBlock(pos: BlockPos): Block {
        return getState(pos).block
    }

    private fun getState(pos: BlockPos): IBlockState {
        return mc.world.getBlockState(pos)
    }

    private fun faceVectorPacketInstant(vec: Vec3d) {
        val rotations = getLegitRotations(vec)
        mc.player.connection.sendPacket(CPacketPlayer.Rotation(rotations[0], rotations[1], mc.player.onGround))
    }

    private fun getLegitRotations(vec: Vec3d): FloatArray {
        val eyesPos = eyesPos
        val diffX = vec.x - eyesPos.x
        val diffY = vec.y - eyesPos.y
        val diffZ = vec.z - eyesPos.z

        val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)
        val yaw = Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90.0f
        val pitch = (-Math.toDegrees(atan2(diffY, diffXZ))).toFloat()

        return floatArrayOf(mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw), mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch))
    }

    private val eyesPos: Vec3d
        get() = Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight().toDouble(), mc.player.posZ)
}
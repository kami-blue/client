package org.kamiblue.client.module.modules.player

import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.mixin.extension.onGround
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.EntityUtils
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.client.util.world.getGroundPos

internal object NoFall : Module(
    name = "NoFall",
    category = Category.PLAYER,
    description = "Prevents fall damage"
) {
    private val distance = setting("Distance", 3, 1..10, 1)
    private val mode = setting("Mode", Mode.CATCH)
    private val fallModeSetting = setting("Fall", FallMode.PACKET, { mode.value == Mode.FALL })
    private val catchModeSetting = setting("Catch", CatchMode.MOTION, { mode.value == Mode.CATCH })
    private val voidOnly = setting("Void Only", false, { mode.value == Mode.CATCH })

    private enum class Mode {
        FALL, CATCH
    }

    private enum class FallMode {
        BUCKET, PACKET
    }

    private enum class CatchMode {
        BLOCK, MOTION
    }

    private var last: Long = 0

    init {
        safeListener<PacketEvent.Send> {
            if (it.packet !is CPacketPlayer || player.isElytraFlying) return@safeListener
            if ((mode.value == Mode.FALL && fallModeSetting.value == FallMode.PACKET || mode.value == Mode.CATCH)) {
                it.packet.onGround = true
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (player.isCreative || player.isSpectator || !fallDistCheck()) return@safeListener
            if (mode.value == Mode.FALL) {
                fallMode()
            } else if (mode.value == Mode.CATCH) {
                catchMode()
            }
        }
    }

    private fun SafeClientEvent.fallDistCheck() = (!voidOnly.value && player.fallDistance >= distance.value) || world.getGroundPos(player).y == -69420.0

    // TODO: This really needs a rewrite to spoof placing and the such instead of manual rotations
    private fun SafeClientEvent.fallMode() {
        if (fallModeSetting.value == FallMode.BUCKET && player.dimension != -1 && !EntityUtils.isAboveLiquid(player) && System.currentTimeMillis() - last > 100) {
            val posVec = player.positionVector
            val result = world.rayTraceBlocks(posVec, posVec.add(0.0, -5.33, 0.0), true, true, false)

            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                var hand = EnumHand.MAIN_HAND
                if (player.heldItemOffhand.item === Items.WATER_BUCKET) hand = EnumHand.OFF_HAND else if (player.heldItemMainhand.item !== Items.WATER_BUCKET) {
                    for (i in 0..8) if (player.inventory.getStackInSlot(i).item === Items.WATER_BUCKET) {
                        player.inventory.currentItem = i
                        player.rotationPitch = 90f
                        last = System.currentTimeMillis()
                        return
                    }
                    return
                }

                player.rotationPitch = 90f
                playerController.processRightClick(player, world, hand)
            }
        }
    }

    private fun SafeClientEvent.catchMode() {
        when (catchModeSetting.value) {
            CatchMode.BLOCK -> {
                var slot = -1
                for (i in 0..8) {
                    val stack = player.inventory.getStackInSlot(i)
                    if (stack != ItemStack.EMPTY && stack.item is ItemBlock) {
                        slot = i
                    }
                }

                if (slot == -1) {
                    MessageSendHelper.sendChatMessage("$chatName Missing blocks for Catch Mode Block!")
                    return
                } else {
                    player.inventory.currentItem = slot
                }

                val posVec = player.positionVector
                val result = world.rayTraceBlocks(posVec, posVec.add(0.0, -5.33, 0.0), true, true, false)
                if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                    placeBlock(); placeBlock(); placeBlock() // yes
                }
            }
            CatchMode.MOTION -> {
                player.motionY = 10.0
                player.motionY = -1.0
            }
        }
    }

    private fun SafeClientEvent.placeBlock() {
        val hitVec = Vec3d(BlockPos(player)).add(0.0, -1.0, 0.0)
        playerController.processRightClickBlock(player, world, BlockPos(hitVec), EnumFacing.DOWN, hitVec, EnumHand.MAIN_HAND)
        player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
    }
}
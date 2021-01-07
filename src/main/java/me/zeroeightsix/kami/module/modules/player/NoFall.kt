package me.zeroeightsix.kami.module.modules.player

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.mixin.extension.onGround
import me.zeroeightsix.kami.mixin.extension.rightClickMouse
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.WorldUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.onMainThreadSafe
import me.zeroeightsix.kami.util.threads.safeListener
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
import org.kamiblue.event.listener.listener

object NoFall : Module(
    name = "NoFall",
    category = Category.PLAYER,
    description = "Prevents fall damage"
) {
    private val distance = setting("Distance", 3, 1..10, 1)
    private val mode = setting("Mode", Mode.CATCH)
    private val fallModeSetting = setting("Fall", FallMode.PACKET, { mode.value == Mode.FALL })
    private val catchModeSetting = setting("Catch", CatchMode.MOTION, { mode.value == Mode.CATCH })
    private val pickup = setting("Pickup", false, { mode.value == Mode.FALL && fallModeSetting.value == FallMode.BUCKET })
    private val pickupDelay = setting("PickupDelay", 300, 100..1000, 50, { mode.value == Mode.FALL && fallModeSetting.value == FallMode.BUCKET && pickup.value })
    private val voidOnly = setting("VoidOnly", false, { mode.value == Mode.CATCH })

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
        listener<PacketEvent.Send> {
            if (it.packet !is CPacketPlayer || mc.player.isElytraFlying) return@listener
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

    private fun fallDistCheck() = (!voidOnly.value && mc.player.fallDistance >= distance.value) || WorldUtils.getGroundPos().y == -999.0

    private fun fallMode() {
        if (fallModeSetting.value == FallMode.BUCKET && mc.player.dimension != -1 && !EntityUtils.isAboveWater(mc.player) && System.currentTimeMillis() - last > 100) {
            val posVec = mc.player.positionVector
            val result = mc.world.rayTraceBlocks(posVec, posVec.add(0.0, -5.33, 0.0), true, true, false)

            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                var hand = EnumHand.MAIN_HAND
                if (mc.player.heldItemOffhand.item === Items.WATER_BUCKET) hand = EnumHand.OFF_HAND else if (mc.player.heldItemMainhand.item !== Items.WATER_BUCKET) {
                    for (i in 0..8) if (mc.player.inventory.getStackInSlot(i).item === Items.WATER_BUCKET) {
                        mc.player.inventory.currentItem = i
                        mc.player.rotationPitch = 90f
                        last = System.currentTimeMillis()
                        return
                    }
                    return
                }
                mc.player.rotationPitch = 90f
                mc.playerController.processRightClick(mc.player, mc.world, hand)
            }

            if (pickup.value) {
                defaultScope.launch {
                    delay(pickupDelay.value.toLong())
                    onMainThreadSafe {
                        player.rotationPitch = 90f
                        mc.rightClickMouse()
                    }
                }
            }
        }
    }

    private fun catchMode() {
        when (catchModeSetting.value) {
            CatchMode.BLOCK -> {
                var slot = -1
                for (i in 0..8) {
                    val stack = mc.player.inventory.getStackInSlot(i)
                    if (stack != ItemStack.EMPTY && stack.item is ItemBlock) {
                        slot = i
                    }
                }

                if (slot == -1) {
                    MessageSendHelper.sendChatMessage("$chatName Missing blocks for Catch Mode Block!")
                    return
                } else {
                    mc.player.inventory.currentItem = slot
                }

                val posVec = mc.player.positionVector
                val result = mc.world.rayTraceBlocks(posVec, posVec.add(0.0, -5.33, 0.0), true, true, false)
                if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                    placeBlock(); placeBlock(); placeBlock() // yes
                }
            }
            CatchMode.MOTION -> {
                mc.player.motionY = 10.0
                mc.player.motionY = -1.0
            }
        }
    }

    private fun placeBlock() {
        val hitVec = Vec3d(BlockPos(mc.player)).add(0.0, -1.0, 0.0)
        mc.playerController.processRightClickBlock(mc.player, mc.world, BlockPos(hitVec), EnumFacing.DOWN, hitVec, EnumHand.MAIN_HAND)
        mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
    }
}
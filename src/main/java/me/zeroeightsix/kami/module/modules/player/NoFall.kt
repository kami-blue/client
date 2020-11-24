package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.command.commands.TeleportCommand
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.mixin.extension.onGround
import me.zeroeightsix.kami.mixin.extension.rightClickMouse
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BlockUtils
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.VectorUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
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

@Module.Info(
        name = "NoFall",
        category = Module.Category.PLAYER,
        description = "Prevents fall damage"
)
object NoFall : Module() {
    private val distance = register(Settings.integerBuilder("Distance").withValue(3).withRange(1, 10))
    private val mode = register(Settings.e<Mode>("Mode", Mode.CATCH))
    private val fallModeSetting = register(Settings.enumBuilder(FallMode::class.java, "Fall").withValue(FallMode.PACKET).withVisibility { mode.value == Mode.FALL })
    private val catchModeSetting = register(Settings.enumBuilder(CatchMode::class.java, "Catch").withValue(CatchMode.MOTION).withVisibility { mode.value == Mode.CATCH })
    private val pickup = register(Settings.booleanBuilder("Pickup").withValue(false).withVisibility { mode.value == Mode.FALL && fallModeSetting.value == FallMode.BUCKET })
    private val pickupDelay = register(Settings.integerBuilder("PickupDelay").withValue(300).withMinimum(100).withMaximum(1000).withVisibility { mode.value == Mode.FALL && fallModeSetting.value == FallMode.BUCKET && pickup.value })
    private val voidOnly = register(Settings.booleanBuilder("VoidOnly").withValue(false).withVisibility { mode.value == Mode.CATCH })

    private enum class Mode {
        FALL, CATCH
    }

    private enum class FallMode {
        BUCKET, PACKET
    }

    private enum class CatchMode {
        BLOCK, TP, MOTION
    }

    private var last: Long = 0

    init {
        listener<PacketEvent.Send> {
            if (it.packet !is CPacketPlayer || mc.player.isElytraFlying) return@listener
            if ((mode.value == Mode.FALL && fallModeSetting.value == FallMode.PACKET || mode.value == Mode.CATCH)) {
                it.packet.onGround = true
            }
        }

        listener<SafeTickEvent> {
            if (mc.player.isCreative || mc.player.isSpectator || !fallDistCheck()) return@listener
            if (mode.value == Mode.FALL) {
                fallMode()
            } else if (mode.value == Mode.CATCH) {
                catchMode()
            }
        }
    }

    private fun fallDistCheck() = (!voidOnly.value && mc.player.fallDistance >= distance.value) || BlockUtils.getGroundPosY(false) == -999.0

    private fun fallMode() {
        if (fallModeSetting.value == FallMode.BUCKET && mc.player.dimension != -1 && !EntityUtils.isAboveWater(mc.player) && System.currentTimeMillis() - last > 100) {
            val posVec = mc.player.positionVector
            val result = mc.world.rayTraceBlocks(posVec, posVec.add(0.0, -5.33, 0.0), true, true, false)
            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                var hand = EnumHand.MAIN_HAND
                if (mc.player.heldItemOffhand.getItem() === Items.WATER_BUCKET) hand = EnumHand.OFF_HAND else if (mc.player.heldItemMainhand.getItem() !== Items.WATER_BUCKET) {
                    for (i in 0..8) if (mc.player.inventory.getStackInSlot(i).getItem() === Items.WATER_BUCKET) {
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
                Thread {
                    try { // this is just pozzed and should be properly calculating it based on velocity but I cba to do it
                        Thread.sleep(pickupDelay.value.toLong())
                    } catch (ignored: InterruptedException) {

                    }
                    mc.player.rotationPitch = 90f
                    mc.rightClickMouse()
                }.start()
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
            CatchMode.TP -> {
                val pos = VectorUtils.getHighestTerrainPos(mc.player.position)
                TeleportCommand.teleport(mc, Vec3d(pos), false)
            }
            CatchMode.MOTION -> {
                mc.player.motionY = 10.0
                mc.player.motionY = -1.0
            }
            else -> {
            }
        }
    }

    private fun placeBlock() {
        val hitVec = Vec3d(BlockPos(mc.player)).add(0.0, -1.0, 0.0)
        mc.playerController.processRightClickBlock(mc.player, mc.world, BlockPos(hitVec), EnumFacing.DOWN, hitVec, EnumHand.MAIN_HAND)
        mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
    }
}
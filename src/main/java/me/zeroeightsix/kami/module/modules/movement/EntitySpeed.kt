package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MovementUtils
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityBoat
import net.minecraft.entity.passive.AbstractHorse
import net.minecraft.entity.passive.EntityHorse
import net.minecraft.entity.passive.EntityPig
import net.minecraft.network.play.client.CPacketInput
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketVehicleMove
import net.minecraft.network.play.server.SPacketMoveVehicle
import net.minecraft.util.EnumHand
import net.minecraft.world.chunk.EmptyChunk
import kotlin.math.cos
import kotlin.math.sin

@Module.Info(
        name = "EntitySpeed",
        category = Module.Category.MOVEMENT,
        description = "Abuse client-sided movement to shape sound barrier breaking rideables"
)
object EntitySpeed : Module() {
    private val speed = register(Settings.floatBuilder("Speed").withValue(1.0f).withRange(0.1f, 25.0f).withStep(0.1f))
    private val antiStuck = register(Settings.b("AntiStuck", true))
    private val flight = register(Settings.b("Flight", false))
    private val glideSpeed = register(Settings.floatBuilder("GlideSpeed").withValue(0.1f).withRange(0.0f, 1.0f).withStep(0.01f).withVisibility { flight.value })
    private val upSpeed = register(Settings.floatBuilder("UpSpeed").withValue(1.0f).withRange(0.0f, 5.0f).withStep(0.1f).withVisibility { flight.value })
    private val opacity = register(Settings.floatBuilder("BoatOpacity").withValue(1.0f).withRange(0.0f, 1.0f).withStep(0.01f))
    private val forceInteract = register(Settings.b("ForceInteract", false))
    private val interactTickDelay = register(Settings.integerBuilder("InteractTickDelay").withValue(2).withRange(1, 20).withStep(1).withVisibility { forceInteract.value })

    init {
        listener<PacketEvent.Send> {
            if (!forceInteract.value || mc.player?.ridingEntity !is EntityBoat) return@listener
            if (it.packet is CPacketPlayer.Rotation || it.packet is CPacketInput) {
                it.cancel()
            }
            if (it.packet is CPacketVehicleMove) {
                if (mc.player?.ridingEntity is EntityBoat && mc.player.ticksExisted % interactTickDelay.value == 0) {
                    mc.playerController.interactWithEntity(mc.player, mc.player.ridingEntity, EnumHand.MAIN_HAND)
                }
            }
        }

        listener<PacketEvent.Receive> {
            if (!forceInteract.value || mc.player?.ridingEntity !is EntityBoat || it.packet !is SPacketMoveVehicle) return@listener
            it.cancel()
        }

        listener<PlayerTravelEvent> {
            mc.player?.ridingEntity?.let {
                if (it is EntityPig || it is AbstractHorse || it is EntityBoat && it.controllingPassenger == mc.player) {
                    steerEntity(it)
                    if (flight.value) fly(it)
                }
            }
        }
    }

    private fun steerEntity(entity: Entity) {
        val yawRad = MovementUtils.calcMoveYaw()

        val motionX = -sin(yawRad) * speed.value
        val motionZ = cos(yawRad) * speed.value

        if (MovementUtils.isInputing() && !isBorderingChunk(entity, motionX, motionZ)) {
            entity.motionX = motionX
            entity.motionZ = motionZ
        } else {
            entity.motionX = 0.0
            entity.motionZ = 0.0
        }

        if (entity is EntityHorse || entity is EntityBoat) {
            entity.rotationYaw = mc.player.rotationYaw

            // Make sure the boat doesn't turn etc (params: isLeftDown, isRightDown, isForwardDown, isBackDown)
            if (entity is EntityBoat) entity.updateInputs(false, false, false, false)
        }
    }

    private fun fly(entity: Entity) {
        if (!entity.isInWater) entity.motionY = -glideSpeed.value.toDouble()
        if (mc.gameSettings.keyBindJump.isKeyDown) entity.motionY += upSpeed.value / 2.0
    }

    private fun isBorderingChunk(entity: Entity, motionX: Double, motionZ: Double): Boolean {
        return antiStuck.value && mc.world.getChunk((entity.posX + motionX).toInt() shr 4, (entity.posZ + motionZ).toInt() shr 4) is EmptyChunk
    }

    @JvmStatic
    fun getOpacity(): Float = opacity.value
}
package org.kamiblue.client.module.modules.movement

import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityBoat
import net.minecraft.network.play.client.CPacketInput
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketVehicleMove
import net.minecraft.network.play.server.SPacketMoveVehicle
import net.minecraft.util.EnumHand
import net.minecraft.world.chunk.EmptyChunk
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.event.events.PlayerTravelEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.EntityUtils.steerEntity
import org.kamiblue.client.util.MovementUtils
import org.kamiblue.client.util.MovementUtils.calcMoveYaw
import org.kamiblue.client.util.threads.safeListener
import kotlin.math.cos
import kotlin.math.sin

internal object BoatFly : Module(
    name = "BoatFly",
    category = Category.MOVEMENT,
    description = "Fly using boats"
) {
    private const val flight = true

    private val speed by setting("Speed", 1.0f, 0.1f..25.0f, 0.1f)
    private val antiStuck by setting("Anti Stuck", true)
    private val glideSpeed by setting("Glide Speed", 0.1f, 0.0f..1.0f, 0.01f)
    private val upSpeed by setting("Up Speed", 1.0f, 0.0f..5.0f, 0.1f)
    val opacity by setting("Boat Opacity", 1.0f, 0.0f..1.0f, 0.01f)
    val size by setting("Boat Scale", 1.0, 0.05..1.5, 0.01)
    private val forceInteract by setting("Force Interact", false)
    private val interactTickDelay by setting("Interact Delay", 2, 1..20, 1, { forceInteract }, description = "Force interact packet delay, in ticks.")

    init {
        safeListener<PacketEvent.Send> {
            val ridingEntity = player.ridingEntity

            if (!forceInteract || ridingEntity !is EntityBoat) return@safeListener

            if (it.packet is CPacketPlayer.Rotation || it.packet is CPacketInput) {
                it.cancel()
            }

            if (it.packet is CPacketVehicleMove) {
                if (player.ticksExisted % interactTickDelay == 0) {
                    playerController.interactWithEntity(player, ridingEntity, EnumHand.MAIN_HAND)
                }
            }
        }

        safeListener<PacketEvent.Receive> {
            if (!forceInteract || player.ridingEntity !is EntityBoat || it.packet !is SPacketMoveVehicle) return@safeListener
            it.cancel()
        }

        safeListener<PlayerTravelEvent> {
            player.ridingEntity?.let { entity ->
                if (entity is EntityBoat && entity.controllingPassenger == player) {
                    steerEntity(entity, speed, antiStuck)

                    // Make sure the boat doesn't turn etc (params: isLeftDown, isRightDown, isForwardDown, isBackDown)
                    entity.rotationYaw = player.rotationYaw
                    entity.updateInputs(false, false, false, false)

                    if (flight) fly(entity)
                }
            }
        }
    }

    private fun fly(entity: Entity) {
        if (!entity.isInWater) entity.motionY = -glideSpeed.toDouble()
        if (mc.gameSettings.keyBindJump.isKeyDown) entity.motionY += upSpeed / 2.0
    }

    @JvmStatic
    fun isBoatFlying(entityIn: Entity): Boolean {
        return isEnabled && mc.player?.ridingEntity == entityIn
    }

    @JvmStatic
    fun shouldModifyScale(entityIn: Entity): Boolean {
        return isBoatFlying(entityIn) && mc.gameSettings.thirdPersonView == 0
    }

}

package org.kamiblue.client.module.modules.movement

import net.minecraft.entity.Entity
import net.minecraft.entity.passive.AbstractHorse
import net.minecraft.entity.passive.EntityHorse
import net.minecraft.entity.passive.EntityPig
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

internal object EntitySpeed : Module(
    name = "EntitySpeed",
    category = Category.MOVEMENT,
    description = "Abuse client-sided movement to shape sound barrier breaking rideables"
) {
    private val speed by setting("Speed", 1.0f, 0.1f..25.0f, 0.1f)
    private val antiStuck by setting("Anti Stuck", true)
    private val flight by setting("Flight", false)
    private val glideSpeed by setting("Glide Speed", 0.1f, 0.0f..1.0f, 0.01f, { flight })
    private val upSpeed by setting("Up Speed", 1.0f, 0.0f..5.0f, 0.1f, { flight })

    init {
        safeListener<PlayerTravelEvent> {
            player.ridingEntity?.let { entity ->
                if (entity is EntityPig || entity is AbstractHorse && entity.controllingPassenger == player) {
                    steerEntity(entity, speed, antiStuck)

                    if (entity is EntityHorse) {
                        entity.rotationYaw = player.rotationYaw
                    }

                    if (flight) fly(entity)
                }
            }
        }
    }

    private fun fly(entity: Entity) {
        if (!entity.isInWater) entity.motionY = -glideSpeed.toDouble()
        if (mc.gameSettings.keyBindJump.isKeyDown) entity.motionY += upSpeed / 2.0
    }
}

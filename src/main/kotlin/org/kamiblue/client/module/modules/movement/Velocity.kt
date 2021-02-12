package org.kamiblue.client.module.modules.movement

import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketExplosion
import org.kamiblue.client.event.events.EntityCollisionEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.mixin.client.world.MixinBlockLiquid
import org.kamiblue.client.mixin.extension.packetMotionX
import org.kamiblue.client.mixin.extension.packetMotionY
import org.kamiblue.client.mixin.extension.packetMotionZ
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.event.listener.listener

/**
 * @see MixinBlockLiquid.modifyAcceleration
 */
internal object Velocity : Module(
    name = "Velocity",
    alias = arrayOf("AntiKB", "Knockback"),
    description = "Modify knock back impact",
    category = Category.MOVEMENT
) {
    private val noPush = setting("No Push", true)
    private val horizontal = setting("Horizontal", 0f, -5f..5f, 0.05f)
    private val vertical = setting("Vertical", 0f, -5f..5f, 0.05f)

    init {
        listener<PacketEvent.Receive> {
            if (mc.player == null) return@listener
            if (it.packet is SPacketEntityVelocity) {
                with(it.packet) {
                    if (entityID != mc.player.entityId) return@listener
                    if (isZero) {
                        it.cancel()
                    } else {
                        packetMotionX = (packetMotionX * horizontal.value).toInt()
                        packetMotionY = (packetMotionY * vertical.value).toInt()
                        packetMotionZ = (packetMotionZ * horizontal.value).toInt()
                    }
                }
            } else if (it.packet is SPacketExplosion) {
                with(it.packet) {
                    if (isZero) {
                        it.cancel()
                    } else {
                        packetMotionX *= horizontal.value
                        packetMotionY *= vertical.value
                        packetMotionZ *= horizontal.value
                    }
                }
            }
        }

        listener<EntityCollisionEvent> {
            if (it.entity != mc.player) return@listener
            if (noPush.value || isZero) {
                it.cancel()
            } else {
                it.x = it.x * horizontal.value
                it.y = it.y * vertical.value
                it.z = it.z * horizontal.value
            }
        }
    }

    private val isZero get() = horizontal.value == 0f && vertical.value == 0f
}

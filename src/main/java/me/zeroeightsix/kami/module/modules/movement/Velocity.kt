package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.events.EntityCollisionEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.mixin.client.world.MixinBlockLiquid
import me.zeroeightsix.kami.mixin.extension.packetMotionX
import me.zeroeightsix.kami.mixin.extension.packetMotionY
import me.zeroeightsix.kami.mixin.extension.packetMotionZ
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketExplosion
import org.kamiblue.event.listener.listener

/**
 * @see MixinBlockLiquid.modifyAcceleration
 */
object Velocity : Module(
    name = "Velocity",
    description = "Modify knock back impact",
    category = Category.MOVEMENT
) {
    private val noPush = setting("NoPush", true)
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
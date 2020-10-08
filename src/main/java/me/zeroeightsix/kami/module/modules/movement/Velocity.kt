package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.events.EntityEvent.EntityCollision
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketExplosion

/**
 * @see me.zeroeightsix.kami.mixin.client.MixinBlockLiquid
 */
@Module.Info(
        name = "Velocity",
        description = "Modify knockback impact",
        category = Module.Category.MOVEMENT
)
object Velocity : Module() {
    private val noPush = register(Settings.b("NoPush", true))
    private val horizontal = register(Settings.floatBuilder("Horizontal").withValue(0f).withRange(-5f, 5f))
    private val vertical = register(Settings.floatBuilder("Vertical").withValue(0f).withRange(-5f, 5f))

    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketEntityVelocity && it.packet !is SPacketExplosion) return@listener
            if (horizontal.value == 0f && vertical.value == 0f) {
                it.cancel()
            } else if (it.packet is SPacketEntityVelocity) {
                with(it.packet) {
                    if (entityID == mc.player.entityId) {
                        motionX = (motionX * horizontal.value).toInt()
                        motionY = (motionY * vertical.value).toInt()
                        motionZ = (motionZ * horizontal.value).toInt()
                    }
                }
            } else if (it.packet is SPacketExplosion) {
                with(it.packet) {
                    motionX *= horizontal.value
                    motionY *= vertical.value
                    motionZ *= horizontal.value
                }
            }
        }

        listener<EntityCollision> {
            if (it.entity != mc.player) return@listener
            if (noPush.value || horizontal.value == 0f && vertical.value == 0f) {
                it.cancel()
            } else {
                it.x = it.x * horizontal.value
                it.y = it.y * vertical.value
                it.z = it.z * horizontal.value
            }
        }
    }
}
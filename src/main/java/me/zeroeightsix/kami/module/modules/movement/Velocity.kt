package me.zeroeightsix.kami.module.modules.movement

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.EntityEvent.EntityCollision
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
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

    @EventHandler
    private val packetEventListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (event.packet !is SPacketEntityVelocity && event.packet !is SPacketExplosion) return@EventHook
        if (horizontal.value == 0f && vertical.value == 0f) {
            event.cancel()
        } else if (event.packet is SPacketEntityVelocity) {
            with(event.packet) {
                if (entityID == mc.player.entityId) {
                    motionX = (motionX * horizontal.value).toInt()
                    motionY = (motionY * vertical.value).toInt()
                    motionZ = (motionZ * horizontal.value).toInt()
                }
            }
        } else if (event.packet is SPacketExplosion) {
            with(event.packet) {
                motionX *= horizontal.value
                motionY *= vertical.value
                motionZ *= horizontal.value
            }
        }
    })

    @EventHandler
    private val entityCollisionListener = Listener(EventHook { event: EntityCollision ->
        if (event.entity != mc.player) return@EventHook
        if (noPush.value || horizontal.value == 0f && vertical.value == 0f) {
            event.cancel()
        } else {
            event.x = event.x * horizontal.value
            event.y = event.y * vertical.value
            event.z = event.z * horizontal.value
        }
    })
}
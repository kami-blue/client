package me.zeroeightsix.kami.module.modules.movement

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.KamiEvent
import me.zeroeightsix.kami.event.events.EntityEvent.EntityCollision
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketExplosion

/**
 * Created by 086 on 16/11/2017.
 * @see me.zeroeightsix.kami.mixin.client.MixinBlockLiquid
 */
@Module.Info(
        name = "Velocity",
        description = "Modify knockback impact",
        category = Module.Category.MOVEMENT
)
class Velocity : Module() {
    private val noPush = register(Settings.b("NoPush", true))
    private val horizontal = register(Settings.f("Horizontal", 0f))
    private val vertical = register(Settings.f("Vertical", 0f))

    @EventHandler
    private val packetEventListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (event.era == KamiEvent.Era.PRE) {
            if (event.packet is SPacketEntityVelocity) {
                val velocity = event.packet as SPacketEntityVelocity
                if (velocity.getEntityID() == mc.player.entityId) {
                    if (horizontal.value == 0f && vertical.value == 0f) event.cancel()
                    (velocity.motionX) *= (horizontal.value).toInt()
                    (velocity.motionY) *= (vertical.value).toInt()
                    (velocity.motionZ) *= (horizontal.value).toInt()
                }
            } else if (event.packet is SPacketExplosion) {
                if (horizontal.value == 0f && vertical.value == 0f) event.cancel()
                val velocity = event.packet as SPacketExplosion
                velocity.motionX *= horizontal.value
                velocity.motionY *= vertical.value
                velocity.motionZ *= horizontal.value
            }
        }
    })

    @EventHandler
    private val entityCollisionListener = Listener(EventHook { event: EntityCollision ->
        if (event.entity === mc.player) {
            if (horizontal.value == 0f && vertical.value == 0f || noPush.value) {
                event.cancel()
                return@EventHook
            }
            event.x = -event.x * horizontal.value
            event.y = 0.0
            event.z = -event.z * horizontal.value
        }
    })
}
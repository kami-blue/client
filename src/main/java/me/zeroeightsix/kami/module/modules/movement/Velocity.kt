package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.events.EntityEvent.EntityCollision
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
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
    private val noPush = setting("NoPush", true)
    private val horizontal = setting("Horizontal", 0f, -5f..5f, 0.05f)
    private val vertical = setting("Vertical", 0f, -5f..5f, 0.05f)

    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketEntityVelocity && it.packet !is SPacketExplosion) return@listener
            if (it.packet is SPacketEntityVelocity) {
                with(it.packet) {
                    if (entityID != mc.player.entityId) return@listener
                    if (isZero) {
                        it.cancel()
                    } else {
                        motionX = (motionX * horizontal.value).toInt()
                        motionY = (motionY * vertical.value).toInt()
                        motionZ = (motionZ * horizontal.value).toInt()
                    }
                }
            } else if (it.packet is SPacketExplosion) {
                with(it.packet) {
                    if (isZero) {
                        it.cancel()
                    } else {
                        motionX *= horizontal.value
                        motionY *= vertical.value
                        motionZ *= horizontal.value
                    }
                }
            }
        }

        listener<EntityCollision> {
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
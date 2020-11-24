package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.events.EntityEvent.EntityCollision
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.mixin.client.world.MixinBlockLiquid
import me.zeroeightsix.kami.mixin.extension.packetMotionX
import me.zeroeightsix.kami.mixin.extension.packetMotionY
import me.zeroeightsix.kami.mixin.extension.packetMotionZ
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketExplosion

/**
 * @see MixinBlockLiquid.modifyAcceleration
 */
@Module.Info(
        name = "Velocity",
        description = "Modify knock back impact",
        category = Module.Category.MOVEMENT
)
object Velocity : Module() {
    private val noPush = register(Settings.b("NoPush", true))
    private val horizontal = register(Settings.floatBuilder("Horizontal").withValue(0f).withRange(-5f, 5f))
    private val vertical = register(Settings.floatBuilder("Vertical").withValue(0f).withRange(-5f, 5f))

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
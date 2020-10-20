package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import kotlin.math.round

@Module.Info(
        name = "YawLock",
        category = Module.Category.PLAYER,
        description = "Locks your camera yaw"
)
object YawLock : Module() {
    private val auto = register(Settings.b("Auto", true))
    private val yaw = register(Settings.floatBuilder("Yaw").withValue(180.0f).withRange(-180.0f, 180.0f).withStep(1.0f))
    private val slice = register(Settings.integerBuilder("Slice").withValue(8).withRange(2, 32).withStep(1).withVisibility { auto.value })

    init {
        listener<SafeTickEvent> {
            if (auto.value) {
                val angle = 360.0f / slice.value
                mc.player.rotationYaw = round(mc.player.rotationYaw / angle) * angle
                mc.player.ridingEntity?.let { it.rotationYaw = mc.player.rotationYaw }
            } else {
                mc.player.rotationYaw = yaw.value
            }
        }
    }
}
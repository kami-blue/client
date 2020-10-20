package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import kotlin.math.round

@Module.Info(
        name = "PitchLock",
        category = Module.Category.PLAYER,
        description = "Locks your camera pitch"
)
object PitchLock : Module() {
    private val auto = register(Settings.b("Auto", true))
    private val pitch = register(Settings.floatBuilder("Pitch").withValue(180.0f).withRange(-90.0f, 90.0f).withStep(1.0f))
    private val slice = register(Settings.integerBuilder("Slice").withValue(8).withRange(2, 32).withStep(1).withVisibility { auto.value })

    init {
        listener<SafeTickEvent> {
            if (auto.value) {
                val angle = 360.0f / slice.value
                mc.player.rotationPitch = round(mc.player.rotationPitch / angle) * angle
            } else {
                mc.player.rotationPitch = pitch.value
            }
        }
    }
}
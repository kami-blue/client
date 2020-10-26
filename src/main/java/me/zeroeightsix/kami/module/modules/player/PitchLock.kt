package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.event.listener
import kotlin.math.round

@Module.Info(
        name = "PitchLock",
        category = Module.Category.PLAYER,
        description = "Locks your camera pitch"
)
object PitchLock : Module() {
    private val auto = setting("Auto", true)
    private val pitch = setting("Pitch", 180.0f, -90.0f..90.0f, 1.0f)
    private val slice = setting("Slice", 8, 2..32, 1, { auto.value })

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
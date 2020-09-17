package me.zeroeightsix.kami.module.modules.movement

import baritone.api.BaritoneAPI
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings

@Module.Info(
        name = "SafeWalk",
        category = Module.Category.MOVEMENT,
        description = "Keeps you from walking off edges"
)
object SafeWalk : Module() {
    private val baritoneCompat = register(Settings.b("BaritoneCompatibility", true))

    fun shouldSafewalk(): Boolean {
        return isEnabled && (baritoneCompat.value && BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.goal == null || !baritoneCompat.value)
    }
}
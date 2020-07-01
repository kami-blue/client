package me.zeroeightsix.kami.module.modules.movement

import baritone.api.BaritoneAPI
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings

/**
 * Created by 086 on 11/10/2018.
 */
@Module.Info(name = "SafeWalk", category = Module.Category.MOVEMENT, description = "Keeps you from walking off edges")
class SafeWalk : Module() {
    private var baritoneCompat = register(Settings.b("BaritoneCompatibility", true))

    fun shouldSafewalk(): Boolean {
        return isEnabled && (baritoneCompat.value && BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.goal == null || !baritoneCompat.value)
    }

    companion object {
        @JvmField
        var INSTANCE: SafeWalk? = null
    }

    init {
        INSTANCE = this
    }
}
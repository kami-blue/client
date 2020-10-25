package me.zeroeightsix.kami.util

import baritone.api.BaritoneAPI
import baritone.api.Settings
import me.zeroeightsix.kami.process.TemporaryPauseProcess

object BaritoneUtils {
    var settingsInitialized = false

    var paused = false
        private set
    val isPathing get() = BaritoneAPI.getProvider().primaryBaritone.pathingBehavior.isPathing
    val isCustomGoalActive get() = BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.isActive

    fun pause() {
        if (!paused) {
            BaritoneAPI.getProvider().primaryBaritone.pathingControlManager.registerProcess(TemporaryPauseProcess)
            paused = true
        }
    }

    fun unpause() {
        if (paused) {
            val process = BaritoneAPI.getProvider().primaryBaritone.pathingControlManager.mostRecentInControl()
            if (process.isPresent && process.get() == TemporaryPauseProcess) /* Don't run if not paused lol */ {
                paused = false
                process.get().onLostControl()
            }
        }
    }

    fun settings(): Settings? {
        return if (!settingsInitialized) {
            null
        } else {
            BaritoneAPI.getSettings()
        }
    }
}
package me.zeroeightsix.kami.util

import baritone.api.BaritoneAPI
import me.zeroeightsix.kami.process.TemporaryPauseProcess

object BaritoneUtils {
    var initialized = false
    var paused = false; private set

    val provider get() = if (initialized) BaritoneAPI.getProvider() else null
    val settings get() = if (initialized) BaritoneAPI.getSettings() else null
    val primary get() = provider?.primaryBaritone
    val prefix get() = settings?.prefix?.value ?: "#"

    val isPathing get() = primary?.pathingBehavior?.isPathing ?: false
    val isActive
        get() = primary?.customGoalProcess?.isActive ?: false
            || primary?.pathingControlManager?.mostRecentInControl()?.orElse(null)?.isActive ?: false

    fun pause() {
        if (!paused) {
            paused = true
            primary?.pathingControlManager?.registerProcess(TemporaryPauseProcess)
        }
    }

    fun unpause() {
        paused = false
    }

    fun cancelEverything() = primary?.pathingBehavior?.cancelEverything()
}
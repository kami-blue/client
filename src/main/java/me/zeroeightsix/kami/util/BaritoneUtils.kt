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
            || primary?.pathingControlManager?.mostRecentInControl()?.let {
            it.isPresent && it.get().isActive
        } ?: false

    fun pause() {
        if (!paused) {
            primary?.pathingControlManager?.registerProcess(TemporaryPauseProcess)
            paused = true
        }
    }

    fun unpause() {
        if (paused) {
            primary?.pathingControlManager?.mostRecentInControl()?.let {
                if (it.isPresent && it.get() == TemporaryPauseProcess) /* Don't run if not paused lol */ {
                    paused = false
                    it.get().onLostControl()
                }
            }
        }
    }

    fun cancelEverything() = primary?.pathingBehavior?.cancelEverything()
}
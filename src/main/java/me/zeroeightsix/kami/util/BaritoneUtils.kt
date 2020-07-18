package me.zeroeightsix.kami.util

import baritone.api.BaritoneAPI
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.process.TemporaryPauseProcess

object BaritoneUtils {
    var paused = false

    fun pause() {
        if (!paused) {
            BaritoneAPI.getProvider().primaryBaritone.pathingControlManager.registerProcess(KamiMod.pauseProcess)
            paused = true
        }
    }

    fun unpause() {
        if (paused) {
            if (BaritoneAPI.getProvider().primaryBaritone.pathingControlManager.mostRecentInControl().isPresent &&
                    BaritoneAPI.getProvider().primaryBaritone.pathingControlManager.mostRecentInControl().get() is TemporaryPauseProcess) /* Don't run if not paused lol */ {
                paused = false
                BaritoneAPI.getProvider().primaryBaritone.pathingControlManager.mostRecentInControl().get().onLostControl()
            }
        }
    }
}
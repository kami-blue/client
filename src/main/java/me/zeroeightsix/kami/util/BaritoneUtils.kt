package me.zeroeightsix.kami.util

import baritone.api.BaritoneAPI
import me.zeroeightsix.kami.KamiMod

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
            val process = BaritoneAPI.getProvider().primaryBaritone.pathingControlManager.mostRecentInControl()
            if (process.isPresent && process.get() == KamiMod.pauseProcess) /* Don't run if not paused lol */ {
                paused = false
                process.get().onLostControl()
            }
        }
    }
}
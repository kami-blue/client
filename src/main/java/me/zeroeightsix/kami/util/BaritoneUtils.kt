package me.zeroeightsix.kami.util

import baritone.api.BaritoneAPI
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.process.TemporaryPauseProcess

object BaritoneUtils
{
    fun pause()
    {
        BaritoneAPI.getProvider().primaryBaritone.pathingControlManager.registerProcess(KamiMod.pauseProcess)
    }

    fun unpause()
    {
        if (BaritoneAPI.getProvider().primaryBaritone.pathingControlManager.mostRecentInControl().isPresent)
        {
            BaritoneAPI.getProvider().primaryBaritone.pathingControlManager.mostRecentInControl().get().onLostControl()
        }
    }
}
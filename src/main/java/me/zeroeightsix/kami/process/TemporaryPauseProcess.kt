package me.zeroeightsix.kami.process

import baritone.api.process.IBaritoneProcess
import baritone.api.process.PathingCommand
import baritone.api.process.PathingCommandType
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.module.modules.player.AutoEat
import me.zeroeightsix.kami.util.BaritoneUtils

/**
 * Created by Dewy on the 17th of May, 2020
 * Updated by Xiaro on 11/09/20
 *
 * thanks leijurv for pseudocode
 */
object TemporaryPauseProcess : IBaritoneProcess {

    override fun isTemporary(): Boolean {
        return true
    }

    override fun priority(): Double {
        return 3.0
    }

    override fun isActive(): Boolean {
        return BaritoneUtils.paused
    }

    override fun onTick(calcFailed: Boolean, isSafeToCancel: Boolean): PathingCommand {
        if (!isSafeToCancel) {
            ModuleManager.getModuleT(AutoEat::class.java)?.eating = false
        }
        return PathingCommand(null, PathingCommandType.REQUEST_PAUSE)
    }

    override fun onLostControl() {
        // nothing :p
    }

    override fun displayName0(): String {
        return "KAMI Blue Pauser"
    }
}
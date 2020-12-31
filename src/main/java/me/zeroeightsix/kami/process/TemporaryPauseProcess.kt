package me.zeroeightsix.kami.process

import baritone.api.process.IBaritoneProcess
import baritone.api.process.PathingCommand
import baritone.api.process.PathingCommandType
import me.zeroeightsix.kami.util.BaritoneUtils

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
        return PathingCommand(null, PathingCommandType.REQUEST_PAUSE)
    }

    override fun onLostControl() {
        // nothing :p
    }

    override fun displayName0(): String {
        return "KAMI Blue Pauser"
    }
}
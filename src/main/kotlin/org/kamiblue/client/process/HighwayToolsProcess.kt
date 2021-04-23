package org.kamiblue.client.process

import baritone.api.process.IBaritoneProcess
import baritone.api.process.PathingCommand
import baritone.api.process.PathingCommandType
import org.kamiblue.client.module.modules.misc.HighwayTools
import org.kamiblue.client.util.math.CoordinateConverter.asString

/**
 * @author Avanatiker
 * @since 26/08/20
 */
object HighwayToolsProcess : IBaritoneProcess {

    override fun isTemporary(): Boolean {
        return true
    }

    override fun priority(): Double {
        return 2.0
    }

    override fun onLostControl() {}

    override fun displayName0(): String {
        val lastTask = HighwayTools.lastTask

        val processName = HighwayTools.goal?.goalPos?.asString()
            ?: lastTask?.toString()
            ?: "Thinking"

        return "HighwayTools: $processName"
    }

    override fun isActive(): Boolean {
        return HighwayTools.isActive()
    }

    override fun onTick(p0: Boolean, p1: Boolean): PathingCommand {
        return HighwayTools.goal?.let {
            PathingCommand(it, PathingCommandType.SET_GOAL_AND_PATH)
        } ?: PathingCommand(null, PathingCommandType.REQUEST_PAUSE)
    }
}
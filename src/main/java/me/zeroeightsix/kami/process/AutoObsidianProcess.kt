package me.zeroeightsix.kami.process

import baritone.api.BaritoneAPI
import baritone.api.IBaritone
import baritone.api.pathing.goals.GoalNear
import baritone.api.process.IBaritoneProcess
import baritone.api.process.PathingCommand
import baritone.api.process.PathingCommandType
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.modules.misc.AutoObsidian

/**
 * Created by Xiaro on 13/07/20.
 */
class AutoObsidianProcess : IBaritoneProcess {

    private lateinit var baritone: IBaritone

    override fun isTemporary(): Boolean {
        return true
    }

    override fun priority(): Double {
        return 2.0
    }

    override fun onLostControl() {}

    override fun displayName0(): String {
        return "AutoObsidian: " + KamiMod.MODULE_MANAGER.getModuleT(AutoObsidian::class.java).state.toString().toLowerCase()
    }

    override fun isActive(): Boolean {
        return (KamiMod.MODULE_MANAGER.isModuleEnabled(AutoObsidian::class.java)
                && KamiMod.MODULE_MANAGER.getModuleT(AutoObsidian::class.java).active)
    }

    override fun onTick(p0: Boolean, p1: Boolean): PathingCommand? {
        baritone = BaritoneAPI.getProvider().primaryBaritone
        val autoObsidian = KamiMod.MODULE_MANAGER.getModuleT(AutoObsidian::class.java)
        return if (autoObsidian.pathing && autoObsidian.goal != null) {
            PathingCommand(GoalNear(autoObsidian.goal, 0), PathingCommandType.SET_GOAL_AND_PATH)
        } else PathingCommand(null, PathingCommandType.REQUEST_PAUSE)
    }
}
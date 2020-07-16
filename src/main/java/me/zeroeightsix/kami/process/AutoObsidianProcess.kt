package me.zeroeightsix.kami.process

import baritone.api.BaritoneAPI
import baritone.api.IBaritone
import baritone.api.pathing.goals.GoalBlock
import baritone.api.process.IBaritoneProcess
import baritone.api.process.PathingCommand
import baritone.api.process.PathingCommandType
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.modules.misc.AutoObsidian
import net.minecraft.util.math.BlockPos

class AutoObsidianProcess : IBaritoneProcess {

    private lateinit var baritone: IBaritone

    override fun isTemporary(): Boolean {
        return true
    }

    override fun priority(): Double {
        return 3.0
    }

    override fun onLostControl() {}

    override fun displayName0(): String {
        return "Auto Obsidian"
    }

    override fun isActive(): Boolean {
        return (KamiMod.MODULE_MANAGER.isModuleEnabled(AutoObsidian::class.java)
                && KamiMod.MODULE_MANAGER.getModuleT(AutoObsidian::class.java).active)
    }

    override fun onTick(p0: Boolean, p1: Boolean): PathingCommand? {
        baritone = BaritoneAPI.getProvider().primaryBaritone
        val autoObsidian = KamiMod.MODULE_MANAGER.getModuleT(AutoObsidian::class.java)
        return if (autoObsidian.goal != null && autoObsidian.pathing) {
            PathingCommand(GoalBlock(autoObsidian.goal), PathingCommandType.SET_GOAL_AND_PATH)
        } else {
            PathingCommand(null, PathingCommandType.DEFER)
        }
    }
}
package me.zeroeightsix.kami.module.modules.hidden

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.graphics.GuiFrameUtil

/**
 * @author dominikaaaa
 * @see me.zeroeightsix.kami.command.commands.FixGuiCommand
 */
@Module.Info(
        name = "FixGui",
        category = Module.Category.HIDDEN,
        showOnArray = Module.ShowOnArray.OFF,
        description = "Moves GUI elements back on screen"
)
class FixGui : Module() {
    override fun onUpdate() {
        if (mc.player == null) return
        GuiFrameUtil.fixFrames(mc)
        disable()
    }
}
package me.zeroeightsix.kami.module.modules.hidden

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.ClickGUI
import me.zeroeightsix.kami.util.graphics.GuiFrameUtil

/**
 * @author l1ving
 * @see me.zeroeightsix.kami.command.commands.FixGuiCommand
 *
 * Created by l1ving on 24/03/20
 * Updated by Xiaro on 28/08/20
 */
@Module.Info(
        name = "FixGui",
        category = Module.Category.HIDDEN,
        description = "Reset GUI scale and moves GUI elements back on screen",
        showOnArray = Module.ShowOnArray.OFF,
        enabledByDefault = true
)
object FixGui : Module() {
    override fun onUpdate() {
        ClickGUI.resetScale()
        GuiFrameUtil.fixFrames(mc)
        disable()
    }
}
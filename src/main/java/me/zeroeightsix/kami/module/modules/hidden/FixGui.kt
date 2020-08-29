package me.zeroeightsix.kami.module.modules.hidden

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.module.modules.ClickGUI
import me.zeroeightsix.kami.util.graphics.GuiFrameUtil

/**
 * @author dominikaaaa
 * @see me.zeroeightsix.kami.command.commands.FixGuiCommand
 *
 * Created by dominikaaaa on 24/03/20
 * Updated by Xiaro on 28/08/20
 */
@Module.Info(
        name = "FixGui",
        category = Module.Category.HIDDEN,
        showOnArray = Module.ShowOnArray.OFF,
        description = "Reset GUI scale and moves GUI elements back on screen"
)
class FixGui : Module() {
    override fun onUpdate() {
        ModuleManager.getModuleT(ClickGUI::class.java)?.resetScale()
        GuiFrameUtil.fixFrames(mc)
        disable()
    }
}
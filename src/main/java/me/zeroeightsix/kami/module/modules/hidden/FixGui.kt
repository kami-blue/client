package me.zeroeightsix.kami.module.modules.hidden

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.ClickGUI
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.GuiFrameUtil

/**
 * @see me.zeroeightsix.kami.command.commands.FixGuiCommand
 */
@Module.Info(
        name = "FixGui",
        category = Module.Category.HIDDEN,
        description = "Reset GUI scale and moves GUI elements back on screen",
        showOnArray = Module.ShowOnArray.OFF,
        enabledByDefault = true
)
object FixGui : Module() {
    init {
        listener<SafeTickEvent> {
            ClickGUI.resetScale()
            GuiFrameUtil.fixFrames(mc)
            disable()
        }
    }
}
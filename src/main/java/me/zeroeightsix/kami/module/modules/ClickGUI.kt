package me.zeroeightsix.kami.module.modules

import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.module.Module
import org.lwjgl.input.Keyboard

/**
 * Created by 086 on 23/08/2017.
 * Updated by Xiaro on 18/08/20
 */
@Module.Info(
        name = "ClickGUI",
        description = "Opens the Click GUI",
        category = Module.Category.CLIENT
)
class ClickGUI : Module() {

    override fun onEnable() {
        if (mc.currentScreen !is DisplayGuiScreen) {
            mc.displayGuiScreen(DisplayGuiScreen(mc.currentScreen))
        }
    }

    override fun onDisable() {
        if (mc.currentScreen is DisplayGuiScreen) {
            (mc.currentScreen as DisplayGuiScreen).closeGui()
        }
    }

    init {
        bind.value.key = Keyboard.KEY_Y
    }
}
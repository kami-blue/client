package me.zeroeightsix.kami.module.modules

import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import org.lwjgl.input.Keyboard

/**
 * Created by 086 on 23/08/2017.
 */
@Module.Info(
        name = "clickGUI",
        description = "Opens the Click GUI",
        category = Module.Category.CLIENT
)
class ClickGUI : Module() {
    private val scale = register(Settings.floatBuilder("Scale").withValue(1f).withRange(0f, 4f).build())

    override fun onEnable() {
        if (mc.currentScreen !is DisplayGuiScreen) {
            mc.displayGuiScreen(DisplayGuiScreen(mc.currentScreen))
        }
        disable()
    }

    init {
        bind.value.key = Keyboard.KEY_Y
    }
}
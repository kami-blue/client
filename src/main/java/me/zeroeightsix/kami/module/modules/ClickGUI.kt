package me.zeroeightsix.kami.module.modules

import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimerUtils
import org.lwjgl.input.Keyboard
import kotlin.math.round

@Module.Info(
        name = "ClickGUI",
        description = "Opens the Click GUI",
        showOnArray = Module.ShowOnArray.OFF,
        category = Module.Category.CLIENT,
        alwaysListening = true
)
object ClickGUI : Module() {
    private val scaleSetting = register(Settings.integerBuilder("Scale").withValue(100).withRange(10, 400).build())

    private var prevScale = scaleSetting.value / 100.0
    private var scale = prevScale
    private val settingTimer = TimerUtils.StopTimer()

    fun resetScale() {
        scaleSetting.value = 100
        prevScale = 1.0
        scale = 1.0
    }

    fun getScaleFactor(): Double {
        return (prevScale + (scale - prevScale) * mc.renderPartialTicks) * 2.0
    }

    override fun onUpdate() {
        prevScale = scale
        if (settingTimer.stop() > 500L) {
            val diff = scale - getRoundedScale()
            when {
                diff < -0.025 -> scale += 0.025
                diff > 0.025 -> scale -= 0.025
                else -> scale = getRoundedScale()
            }
        }
    }

    private fun getRoundedScale(): Double {
        return round((scaleSetting.value / 100.0) / 0.1) * 0.1
    }

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
        scaleSetting.settingListener = Setting.SettingListeners {
            settingTimer.reset()
        }
    }
}

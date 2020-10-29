package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.clickgui.KamiClickGui
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import org.lwjgl.input.Keyboard
import kotlin.math.round

@Module.Info(
        name = "ClickGUI",
        description = "Opens the Click GUI",
        category = Module.Category.CLIENT,
        showOnArray = false,
        alwaysListening = true
)
object ClickGUI : Module() {
    private val scaleSetting = setting("Scale", 100, 10..400, 10)
    val blur = setting("Blur", 0.5f, 0.0f..1.0f, 0.05f)
    val darkness = setting("Darkness", 0.25f, 0.0f..1.0f, 0.05f)

    private var prevScale = scaleSetting.value / 100.0f
    private var scale = prevScale
    private val settingTimer = TimerUtils.StopTimer()

    fun resetScale() {
        scaleSetting.value = 100
        prevScale = 1.0f
        scale = 1.0f
    }

    fun getScaleFactorFloat() = (prevScale + (scale - prevScale) * mc.renderPartialTicks) * 2.0f

    fun getScaleFactor() = (prevScale + (scale - prevScale) * mc.renderPartialTicks) * 2.0

    init {
        listener<SafeTickEvent> {
            prevScale = scale
            if (settingTimer.stop() > 500L) {
                val diff = scale - getRoundedScale()
                when {
                    diff < -0.025 -> scale += 0.025f
                    diff > 0.025 -> scale -= 0.025f
                    else -> scale = getRoundedScale()
                }
            }
        }
    }

    private fun getRoundedScale(): Float {
        return round((scaleSetting.value / 100.0f) / 0.1f) * 0.1f
    }

    override fun onEnable() {
        if (mc.currentScreen !is KamiClickGui) {
            HudGUI.disable()
            mc.displayGuiScreen(KamiClickGui)
            KamiEventBus.subscribe(KamiClickGui)
            KamiClickGui.onDisplayed()
        }
    }

    override fun onDisable() {
        if (mc.currentScreen is KamiClickGui) {
            mc.displayGuiScreen(null)
            KamiEventBus.unsubscribe(KamiClickGui)
        }
    }

    init {
        bind.value.key = Keyboard.KEY_Y
        scaleSetting.listeners.add {
            settingTimer.reset()
        }
    }
}

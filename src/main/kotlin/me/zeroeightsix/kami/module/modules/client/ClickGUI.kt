package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.event.events.ShutdownEvent
import me.zeroeightsix.kami.gui.clickgui.KamiClickGui
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.StopTimer
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard
import kotlin.math.round

internal object ClickGUI : Module(
    name = "ClickGUI",
    description = "Opens the Click GUI",
    category = Category.CLIENT,
    showOnArray = false,
    alwaysListening = true
) {
    private val scaleSetting = setting("Scale", 100, 50..400, 5)
    val blur by setting("Blur", 0.0f, 0.0f..1.0f, 0.05f)
    val darkness by setting("Darkness", 0.25f, 0.0f..1.0f, 0.05f)
    val fadeInTime by setting("FadeInTime", 0.25f, 0.0f..1.0f, 0.05f)
    val fadeOutTime by setting("FadeOutTime", 0.1f, 0.0f..1.0f, 0.05f)

    private var prevScale = scaleSetting.value / 100.0f
    private var scale = prevScale
    private val settingTimer = StopTimer()

    fun resetScale() {
        scaleSetting.value = 100
        prevScale = 1.0f
        scale = 1.0f
    }

    fun getScaleFactorFloat() = (prevScale + (scale - prevScale) * mc.renderPartialTicks) * 2.0f

    fun getScaleFactor() = (prevScale + (scale - prevScale) * mc.renderPartialTicks) * 2.0

    init {
        safeListener<TickEvent.ClientTickEvent> {
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

        listener<ShutdownEvent> {
            disable()
        }
    }

    private fun getRoundedScale(): Float {
        return round((scaleSetting.value / 100.0f) / 0.1f) * 0.1f
    }

    init {
        onEnable {
            if (mc.currentScreen !is KamiClickGui) {
                HudEditor.disable()
                mc.displayGuiScreen(KamiClickGui)
                KamiClickGui.onDisplayed()
            }
        }

        onDisable {
            if (mc.currentScreen is KamiClickGui) {
                mc.displayGuiScreen(null)
            }
        }

        bind.value.setBind(Keyboard.KEY_Y)
        scaleSetting.listeners.add {
            settingTimer.reset()
        }
    }
}

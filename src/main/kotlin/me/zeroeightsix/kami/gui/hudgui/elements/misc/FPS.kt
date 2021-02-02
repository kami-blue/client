package me.zeroeightsix.kami.gui.hudgui.elements.misc

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.graphics.AnimationUtils
import net.minecraft.client.Minecraft
import org.kamiblue.event.listener.listener
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object FPS : LabelHud(
    name = "FPS",
    category = Category.MISC,
    description = "Frame per second in game"
) {

    private val showAverage = setting("ShowAverage", true)
    private val showMin = setting("ShowMin", false)
    private val showMax = setting("ShowMax", false)

    private val timer = TickTimer()
    private var prevFps = 0
    private var currentFps = 0

    private val longFps = IntArray(10)
    private var longFpsIndex = 0

    private var prevAvgFps = 0
    private var currentAvgFps = 0

    init {
        listener<RenderEvent> {
            if (timer.tick(1000L)) {
                prevFps = currentFps
                currentFps = Minecraft.getDebugFPS()

                longFps[longFpsIndex] = currentFps
                longFpsIndex = (longFpsIndex + 1) % 10

                prevAvgFps = currentAvgFps
                currentAvgFps = longFps.average().roundToInt()
            }
        }
    }

    override fun SafeClientEvent.updateText() {
        val deltaTime = AnimationUtils.toDeltaTimeFloat(timer.time) / 1000.0f
        val fps = (prevFps + (currentFps - prevFps) * deltaTime).roundToInt()
        val avg = (prevAvgFps + (currentAvgFps - prevAvgFps) * deltaTime).roundToInt()

        var min = 6969
        var max = 0
        for (value in longFps) {
            if (value != 0) min = min(value, min)
            max = max(value, max)
        }

        displayText.add("FPS", secondaryColor)
        displayText.add(fps.toString(), primaryColor)

        if (showAverage.value) {
            displayText.add("AVG", secondaryColor)
            displayText.add(avg.toString(), primaryColor)
        }

        if (showMin.value) {
            displayText.add("MIN", secondaryColor)
            displayText.add(min.toString(), primaryColor)
        }

        if (showMax.value) {
            displayText.add("MAX", secondaryColor)
            displayText.add(max.toString(), primaryColor)
        }
    }

}
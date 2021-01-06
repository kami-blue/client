package me.zeroeightsix.kami.gui.hudgui.elements.misc

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.mixin.extension.tickLength
import me.zeroeightsix.kami.mixin.extension.timer
import me.zeroeightsix.kami.setting.GuiConfig.setting
import net.minecraftforge.fml.common.gameevent.TickEvent
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

    private var fpsCounter = 0
    private val fptList = IntArray(20)
    private var fptIndex = 0
    private val fpsList = IntArray(300)
    private var fpsIndex = 0

    init {
        listener<RenderEvent> {
            fpsCounter++
        }

        listener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@listener
            fptList[fptIndex] = ((fpsCounter * (1000.0f / mc.timer.tickLength)).roundToInt())
            fptIndex = (fptIndex + 1) % 20
            fpsCounter = 0
        }
    }

    override fun updateText() {
        val fps = fptList.average().roundToInt()
        fpsList[fpsIndex] = fps
        fpsIndex = (fpsIndex + 1) % 300

        var avg = 0
        var min = 6969
        var max = 0
        for (value in fpsList) {
            avg += value
            if (min != 0) min = min(value, min)
            max = max(value, max)
        }
        avg /= 300

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
package me.zeroeightsix.kami.gui.hudgui.elements.misc

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.event.listener
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@HudElement.Info(
        category = HudElement.Category.MISC,
        description = "Display game FPS"
)
object FPS : LabelHud("FPS") {

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
            fptList[fptIndex] =((fpsCounter * (1000.0f / mc.timer.tickLength)).roundToInt())
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

        displayText.add("$fps")
        if (showAverage.value) displayText.add("avg. ${avg / 300}")
        if (showMin.value) displayText.add("min. $min")
        if (showMax.value) displayText.add("max. $max")
        displayText.add("fps")
    }

}
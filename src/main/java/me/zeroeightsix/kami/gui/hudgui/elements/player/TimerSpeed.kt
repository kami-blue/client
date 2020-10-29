package me.zeroeightsix.kami.gui.hudgui.elements.player

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.util.math.MathUtils

@HudElement.Info(
        category = HudElement.Category.MISC,
        description = "Display client side timer speed"
)
object TimerSpeed : LabelHud("TimerSpeed") {

    override fun updateText() {
        val timerSpeed = MathUtils.round(50.0f / mc.timer.tickLength, 2)
        displayText.add(timerSpeed.toString())
        displayText.add("x")
    }

    init {
        visible.value = false
    }
}
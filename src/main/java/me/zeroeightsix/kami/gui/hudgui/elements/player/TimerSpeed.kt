package me.zeroeightsix.kami.gui.hudgui.elements.player

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.mixin.extension.tickLength
import me.zeroeightsix.kami.mixin.extension.timer
import org.kamiblue.commons.utils.MathUtils

@HudElement.Info(
        category = HudElement.Category.PLAYER,
        description = "Client side timer speed"
)
object TimerSpeed : LabelHud("TimerSpeed") {

    override fun updateText() {
        val timerSpeed = MathUtils.round(50.0f / mc.timer.tickLength, 2)
        displayText.add(timerSpeed.toString(), primaryColor.value)
        displayText.add("x", secondaryColor.value)
    }

    init {
        visible.value = false
    }
}
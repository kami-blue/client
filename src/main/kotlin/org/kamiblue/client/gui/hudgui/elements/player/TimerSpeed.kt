package org.kamiblue.client.gui.hudgui.elements.player

import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.mixin.extension.tickLength
import org.kamiblue.client.mixin.extension.timer
import org.kamiblue.commons.utils.MathUtils

object TimerSpeed : LabelHud(
    name = "TimerSpeed",
    category = Category.PLAYER,
    description = "Client side timer speed"
) {

    override fun SafeClientEvent.updateText() {
        val timerSpeed = MathUtils.round(50.0f / mc.timer.tickLength, 2)

        displayText.add("%.2f".format(timerSpeed), primaryColor)
        displayText.add("x", secondaryColor)
    }

}
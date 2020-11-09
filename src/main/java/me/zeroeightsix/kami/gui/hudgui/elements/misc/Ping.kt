package me.zeroeightsix.kami.gui.hudgui.elements.misc

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.util.InfoCalculator

@HudElement.Info(
        category = HudElement.Category.MISC,
        description = "Delay between client and server"
)
object Ping : LabelHud("Ping") {

    override fun updateText() {
        displayText.add(InfoCalculator.ping().toString(), primaryColor.value)
        displayText.add("ms", secondaryColor.value)
    }

}
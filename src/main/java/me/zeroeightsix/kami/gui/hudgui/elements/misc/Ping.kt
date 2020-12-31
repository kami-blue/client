package me.zeroeightsix.kami.gui.hudgui.elements.misc

import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.util.InfoCalculator

object Ping : LabelHud(
    name = "Ping",
    category = Category.MISC,
    description = "Delay between client and server"
) {

    override fun updateText() {
        displayText.add(InfoCalculator.ping().toString(), primaryColor)
        displayText.add("ms", secondaryColor)
    }

}
package org.kamiblue.client.gui.hudgui.elements.misc

import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.util.TpsCalculator

object TPS : LabelHud(
    name = "TPS",
    category = Category.MISC,
    description = "Server TPS"
) {

    private val tpsList = FloatArray(20) { 20.0f }
    private var tpsIndex = 0

    override fun SafeClientEvent.updateText() {
        tpsList[tpsIndex] = TpsCalculator.tickRate
        tpsIndex = (tpsIndex + 1) % 20

        displayText.add("%.2f".format(tpsList.average()), primaryColor)
        displayText.add("tps", secondaryColor)
    }

}
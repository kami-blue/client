package org.kamiblue.client.gui.hudgui.elements.misc

import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.util.CircularArray
import org.kamiblue.client.util.TpsCalculator

object TPS : LabelHud(
    name = "TPS",
    category = Category.MISC,
    description = "Server TPS"
) {

    // buffered TPS readings to add some fluidity to the TPS HUD element
    private val tpsBuffer = CircularArray.create(20, 20f)

    override fun SafeClientEvent.updateText() {
        tpsBuffer.add(TpsCalculator.tickRate)

        displayText.add("%.2f".format(tpsBuffer.average()), primaryColor)
        displayText.add("tps", secondaryColor)
    }

}
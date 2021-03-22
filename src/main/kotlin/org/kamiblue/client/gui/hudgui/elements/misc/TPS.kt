package org.kamiblue.client.gui.hudgui.elements.misc

import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.gui.hudgui.elements.misc.MemoryUsage.setting
import org.kamiblue.client.util.CircularArray
import org.kamiblue.client.util.TpsCalculator

internal object TPS : LabelHud(
    name = "TPS",
    category = Category.MISC,
    description = "Server TPS"
) {
    private val mspt = setting("Show MSPT", false)

    // buffered TPS readings to add some fluidity to the TPS HUD element
    private val tpsBuffer = CircularArray.create(20, 20f)

    override fun SafeClientEvent.updateText() {
        tpsBuffer.add(TpsCalculator.tickRate)
        if (mspt.value) {
            displayText.add("%.2f".format(1000 / tpsBuffer.average()), primaryColor)
            displayText.add("MS/Tick", secondaryColor)
        } else {
            displayText.add("%.2f".format(tpsBuffer.average()), primaryColor)
            displayText.add("tps", secondaryColor)
        }
    }

}
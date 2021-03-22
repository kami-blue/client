package org.kamiblue.client.gui.hudgui.elements.misc

import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.util.CircularArray
import org.kamiblue.client.util.TpsCalculator

internal object MSPT : LabelHud(
    name = "MSPT",
    category = Category.MISC,
    description = "Server ms/tick"
) {

    private val tpsBuffer = CircularArray.create(20, 20f)

    override fun SafeClientEvent.updateText() {
        tpsBuffer.add(TpsCalculator.tickRate)

        displayText.add("%.2f".format(1000 / tpsBuffer.average()), primaryColor)
        displayText.add("MS/Tick", secondaryColor)
    }

}
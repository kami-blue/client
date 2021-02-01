package me.zeroeightsix.kami.gui.hudgui.elements.misc

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.util.TpsCalculator
import org.kamiblue.commons.utils.MathUtils

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

        val tps = MathUtils.round(tpsList.average(), 2)

        displayText.add("$tps", primaryColor)
        displayText.add("tps", secondaryColor)
    }

}
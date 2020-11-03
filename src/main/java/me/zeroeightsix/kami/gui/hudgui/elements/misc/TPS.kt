package me.zeroeightsix.kami.gui.hudgui.elements.misc

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.util.TpsCalculator
import me.zeroeightsix.kami.util.math.MathUtils

@HudElement.Info(
        category = HudElement.Category.MISC,
        description = "Display server TPS"
)
object TPS : LabelHud("TPS") {

    private val tpsList = FloatArray(100) { 20.0f }
    private var tpsIndex = 0

    override fun updateText() {
        tpsList[tpsIndex] = TpsCalculator.tickRate
        tpsIndex = (tpsIndex + 1) % 20

        val tps = MathUtils.round(tpsList.average(), 2)

        displayText.add("$tps", primaryColor.value)
        displayText.add("tps", secondaryColor.value)
    }

}
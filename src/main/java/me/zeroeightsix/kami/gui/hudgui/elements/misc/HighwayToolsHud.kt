package me.zeroeightsix.kami.gui.hudgui.elements.misc

import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.module.modules.misc.HighwayTools

object HighwayToolsHud : LabelHud(
    name = "HighwayTools",
    category = Category.MISC,
    description = "Hud for HighwayTools module"
) {
    override fun updateText() {
        val list = HighwayTools.gatherStatistics()
        for (line in list) {
            displayText.addLine(line)
        }
    }
}
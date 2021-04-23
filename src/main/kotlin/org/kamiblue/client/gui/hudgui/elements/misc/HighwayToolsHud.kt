package org.kamiblue.client.gui.hudgui.elements.misc

import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.module.modules.misc.HighwayTools.gatherStatistics

internal object HighwayToolsHud : LabelHud(
    name = "HighwayToolsHud",
    category = Category.MISC,
    description = "Hud for HighwayTools module"
) {
    override fun SafeClientEvent.updateText() {
        gatherStatistics(displayText)
    }
}
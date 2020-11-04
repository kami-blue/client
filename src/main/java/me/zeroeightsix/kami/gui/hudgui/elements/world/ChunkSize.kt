package me.zeroeightsix.kami.gui.hudgui.elements.world

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.math.MathUtils

@HudElement.Info(
        category = HudElement.Category.MISC,
        description = "Display size of the chunk you are in"
)
object ChunkSize : LabelHud("ChunkSize") {

    override fun updateText() {
        val chunkSize = MathUtils.round(InfoCalculator.chunkSize() / 1024.0, 2)
        displayText.add(chunkSize.toString(), primaryColor.value)
        displayText.add("KB (Chunk)", secondaryColor.value)
    }

}
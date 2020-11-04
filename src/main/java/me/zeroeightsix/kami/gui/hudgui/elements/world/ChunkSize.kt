package me.zeroeightsix.kami.gui.hudgui.elements.world

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.util.InfoCalculator

@HudElement.Info(
        category = HudElement.Category.MISC,
        description = "Display size of the chunk you are in"
)
object ChunkSize : LabelHud("ChunkSize") {

    override fun updateText() {
        val chunkSize = InfoCalculator.chunkSize() / 1024L
        displayText.add(chunkSize.toString(), primaryColor.value)
        displayText.add("KB (Chunk)", secondaryColor.value)
    }

}
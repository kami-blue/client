package me.zeroeightsix.kami.gui.hudgui.elements.world

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud

@HudElement.Info(
        category = HudElement.Category.MISC,
        description = "Display the current biome you are in"
)
object Biome : LabelHud("Biome") {

    override fun updateText() {
        val biome = mc.player?.let {
            mc.world?.getBiome(it.position)?.biomeName
        } ?: "Unknown"
        displayText.add(biome, primaryColor.value)
        displayText.add("Biome", secondaryColor.value)
    }

}
package me.zeroeightsix.kami.gui.hudgui.elements.world

import me.zeroeightsix.kami.gui.hudgui.LabelHud

object Biome : LabelHud(
    name = "Biome",
    category = Category.WORLD,
    description = "Display the current biome you are in"
) {

    override fun updateText() {
        val biome = mc.player?.let {
            mc.world?.getBiome(it.position)?.biomeName
        } ?: "Unknown"

        displayText.add(biome, primaryColor)
        displayText.add("Biome", secondaryColor)
    }

}
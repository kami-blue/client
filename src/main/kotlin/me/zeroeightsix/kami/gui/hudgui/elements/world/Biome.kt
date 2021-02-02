package me.zeroeightsix.kami.gui.hudgui.elements.world

import org.kamiblue.client.event.SafeClientEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud

object Biome : LabelHud(
    name = "Biome",
    category = Category.WORLD,
    description = "Display the current biome you are in"
) {

    override fun SafeClientEvent.updateText() {
        val biome = world.getBiome(player.position).biomeName ?: "Unknown"

        displayText.add(biome, primaryColor)
        displayText.add("Biome", secondaryColor)
    }

}
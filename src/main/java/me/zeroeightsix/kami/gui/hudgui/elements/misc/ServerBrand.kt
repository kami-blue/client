package me.zeroeightsix.kami.gui.hudgui.elements.misc

import me.zeroeightsix.kami.gui.hudgui.LabelHud

object ServerBrand : LabelHud(
    name = "ServerBrand",
    category = Category.MISC,
    description = "Brand of the server"
) {

    override fun updateText() {
        val serverBrand = mc.player?.serverBrand ?: "Unknown"
        displayText.add(serverBrand, primaryColor.value)
    }

}
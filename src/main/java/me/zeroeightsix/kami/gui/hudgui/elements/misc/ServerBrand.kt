package me.zeroeightsix.kami.gui.hudgui.elements.misc

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud

@HudElement.Info(
        category = HudElement.Category.MISC,
        description = "Brand of the server"
)
object ServerBrand : LabelHud("ServerBrand") {

    override fun updateText() {
        val serverBrand = mc.player?.serverBrand?: "Unknown"
        displayText.add(serverBrand, primaryColor.value)
    }

}
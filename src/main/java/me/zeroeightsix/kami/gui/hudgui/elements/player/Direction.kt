package me.zeroeightsix.kami.gui.hudgui.elements.player

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.util.math.Direction

@HudElement.Info(
        category = HudElement.Category.PLAYER,
        description = "Direction of player facing to"
)
object Direction : LabelHud("Direction") {

    override fun updateText() {
        val entity = mc.renderViewEntity ?: mc.player ?: return
        val direction = Direction.fromEntity(entity)
        displayText.add(direction.displayName, secondaryColor.value)
        displayText.add("(${direction.displayNameXY})", primaryColor.value)
    }

}
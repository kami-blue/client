package me.zeroeightsix.kami.gui.hudgui.elements.player

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.util.math.Direction

object Direction : LabelHud(
    category = Category.PLAYER
) {

    override fun SafeClientEvent.updateText() {
        val entity = mc.renderViewEntity ?: mc.player ?: return
        val direction = Direction.fromEntity(entity)
        displayText.add(direction.displayName, secondaryColor)
        displayText.add("(${direction.displayNameXY})", primaryColor)
    }

}
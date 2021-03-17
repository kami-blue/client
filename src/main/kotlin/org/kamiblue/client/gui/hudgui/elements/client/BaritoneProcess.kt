package org.kamiblue.client.gui.hudgui.elements.client

import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.module.modules.movement.AutoWalk
import org.kamiblue.client.process.PauseProcess
import org.kamiblue.client.util.BaritoneUtils

internal object BaritoneProcess : LabelHud(
    name = "BaritoneProcess",
    category = Category.CLIENT,
    description = "Shows what Baritone is doing"
) {

    override fun SafeClientEvent.updateText() {
        val process = BaritoneUtils.primary?.pathingControlManager?.mostRecentInControl()?.orElse(null) ?: return

        when {
            process == PauseProcess -> {
                displayText.addLine(process.displayName0())
            }
            AutoWalk.baritoneWalk -> {
                displayText.addLine("AutoWalk (${AutoWalk.direction.displayName})")
            }
            else -> {
                displayText.addLine("Process: ${process.displayName()}")
            }
        }
    }

}
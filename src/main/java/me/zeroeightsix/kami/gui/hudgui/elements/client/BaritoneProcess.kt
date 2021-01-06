package me.zeroeightsix.kami.gui.hudgui.elements.client

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.module.modules.movement.AutoWalk
import me.zeroeightsix.kami.process.TemporaryPauseProcess
import me.zeroeightsix.kami.util.BaritoneUtils

object BaritoneProcess : LabelHud(
    name = "BaritoneProcess",
    category = Category.CLIENT,
    description = "Shows what Baritone is doing"
) {

    override val minWidth = 10f
    override val minHeight = 10f

    override fun SafeClientEvent.updateText() {
        val process = BaritoneUtils.primary?.pathingControlManager?.mostRecentInControl() ?: return
        if (!process.isPresent) return

        if (process.get() !== TemporaryPauseProcess && AutoWalk.isEnabled && AutoWalk.mode.value == AutoWalk.AutoWalkMode.BARITONE) {
            displayText.addLine("Process: AutoWalk (${AutoWalk.direction.displayName})")
        } else {
            displayText.addLine("Process: ${process.get().displayName()}")
        }
    }

}
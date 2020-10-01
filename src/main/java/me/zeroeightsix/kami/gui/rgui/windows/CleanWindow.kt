package me.zeroeightsix.kami.gui.rgui.windows

import me.zeroeightsix.kami.gui.rgui.WindowComponent

/**
 * Window with no rendering
 */
open class CleanWindow(
        override var name: String,
        override var posX: Double,
        override var posY: Double,
        override var width: Double,
        override var height: Double
) : WindowComponent() {
    override val draggableHeight: Double
        get() = height
}
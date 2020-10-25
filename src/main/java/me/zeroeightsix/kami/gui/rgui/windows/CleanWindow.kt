package me.zeroeightsix.kami.gui.rgui.windows

import me.zeroeightsix.kami.gui.rgui.WindowComponent

/**
 * Window with no rendering
 */
open class CleanWindow(
        override var name: String,
        override var posX: Float,
        override var posY: Float,
        override var width: Float,
        override var height: Float
) : WindowComponent() {
    override val draggableHeight: Float
        get() = height
}
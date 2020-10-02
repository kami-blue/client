package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.util.math.Vec2d

open class CheckButton(override var name: String, stateIn: Boolean) : Slider(name, 0.0) {
    init {
        value = if (stateIn) 1.0 else 0.0
    }

    override fun onRelease(mousePos: Vec2d, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        value = if (value == 1.0) 0.0 else 1.0
    }
}
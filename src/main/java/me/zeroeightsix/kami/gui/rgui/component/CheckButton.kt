package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.util.math.Vec2f

open class CheckButton(override var name: String, stateIn: Boolean) : Slider(name, 0.0f) {
    init {
        value = if (stateIn) 1.0f else 0.0f
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        value = if (value == 1.0f) 0.0f else 1.0f
    }
}
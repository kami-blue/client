package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.util.math.Vec2f

class CheckButton(
    name: String,
    stateIn: Boolean,
    description: String = "",
    visibility: (() -> Boolean)? = null
) : BooleanSlider(name, 0.0, description, visibility) {
    init {
        value = if (stateIn) 1.0 else 0.0
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (prevState != MouseState.DRAG) {
            value = if (value == 1.0) 0.0 else 1.0
        }
    }
}
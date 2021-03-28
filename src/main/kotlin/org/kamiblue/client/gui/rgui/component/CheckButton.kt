package org.kamiblue.client.gui.rgui.component

import org.kamiblue.client.util.math.Vec2f

class CheckButton(
    name: String,
    stateIn: Boolean,
    description: String = "",
    visibility: (() -> Boolean)? = null
) : BooleanSlider(name, 0.0, description, visibility) {
    init {
        value = if (stateIn) 1.0 else 0.0
    }

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        value = if (value == 1.0) 0.0 else 1.0
    }
}
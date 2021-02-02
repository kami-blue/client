package org.kamiblue.client.gui.rgui.component

import org.kamiblue.client.util.math.Vec2f

class Button(
    name: String,
    private val action: (Button) -> Unit,
    description: String = "",
    visibility: (() -> Boolean)? = null
) : BooleanSlider(name, 0.0, description, visibility) {
    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        value = 1.0
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (prevState != MouseState.DRAG) {
            value = 0.0
            action(this)
        }
    }

    override fun onLeave(mousePos: Vec2f) {
        super.onLeave(mousePos)
        value = 0.0
    }
}
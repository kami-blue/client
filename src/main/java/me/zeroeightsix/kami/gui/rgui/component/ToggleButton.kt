package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.util.math.Vec2f

class ToggleButton(
    name: String,
    description: String,
    val check: () -> Boolean,
    val toggle: () -> Unit)
    : BooleanSlider(name, 0.0, description, { true }) {

    init {
        if (check()) value = 1.0
    }

    override fun onTick() {
        super.onTick()
        value = if (check()) 1.0 else 0.0
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (prevState != MouseState.DRAG) {
            toggle()
        }
    }
}
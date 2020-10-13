package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.util.math.Vec2f

open class Button(override var name: String, val action: () -> Unit) : Slider(name, 0.0f) {

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        value = 1.0f
        action()
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        value = 0.0f
    }
}
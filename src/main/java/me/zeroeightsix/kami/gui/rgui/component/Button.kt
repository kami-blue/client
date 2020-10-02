package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.util.math.Vec2d

open class Button(override var name: String, val action: () -> Unit) : Slider(name, 0.0) {

    override fun onClick(mousePos: Vec2d, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        value = 1.0
        action()
    }

    override fun onRelease(mousePos: Vec2d, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        value = 0.0
    }
}
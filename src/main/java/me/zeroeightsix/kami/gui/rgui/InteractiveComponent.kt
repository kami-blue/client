package me.zeroeightsix.kami.gui.rgui

import me.zeroeightsix.kami.util.math.Vec2d

abstract class InteractiveComponent : Component() {
    // Basic interactive methods
    open fun onHover(mousePos: Vec2d) {}

    open fun onClick(mousePos: Vec2d, buttonId: Int) {}

    open fun onRelease(mousePos: Vec2d, buttonId: Int) {}

    open fun onDrag(mousePos: Vec2d, clickPos: Vec2d, buttonId: Int) {}
}
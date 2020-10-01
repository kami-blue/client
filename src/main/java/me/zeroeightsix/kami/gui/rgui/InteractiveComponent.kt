package me.zeroeightsix.kami.gui.rgui

import me.zeroeightsix.kami.util.math.Vec2d

abstract class InteractiveComponent : Component() {
    // Interactive info
    protected var lastStateUpdateTime = System.currentTimeMillis(); private set
    protected var prevState = MouseState.NONE; private set
    var state = MouseState.NONE
        private set(value) {
            prevState = field
            lastStateUpdateTime = System.currentTimeMillis()
            field = value
        }

    override fun onGuiInit() {
        super.onGuiInit()
        state = MouseState.NONE
        prevState = MouseState.NONE
        lastStateUpdateTime = System.currentTimeMillis()
    }

    // Interactive methods
    open fun onHover(mousePos: Vec2d) {
        state = MouseState.HOVER
    }

    open fun onClick(mousePos: Vec2d, buttonId: Int) {
        state = MouseState.CLICK
    }

    open fun onRelease(mousePos: Vec2d, buttonId: Int) {
        state = MouseState.NONE
    }

    open fun onDrag(mousePos: Vec2d, clickPos: Vec2d, buttonId: Int) {
        state = MouseState.DRAG
    }

    @Suppress("UNUSED")
    enum class MouseState {
        NONE, HOVER, CLICK, DRAG
    }
}
package me.zeroeightsix.kami.gui.rgui

import me.zeroeightsix.kami.util.math.Vec2d

abstract class InteractiveComponent : Component() {
    // Interactive info
    protected var lastStateUpdateTime = System.currentTimeMillis(); private set
    protected var prevState = MouseState.NONE; private set
    var mouseState = MouseState.NONE
        private set(value) {
            prevState = field
            lastStateUpdateTime = System.currentTimeMillis()
            field = value
        }

    override fun onGuiInit() {
        super.onGuiInit()
        mouseState = MouseState.NONE
        prevState = MouseState.NONE
        lastStateUpdateTime = System.currentTimeMillis()
    }

    // Interactive methods
    open fun onMouseInput(mousePos: Vec2d) {

    }

    open fun onHover(mousePos: Vec2d) {
        mouseState = MouseState.HOVER
    }

    open fun onLeave(mousePos: Vec2d) {
        mouseState = MouseState.NONE
    }

    open fun onClick(mousePos: Vec2d, buttonId: Int) {
        mouseState = MouseState.CLICK
    }

    open fun onRelease(mousePos: Vec2d, buttonId: Int) {
        mouseState = if (isInComponent(mousePos)) MouseState.HOVER
        else MouseState.NONE
    }

    open fun onDrag(mousePos: Vec2d, clickPos: Vec2d, buttonId: Int) {
        mouseState = MouseState.DRAG
    }

    fun isInComponent(mousePos: Vec2d) = mousePos.x in 0.0..width && mousePos.y in 0.0..height

    @Suppress("UNUSED")
    enum class MouseState {
        NONE, HOVER, CLICK, DRAG
    }
}
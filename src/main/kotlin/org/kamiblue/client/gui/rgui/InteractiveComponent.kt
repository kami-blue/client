package org.kamiblue.client.gui.rgui

import org.kamiblue.client.setting.GuiConfig
import org.kamiblue.client.setting.configs.AbstractConfig
import org.kamiblue.client.util.math.Vec2f
import org.kamiblue.commons.interfaces.Nameable

open class InteractiveComponent(
    name: String,
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
    settingGroup: SettingGroup,
    config: AbstractConfig<out Nameable> = GuiConfig
) : Component(name, posX, posY, width, height, settingGroup, config) {

    // Interactive info
    protected var lastMousePos = Vec2f.ZERO
    var mouseState = MouseState.NONE
        private set(value) {
            prevState = field
            lastStateUpdateTime = System.currentTimeMillis()
            field = value
        }
    protected var prevState = MouseState.NONE; private set
    protected var lastStateUpdateTime = System.currentTimeMillis(); private set

    override fun onGuiInit() {
        super.onGuiInit()
        mouseState = MouseState.NONE
        prevState = MouseState.NONE
        lastStateUpdateTime = System.currentTimeMillis()
    }

    // Interactive methods
    open fun onMouseInput(mousePos: Vec2f) {
        lastMousePos = mousePos
    }

    open fun onHover(mousePos: Vec2f) {
        mouseState = MouseState.HOVER
    }

    open fun onLeave(mousePos: Vec2f) {
        mouseState = MouseState.NONE
    }

    open fun onClick(mousePos: Vec2f, buttonId: Int) {
        mouseState = MouseState.CLICK
    }

    open fun onRelease(mousePos: Vec2f, buttonId: Int) {
        mouseState = if (isInComponent(mousePos)) MouseState.HOVER
        else MouseState.NONE
    }

    open fun onDrag(mousePos: Vec2f, clickPos: Vec2f, buttonId: Int) {
        mouseState = MouseState.DRAG
    }

    open fun onKeyInput(keyCode: Int, keyState: Boolean) {

    }

    fun isInComponent(mousePos: Vec2f) = mousePos.x in 0.0f..width && mousePos.y in 0.0f..height

    @Suppress("UNUSED")
    enum class MouseState {
        NONE, HOVER, CLICK, DRAG
    }
}
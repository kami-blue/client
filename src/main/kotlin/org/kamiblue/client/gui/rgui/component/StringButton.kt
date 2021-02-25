package org.kamiblue.client.gui.rgui.component

import org.kamiblue.client.module.modules.client.ClickGUI
import org.kamiblue.client.setting.settings.impl.primitive.StringSetting
import org.kamiblue.client.util.math.Vec2f
import org.lwjgl.input.Keyboard
import kotlin.math.max

class StringButton(val setting: StringSetting) : BooleanSlider(setting.name, 1.0, setting.description, setting.visibility) {

    override val isBold
        get() = setting.isModified && ClickGUI.showModifiedInBold

    override fun onDisplayed() {
        super.onDisplayed()
        value = 1.0
    }

    override fun onStopListening(success: Boolean) {
        if (success) {
            setting.setValue(name)
        }

        super.onStopListening(success)
        name = originalName
        value = 1.0
    }

    override fun onMouseInput(mousePos: Vec2f) {
        super.onMouseInput(mousePos)
        if (!listening) {
            name = if (mouseState == MouseState.NONE) originalName
            else setting.value
        }
    }

    override fun onTick() {
        super.onTick()
        if (!listening) {
            name = if (mouseState != MouseState.NONE) setting.value
            else originalName
        }
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (buttonId == 1) {
            if (!listening) {
                listening = true
                name = setting.value
                value = 0.0
            } else {
                onStopListening(false)
            }
        } else if (buttonId == 0 && listening) {
            onStopListening(true)
        }
    }

    override fun onKeyInput(keyCode: Int, keyState: Boolean) {
        super.onKeyInput(keyCode, keyState)
        val typedChar = Keyboard.getEventCharacter()
        if (keyState) {
            when (keyCode) {
                Keyboard.KEY_RETURN -> {
                    onStopListening(true)
                }
                Keyboard.KEY_BACK, Keyboard.KEY_DELETE -> {
                    name = name.substring(0, max(name.length - 1, 0))
                }
                else -> if (typedChar >= ' ') {
                    name += typedChar
                }
            }
        }
    }
}
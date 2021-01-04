package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.setting.settings.impl.primitive.StringSetting
import me.zeroeightsix.kami.util.math.Vec2f
import org.lwjgl.input.Keyboard
import kotlin.math.max

class StringButton(val setting: StringSetting) : BooleanSlider(setting.name, 1.0, setting.description) {

    override fun onClosed() {
        super.onClosed()
        name = originalName
    }

    override fun onDisplayed() {
        super.onDisplayed()
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
            listening = !listening

            value = if (listening) {
                name = ""
                0.0
            } else {
                1.0
            }
        }
    }

    override fun onKeyInput(keyCode: Int, keyState: Boolean) {
        super.onKeyInput(keyCode, keyState)
        val typedChar = Keyboard.getEventCharacter()
        if (keyState) {
            when (keyCode) {
                Keyboard.KEY_RETURN -> {
                    setting.setValue(name)
                    listening = false
                    name = originalName
                    value = 1.0
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
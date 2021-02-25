package org.kamiblue.client.gui.rgui.component

import org.kamiblue.client.module.modules.client.ClickGUI
import org.kamiblue.client.module.modules.client.GuiColors
import org.kamiblue.client.setting.settings.impl.other.BindSetting
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.graphics.font.FontRenderAdapter
import org.kamiblue.client.util.math.Vec2f
import org.lwjgl.input.Keyboard

class BindButton(
    private val setting: BindSetting
) : Slider(setting.name, 0.0, setting.description, setting.visibility) {

    override val isBold
        get() = setting.isModified && ClickGUI.showModifiedInBold

    override val renderProgress: Double = 0.0

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        listening = !listening
    }

    override fun onKeyInput(keyCode: Int, keyState: Boolean) {
        super.onKeyInput(keyCode, keyState)
        if (listening && !keyState) {
            setting.value.apply {
                if (keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE) clear()
                else setBind(keyCode)
                listening = false
            }
        }
    }

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        super.onRender(vertexHelper, absolutePos)

        val valueText = if (listening) "Listening"
        else setting.value.toString()

        protectedWidth = FontRenderAdapter.getStringWidth(valueText, 0.75f).toDouble()
        val posX = (renderWidth - protectedWidth - 2.0f).toFloat()
        val posY = renderHeight - 2.0f - FontRenderAdapter.getFontHeight(0.75f)
        FontRenderAdapter.drawString(valueText, posX, posY, color = GuiColors.text, scale = 0.75f)
    }
}
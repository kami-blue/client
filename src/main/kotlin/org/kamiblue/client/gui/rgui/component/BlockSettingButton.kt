package org.kamiblue.client.gui.rgui.component

import org.kamiblue.client.gui.rgui.windows.BlockPicker
import org.kamiblue.client.module.modules.client.GuiColors
import org.kamiblue.client.setting.settings.impl.other.BlockSetting
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.graphics.font.FontRenderAdapter
import org.kamiblue.client.util.math.Vec2f

class BlockSettingButton(val setting: BlockSetting) : Slider(setting.name, 0.0, setting.description, setting.visibility) {

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        val valueText = setting.value.localizedName
        protectedWidth = FontRenderAdapter.getStringWidth(valueText, 0.75f).toDouble()

        super.onRender(vertexHelper, absolutePos)
        val posX = (renderWidth - protectedWidth - 2.0f).toFloat()
        val posY = renderHeight - 2.0f - FontRenderAdapter.getFontHeight(0.75f)
        FontRenderAdapter.drawString(valueText, posX, posY, color = GuiColors.text, scale = 0.75f)
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        displayBlockPicker()
    }

    private fun displayBlockPicker() {
        BlockPicker.visible = true
        BlockPicker.setting = setting
        BlockPicker.onDisplayed()
    }
}
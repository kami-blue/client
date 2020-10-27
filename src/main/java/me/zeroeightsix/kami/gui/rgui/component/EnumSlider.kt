package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.module.modules.client.GuiColors
import me.zeroeightsix.kami.setting.impl.primitive.EnumSetting
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.math.Vec2f
import kotlin.math.floor

class EnumSlider(val setting: EnumSetting<*>) : AbstractSlider(setting.name, 0.0) {
    private val enumValues = setting.enumValues

    override fun onTick() {
        super.onTick()
        val settingValue = setting.value.ordinal
        if (roundInput(value) != settingValue) {
            value = settingValue / enumValues.size.toDouble()
        }
        visible.value = setting.isVisible
    }

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        setting.setValue(enumValues[(setting.value.ordinal + 1) % enumValues.size].name)
    }

    override fun onDrag(mousePos: Vec2f, clickPos: Vec2f, buttonId: Int) {
        super.onDrag(mousePos, clickPos, buttonId)
        updateValue(mousePos)
    }

    private fun updateValue(mousePos: Vec2f) {
        value = (mousePos.x / width.value).toDouble()
        setting.setValue(enumValues[roundInput(value)].name)
    }

    private fun roundInput(valueIn: Double) = floor(valueIn * enumValues.size).toInt().coerceIn(0, enumValues.size - 1)

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        val valueText = setting.value.name.split('_').joinToString(" ") { it.toLowerCase().capitalize() }
        protectedWidth = FontRenderAdapter.getStringWidth(valueText, 0.75f).toDouble()

        super.onRender(vertexHelper, absolutePos)
        val posX = (renderWidth - protectedWidth - 2.0f).toFloat()
        val posY = renderHeight - 2.0f - FontRenderAdapter.getFontHeight(0.75f)
        FontRenderAdapter.drawString(valueText, posX, posY, color = GuiColors.text, scale = 0.75f)
    }
}
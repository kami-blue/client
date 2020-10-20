package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.module.modules.client.GuiColors
import me.zeroeightsix.kami.setting.impl.EnumSetting
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.KamiFontRenderer
import me.zeroeightsix.kami.util.math.Vec2f

class EnumSlider(val setting: EnumSetting<*>) : AbstractSlider(setting.name, 0.0) {
    private val enumValues = setting.clazz.enumConstants

    override fun onTick() {
        super.onTick()
        val settingValue = setting.value.ordinal
        if (floorToStep(value) != settingValue) {
            value = settingValue / enumValues.size.toDouble()
        }
        visible = setting.isVisible
    }

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        updateValue(mousePos)
    }

    override fun onDrag(mousePos: Vec2f, clickPos: Vec2f, buttonId: Int) {
        super.onDrag(mousePos, clickPos, buttonId)
        updateValue(mousePos)
    }

    private fun updateValue(mousePos: Vec2f) {
        value = (mousePos.x / width).toDouble()
        setting.setValueFromString(enumValues[floorToStep(value)].name, false)
    }

    private fun floorToStep(valueIn: Double) = (valueIn * (enumValues.size - 1)).toInt()

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        val valueText = setting.value.name
        protectedWidth = KamiFontRenderer.getStringWidth(valueText).toDouble()

        super.onRender(vertexHelper, absolutePos)
        KamiFontRenderer.drawString(valueText, (renderWidth - protectedWidth - 2.0f).toFloat(), 1.0f, colorIn = GuiColors.text)
    }
}
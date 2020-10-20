package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.module.modules.client.GuiColors
import me.zeroeightsix.kami.setting.impl.numerical.DoubleSetting
import me.zeroeightsix.kami.setting.impl.numerical.FloatSetting
import me.zeroeightsix.kami.setting.impl.numerical.IntegerSetting
import me.zeroeightsix.kami.setting.impl.numerical.NumberSetting
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.KamiFontRenderer
import me.zeroeightsix.kami.util.math.MathUtils
import me.zeroeightsix.kami.util.math.Vec2f
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.roundToInt

class SettingSlider(val setting: NumberSetting<*>) : AbstractSlider(setting.name, 0.0) {
    private val range = setting.max.toDouble() - setting.min.toDouble()
    private val settingValueDouble get() = setting.value.toDouble()
    private val settingStep = if (setting.step != null && setting.step.toDouble() > 0.0) setting.step else getDefaultStep()
    private val stepDouble = settingStep.toDouble()
    private val places = when (setting) {
        is IntegerSetting -> 1
        is FloatSetting -> MathUtils.decimalPlaces(settingStep.toFloat())
        else -> MathUtils.decimalPlaces(settingStep.toDouble())
    }

    private fun getDefaultStep() = when (setting) {
        is IntegerSetting -> range / 20
        is FloatSetting -> range / 20.0f
        else -> range / 20.0
    }

    override fun onTick() {
        super.onTick()
        val min = setting.min.toDouble()
        val flooredSettingValue = floor((settingValueDouble - min) / stepDouble) * stepDouble
        if (value * range + min !in (flooredSettingValue - stepDouble)..(flooredSettingValue + stepDouble)) {
            value = (setting.value.toDouble() - min) / range
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
        value = mousePos.x.toDouble() / width.toDouble()
        val roundedValue = MathUtils.round(round((value * range + setting.min.toDouble()) / stepDouble) * stepDouble, places)
        when (setting) {
            is IntegerSetting -> {
                setting.value = roundedValue.roundToInt().coerceIn(setting.min, setting.max)
            }
            is FloatSetting -> {
                setting.value = roundedValue.toFloat().coerceIn(setting.min, setting.max)
            }
            is DoubleSetting -> {
                setting.value = roundedValue.coerceIn(setting.min, setting.max)
            }
        }
    }

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        val valueText = setting.valueAsString
        protectedWidth = KamiFontRenderer.getStringWidth(valueText, 0.75f).toDouble()

        super.onRender(vertexHelper, absolutePos)
        val posX = (renderWidth - protectedWidth - 2.0f).toFloat()
        val posY = renderHeight - 2.0f - KamiFontRenderer.getFontHeight(0.75f)
        KamiFontRenderer.drawString(valueText, posX, posY, colorIn = GuiColors.text, scale = 0.75f)
    }
}
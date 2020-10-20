package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.setting.impl.numerical.DoubleSetting
import me.zeroeightsix.kami.setting.impl.numerical.FloatSetting
import me.zeroeightsix.kami.setting.impl.numerical.IntegerSetting
import me.zeroeightsix.kami.setting.impl.numerical.NumberSetting
import me.zeroeightsix.kami.util.math.Vec2f
import kotlin.math.floor

class SettingSlider<T : Number>(val setting: NumberSetting<T>) : AbstractSlider(setting.name, 0.0) {
    private val range = setting.max.toDouble() - setting.min.toDouble()
    private val step = if (setting.step != null && setting.step.toDouble() > 0.0) setting.step.toDouble() else getDefaultStep()

    private fun getDefaultStep(): Double {
        val step = (range / 20.0)
        return if (setting is IntegerSetting) floor(step) else step
    }

    override fun onTick() {
        val settingValue = floorToStep(setting.value.toDouble())
        if (value * range !in settingValue..settingValue + step) {
            value = setting.value.toDouble() / range
        }
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
        setting.setValue(floorToStep(value))
    }

    private fun floorToStep(valueIn: Double) = floor(valueIn * range / step) * step

    private fun NumberSetting<*>.setValue(valueIn: Double) {
        when (this) {
            is IntegerSetting -> {
                this.value = valueIn.toInt()
            }
            is FloatSetting -> {
                this.value = valueIn.toFloat()
            }
            is DoubleSetting -> {
                this.value = valueIn
            }
        }
    }
}
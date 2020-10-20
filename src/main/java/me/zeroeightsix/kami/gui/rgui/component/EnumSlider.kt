package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.setting.impl.EnumSetting
import me.zeroeightsix.kami.util.math.Vec2f
import kotlin.math.floor

class EnumSlider<T : Enum<*>>(val setting: EnumSetting<T>) : AbstractSlider(setting.name, 0.0) {
    private val enumValues = setting.clazz.enumConstants

    override fun onTick() {
        val settingValue = setting.value.ordinal
        if (floorToStep(value) != settingValue) {
            value = settingValue / enumValues.size.toDouble()
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
        setting.setValueFromString(enumValues[floorToStep(value)].name, false)
    }

    private fun floorToStep(valueIn: Double) = (valueIn * enumValues.size).toInt()
}
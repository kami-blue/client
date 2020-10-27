package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.util.graphics.AnimationUtils

abstract class AbstractBooleanSlider(
        name: String,
        valueIn: Double,
        descriptionIn: String
) : AbstractSlider(name, valueIn, descriptionIn) {
    override val renderProgress: Double
        get() = AnimationUtils.exponent(AnimationUtils.toDeltaTimeDouble(prevValue.lastUpdateTime), 200.0, prevValue.value, value)
}
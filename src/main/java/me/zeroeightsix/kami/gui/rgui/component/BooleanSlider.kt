package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.util.graphics.AnimationUtils

open class BooleanSlider(
    name: String,
    valueIn: Double,
    descriptionIn: String
) : Slider(name, valueIn, descriptionIn) {
    override val renderProgress: Double
        get() = AnimationUtils.exponent(AnimationUtils.toDeltaTimeDouble(prevValue.lastUpdateTime), 200.0, prevValue.value, value)
}
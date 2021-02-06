package org.kamiblue.client.setting.settings.impl.number

import com.google.gson.JsonElement

class DoubleSetting(
    name: String,
    value: Double,
    range: ClosedFloatingPointRange<Double>,
    step: Double,
    visibility: () -> Boolean = { true },
    consumer: (prev: Double, input: Double) -> Double = { _, input -> input },
    description: String = "",
    fineStep: Double = step
) : NumberSetting<Double>(name, value, range, step, visibility, consumer, description, fineStep) {

    init {
        consumers.add(0) { _, it ->
            it.coerceIn(range)
        }
    }

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonPrimitive?.asDouble?.let { value = it }
    }

    override fun setValue(valueIn: Double) {
        value = valueIn
    }

}
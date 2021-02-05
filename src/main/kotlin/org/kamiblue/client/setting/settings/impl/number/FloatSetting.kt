package org.kamiblue.client.setting.settings.impl.number

import com.google.gson.JsonElement

class FloatSetting(
    name: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    visibility: () -> Boolean = { true },
    consumer: (prev: Float, input: Float) -> Float = { _, input -> input },
    description: String = "",
    fineStep: Float = step
) : NumberSetting<Float>(name, value, range, step, visibility, consumer, description, fineStep) {

    init {
        consumers.add(0) { _, it ->
            it.coerceIn(range)
        }
    }

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonPrimitive?.asFloat?.let { value = it }
    }

    override fun setValue(valueIn: Double) {
        value = valueIn.toFloat()
    }

}
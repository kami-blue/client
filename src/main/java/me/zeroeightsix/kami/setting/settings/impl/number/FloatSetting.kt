package me.zeroeightsix.kami.setting.settings.impl.number

import com.google.gson.JsonElement

class FloatSetting(
    name: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    visibility: () -> Boolean = { true },
    consumer: (prev: Float, input: Float) -> Float = { _, input -> input },
    description: String = ""
) : NumberSetting<Float>(name, value, range, step, visibility, consumer, description) {

    init {
        consumers.add(0) { _, it ->
            it.coerceIn(range)
        }
    }

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonPrimitive?.asFloat?.let { value = it }
    }

}
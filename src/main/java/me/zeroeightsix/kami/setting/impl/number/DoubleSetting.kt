package me.zeroeightsix.kami.setting.impl.number

import com.google.gson.JsonElement

class DoubleSetting(
        name: String,
        value: Double,
        range: ClosedFloatingPointRange<Double>,
        step: Double,
        visibility: () -> Boolean = { true },
        consumer: (prev: Double, input: Double) -> Double = { _, input -> input },
        description: String = ""
) : NumberSetting<Double>(name, value, range.start, range.endInclusive, step, visibility, consumer, description) {

    override var value: Double = value
        set(value) {
            field = consumer(field, value.coerceIn(min, max))
            for (listener in listeners) listener()
        }

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonPrimitive?.asDouble?.let { value = it }
    }

}
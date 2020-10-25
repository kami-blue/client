package me.zeroeightsix.kami.setting.impl.number

import com.google.gson.JsonElement

class IntegerSetting(
        name: String,
        value: Int,
        range: IntRange,
        step: Int,
        visibility: () -> Boolean = { true },
        consumer: (prev: Int, input: Int) -> Int = { _, input -> input },
        description: String = ""
) : NumberSetting<Int>(name, value, range.first, range.last, step, visibility, consumer, description) {

    override var value: Int = value
        set(value) {
            field = consumer(field, value.coerceIn(min, max))
            for (listener in listeners) listener()
        }

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonPrimitive?.asInt?.let { value = it }
    }

}
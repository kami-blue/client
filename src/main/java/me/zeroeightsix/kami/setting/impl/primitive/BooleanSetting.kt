package me.zeroeightsix.kami.setting.impl.primitive

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.zeroeightsix.kami.setting.Setting

open class BooleanSetting(
        name: String,
        value: Boolean,
        visibility: () -> Boolean = { true },
        consumer: (prev: Boolean, input: Boolean) -> Boolean = { _, input -> input },
        description: String = ""
) : Setting<Boolean>(name, value, visibility, consumer, description) {

    override fun write(): JsonElement = JsonPrimitive(value)

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonPrimitive?.asBoolean?.let { value = it }
    }

}
package org.kamiblue.client.setting.settings.impl.primitive

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.kamiblue.client.setting.settings.MutableSetting

class StringSetting(
    name: String,
    value: String,
    visibility: () -> Boolean = { true },
    consumer: (prev: String, input: String) -> String = { _, input -> input },
    description: String = ""
) : MutableSetting<String>(name, value, visibility, consumer, description) {

    override fun setValue(valueIn: String) {
        value = valueIn
    }

    override fun write() = JsonPrimitive(value)

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonPrimitive?.asString?.let { value = it }
    }

}
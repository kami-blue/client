package me.zeroeightsix.kami.setting.settings.impl.primitive

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.zeroeightsix.kami.setting.settings.MutableSetting
import me.zeroeightsix.kami.util.translation.TranslationKey
import me.zeroeightsix.kami.util.translation.TranslationKeyBlank

class StringSetting(
        name: TranslationKey,
        value: String,
        visibility: () -> Boolean = { true },
        consumer: (prev: String, input: String) -> String = { _, input -> input },
        description: TranslationKey = TranslationKeyBlank()
) : MutableSetting<String>(name, value, visibility, consumer, description) {

    override fun setValue(valueIn: String) {
        value = valueIn
    }

    override fun write() = JsonPrimitive(value)

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonPrimitive?.asString?.let { value = it }
    }

}
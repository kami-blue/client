package me.zeroeightsix.kami.setting.settings.impl.primitive

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.zeroeightsix.kami.setting.settings.MutableSetting
import me.zeroeightsix.kami.util.translation.TranslationKey
import me.zeroeightsix.kami.util.translation.TranslationKeyBlank

open class BooleanSetting(
        name: TranslationKey,
        value: Boolean,
        visibility: () -> Boolean = { true },
        consumer: (prev: Boolean, input: Boolean) -> Boolean = { _, input -> input },
        description: TranslationKey = TranslationKeyBlank()
) : MutableSetting<Boolean>(name, value, visibility, consumer, description) {

    override fun write(): JsonElement = JsonPrimitive(value)

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonPrimitive?.asBoolean?.let { value = it }
    }

}
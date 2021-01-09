package me.zeroeightsix.kami.setting.settings.impl.number

import com.google.gson.JsonElement
import me.zeroeightsix.kami.util.translation.TranslationKey
import me.zeroeightsix.kami.util.translation.TranslationKeyBlank

class IntegerSetting(
        name: TranslationKey,
        value: Int,
        range: IntRange,
        step: Int,
        visibility: () -> Boolean = { true },
        consumer: (prev: Int, input: Int) -> Int = { _, input -> input },
        description: TranslationKey = TranslationKeyBlank()
) : NumberSetting<Int>(name, value, range, step, visibility, consumer, description) {

    init {
        consumers.add(0) { _, it ->
            it.coerceIn(range)
        }
    }

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonPrimitive?.asInt?.let { value = it }
    }

}
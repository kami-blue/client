package me.zeroeightsix.kami.setting.settings.impl.number

import com.google.gson.JsonElement
import me.zeroeightsix.kami.util.translation.TranslationKey
import me.zeroeightsix.kami.util.translation.TranslationKeyBlank

class FloatSetting(
        name: TranslationKey,
        value: Float,
        range: ClosedFloatingPointRange<Float>,
        step: Float,
        visibility: () -> Boolean = { true },
        consumer: (prev: Float, input: Float) -> Float = { _, input -> input },
        description: TranslationKey = TranslationKeyBlank()
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
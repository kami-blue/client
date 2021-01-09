package me.zeroeightsix.kami.setting.settings.impl.number

import com.google.gson.JsonElement
import me.zeroeightsix.kami.util.translation.TranslationKey
import me.zeroeightsix.kami.util.translation.TranslationKeyBlank

class DoubleSetting(
        name: TranslationKey,
        value: Double,
        range: ClosedFloatingPointRange<Double>,
        step: Double,
        visibility: () -> Boolean = { true },
        consumer: (prev: Double, input: Double) -> Double = { _, input -> input },
        description: TranslationKey = TranslationKeyBlank()
) : NumberSetting<Double>(name, value, range, step, visibility, consumer, description) {

    init {
        consumers.add(0) { _, it ->
            it.coerceIn(range)
        }
    }

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonPrimitive?.asDouble?.let { value = it }
    }

}
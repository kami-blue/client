package me.zeroeightsix.kami.setting.settings.impl.number

import com.google.gson.JsonPrimitive
import me.zeroeightsix.kami.setting.settings.MutableSetting
import me.zeroeightsix.kami.util.translation.TranslationKey
import me.zeroeightsix.kami.util.translation.TranslationKeyBlank

abstract class NumberSetting<T>(
    name: TranslationKey,
    value: T,
    val range: ClosedRange<T>,
    val step: T,
    visibility: () -> Boolean,
    consumer: (prev: T, input: T) -> T,
    description: TranslationKey = TranslationKeyBlank()
) : MutableSetting<T>(name, value, visibility, consumer, description)
    where T : Number, T : Comparable<T> {

    override fun write() = JsonPrimitive(value)

}

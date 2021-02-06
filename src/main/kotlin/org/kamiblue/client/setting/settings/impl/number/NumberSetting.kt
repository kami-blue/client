package org.kamiblue.client.setting.settings.impl.number

import com.google.gson.JsonPrimitive
import org.kamiblue.client.setting.settings.MutableSetting

abstract class NumberSetting<T>(
    name: String,
    value: T,
    val range: ClosedRange<T>,
    val step: T,
    visibility: () -> Boolean,
    consumer: (prev: T, input: T) -> T,
    description: String = "",
    val fineStep: T
) : MutableSetting<T>(name, value, visibility, consumer, description)
    where T : Number, T : Comparable<T> {

    override fun write() = JsonPrimitive(value)

    final override fun setValue(valueIn: String) {
        valueIn.toDoubleOrNull()?.let {
            setValue(it)
        }
    }

    abstract fun setValue(valueIn: Double)

}

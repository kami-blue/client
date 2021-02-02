package org.kamiblue.client.setting.settings.impl.primitive

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.kamiblue.client.setting.settings.MutableSetting
import org.kamiblue.commons.extension.next
import java.util.*

class EnumSetting<T : Enum<T>>(
    name: String,
    value: T,
    visibility: () -> Boolean = { true },
    consumer: (prev: T, input: T) -> T = { _, input -> input },
    description: String = ""
) : MutableSetting<T>(name, value, visibility, consumer, description) {

    val enumClass: Class<T> = value.declaringClass
    val enumValues: Array<out T> = enumClass.enumConstants

    fun nextValue() {
        value = value.next()
    }

    override fun setValue(valueIn: String) {
        super.setValue(valueIn.toUpperCase(Locale.ROOT).replace(' ', '_'))
    }

    override fun write(): JsonElement = JsonPrimitive(value.name)

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonPrimitive?.asString?.let { element ->
            enumValues.firstOrNull { it.name.equals(element, true) }?.let {
                value = it
            }
        }
    }

}
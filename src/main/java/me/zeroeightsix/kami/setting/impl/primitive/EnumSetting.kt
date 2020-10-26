package me.zeroeightsix.kami.setting.impl.primitive

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.zeroeightsix.kami.setting.Setting

class EnumSetting<T : Enum<T>>(
        name: String,
        value: T,
        visibility: () -> Boolean = { true },
        consumer: (prev: T, input: T) -> T = { _, input -> input },
        description: String = ""
) : Setting<T>(name, value, visibility, consumer, description) {

    val enumClass: Class<T> = value.declaringClass
    val enumValues: Array<out T> = enumClass.enumConstants

    override fun write(): JsonElement = JsonPrimitive(value.name)

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonPrimitive?.asString?.let { element ->
            enumValues.firstOrNull { it.name.equals(element, true) }?.let {
                value = it
            }
        }
    }

}
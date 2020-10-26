package me.zeroeightsix.kami.setting.impl.primitive

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import me.zeroeightsix.kami.setting.IMultiSetting
import me.zeroeightsix.kami.setting.Setting

class BooleanMultiSetting(
        name: String,
        value: Boolean,
        override val subSettings: Array<out Setting<*>>,
        visibility: () -> Boolean = { true },
        consumer: (prev: Boolean, input: Boolean) -> Boolean = { _, input -> input },
        description: String = ""
) : BooleanSetting(name, value, visibility, consumer, description), IMultiSetting {

    override var expanded = false

    override fun write(): JsonElement = JsonObject().apply {
        add("value", JsonPrimitive(value))
        for (setting in subSettings) {
            add(setting.name, setting.write())
        }
    }

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonObject?.let {
            value = it.getAsJsonPrimitive("value").asBoolean
            for (setting in subSettings) {
                setting.read(it.get(setting.name))
            }
        }
    }

}
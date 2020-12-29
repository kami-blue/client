package me.zeroeightsix.kami.setting.settings.impl.number

import com.google.gson.JsonPrimitive
import me.zeroeightsix.kami.setting.settings.Setting

abstract class NumberSetting<T : Number>(
        name: String,
        value: T,
        val min: T,
        val max: T,
        val step: T,
        visibility: () -> Boolean,
        consumer: (prev: T, input: T) -> T,
        description: String = ""
) : Setting<T>(name, value, visibility, consumer, description) {

    override fun write() = JsonPrimitive(value)

}

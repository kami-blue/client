package me.zeroeightsix.kami.setting.impl.other

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.util.Bind
import org.lwjgl.input.Keyboard

class BindSetting(
        name: String,
        visibility: () -> Boolean = { true },
        description: String = ""
) : Setting<Bind>(name, Bind.none(), visibility, { _, input -> input }, description) {

    override fun write() = JsonPrimitive(value.toString())

    override fun read(jsonElement: JsonElement?) {
        var string = jsonElement?.asString ?: "None"
        if (string.equals("None", ignoreCase = true)) value = Bind.none()

        val ctrl = string.startsWith("Ctrl+")
        if (ctrl) {
            string = string.substring(5)
        }

        val alt = string.startsWith("Alt+")
        if (alt) {
            string = string.substring(4)
        }

        val shift = string.startsWith("Shift+")
        if (shift) {
            string = string.substring(6)
        }

        val key = Keyboard.getKeyIndex(string.toUpperCase())

        value = if (key == 0) Bind.none() else Bind(ctrl, alt, shift, key)
    }

}
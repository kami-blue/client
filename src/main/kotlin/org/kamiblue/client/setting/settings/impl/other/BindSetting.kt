package org.kamiblue.client.setting.settings.impl.other

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.kamiblue.client.setting.settings.ImmutableSetting
import org.kamiblue.client.util.Bind
import org.lwjgl.input.Keyboard

class BindSetting(
    name: String,
    value: Bind,
    visibility: () -> Boolean = { true },
    description: String = ""
) : ImmutableSetting<Bind>(name, value, visibility, { _, input -> input }, description) {

    override val defaultValue: Bind = Bind(value.ctrl, value.alt, value.shift, value.key)

    override fun resetValue() {
        value.setBind(defaultValue.ctrl, defaultValue.alt, defaultValue.shift, defaultValue.key)
    }

    override fun setValue(valueIn: String) {
        var string = valueIn

        if (string.equals("None", ignoreCase = true)) {
            value.setBind(ctrlIn = false, altIn = false, shiftIn = false, keyIn = 0)
            return
        }

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

        value.setBind(ctrl, alt, shift, key)
    }

    override fun write() = JsonPrimitive(value.toString())

    override fun read(jsonElement: JsonElement?) {
        setValue(jsonElement?.asString ?: "None")
    }

}
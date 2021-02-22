package org.kamiblue.client.util

import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.formatValue
import org.lwjgl.input.Keyboard
import java.util.*
import kotlin.collections.HashMap

object KeyboardUtils {
    val allKeys = IntArray(Keyboard.KEYBOARD_SIZE) { it }

    private val displayNames = Array(Keyboard.KEYBOARD_SIZE) {
        Keyboard.getKeyName(it)?.toLowerCase()?.capitalize()
    }

    private val keyMap: Map<String, Int> = HashMap<String, Int>().apply {
        // LWJGL names
        for (key in 0 until Keyboard.KEYBOARD_SIZE) {
            val name = Keyboard.getKeyName(key) ?: continue
            this[name.toLowerCase(Locale.ROOT)] = key
        }

        // Display names
        for ((index, name) in displayNames.withIndex()) {
            if (name == null) continue
            this[name.toLowerCase(Locale.ROOT)] = index
        }

        // Modifier names
        this["ctrl"] = Keyboard.KEY_LCONTROL
        this["alt"] = Keyboard.KEY_LMENU
        this["shift"] = Keyboard.KEY_LSHIFT
        this["meta"] = Keyboard.KEY_LMETA
    }

    fun sendUnknownKeyError(bind: String) {
        MessageSendHelper.sendErrorMessage("Unknown key [${formatValue(bind)}]! " +
            "Right shift is ${formatValue("rshift")}, " +
            "left Control is ${formatValue("lcontrol")}, " +
            "and ` is ${formatValue("grave")}. " +
            "You cannot bind the ${formatValue("meta")} key."
        )
    }

    fun getKey(keyName: String): Int {
        return keyMap[keyName.toLowerCase(Locale.ROOT)] ?: 0
    }

    fun getKeyName(keycode: Int): String? {
        return Keyboard.getKeyName(keycode)
    }

    fun getDisplayName(keycode: Int): String? {
        return displayNames.getOrNull(keycode)
    }
}
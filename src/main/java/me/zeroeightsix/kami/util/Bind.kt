package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.command.commands.BindCommand
import org.lwjgl.input.Keyboard

/**
 * Created by 086 on 9/10/2018.
 * Updated by Xiaro on 08/18/20
 */
class Bind(
        var isCtrl: Boolean,
        var isAlt: Boolean,
        var isShift: Boolean,
        var key: Int
) {
    val isEmpty: Boolean get() = !isCtrl && !isShift && !isAlt && key < 0

    override fun toString(): String {
        return if (isEmpty) "None" else (if (isCtrl) "Ctrl+" else "") + (if (isAlt) "Alt+" else "") + (if (isShift) "Shift+" else "") + if (key < 0) "None" else capitalise(Keyboard.getKeyName(key))
    }

    fun isDown(eventKey: Int): Boolean {
        return !isEmpty && (!BindCommand.modifiersEnabled.value || isShift == isShiftDown() && isCtrl == isCtrlDown() && isAlt == isAltDown()) && eventKey == key
    }

    fun capitalise(str: String): String {
        return if (str.isEmpty()) "" else Character.toUpperCase(str[0]).toString() + if (str.length != 1) str.substring(1).toLowerCase() else ""
    }

    companion object {
        @JvmStatic
        fun isShiftDown(): Boolean {
            return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
        }

        @JvmStatic
        fun isCtrlDown(): Boolean {
            return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)
        }

        @JvmStatic
        fun isAltDown(): Boolean {
            return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)
        }

        @JvmStatic
        fun none(): Bind {
            return Bind(false, false, false, -1)
        }
    }
}
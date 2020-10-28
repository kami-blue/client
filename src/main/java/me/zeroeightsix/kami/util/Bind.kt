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

    fun clear() {
        isCtrl = false
        isShift = false
        isAlt = false
        key = -1
    }

    fun isDown(eventKey: Int): Boolean {
        return !isEmpty && (!BindCommand.modifiersEnabled.value || isShift == isShiftDown() && isCtrl == isCtrlDown() && isAlt == isAltDown()) && eventKey == key
    }

    fun setBind(eventKey: Int) {
        isCtrl = isCtrlDown()
        isShift = isShiftDown()
        isAlt = isAltDown()
        key = eventKey
    }

    private fun isShiftDown(): Boolean {
        val eventKey = Keyboard.getEventKey()
        return eventKey != Keyboard.KEY_LSHIFT && eventKey != Keyboard.KEY_RSHIFT
                && (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
    }

    private fun isCtrlDown(): Boolean {
        val eventKey = Keyboard.getEventKey()
        return eventKey != Keyboard.KEY_LCONTROL && eventKey != Keyboard.KEY_RCONTROL
                && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
    }

    private fun isAltDown(): Boolean {
        val eventKey = Keyboard.getEventKey()
        return eventKey != Keyboard.KEY_LMENU && eventKey != Keyboard.KEY_RMENU
                && (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU))
    }

    override fun toString(): String {
        return if (isEmpty) "None"
        else {
            StringBuffer().apply {
                if (isCtrl) append("Ctrl+")
                if (isAlt) append("Alt+")
                if (isShift) append("Shift+")
                append(getKeyName())
            }.toString()
        }
    }

    private fun getKeyName(): String {
        return if (key in 0..255) {
            Keyboard.getKeyName(key).toLowerCase().capitalize()
        } else {
            "None"
        }
    }

    companion object {
        @JvmStatic
        fun none(): Bind {
            return Bind(false, false, false, -1)
        }
    }
}
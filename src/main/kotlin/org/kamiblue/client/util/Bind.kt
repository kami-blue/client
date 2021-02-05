package org.kamiblue.client.util

import org.kamiblue.client.module.modules.client.CommandConfig
import org.lwjgl.input.Keyboard

class Bind(
    ctrlIn: Boolean,
    altIn: Boolean,
    shiftIn: Boolean,
    keyIn: Int
) {

    constructor() : this(0)

    constructor(keyIn: Int) : this(false, false, false, keyIn)

    var ctrl = ctrlIn; private set
    var alt = altIn; private set
    var shift = shiftIn; private set
    var key = keyIn; private set

    val isEmpty: Boolean get() = !ctrl && !shift && !alt && key < 0

    fun clear() {
        ctrl = false
        shift = false
        alt = false
        key = -1
    }

    fun isDown(keyIn: Int): Boolean {
        return !isEmpty
            && (!CommandConfig.modifierEnabled.value || shift == isShiftDown() && ctrl == isCtrlDown() && alt == isAltDown())
            && key == keyIn
    }

    fun setBind(ctrlIn: Boolean, altIn: Boolean, shiftIn: Boolean, keyIn: Int) {
        ctrl = ctrlIn
        alt = altIn
        shift = shiftIn
        key = keyIn
    }

    fun setBind(keyIn: Int) {
        ctrl = isCtrlDown()
        alt = isAltDown()
        shift = isShiftDown()
        key = keyIn
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
                if (ctrl) append("Ctrl+")
                if (alt) append("Alt+")
                if (shift) append("Shift+")
                append(
                    if (key in 0..255) Keyboard.getKeyName(key).toLowerCase().capitalize()
                    else "None"
                )
            }.toString()
        }
    }
}
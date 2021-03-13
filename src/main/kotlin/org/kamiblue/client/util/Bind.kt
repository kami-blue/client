package org.kamiblue.client.util

import org.lwjgl.input.Keyboard
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Bind(
    modifierKeysIn: TreeSet<Int>,
    keyIn: Int
) {

    constructor() : this(0)

    constructor(key: Int) : this(TreeSet(keyComparator), key)

    constructor(vararg modifierKeys: Int, key: Int) : this(TreeSet(keyComparator).apply { modifierKeys.forEach { add(it) } }, key)

    val modifierKeys = modifierKeysIn
    var key = keyIn; private set
    private var cachedName = getName()

    val isEmpty get() = key !in 1..255

    fun isDown(eventKey: Int): Boolean {
        return !isEmpty
            && key == eventKey
            && synchronized(this) { modifierKeys.all { isModifierKeyDown(eventKey, it) } }
    }

    private fun isModifierKeyDown(eventKey: Int, modifierKey: Int) =
        eventKey != modifierKey
            && when (modifierKey) {
            Keyboard.KEY_LCONTROL, Keyboard.KEY_RCONTROL -> {
                Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)
            }
            Keyboard.KEY_LMENU, Keyboard.KEY_RMENU -> {
                Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)
            }
            Keyboard.KEY_LSHIFT, Keyboard.KEY_RSHIFT -> {
                Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
            }
            Keyboard.KEY_LMETA, Keyboard.KEY_RMETA -> {
                Keyboard.isKeyDown(Keyboard.KEY_LMETA) || Keyboard.isKeyDown(Keyboard.KEY_RMETA)
            }
            in 0..255 -> {
                Keyboard.isKeyDown(modifierKey)
            }
            else -> {
                false
            }
        }

    fun setBind(keyIn: Int) {
        val cache = ArrayList<Int>()

        for (key in Keyboard.KEYBOARD_SIZE - 1 downTo 0) {
            if (key == keyIn) continue
            if (!Keyboard.isKeyDown(key)) continue
            cache.add(key)
        }

        setBind(cache, keyIn)
    }

    fun setBind(modifierKeysIn: Collection<Int>, keyIn: Int) {
        synchronized(this) {
            modifierKeys.clear()
            modifierKeys.addAll(modifierKeysIn)
            key = keyIn
            cachedName = getName()
        }
    }

    fun clear() {
        synchronized(this) {
            modifierKeys.clear()
            key = 0
            cachedName = getName()
        }
    }

    override fun toString(): String {
        return cachedName
    }

    private fun getName(): String {
        return if (isEmpty) {
            "None"
        } else {
            StringBuilder().run {
                for (key in modifierKeys) {
                    val name = modifierName[key] ?: KeyboardUtils.getDisplayName(key) ?: continue
                    append(name)
                    append('+')
                }
                append(KeyboardUtils.getDisplayName(key))
                toString()
            }
        }
    }

    companion object {
        private val modifierName: Map<Int, String> = hashMapOf(
            Keyboard.KEY_LCONTROL to "Ctrl",
            Keyboard.KEY_RCONTROL to "Ctrl",
            Keyboard.KEY_LMENU to "Alt",
            Keyboard.KEY_RMENU to "Alt",
            Keyboard.KEY_LSHIFT to "Shift",
            Keyboard.KEY_RSHIFT to "Shift",
            Keyboard.KEY_LMETA to "Meta",
            Keyboard.KEY_RMETA to "Meta"
        )

        private val priorityMap: Map<Int, Int> = HashMap<Int, Int>().apply {
            val priorityKey = arrayOf(
                Keyboard.KEY_LCONTROL, Keyboard.KEY_RCONTROL,
                Keyboard.KEY_LMENU, Keyboard.KEY_RMENU,
                Keyboard.KEY_LSHIFT, Keyboard.KEY_RSHIFT,
                Keyboard.KEY_LMETA, Keyboard.KEY_RMETA
            )

            for ((index, key) in priorityKey.withIndex()) {
                this[key] = index / 2
            }

            val sortedKeys = KeyboardUtils.allKeys.sortedBy { Keyboard.getKeyName(it) }

            for ((index, key) in sortedKeys.withIndex()) {
                this.putIfAbsent(key, index + priorityKey.size / 2)
            }
        }

        val keyComparator = compareBy<Int> {
            priorityMap[it] ?: -1
        }
    }
}
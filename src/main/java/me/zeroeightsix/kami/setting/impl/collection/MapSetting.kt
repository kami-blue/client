package me.zeroeightsix.kami.setting.impl.collection

import me.zeroeightsix.kami.setting.Setting

open class MapSetting<K : Any, V : Any, T : MutableMap<K, V>>(
        name: String,
        value: T,
        visibility: () -> Boolean = { true },
        description: String = ""
) : Setting<T>(name, value, visibility, { _, input -> input }, description) {

    init {
        defaultValue.toMap(value)
    }

    override fun toString() = value.entries.joinToString { "${it.key} to ${it.value}" }

}
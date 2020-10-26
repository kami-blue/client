package me.zeroeightsix.kami.setting.impl.collection

import com.google.gson.JsonElement
import me.zeroeightsix.kami.setting.Setting

open class CollectionSetting<E : Any, T : MutableCollection<E>>(
        name: String,
        value: T,
        visibility: () -> Boolean = { true },
        description: String = ""
) : Setting<T>(name, value, visibility, { _, input -> input }, description) {

    init {
        defaultValue.toCollection(value)
    }

    override fun write(): JsonElement = gson.toJsonTree(value)

    override fun toString() = value.joinToString { it.toString() }

}
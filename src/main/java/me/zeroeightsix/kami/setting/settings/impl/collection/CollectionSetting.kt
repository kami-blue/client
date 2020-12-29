package me.zeroeightsix.kami.setting.settings.impl.collection

import com.google.gson.JsonElement
import me.zeroeightsix.kami.setting.settings.Setting

open class CollectionSetting<E : Any, T : MutableCollection<E>>(
        name: String,
        value: T,
        visibility: () -> Boolean = { true },
        description: String = ""
) : Setting<T>(name, value, visibility, { _, input -> input }, description), MutableCollection<E> by value {

    init {
        defaultValue.toCollection(value)
    }

    override fun write(): JsonElement = gson.toJsonTree(value)

    override fun toString() = value.joinToString { it.toString() }

}
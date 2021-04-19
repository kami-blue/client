package org.kamiblue.client.setting.settings.impl.collection

import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import org.kamiblue.client.setting.settings.ImmutableSetting

class CollectionSetting<E : Any, T : MutableCollection<E>>(
    name: String,
    override val value: T,
    visibility: () -> Boolean = { true },
    description: String = "",
) : ImmutableSetting<T>(name, value, visibility, { _, input -> input }, description), MutableCollection<E> by value {

    override val defaultValue: T = valueClass.newInstance()
    private val lockObject = Any()
    private val type = TypeToken.getArray(value.first().javaClass).type
    val editListeners = ArrayList<() -> Unit>()

    init {
        value.toCollection(defaultValue)
    }

    // Should be used instead of directly accessing value itself
    // TODO: Hook into [MutableSetting] and allow this to work with `this.value.add(someVal)`
    fun editValue(block: (value: CollectionSetting<E, T>) -> Unit) {
        block.invoke(this)
        editListeners.forEach { it.invoke() }
    }

    override fun resetValue() {
        synchronized(lockObject) {
            value.clear()
            value.addAll(defaultValue)
        }
    }

    override fun write(): JsonElement = gson.toJsonTree(value)

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonArray?.let {
            val cacheArray = gson.fromJson<Array<E>>(it, type)
            synchronized(lockObject) {
                value.clear()
                value.addAll(cacheArray)
            }
        }
    }

    override fun toString() = value.joinToString { it.toString() }

}
package org.kamiblue.client.setting.settings

import com.google.gson.JsonElement
import kotlin.reflect.KProperty

/**
 * Basic MutableSetting class
 *
 * @param T Type of this setting
 * @param name Name of this setting
 * @param visibility Called by [isVisible]
 * @param consumer Called on setting [value] to process the value input
 * @param description Description of this setting
 */
open class MutableSetting<T : Any>(
    override val name: String,
    valueIn: T,
    override val visibility: () -> Boolean,
    consumer: (prev: T, input: T) -> T,
    override val description: String
) : AbstractSetting<T>() {

    override val defaultValue = valueIn
    override var value = valueIn
        set(value) {
            if (value != field) {
                val prev = field
                var new = value

                for (index in consumers.size - 1 downTo 0) {
                    new = consumers[index](prev, new)
                }
                field = new

                // TODO: CollectionSetting.editValue needs to somehow work here
                valueListeners.forEach { it.invoke(prev, field) }
                listeners.forEach { it.invoke() }
            }
        }

    override val valueClass: Class<T> = valueIn.javaClass
    val consumers = arrayListOf(consumer)

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }

    final override fun resetValue() {
        value = defaultValue
    }

    override fun write(): JsonElement = gson.toJsonTree(value)

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.let {
            value = gson.fromJson(it, valueClass)
        }
    }

}
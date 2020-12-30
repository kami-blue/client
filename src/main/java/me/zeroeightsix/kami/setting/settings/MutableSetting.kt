package me.zeroeightsix.kami.setting.settings

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
    val consumer: (prev: T, input: T) -> T,
    override val description: String
) : AbstractSetting<T>() {

    override val defaultValue = valueIn
    override var value = valueIn
        set(value) {
            if (value != field) {
                val prev = field
                field = consumer(field, value)
                for (listener in valueListeners) listener(prev, field)
                for (listener in listeners) listener()
            }
        }
    override val valueClass: Class<T> = valueIn.javaClass

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
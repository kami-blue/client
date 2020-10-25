package me.zeroeightsix.kami.setting

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser

/**
 * Basic Setting class
 *
 * @param T Type of this setting
 * @param name Name of this setting
 * @param visibility Called by [isVisible]
 * @param consumer Called on setting [value] to process the value input
 * @param description Description of this setting
 */
open class Setting<T : Any>(
        val name: String,
        value: T,
        val visibility: () -> Boolean,
        val consumer: (prev: T, input: T) -> T,
        val description: String
) {

    open var value = value
        set(value) {
            if (value != field) {
                field = consumer(field, value)
                for (listener in listeners) listener()
            }
        }
    open val defaultValue = value
    val valueClass = value::class.java
    val isVisible get() = visibility()
    val listeners = ArrayList<() -> Unit>()
    val valueListeners = ArrayList<(T) -> Unit>()

    open fun setValue(valueIn: String) {
        read(parser.parse(valueIn))
    }

    fun resetValue() {
        value = defaultValue
    }

    override fun toString() = value.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Setting<*>) return false

        if (name != other.name) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode() * 31 + value.hashCode()
    }

    open fun write(): JsonElement = gson.toJsonTree(value)

    open fun read(jsonElement: JsonElement?) {
        jsonElement?.let {
            value = gson.fromJson(it, valueClass)
        }
    }

    protected companion object {
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        val parser = JsonParser()
    }

}
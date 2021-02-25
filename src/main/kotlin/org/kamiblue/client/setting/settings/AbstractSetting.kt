package org.kamiblue.client.setting.settings

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.kamiblue.commons.interfaces.Nameable
import kotlin.reflect.KProperty

abstract class AbstractSetting<T : Any> : Nameable {

    abstract val value: T
    abstract val defaultValue: T
    abstract val valueClass: Class<T>
    abstract val visibility: () -> Boolean
    abstract val description: String

    val listeners = ArrayList<() -> Unit>()
    val valueListeners = ArrayList<(prev: T, input: T) -> Unit>()

    val isVisible get() = visibility()

    val isModified get() = this.value != this.defaultValue

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = value

    open fun setValue(valueIn: String) {
        read(parser.parse(valueIn))
    }

    abstract fun resetValue()

    abstract fun write(): JsonElement
    abstract fun read(jsonElement: JsonElement?)

    override fun toString() = value.toString()

    override fun equals(other: Any?) = this === other
        || (other is AbstractSetting<*>
        && this.valueClass == other.valueClass
        && this.name == other.name
        && this.value == other.value)

    override fun hashCode() = valueClass.hashCode() * 31 +
        name.hashCode() * 31 +
        value.hashCode()

    protected companion object {
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        val parser = JsonParser()
    }
}
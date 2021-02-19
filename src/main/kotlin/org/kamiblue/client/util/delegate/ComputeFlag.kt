package org.kamiblue.client.util.delegate

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ComputeFlag(private val block: () -> Boolean) : ReadOnlyProperty<Any?, Boolean> {
    private var value = false

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return value || block.invoke().also { value = it }
    }
}
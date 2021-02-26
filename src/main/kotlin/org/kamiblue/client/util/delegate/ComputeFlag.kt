package org.kamiblue.client.util.delegate

import java.util.function.BooleanSupplier
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ComputeFlag(private val block: BooleanSupplier) : ReadOnlyProperty<Any?, Boolean> {
    private var value = false

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return value || block.asBoolean.also { value = it }
    }
}
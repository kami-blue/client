package org.kamiblue.client.util.delegate

import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class CachedValue<T>(
    protected val updateTime: Long,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    protected val block: () -> T
) : ReadWriteProperty<Any?, T> {
    protected var value: T? = null
    protected val timer = TickTimer(timeUnit)

    open fun get(): T {
        val cached = value

        return if (cached == null || timer.tick(updateTime)) {
            block().also { value = it }
        } else {
            cached
        }
    }

    open fun update() {
        block().also { value = it }
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>) = get()

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}
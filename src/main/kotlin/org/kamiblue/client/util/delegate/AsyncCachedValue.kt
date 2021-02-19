package org.kamiblue.client.util.delegate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.threads.defaultScope
import kotlin.coroutines.CoroutineContext
import kotlin.properties.ReadWriteProperty

class AsyncCachedValue<T>(
    updateTime: Long,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    private val context: CoroutineContext = Dispatchers.Default,
    block: () -> T
) : CachedValue<T>(updateTime, timeUnit, block), ReadWriteProperty<Any?, T> {

    override fun get(): T {
        val cached = value

        return when {
            cached == null -> {
                block().also { value = it }
            }
            timer.tick(updateTime) -> {
                defaultScope.launch(context) {
                    value = block()
                }
                cached
            }
            else -> {
                cached
            }
        }
    }

    override fun update() {
        defaultScope.launch(context) {
            value = block()
        }
    }
}
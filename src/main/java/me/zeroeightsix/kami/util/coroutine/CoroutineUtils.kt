package me.zeroeightsix.kami.util.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.newSingleThreadContext
import me.zeroeightsix.kami.command.ClientEvent
import me.zeroeightsix.kami.command.SafeClientEvent
import me.zeroeightsix.kami.command.toSafe
import me.zeroeightsix.kami.util.Wrapper
import java.util.concurrent.Callable

@Suppress("EXPERIMENTAL_API_USAGE")
val mainScope = CoroutineScope(newSingleThreadContext("KAMI Blue Main"))

val ioScope = CoroutineScope(Dispatchers.IO)

/**
 * Return true if the job is active, or false is not active or null
 */
val Job?.isActiveOrFalse get() = this?.isActive ?: false

fun onMainThread(block: ClientEvent.() -> Unit) {
    Wrapper.minecraft.addScheduledTask(Callable {
        try {
            ClientEvent().block()
            null
        } catch (e: Exception) {
            e
        }
    }).get()?.let {
        throw it
    }
}

fun onMainThreadSafe(block: SafeClientEvent.() -> Unit) {
    Wrapper.minecraft.addScheduledTask(Callable {
        try {
            ClientEvent().toSafe()?.block()
            null
        } catch (e: Exception) {
            e
        }
    }).get()?.let {
        throw it
    }
}
package me.zeroeightsix.kami.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.newSingleThreadContext
import me.zeroeightsix.kami.command.ClientEvent
import me.zeroeightsix.kami.command.SafeClientEvent
import me.zeroeightsix.kami.command.toSafe
import java.util.concurrent.Callable

@Suppress("EXPERIMENTAL_API_USAGE")
val mainScope = CoroutineScope(newSingleThreadContext("KAMI Blue Main"))

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
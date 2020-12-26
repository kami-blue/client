package me.zeroeightsix.kami.util.threads

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.newSingleThreadContext
import me.zeroeightsix.kami.event.ClientEvent
import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.util.Wrapper
import java.util.concurrent.Callable

/**
 * Single thread scope to use in KAMI Blue
 */
@Suppress("EXPERIMENTAL_API_USAGE")
val mainScope = CoroutineScope(newSingleThreadContext("KAMI Blue Main"))

/**
 * Common scope with [Dispatchers.Default]
 */
val defaultScope = CoroutineScope(Dispatchers.Default)

/**
 * IO scope to use for IO blocking operations
 */
val ioScope = CoroutineScope(Dispatchers.IO)

/**
 * Return true if the job is active, or false is not active or null
 */
val Job?.isActiveOrFalse get() = this?.isActive ?: false

/**
 * Run [block] on Minecraft main thread (Client thread) and wait for its result while blocking the current thread.
 *
 * @throws Exception if an exception thrown during [block] execution
 *
 * @see [onMainThreadSafe]
 */
fun <R> onMainThread(block: ClientEvent.() -> R) =
    Wrapper.minecraft.addScheduledTask(Callable {
        runCatching { ClientEvent().block() }
    }).get().getOrThrow()

/**
 * Run [block] on Minecraft main thread (Client thread) and wait for its result while blocking the current thread.
 * The [block] will the called with a [SafeClientEvent] to ensure null safety
 *
 * @throws Exception if an exception thrown during [block] execution
 *
 * @see [onMainThread]
 */
fun <R> onMainThreadSafe(block: SafeClientEvent.() -> R) =
    Wrapper.minecraft.addScheduledTask(Callable {
        runCatching { ClientEvent().toSafe()?.block() }
    }).get().getOrThrow()
package org.kamiblue.client.util.threads

import org.kamiblue.client.KamiMod
import org.kamiblue.client.event.ClientEvent
import org.kamiblue.client.event.ClientExecuteEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.SafeExecuteEvent
import org.kamiblue.client.util.Wrapper
import org.kamiblue.event.ListenerManager
import org.kamiblue.event.listener.AsyncListener
import org.kamiblue.event.listener.DEFAULT_PRIORITY
import org.kamiblue.event.listener.Listener
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

inline fun <reified T : Any> Any.safeAsyncListener(noinline function: suspend SafeClientEvent.(T) -> Unit) {
    ListenerManager.register(this, AsyncListener(this, T::class.java) { runSafeSuspend { function(it) } })
}

inline fun <reified T : Any> Any.safeListener(priority: Int = DEFAULT_PRIORITY, noinline function: SafeClientEvent.(T) -> Unit) {
    ListenerManager.register(this, Listener(this, T::class.java, priority) { runSafe { function(it) } })
}

fun ClientEvent.toSafe() =
    if (world != null && player != null && playerController != null && connection != null) SafeClientEvent(world, player, playerController, connection)
    else null

fun ClientExecuteEvent.toSafe() =
    if (world != null && player != null && playerController != null && connection != null) SafeExecuteEvent(world, player, playerController, connection, this)
    else null

fun runSafe(block: SafeClientEvent.() -> Unit) {
    ClientEvent().toSafe()?.let { block(it) }
}

fun <R> runSafeR(block: SafeClientEvent.() -> R): R? {
    return ClientEvent().toSafe()?.let { block(it) }
}

@JvmName("runSafeSuspendUnit")
suspend fun runSafeSuspend(block: suspend SafeClientEvent.() -> Unit) {
    ClientEvent().toSafe()?.let { block(it) }
}

suspend fun <R> runSafeSuspend(block: suspend SafeClientEvent.() -> R): R? {
    return ClientEvent().toSafe()?.let { block(it) }
}


/**
 * Runs [block] on Minecraft main thread (Client thread) without waiting for the result to be returned.
 * The [block] will the called with a [SafeClientEvent] to ensure null safety.
 * All the thrown exceptions will be handled by minecraft scheduled task system.
 *
 * @see [onMainThread]
 */
fun onMainThreadSafe(block: SafeClientEvent.() -> Unit) {
    onMainThread { ClientEvent().toSafe()?.block() }
}


/**
 * Runs [block] on Minecraft main thread (Client thread) without waiting for the result to be returned.
 * The [block] will the called with a [SafeClientEvent] to ensure null safety.
 * All the thrown exceptions will be handled by minecraft scheduled task system.
 *
 * @see [onMainThread]
 */
fun onMainThread(block: () -> Unit) {
    Wrapper.minecraft.addScheduledTask(block)
}

/**
 * Runs [block] on Minecraft main thread (Client thread) and wait for its result while blocking the current thread.
 *
 * @throws Exception if an exception thrown during [block] execution
 *
 * @see [onMainThreadSafeW]
 */
fun <R> onMainThreadW(timeout: Long = 100L, block: () -> R) =
    runCatching {
        Wrapper.minecraft.addScheduledTask(Callable {
            runCatching { block() }
        }).get(timeout, TimeUnit.MILLISECONDS)
    }.getOrElse {
        when (it) {
            is TimeoutException -> KamiMod.LOG.info("Task timeout while running on main thread!", it)
            is CancellationException -> KamiMod.LOG.warn("Task cancelled while running on main thread!", it)
            is InterruptedException -> KamiMod.LOG.warn("Task interrupted while running on main thread!", it)
            else -> throw it
        }
        null
    }?.getOrThrow()

package me.zeroeightsix.kami.util.threads

import me.zeroeightsix.kami.event.ClientEvent
import me.zeroeightsix.kami.event.ClientExecuteEvent
import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.SafeExecuteEvent
import me.zeroeightsix.kami.util.Wrapper
import org.kamiblue.event.ListenerManager
import org.kamiblue.event.listener.AsyncListener
import org.kamiblue.event.listener.DEFAULT_PRIORITY
import org.kamiblue.event.listener.Listener
import java.util.concurrent.Callable

inline fun <reified T : Any> Any.safeAsyncListener(noinline function: suspend SafeClientEvent.(T) -> Unit) {
    ListenerManager.register(this, AsyncListener(T::class.java) { runSafeSuspend{ function(it) } })
}

inline fun <reified T : Any> Any.safeListener(priority: Int = DEFAULT_PRIORITY, noinline function: SafeClientEvent.(T) -> Unit) {
    ListenerManager.register(this, Listener(T::class.java, priority) { runSafe { function(it) } })
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

fun <R> runSafe(block: SafeClientEvent.() -> R): R? {
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
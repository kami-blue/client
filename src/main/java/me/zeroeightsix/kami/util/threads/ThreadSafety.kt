package me.zeroeightsix.kami.util.threads

import me.zeroeightsix.kami.event.ClientEvent
import me.zeroeightsix.kami.event.ClientExecuteEvent
import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.SafeExecuteEvent

fun ClientEvent.toSafe() =
    if (world != null && player != null && playerController != null && connection != null) SafeClientEvent(world, player, playerController, connection)
    else null

fun ClientExecuteEvent.toSafe() =
    if (world != null && player != null && playerController != null && connection != null) SafeExecuteEvent(world, player, playerController, connection, this)
    else null

fun <R> runSafe(block: SafeClientEvent.() -> R) : R? {
    return ClientEvent().toSafe()?.let { block(it) }
}

suspend fun <R> runSafeSuspend(block: suspend SafeClientEvent.() -> R) : R? {
    return ClientEvent().toSafe()?.let { block(it) }
}
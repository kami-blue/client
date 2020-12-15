package me.zeroeightsix.kami.util

import kotlinx.coroutines.CoroutineScope
import me.zeroeightsix.kami.command.ClientEvent
import me.zeroeightsix.kami.command.SafeClientEvent
import me.zeroeightsix.kami.command.toSafe

fun CoroutineScope.onMainThread(block: ClientEvent.() -> Unit) {
    val event = ClientEvent()
    Wrapper.minecraft.addScheduledTask {
        event.block()
    }
}

fun CoroutineScope.onMainThreadSafe(block: SafeClientEvent.() -> Unit) {
    Wrapper.minecraft.addScheduledTask {
        ClientEvent().toSafe()?.block()
    }
}
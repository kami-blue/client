package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.command.ClientEvent
import me.zeroeightsix.kami.command.SafeClientEvent
import me.zeroeightsix.kami.command.toSafe
import java.util.concurrent.Callable

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
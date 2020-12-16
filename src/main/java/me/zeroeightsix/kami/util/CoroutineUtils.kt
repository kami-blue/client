package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.command.ClientEvent
import me.zeroeightsix.kami.command.SafeClientEvent
import me.zeroeightsix.kami.command.toSafe

fun onMainThread(block: ClientEvent.() -> Unit) {
    Wrapper.minecraft.addScheduledTask {
        ClientEvent().block()
    }
}

fun onMainThreadSafe(block: SafeClientEvent.() -> Unit) {
    Wrapper.minecraft.addScheduledTask {
        ClientEvent().toSafe()?.block()
    }
}
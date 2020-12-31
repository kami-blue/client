package me.zeroeightsix.kami.event

open class KamiEvent() {
    var cancelled = false

    fun cancel() {
        cancelled = true
    }
}
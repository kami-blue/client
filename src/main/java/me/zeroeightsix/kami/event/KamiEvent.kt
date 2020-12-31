package me.zeroeightsix.kami.event

open class KamiEvent {
    var era = Era.PRE

    var cancelled = false

    fun cancel() {
        cancelled = true
    }

    enum class Era {
        PRE, PERI, POST
    }
}
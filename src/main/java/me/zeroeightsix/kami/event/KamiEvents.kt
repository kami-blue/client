package me.zeroeightsix.kami.event

interface Event

interface IMultiPhase<T : Event> {
    val phase: Phase

    fun nextPhase(): T
}

interface ICancellable {
    var cancelled: Boolean

    fun cancel() {
        cancelled = true
    }
}

open class Cancellable : ICancellable {
    override var cancelled = false
}

enum class Phase {
    PRE, PERI, POST;
}
package me.zeroeightsix.kami.event

open class Event {
    protected open fun pre() {

    }

    protected open fun post() {

    }
}

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
package me.zeroeightsix.kami.event

interface MultiPhaseEvent<T> {
    val phase: Phase

    fun nextPhase(): T
}
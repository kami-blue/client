package org.kamiblue.client.manager.managers

import org.kamiblue.client.event.events.RunGameLoopEvent
import org.kamiblue.client.manager.Manager
import org.kamiblue.client.module.AbstractModule
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.event.listener.listener

object PauseProcessManager : Manager {

    private val pauseModules = HashMap<AbstractModule, Long>()
    private val timer = TickTimer(TimeUnit.SECONDS)
    private var lastPausingModule: AbstractModule? = null

    val isActive get() =
        pauseModules.isNotEmpty()

    init {
        listener<RunGameLoopEvent.Tick> {
            if (timer.tick(1L)) {
                pauseModules.entries.removeIf { it.key.isDisabled || System.currentTimeMillis() - it.value > 3000L }
            }
        }
    }

    fun AbstractModule.pauseBaritone() {
        lastPausingModule = this

        pauseModules[this] = System.currentTimeMillis()
    }

    fun AbstractModule.unpauseBaritone() {
        pauseModules.remove(this)
    }

    fun isPausing(module: AbstractModule) =
        pauseModules.containsKey(module)
}
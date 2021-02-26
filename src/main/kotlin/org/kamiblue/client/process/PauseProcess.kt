package org.kamiblue.client.process

import baritone.api.process.IBaritoneProcess
import baritone.api.process.PathingCommand
import baritone.api.process.PathingCommandType
import org.kamiblue.client.module.AbstractModule
import org.kamiblue.client.util.BaritoneUtils
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit

object PauseProcess : IBaritoneProcess {

    private val pauseModules = HashMap<AbstractModule, Long>()
    private val timer = TickTimer(TimeUnit.SECONDS)
    private var lastPausingModule: AbstractModule? = null

    override fun isTemporary(): Boolean {
        return true
    }

    override fun priority(): Double {
        return 5.0
    }

    override fun isActive(): Boolean {
        return pauseModules.isNotEmpty()
    }

    override fun displayName0(): String {
        return "Paused by module: ${lastPausingModule?.name}"
    }

    override fun onLostControl() {
        // nothing :p
    }

    override fun onTick(calcFailed: Boolean, isSafeToCancel: Boolean): PathingCommand {
        if (timer.tick(1L)) {
            pauseModules.entries.removeIf { it.key.isDisabled || System.currentTimeMillis() - it.value > 3000L }
        }

        return PathingCommand(null, PathingCommandType.REQUEST_PAUSE)
    }

    fun AbstractModule.pauseBaritone() {
        if (pauseModules.isEmpty()) {
            BaritoneUtils.primary?.pathingControlManager?.registerProcess(this@PauseProcess)
        }

        lastPausingModule = this

        pauseModules[this] = System.currentTimeMillis()
    }

    fun AbstractModule.unpauseBaritone() {
        pauseModules.remove(this)
    }

    fun isPausing(module: AbstractModule) =
        pauseModules.containsKey(module)
}
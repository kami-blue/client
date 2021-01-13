package me.zeroeightsix.kami.process

import baritone.api.process.IBaritoneProcess
import baritone.api.process.PathingCommand
import baritone.api.process.PathingCommandType
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit

object PauseProcess : IBaritoneProcess {

    private val pauseModules = HashMap<Module, Long>()
    private val timer = TickTimer(TimeUnit.SECONDS)
    private var lastPausingModule: Module? = null

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

    fun Module.pauseBaritone() {
        if (pauseModules.isEmpty()) {
            BaritoneUtils.primary?.pathingControlManager?.registerProcess(this@PauseProcess)
        }

        lastPausingModule = this

        pauseModules[this] = System.currentTimeMillis()
    }

    fun Module.unpauseBaritone() {
        pauseModules.remove(this)
    }
}
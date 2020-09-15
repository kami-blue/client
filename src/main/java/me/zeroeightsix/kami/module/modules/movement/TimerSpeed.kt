package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings

/**
 * @author TBM
 * Updated by dominikaaaa on 28/01/20
 */
@Module.Info(
        name = "TimerSpeed",
        description = "Automatically change your timer to go fast",
        category = Module.Category.MOVEMENT
)
class TimerSpeed : Module() {

    private val minimumSpeed = register(Settings.floatBuilder("MinimumSpeed").withMinimum(0.1f).withMaximum(10.0f).withValue(4.0f).build())
    private val maxSpeed = register(Settings.floatBuilder("MaxSpeed").withMinimum(0.1f).withMaximum(10.0f).withValue(7.0f).build())
    private val attemptSpeed = register(Settings.floatBuilder("AttemptSpeed").withMinimum(1.0f).withMaximum(10.0f).withValue(4.2f).build())
    private val fastSpeed = register(Settings.floatBuilder("FastSpeed").withMinimum(1.0f).withMaximum(10.0f).withValue(5.0f).build())

    private var tickDelay = 0.0f
    private var curSpeed = 0.0f

    override fun onUpdate() {
        if (tickDelay == minimumSpeed.value) {
            curSpeed = fastSpeed.value
            mc.timer.tickLength = 50.0f / fastSpeed.value
        }
        if (tickDelay >= maxSpeed.value) {
            tickDelay = 0f
            curSpeed = attemptSpeed.value
            mc.timer.tickLength = 50.0f / attemptSpeed.value
        }
        ++tickDelay
    }

    override fun onDisable() {
        mc.timer.tickLength = 50.0f
    }
}
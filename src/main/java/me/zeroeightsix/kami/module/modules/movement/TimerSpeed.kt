package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MathsUtils

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
    private var tickDelay = 0.0f

    private val minimumSpeed = register(Settings.floatBuilder("Minimum Speed").withMinimum(0.1f).withMaximum(10.0f).withValue(4.0f).build())
    private val maxSpeed = register(Settings.floatBuilder("Max Speed").withMinimum(0.1f).withMaximum(10.0f).withValue(7.0f).build())
    private val attemptSpeed = register(Settings.floatBuilder("Attempt Speed").withMinimum(1.0f).withMaximum(10.0f).withValue(4.2f).build())
    private val fastSpeed = register(Settings.floatBuilder("Fast Speed").withMinimum(1.0f).withMaximum(10.0f).withValue(5.0f).build())

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

    public override fun onDisable() {
        mc.timer.tickLength = 50.0f
    }

    companion object {
        private var curSpeed = 0.0f
        @JvmStatic
        fun returnGui(): String {
            return "" + MathsUtils.round(curSpeed.toDouble(), 2)
        }
    }
}
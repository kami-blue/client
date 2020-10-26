package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.event.listener

@Module.Info(
        name = "TimerSpeed",
        description = "Automatically change your timer to go fast",
        category = Module.Category.MOVEMENT
)
object TimerSpeed : Module() {
    private val minimumSpeed = setting("MinimumSpeed", 4.0f, 0.1f..10.0f, 0.1f)
    private val maxSpeed = setting("MaxSpeed", 7.0f, 0.1f..10.0f, 0.1f)
    private val attemptSpeed = setting("AttemptSpeed", 4.2f, 0.1f..10.0f, 0.1f)
    private val fastSpeed = setting("FastSpeed", 5.0f, 0.1f..10.0f, 0.1f)

    private var tickDelay = 0.0f
    private var curSpeed = 0.0f

    init {
        listener<SafeTickEvent> {
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
    }

    override fun onDisable() {
        mc.timer.tickLength = 50.0f
    }
}
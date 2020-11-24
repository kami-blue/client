package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.mixin.extension.isInWeb
import me.zeroeightsix.kami.mixin.extension.tickLength
import me.zeroeightsix.kami.mixin.extension.timer
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener

@Module.Info(
        name = "FastFall",
        category = Module.Category.MOVEMENT,
        description = "Makes you fall faster"
)
object FastFall : Module() {
    private val mode: Setting<Mode> = register(Settings.e("Mode", Mode.MOTION))
    private val fallSpeed = register(Settings.doubleBuilder("FallSpeed").withValue(6.0).withRange(0.1, 10.0).withStep(0.1))
    private val fallDistance = register(Settings.integerBuilder("MaxFallDistance").withValue(2).withRange(0, 10).withStep(1))

    private var timering = false
    private var motioning = false

    private enum class Mode {
        MOTION, TIMER
    }

    init {
        listener<SafeTickEvent> {
            if (mc.player.onGround
                    || mc.player.isElytraFlying
                    || mc.player.isInLava
                    || mc.player.isInWater
                    || mc.player.isInWeb
                    || mc.player.fallDistance < fallDistance.value
                    || mc.player.capabilities.isFlying) {
                reset()
                return@listener
            }

            when (mode.value) {
                Mode.MOTION -> {
                    mc.player.motionY -= fallSpeed.value
                    motioning = true
                }
                Mode.TIMER -> {
                    mc.timer.tickLength = 50.0f / (fallSpeed.value * 2.0f).toFloat()
                    timering = true
                }
                else -> {
                }
            }
        }
    }

    override fun getHudInfo(): String? {
        return if (timering || motioning) "ACTIVE"
        else null
    }

    private fun reset() {
        if (timering) {
            mc.timer.tickLength = 50.0f
            timering = false
        }
        motioning = false
    }
}

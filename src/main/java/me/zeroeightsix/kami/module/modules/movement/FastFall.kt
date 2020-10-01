package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings

@Module.Info(
        name = "FastFall",
        category = Module.Category.MOVEMENT,
        description = "Makes you fall faster"
)
object FastFall : Module() {
    private val mode: Setting<Mode> = register(Settings.e("Mode", Mode.MOTION))
    private val fallSpeed = register(Settings.doubleBuilder("FallSpeed").withMinimum(0.1).withValue(6.0).withMaximum(10.0).build())
    private val fallDistance = register(Settings.integerBuilder("MaxFallDistance").withValue(2).withRange(0, 10).build())

    private var timering = false
    private var motioning = false

    private enum class Mode {
        MOTION, TIMER
    }

    override fun onUpdate() {
        if (mc.player == null
                || mc.player.onGround
                || mc.player.isElytraFlying
                || mc.player.isInLava
                || mc.player.isInWater
                || mc.player.isInWeb
                || mc.player.fallDistance < fallDistance.value
                || mc.player.capabilities.isFlying) {
            reset()
            return
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

    override fun getHudInfo(): String? {
        if (timering || motioning) {
            return "ACTIVE"
        }
        return null
    }

    private fun reset() {
        if (timering) {
            mc.timer.tickLength = 50.0f
            timering = false
        }
        motioning = false
    }
}

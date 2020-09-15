package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.movement.FastFall.Mode.MOTION
import me.zeroeightsix.kami.module.modules.movement.FastFall.Mode.TIMER
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings

/**
 * Created August 11th 2020 by historian
 * Updated by dominikaaaa on 21/08/20
 */
@Module.Info(
        name = "FastFall",
        category = Module.Category.MOVEMENT,
        description = "Makes you fall faster"
)
class FastFall : Module() {
    private val mode: Setting<Mode> = register(Settings.e("Mode", MOTION))
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
            MOTION -> {
                mc.player.motionY -= fallSpeed.value
                motioning = true
            }
            TIMER -> {
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

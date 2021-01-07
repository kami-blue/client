package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.mixin.extension.isInWeb
import me.zeroeightsix.kami.mixin.extension.tickLength
import me.zeroeightsix.kami.mixin.extension.timer
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent

object FastFall : Module(
    name = "FastFall",
    category = Category.MOVEMENT,
    description = "Makes you fall faster"
) {
    private val mode = setting("Mode", Mode.MOTION)
    private val fallSpeed = setting("FallSpeed", 6.0, 0.1..10.0, 0.1)
    private val fallDistance = setting("MaxFallDistance", 2, 0..10, 1)

    private var timering = false
    private var motioning = false

    private enum class Mode {
        MOTION, TIMER
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (player.onGround
                    || player.isElytraFlying
                    || player.isInLava
                    || player.isInWater
                    || player.isInWeb
                    || player.fallDistance < fallDistance.value
                    || player.capabilities.isFlying) {
                reset()
                return@safeListener
            }

            when (mode.value) {
                Mode.MOTION -> {
                    player.motionY -= fallSpeed.value
                    motioning = true
                }
                Mode.TIMER -> {
                    mc.timer.tickLength = 50.0f / (fallSpeed.value * 2.0f).toFloat()
                    timering = true
                }
            }
        }
    }

    override fun getHudInfo(): String {
        return if (timering || motioning) "ACTIVE"
        else ""
    }

    private fun reset() {
        if (timering) {
            mc.timer.tickLength = 50.0f
            timering = false
        }
        motioning = false
    }
}

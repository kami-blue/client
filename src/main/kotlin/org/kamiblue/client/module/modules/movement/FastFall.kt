package org.kamiblue.client.module.modules.movement

import org.kamiblue.client.mixin.extension.isInWeb
import org.kamiblue.client.mixin.extension.tickLength
import org.kamiblue.client.mixin.extension.timer
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent

internal object FastFall : Module(
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

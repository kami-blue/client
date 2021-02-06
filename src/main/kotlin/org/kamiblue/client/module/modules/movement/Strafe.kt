package org.kamiblue.client.module.modules.movement

import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.PlayerTravelEvent
import org.kamiblue.client.mixin.extension.tickLength
import org.kamiblue.client.mixin.extension.timer
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.BaritoneUtils
import org.kamiblue.client.util.MovementUtils
import org.kamiblue.client.util.MovementUtils.calcMoveYaw
import org.kamiblue.client.util.MovementUtils.setSpeed
import org.kamiblue.client.util.MovementUtils.speed
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.threads.safeListener
import net.minecraft.client.settings.KeyBinding
import kotlin.math.cos
import kotlin.math.sin

internal object Strafe : Module(
    name = "Strafe",
    category = Category.MOVEMENT,
    description = "Improves control in air"
) {
    private val airSpeedBoost by setting("Air Speed Boost", true)
    private val timerBoost by setting("Timer Boost", true)
    private val autoJump by setting("Auto Jump", true)
    private val onHoldingSprint by setting("On Holding Sprint", false)
    private val cancelInertia by setting("Cancel Inertia", false)

    private var jumpTicks = 0
    private var strafeTimer = TickTimer(TimeUnit.TICKS)

    /* If you skid this you omega gay */
    init {
        onDisable {
            reset()
        }

        safeListener<PlayerTravelEvent> {
            if (!shouldStrafe()) {
                reset()
                if (cancelInertia && !strafeTimer.tick(2L)) {
                    player.motionX = 0.0
                    player.motionZ = 0.0
                }
                return@safeListener
            }

            setSpeed(player.speed)
            if (airSpeedBoost) player.jumpMovementFactor = 0.029f
            if (timerBoost) mc.timer.tickLength = 45.87155914306640625f
            if (autoJump) jump()

            strafeTimer.reset()
        }
    }

    private fun reset() {
        mc.player?.jumpMovementFactor = 0.02f
        mc.timer.tickLength = 50.0f
        jumpTicks = 0
    }

    private fun SafeClientEvent.shouldStrafe() = !BaritoneUtils.isPathing
        && !player.capabilities.isFlying
        && !player.isElytraFlying
        && (!onHoldingSprint || mc.gameSettings.keyBindSprint.isKeyDown)
        && MovementUtils.isInputting

    private fun SafeClientEvent.jump() {
        if (player.onGround && jumpTicks <= 0) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.keyCode, false)
            player.motionY = 0.41
            if (player.isSprinting) {
                val yaw = calcMoveYaw()
                player.motionX -= sin(yaw) * 0.2
                player.motionZ += cos(yaw) * 0.2
            }
            player.isAirBorne = true
            jumpTicks = 5
        }

        jumpTicks--
    }
}

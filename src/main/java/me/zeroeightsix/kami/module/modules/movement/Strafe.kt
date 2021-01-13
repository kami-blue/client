package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.mixin.extension.tickLength
import me.zeroeightsix.kami.mixin.extension.timer
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.MovementUtils
import me.zeroeightsix.kami.util.MovementUtils.calcMoveYaw
import me.zeroeightsix.kami.util.MovementUtils.setSpeed
import me.zeroeightsix.kami.util.MovementUtils.speed
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.settings.KeyBinding
import kotlin.math.cos
import kotlin.math.sin

object Strafe : Module(
    name = "Strafe",
    category = Category.MOVEMENT,
    description = "Improves control in air"
) {
    private val airSpeedBoost by setting("AirSpeedBoost", true)
    private val timerBoost by setting("TimerBoost", true)
    private val autoJump by setting("AutoJump", true)
    private val onHoldingSprint by setting("OnHoldingSprint", false)
    private val cancelInertia by setting("CancelInertia", false)

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

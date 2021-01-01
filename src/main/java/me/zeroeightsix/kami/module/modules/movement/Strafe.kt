package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.mixin.extension.tickLength
import me.zeroeightsix.kami.mixin.extension.timer
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.MovementUtils
import me.zeroeightsix.kami.util.MovementUtils.speed
import net.minecraft.client.settings.KeyBinding
import org.kamiblue.event.listener.listener
import kotlin.math.cos
import kotlin.math.sin

@Module.Info(
        name = "Strafe",
        category = Module.Category.MOVEMENT,
        description = "Improves control in air"
)
object Strafe : Module() {
    private val airSpeedBoost = register(Settings.b("AirSpeedBoost", true))
    private val timerBoost = register(Settings.b("TimerBoost", false))
    private val autoJump = register(Settings.b("AutoJump", false))
    private val onHolding = register(Settings.b("OnHoldingSprint", false))

    private var jumpTicks = 0

    override fun onDisable() {
        reset()
    }

    /* If you skid this you omega gay */
    init {
        listener<SafeTickEvent> {
            if (!shouldStrafe()) {
                reset()
                return@listener
            }
            MovementUtils.setSpeed(mc.player.speed)
            if (airSpeedBoost.value) mc.player.jumpMovementFactor = 0.029f
            if (timerBoost.value) mc.timer.tickLength = 45.87155914306640625f

            if (autoJump.value && mc.player.onGround && jumpTicks <= 0) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.keyCode, false)
                mc.player.motionY = 0.41
                if (mc.player.isSprinting) {
                    val yaw = MovementUtils.calcMoveYaw()
                    mc.player.motionX -= sin(yaw) * 0.2
                    mc.player.motionZ += cos(yaw) * 0.2
                }
                mc.player.isAirBorne = true
                jumpTicks = 5
            }
            if (jumpTicks > 0) jumpTicks--
        }
    }

    fun shouldStrafe() = !BaritoneUtils.isPathing
            && !mc.player.capabilities.isFlying
            && !mc.player.isElytraFlying
            && (mc.gameSettings.keyBindSprint.isKeyDown || !onHolding.value)
            && (mc.player.moveForward != 0f || mc.player.moveStrafing != 0f)

    private fun reset() {
        mc.player.jumpMovementFactor = 0.02F
        mc.timer.tickLength = 50F
        jumpTicks = 0
    }
}

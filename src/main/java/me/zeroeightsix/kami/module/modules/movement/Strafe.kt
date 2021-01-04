package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.mixin.extension.tickLength
import me.zeroeightsix.kami.mixin.extension.timer
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.MovementUtils
import me.zeroeightsix.kami.util.MovementUtils.speed
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.cos
import kotlin.math.sin

@Module.Info(
        name = "Strafe",
        category = Module.Category.MOVEMENT,
        description = "Improves control in air"
)
object Strafe : Module() {
    private val airSpeedBoost = setting("AirSpeedBoost", true)
    private val timerBoost = setting("TimerBoost", false)
    private val autoJump = setting("AutoJump", false)
    private val onHolding = setting("OnHoldingSprint", false)

    private var jumpTicks = 0

    override fun onDisable() {
        reset()
    }

    /* If you skid this you omega gay */
    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (!shouldStrafe()) {
                reset()
                return@safeListener
            }
            MovementUtils.setSpeed(player.speed)
            if (airSpeedBoost.value) player.jumpMovementFactor = 0.029f
            if (timerBoost.value) mc.timer.tickLength = 45.87155914306640625f

            if (autoJump.value && player.onGround && jumpTicks <= 0) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.keyCode, false)
                player.motionY = 0.41
                if (player.isSprinting) {
                    val yaw = MovementUtils.calcMoveYaw()
                    player.motionX -= sin(yaw) * 0.2
                    player.motionZ += cos(yaw) * 0.2
                }
                player.isAirBorne = true
                jumpTicks = 5
            }
            if (jumpTicks > 0) jumpTicks--
        }
    }

    private fun SafeClientEvent.shouldStrafe() = !BaritoneUtils.isPathing
            && !player.capabilities.isFlying
            && !player.isElytraFlying
            && (mc.gameSettings.keyBindSprint.isKeyDown || !onHolding.value)
            && (player.moveForward != 0f || player.moveStrafing != 0f)

    private fun reset() {
        mc.player.jumpMovementFactor = 0.02F
        mc.timer.tickLength = 50F
        jumpTicks = 0
    }
}

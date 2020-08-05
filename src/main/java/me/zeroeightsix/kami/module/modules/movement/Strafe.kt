package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.StrafeUtils

/**
 * Created july 14th 2020 by historian
 */
@Module.Info(
        name = "Strafe",
        category = Module.Category.MOVEMENT,
        description = "Improves control in air"
)
class Strafe : Module() {
    private val airSpeedBoost = register(Settings.b("AirSpeedBoost", true))
    private val timerBoost = register(Settings.b("TimerBoost", false))
    private val autoJump = register(Settings.b("AutoJump", true))

    private var jumpTicks = 0
    private val strafeUtils = StrafeUtils()

    /* if you skid this you omega gay */
    override fun onUpdate() {
        if(mc.gameSettings.keyBindForward.isKeyDown) {
            strafeUtils.setSpeed(strafeUtils.getSpeed())
            if(airSpeedBoost.value)mc.player.jumpMovementFactor = 0.029F
            if(timerBoost.value)mc.timer.tickLength = 50 / 1.09F
            if(autoJump.value && mc.player.onGround && jumpTicks == 0) {
                mc.player.jump()
                jumpTicks = 5
            }
            if(jumpTicks > 0)jumpTicks--
        }
    }

    override fun onDisable() {
        mc.player.jumpMovementFactor = 0.02F
        mc.timer.tickLength = 50F
        jumpTicks = 0
    }
}
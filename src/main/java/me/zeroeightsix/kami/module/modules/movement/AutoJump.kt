package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimerUtils

@Module.Info(
        name = "AutoJump",
        category = Module.Category.MOVEMENT,
        description = "Automatically jumps if possible"
)
object AutoJump : Module() {
    private val delay = register(Settings.integerBuilder("TickDelay").withValue(10).withRange(0, 40))

    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.TICKS)

    override fun onUpdate() {
        if (mc.player.isInWater || mc.player.isInLava) mc.player.motionY = 0.1
        else if (mc.player.onGround && timer.tick(delay.value.toLong())) mc.player.jump()
    }
}
package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent

@Module.Info(
        name = "AutoJump",
        category = Module.Category.MOVEMENT,
        description = "Automatically jumps if possible"
)
object AutoJump : Module() {
    private val delay = register(Settings.integerBuilder("TickDelay").withValue(10).withRange(0, 40).withStep(1))

    private val timer = TickTimer(TimeUnit.TICKS)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (player.isInWater || player.isInLava) player.motionY = 0.1
            else if (player.onGround && timer.tick(delay.value.toLong())) player.jump()
        }
    }
}
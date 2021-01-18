package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent

internal object AutoJump : Module(
    name = "AutoJump",
    category = Category.MOVEMENT,
    description = "Automatically jumps if possible"
) {
    private val delay = setting("TickDelay", 10, 0..40, 1)

    private val timer = TickTimer(TimeUnit.TICKS)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (player.isInWater || player.isInLava) player.motionY = 0.1
            else if (player.onGround && timer.tick(delay.value.toLong())) player.jump()
        }
    }
}
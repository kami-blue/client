package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings

/**
 * Created by 086 on 24/12/2017.
 */
@Module.Info(
        name = "AutoJump",
        category = Module.Category.MOVEMENT,
        description = "Automatically jumps if possible"
)
class AutoJump : Module() {
    private val delay = register(Settings.integerBuilder("TickDelay").withValue(10).build())

    override fun onUpdate() {
        if (mc.player.isInWater || mc.player.isInLava) mc.player.motionY = 0.1 else jump()
    }

    private fun jump() {
        if (mc.player.onGround && timeout()) {
            mc.player.jump()
            startTime = 0
        }
    }

    private fun timeout(): Boolean {
        if (startTime == 0L) startTime = System.currentTimeMillis()
        if (startTime + delay.value / 20 * 1000 <= System.currentTimeMillis()) { // 1 timeout = 1 second = 1000 ms
            startTime = System.currentTimeMillis()
            return true
        }
        return false
    }

    companion object {
        private var startTime: Long = 0
    }
}
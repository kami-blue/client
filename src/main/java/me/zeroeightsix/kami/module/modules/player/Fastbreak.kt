package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Module

/**
 * @author 086
 */
@Module.Info(
        name = "Fastbreak",
        category = Module.Category.PLAYER,
        description = "Nullifies block hit delay"
)
class Fastbreak : Module() {
    override fun onUpdate() {
        mc.playerController.blockHitDelay = 0
    }
}
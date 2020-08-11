package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings

/**
 * Created August 11th 2020 by historian
 */
@Module.Info(
        name = "FastFall",
        category = Module.Category.MOVEMENT,
        description = "Makes you fall faster"
)
class FastFall : Module() {
    private val fallSpeed = register(Settings.doubleBuilder("FallSpeed").withMinimum(0.1).withValue(1.0).withMaximum(10.0).build())
    private val fallDistance = register(Settings.i("FallDistance", 1))

    override fun onUpdate() {
        if (mc.player.onGround || mc.player.isElytraFlying || mc.player.isInLava || mc.player.isInWater || mc.player.isInWeb || mc.player.fallDistance < fallDistance.value || mc.player.capabilities.isFlying) return

        mc.player.motionY -= fallSpeed.value
    }
}

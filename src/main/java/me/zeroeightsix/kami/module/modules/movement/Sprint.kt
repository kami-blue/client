package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module

/**
 * Created by 086 on 23/08/2017.
 * Updated by dominikaaaa on 06/03/20
 */
@Module.Info(
        name = "Sprint",
        description = "Automatically makes the player sprint",
        category = Module.Category.MOVEMENT,
        showOnArray = Module.ShowOnArray.OFF
)
class Sprint : Module() {
    override fun onUpdate() {
        if (mc.player == null) return
        if (KamiMod.MODULE_MANAGER.getModule(ElytraFlight::class.java).isEnabled && (mc.player.isElytraFlying || mc.player.capabilities.isFlying)) return

        try {
            mc.player.isSprinting = !mc.player.collidedHorizontally && mc.player.moveForward > 0
        } catch (ignored: Exception) { }
    }
}
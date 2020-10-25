package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.event.listener

/**
 * @see me.zeroeightsix.kami.mixin.client.MixinEntityPlayerSP
 */
@Module.Info(
        name = "Sprint",
        description = "Automatically makes the player sprint",
        category = Module.Category.MOVEMENT
)
object Sprint : Module() {
    private val multiDirection = setting("MultiDirection", false)
    private val onHolding = setting("OnHoldingSprint", false)

    var sprinting = false

    init {
        listener<SafeTickEvent> {
            if (!shouldSprint()) return@listener

            sprinting = if (multiDirection.value) mc.player.moveForward != 0f || mc.player.moveStrafing != 0f
            else mc.player.moveForward > 0

            if (mc.player.collidedHorizontally || (onHolding.value && !mc.gameSettings.keyBindSprint.isKeyDown)) sprinting = false

            mc.player.isSprinting = sprinting
        }
    }

    fun shouldSprint() = !mc.player.isElytraFlying && !mc.player.capabilities.isFlying && !BaritoneUtils.isPathing
}
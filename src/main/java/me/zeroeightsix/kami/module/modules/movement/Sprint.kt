package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * @see me.zeroeightsix.kami.mixin.client.player.MixinEntityPlayerSP
 */
@Module.Info(
        name = "Sprint",
        description = "Automatically makes the player sprint",
        category = Module.Category.MOVEMENT
)
object Sprint : Module() {
    private val multiDirection = register(Settings.b("MultiDirection", false))
    private val onHolding = register(Settings.b("OnHoldingSprint", false))

    var sprinting = false

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (!shouldSprint()) return@safeListener

            sprinting = if (multiDirection.value) mc.player.moveForward != 0f || mc.player.moveStrafing != 0f
            else mc.player.moveForward > 0

            if (mc.player.collidedHorizontally || (onHolding.value && !mc.gameSettings.keyBindSprint.isKeyDown)) sprinting = false

            mc.player.isSprinting = sprinting
        }
    }

    fun shouldSprint() = !mc.player.isElytraFlying && !mc.player.capabilities.isFlying && !BaritoneUtils.isPathing
}
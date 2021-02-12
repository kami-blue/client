package org.kamiblue.client.module.modules.movement

import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.BaritoneUtils
import org.kamiblue.client.util.threads.safeListener

/**
 * @see org.kamiblue.client.mixin.client.player.MixinEntityPlayerSP
 */
internal object Sprint : Module(
    name = "Sprint",
    description = "Automatically makes the player sprint",
    category = Category.MOVEMENT
) {
    private val multiDirection = setting("Multi Direction", false)
    private val onHolding = setting("On Holding Sprint", false)

    var sprinting = false

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (!shouldSprint()) return@safeListener

            sprinting = if (multiDirection.value) player.moveForward != 0f || player.moveStrafing != 0f
            else player.moveForward > 0

            if (player.collidedHorizontally || (onHolding.value && !mc.gameSettings.keyBindSprint.isKeyDown)) sprinting = false

            player.isSprinting = sprinting
        }
    }

    fun shouldSprint() = !mc.player.isElytraFlying && !mc.player.capabilities.isFlying && !BaritoneUtils.isPathing
}
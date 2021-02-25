package org.kamiblue.client.module.modules.movement

import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.mixin.extension.onGround
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.event.listener.listener

/**
 * Movement taken from Seppuku
 * https://github.com/seppukudevelopment/seppuku/blob/005e2da/src/main/java/me/rigamortis/seppuku/impl/module/player/NoHungerModule.java
 */
internal object AntiHunger : Module(
    name = "AntiHunger",
    category = Category.MOVEMENT,
    description = "Reduces hunger lost when moving around"
) {
    private val cancelMovementState = setting("Cancel Movement State", true)

    init {
        listener<PacketEvent.Send> {
            if (mc.player == null) return@listener
            if (cancelMovementState.value && it.packet is CPacketEntityAction) {
                if (it.packet.action == CPacketEntityAction.Action.START_SPRINTING || it.packet.action == CPacketEntityAction.Action.STOP_SPRINTING) {
                    it.cancel()
                }
            }
            if (it.packet is CPacketPlayer) {
                // Trick the game to think that tha player is flying even if he is on ground. Also check if the player is flying with the Elytra.
                it.packet.onGround = (mc.player.fallDistance <= 0 || mc.playerController.isHittingBlock) && mc.player.isElytraFlying
            }
        }
    }
}
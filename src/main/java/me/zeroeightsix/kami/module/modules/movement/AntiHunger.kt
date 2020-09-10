package me.zeroeightsix.kami.module.modules.movement

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer

/**
 * Created by 086 on 8/04/2018.
 * Code tweaked by coderynx & OverFloyd.
 * Updated by Xiaro on 09/09/20
 *
 * Movement taken from Seppuku
 * https://github.com/seppukudevelopment/seppuku/blob/005e2da/src/main/java/me/rigamortis/seppuku/impl/module/player/NoHungerModule.java
 */
@Module.Info(
        name = "AntiHunger",
        category = Module.Category.MOVEMENT,
        description = "Reduces hunger lost when moving around"
)
class AntiHunger : Module() {
    private val cancelMovementState = register(Settings.b("CancelMovementState", true))

    @EventHandler
    private val packetListener = Listener(EventHook { event: PacketEvent.Send ->
        if (mc.player == null) return@EventHook
        if (cancelMovementState.value && event.packet is CPacketEntityAction) {
            if (event.packet.action == CPacketEntityAction.Action.START_SPRINTING
                    || event.packet.action == CPacketEntityAction.Action.STOP_SPRINTING) {
                event.cancel()
            }
        }
        if (event.packet is CPacketPlayer) {
            // Trick the game to think that tha player is flying even if he is on ground. Also check if the player is flying with the Elytra.
            event.packet.onGround = (mc.player.fallDistance <= 0 || mc.playerController.isHittingBlock) && mc.player.isElytraFlying
        }
    })
}
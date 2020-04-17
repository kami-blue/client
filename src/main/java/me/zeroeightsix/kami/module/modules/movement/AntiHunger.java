package me.zeroeightsix.kami.module.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;

import static net.minecraft.network.play.client.CPacketEntityAction.Action.START_SPRINTING;
import static net.minecraft.network.play.client.CPacketEntityAction.Action.STOP_SPRINTING;

/**
 * Created by 086 on 8/04/2018.
 * Code tweaked by coderynx & OverFloyd.
 * 
 * Movement taken from Seppuku
 * https://github.com/seppukudevelopment/seppuku/blob/005e2da/src/main/java/me/rigamortis/seppuku/impl/module/player/NoHungerModule.java
 */
@Module.Info(name = "AntiHunger", category = Module.Category.MOVEMENT, description = "Reduces hunger lost when moving around")
public class AntiHunger extends Module {
    private Setting<Boolean> cancelMovementState = register(Settings.b("Cancel Movement State", true));

    @EventHandler
    public Listener<PacketEvent.Send> packetListener = new Listener<>(event -> {
        if (mc.player == null) return;

        if (cancelMovementState.getValue() && event.getPacket() instanceof CPacketEntityAction) {
            CPacketEntityAction packet = (CPacketEntityAction) event.getPacket();
            if (packet.getAction() == START_SPRINTING || packet.getAction() == STOP_SPRINTING) {
                event.cancel();
            }
        }

        if (event.getPacket() instanceof CPacketPlayer) {
            // Trick the game to think that tha player is flying even if he is on ground. Also check if the player is flying with the Elytra.
            ((CPacketPlayer) event.getPacket()).onGround = (mc.player.fallDistance > 0 || mc.playerController.isHittingBlock) && mc.player.isElytraFlying();
        }
    });

}

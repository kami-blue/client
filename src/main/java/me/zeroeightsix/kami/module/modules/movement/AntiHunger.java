package me.zeroeightsix.kami.module.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.command.Command.sendChatMessage;
import static net.minecraft.network.play.client.CPacketEntityAction.Action.START_SPRINTING;
import static net.minecraft.network.play.client.CPacketEntityAction.Action.STOP_SPRINTING;

/**
 * Created by 086 on 8/04/2018.
 * Movement taken from Seppuku
 * https://github.com/seppukudevelopment/seppuku/blob/005e2da/src/main/java/me/rigamortis/seppuku/impl/module/player/NoHungerModule.java
 */
@Module.Info(name = "AntiHunger", category = Module.Category.MOVEMENT, description = "Reduces hunger lost when moving around")
public class AntiHunger extends Module {
    private Setting<Boolean> cancelMovementState = register(Settings.b("Cancel Movement State", true));

    @Override
    public void toggle()
    {
        super.toggle();
        if (isEnabled() && MODULE_MANAGER.isModuleEnabled(ElytraFlight.class) && cancelMovementState.getValue())
        {
            sendChatMessage("You just enabled AntiHunger with 'Cancel Movement State', you cannot elytrafly with it.");
        }
    }

    @EventHandler
    public Listener<PacketEvent.Send> packetListener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketEntityAction) {
            final CPacketEntityAction packet = (CPacketEntityAction) event.getPacket();
            if (cancelMovementState.getValue() && (packet.getAction() == START_SPRINTING || packet.getAction() == STOP_SPRINTING)) {
                event.cancel();
            }
        }
        if (event.getPacket() instanceof CPacketPlayer) {
            ((CPacketPlayer) event.getPacket()).onGround = mc.player.fallDistance > 0 || mc.playerController.isHittingBlock;
        }
    });

}

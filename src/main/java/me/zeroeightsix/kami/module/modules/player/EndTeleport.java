package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.util.text.TextComponentString;

import java.util.Objects;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendWarningMessage;

/**
 * Created by 0x2E | PretendingToCode
 */
@Module.Info(
        name = "EndTeleport",
        category = Module.Category.PLAYER,
        description = "Allows for teleportation when going through end portals"
)
public class EndTeleport extends Module {
    private Setting<Boolean> confirmed = register(Settings.b("Confirm", true));

    @Override
    public void onEnable() {
        if (Wrapper.getMinecraft().getCurrentServerData() == null) {
            sendWarningMessage(getChatName() + "This module does not work in singleplayer");
            disable();
        } else if (!confirmed.getValue()) {
            sendWarningMessage(getChatName() + "This module will kick you from the server! It is part of the exploit and cannot be avoided");
        }
    }

    @EventHandler
    public Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (event.getPacket() instanceof SPacketRespawn) {
            if (((SPacketRespawn) event.getPacket()).getDimensionID() == 1 && confirmed.getValue()) {
                Objects.requireNonNull(Wrapper.getMinecraft().getConnection()).handleDisconnect(new SPacketDisconnect(new TextComponentString("Attempting teleportation exploit")));
                disable();
            }
        }
    });
}

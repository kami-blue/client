package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import java.util.function.*;
import net.minecraft.network.play.server.SPacketChat;
import me.zeroeightsix.kami.util.Wrapper;

/*
* By Katatje 8 Dec 2019
*/

@Module.Info(name = "AutoTPA", description = "Auto Accepts/Declines TPA requests", category = Module.Category.MISC)
public class AutoTP extends Module
{

    private Setting<mode> mod = register(Settings.e("Response", mode.DENY));

    @EventHandler
    public Listener<PacketEvent.Receive> receiveListener;
    public AutoTP() {
        this.receiveListener = new Listener<PacketEvent.Receive>(event -> {
            SPacketChat packet;
            if (event.getPacket() instanceof SPacketChat && (packet = (SPacketChat) event.getPacket()).getChatComponent().getUnformattedText().contains(" has requested to teleport to you.")) {
                switch (mod.getValue())
                {
                    case ACCEPT:
                        Wrapper.getPlayer().sendChatMessage("/tpaccept");
                        break;
                    case DENY:
                        Wrapper.getPlayer().sendChatMessage("/tpdeny");
                        break;
                }
            }
        }, (Predicate<PacketEvent.Receive>[])new Predicate[0]);
    }

    public enum mode {
        ACCEPT, DENY
    }

}

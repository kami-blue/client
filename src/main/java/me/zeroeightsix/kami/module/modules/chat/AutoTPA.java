package me.zeroeightsix.kami.module.modules.chat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.network.play.server.SPacketChat;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendServerMessage;

/*
 * By Katatje 8 Dec 2019
 * Updated by dominikaaaa on 12/04/20
 */
@Module.Info(
        name = "AutoTPA",
        description = "Automatically decline or accept TPA requests",
        category = Module.Category.CHAT
)
public class AutoTPA extends Module {
    private Setting<Boolean> friends = register(Settings.b("Always accept friends", true));
    private Setting<mode> mod = register(Settings.e("Response", mode.DENY));

    @EventHandler
    public Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (event.getPacket() instanceof SPacketChat && ((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText().contains(" has requested to teleport to you.")) {
            /* I tested that getting the first word is compatible with chat timestamp, and it as, as this is Receive and chat timestamp is after Receive */
            String firstWord = ((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText().split("\\s+")[0];
            if (friends.getValue() && Friends.isFriend(firstWord)) {
                sendServerMessage("/tpaccept");
                return;
            }
            switch (mod.getValue()) {
                case ACCEPT:
                    sendServerMessage("/tpaccept");
                    break;
                case DENY:
                    sendServerMessage("/tpdeny");
                    break;
            }
        }
    });

    public enum mode {
        ACCEPT, DENY
    }
}

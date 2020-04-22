package me.zeroeightsix.kami.module.modules.chat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketChatMessage;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendWarningMessage;

/**
 * Created on 16 December by 0x2E | PretendingToCode
 */

@Module.Info(
        name = "FormatChat",
        description = "Add colour and linebreak support to upstream chat packets",
        category = Module.Category.CHAT
)
public class FormatChat extends Module {

    @Override
    public void onEnable() {
        if (Minecraft.getMinecraft().getCurrentServerData() == null) {
            sendWarningMessage(getChatName() + " &6&lWarning: &r&6This does not work in singleplayer");
            disable();
        }
        else {
            sendWarningMessage(getChatName() + " &6&lWarning: &r&6This will kick you on most servers!");
        }
    }

    @EventHandler
    public Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketChatMessage) {
            String message = ((CPacketChatMessage) event.getPacket()).message;
            if (message.contains("&") || message.contains("#n")) {
                message = message.replaceAll("&", KamiMod.colour + "");
                message = message.replaceAll("#n", "\n");

                Wrapper.getPlayer().connection.sendPacket(new CPacketChatMessage(message));
                event.cancel();
            }
        }
    });
}

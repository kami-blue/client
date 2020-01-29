package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.gui.InfoOverlay;
import me.zeroeightsix.kami.util.InfoCalculator;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextFormatting;

/**
 * @author S-B99
 * Updated by S-B99 on 28/01/20
 */
@Module.Info(name = "ChatTimestamp", category = Module.Category.MISC)
public class ChatTimestamp extends Module {
    @EventHandler
    public Listener<PacketEvent.Receive> listener = new Listener<>(event -> {
        if (mc.player == null || this.isDisabled()) return;

        if (!(event.getPacket() instanceof SPacketChat)) return;
        SPacketChat sPacketChat = (SPacketChat) event.getPacket();

        if (addTime(sPacketChat.getChatComponent().getUnformattedText())) {
            event.cancel();
        }
    });

//    public void onUpdate() {
//        InfoOverlay info = new InfoOverlay();
//        String colour = InfoOverlay.ColourCode(secondColour.getValue());
//    }
    private boolean addTime(String message) {
        Command.sendRawChatMessage("<" + new InfoOverlay().formatChatTime() + "> " + message);
        return true;
    }


}
package me.zeroeightsix.kami.module.modules.sdashb.experimental;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.client.CPacketChatMessage;

/**
 * @author S-B99
 * Updated by S-B99 on 29/10/19
 */
@Module.Info(name = "SendRawUnicode", category = Module.Category.EXPERIMENTAL, description = "Converts all text into raw unicode")
public class SendUnicodeModule extends Module {

    private Setting<Boolean> commands = register(Settings.b("Use on commands", false));

    //private final String KAMI_CUSTOM_UNICODE = "\u0299\u029f\u1d1c\u1d07";

    @EventHandler
    public Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketChatMessage) {
            String toSend = ((CPacketChatMessage) event.getPacket()).getMessage();
            System.out.println("神 Raw Message is: " + toSend);
            if (toSend.startsWith("/") && !commands.getValue())
            	return;
            else if (toSend.startsWith(",") && !commands.getValue()) 
            	return;
            else if (toSend.startsWith(".") && !commands.getValue())
            	return;
            else if (toSend.startsWith("-") && !commands.getValue()) 
            	return;
            
            //toSend = KAMI_CUSTOM_UNICODE;
            if (toSend.length() >= 256) 
            	toSend = toSend.substring(0,256);
            ((CPacketChatMessage) event.getPacket()).message = toSend;
            System.out.println("神 Raw Message was: " + toSend);
        }
    });

}

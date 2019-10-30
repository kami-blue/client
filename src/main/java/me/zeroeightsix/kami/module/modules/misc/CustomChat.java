package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.client.CPacketChatMessage;

/**
 * Created by 086 on 8/04/2018.
 * Updated by S-B99 on 28/10/19
 */
@Module.Info(name = "CustomChat", category = Module.Category.MISC, description = "Modifies your chat messages")
public class CustomChat extends Module {

    private Setting<Boolean> commands = register(Settings.b("Use on commands", false));

    private final String KAMI_SUFFIX = " \u00ab \u1d0b\u1d00\u1d0d\u026a \u0299\u029f\u1d1c\u1d07 \u00bb";

    @EventHandler
    public Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketChatMessage) {
            String s = ((CPacketChatMessage) event.getPacket()).getMessage();
            if (s.startsWith("/") && !commands.getValue()) 
            	return;
            else if (s.startsWith(",") && !commands.getValue()) 
            	return;
            else if (s.startsWith(".") && !commands.getValue()) 
            	return;
            else if (s.startsWith("-") && !commands.getValue()) 
            	return;
            s += KAMI_SUFFIX;
            if (s.length() >= 256) s = s.substring(0,256);
            ((CPacketChatMessage) event.getPacket()).message = s;
        }
    });

}

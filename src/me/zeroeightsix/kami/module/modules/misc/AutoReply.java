// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import java.util.function.Predicate;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.network.play.server.SPacketChat;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AutoReply", category = Category.MISC, description = "automatically replies to messages")
public class AutoReply extends Module
{
    @EventHandler
    public Listener<PacketEvent.Receive> receiveListener;
    
    public AutoReply() {
        this.receiveListener = new Listener<PacketEvent.Receive>(event -> {
            if (event.getPacket() instanceof SPacketChat && ((SPacketChat)event.getPacket()).func_148915_c().func_150260_c().contains("whispers:")) {
                Wrapper.getPlayer().func_71165_d("/r sorry. im AFK flying to the world border rn, dm me: tux#6456 :D");
            }
        }, (Predicate<PacketEvent.Receive>[])new Predicate[0]);
    }
}

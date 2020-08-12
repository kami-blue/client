// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.player;

import java.util.function.Predicate;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AntiForceLook", category = Category.PLAYER, description = "Stops packets from turning your head")
public class AntiForceLook extends Module
{
    @EventHandler
    Listener<PacketEvent.Receive> receiveListener;
    
    public AntiForceLook() {
        SPacketPlayerPosLook packet;
        this.receiveListener = new Listener<PacketEvent.Receive>(event -> {
            if (AntiForceLook.mc.field_71439_g != null) {
                if (event.getPacket() instanceof SPacketPlayerPosLook) {
                    packet = (SPacketPlayerPosLook)event.getPacket();
                    packet.field_148936_d = AntiForceLook.mc.field_71439_g.field_70177_z;
                    packet.field_148937_e = AntiForceLook.mc.field_71439_g.field_70125_A;
                }
            }
        }, (Predicate<PacketEvent.Receive>[])new Predicate[0]);
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import java.util.function.Predicate;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.network.play.server.SPacketSoundEffect;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;

@Info(name = "NoSoundLag", category = Category.MISC, description = "Prevents sound lag")
public class NoSoundLag extends Module
{
    @EventHandler
    Listener<PacketEvent.Receive> receiveListener;
    
    public NoSoundLag() {
        SPacketSoundEffect soundPacket;
        this.receiveListener = new Listener<PacketEvent.Receive>(event -> {
            if (NoSoundLag.mc.field_71439_g != null) {
                if (event.getPacket() instanceof SPacketSoundEffect) {
                    soundPacket = (SPacketSoundEffect)event.getPacket();
                    if (soundPacket.func_186977_b() == SoundCategory.PLAYERS && soundPacket.func_186978_a() == SoundEvents.field_187719_p) {
                        event.cancel();
                    }
                }
            }
        }, (Predicate<PacketEvent.Receive>[])new Predicate[0]);
    }
}

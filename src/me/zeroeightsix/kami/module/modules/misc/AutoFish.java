// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import java.util.function.Predicate;
import net.minecraft.network.play.server.SPacketSoundEffect;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AutoFish", category = Category.MISC, description = "Automatically catch fish")
public class AutoFish extends Module
{
    @EventHandler
    private Listener<PacketEvent.Receive> receiveListener;
    
    public AutoFish() {
        SPacketSoundEffect pck;
        int soundX;
        int soundZ;
        int fishX;
        int fishZ;
        this.receiveListener = new Listener<PacketEvent.Receive>(e -> {
            if (e.getPacket() instanceof SPacketSoundEffect) {
                pck = (SPacketSoundEffect)e.getPacket();
                if (pck.func_186978_a().func_187503_a().toString().toLowerCase().contains("entity.bobber.splash")) {
                    if (AutoFish.mc.field_71439_g.field_71104_cf != null) {
                        soundX = (int)pck.func_149207_d();
                        soundZ = (int)pck.func_149210_f();
                        fishX = (int)AutoFish.mc.field_71439_g.field_71104_cf.field_70165_t;
                        fishZ = (int)AutoFish.mc.field_71439_g.field_71104_cf.field_70161_v;
                        if (this.kindaEquals(soundX, fishX) && this.kindaEquals(fishZ, soundZ)) {
                            new Thread(() -> {
                                AutoFish.mc.func_147121_ag();
                                try {
                                    Thread.sleep(1000L);
                                }
                                catch (InterruptedException e2) {
                                    e2.printStackTrace();
                                }
                                AutoFish.mc.func_147121_ag();
                            }).start();
                        }
                    }
                }
            }
        }, (Predicate<PacketEvent.Receive>[])new Predicate[0]);
    }
    
    public boolean kindaEquals(final int kara, final int ni) {
        return ni == kara || ni == kara - 1 || ni == kara + 1;
    }
}

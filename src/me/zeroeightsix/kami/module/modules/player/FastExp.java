// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.player;

import java.util.function.Predicate;
import net.minecraft.init.Items;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;

@Info(name = "FastExp", category = Category.COMBAT, description = "Removes XP Bottle Throw Delay")
public class FastExp extends Module
{
    @EventHandler
    private Listener<PacketEvent.Receive> receiveListener;
    
    public FastExp() {
        this.receiveListener = new Listener<PacketEvent.Receive>(event -> {
            if (FastExp.mc.field_71439_g != null && (FastExp.mc.field_71439_g.func_184614_ca().func_77973_b() == Items.field_151062_by || FastExp.mc.field_71439_g.func_184592_cb().func_77973_b() == Items.field_151062_by)) {
                FastExp.mc.field_71467_ac = 0;
            }
        }, (Predicate<PacketEvent.Receive>[])new Predicate[0]);
    }
}

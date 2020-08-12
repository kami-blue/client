// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumHand;
import me.zeroeightsix.kami.setting.Settings;
import java.util.Random;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AntiAFK", category = Category.MISC, description = "Moves in order not to get kicked. (May be invisible client-sided)")
public class AntiAFK extends Module
{
    private Setting<Boolean> swing;
    private Setting<Boolean> turn;
    private Random random;
    
    public AntiAFK() {
        this.swing = this.register(Settings.b("Swing", true));
        this.turn = this.register(Settings.b("Turn", true));
        this.random = new Random();
    }
    
    @Override
    public void onUpdate() {
        if (AntiAFK.mc.field_71442_b.func_181040_m()) {
            return;
        }
        if (AntiAFK.mc.field_71439_g.field_70173_aa % 40 == 0 && this.swing.getValue()) {
            AntiAFK.mc.func_147114_u().func_147297_a((Packet)new CPacketAnimation(EnumHand.MAIN_HAND));
        }
        if (AntiAFK.mc.field_71439_g.field_70173_aa % 15 == 0 && this.turn.getValue()) {
            AntiAFK.mc.field_71439_g.field_70177_z = (float)(this.random.nextInt(360) - 180);
        }
        if (!this.swing.getValue() && !this.turn.getValue() && AntiAFK.mc.field_71439_g.field_70173_aa % 80 == 0) {
            AntiAFK.mc.field_71439_g.func_70664_aZ();
        }
    }
}

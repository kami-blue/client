// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.dev;

import net.minecraft.client.entity.EntityPlayerSP;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "GMSpeed", category = Category.DEV, description = "Godmode Speed")
public class GMSpeed extends Module
{
    private Setting<Double> gmspeed;
    
    public GMSpeed() {
        this.gmspeed = this.register((Setting<Double>)Settings.doubleBuilder("Speed").withRange(0.1, 10.0).withValue(1.0).build());
    }
    
    @Override
    public void onUpdate() {
        if ((GMSpeed.mc.field_71439_g.field_191988_bg != 0.0f || GMSpeed.mc.field_71439_g.field_70702_br != 0.0f) && !GMSpeed.mc.field_71439_g.func_70093_af() && GMSpeed.mc.field_71439_g.field_70122_E) {
            final EntityPlayerSP field_71439_g = GMSpeed.mc.field_71439_g;
            field_71439_g.field_70159_w *= this.gmspeed.getValue();
            final EntityPlayerSP field_71439_g2 = GMSpeed.mc.field_71439_g;
            field_71439_g2.field_70179_y *= this.gmspeed.getValue();
        }
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.dev;

import me.zeroeightsix.kami.module.Module;

@Info(name = "GMFly", category = Category.DEV, description = "Godmode Fly")
public class GMFly extends Module
{
    public void onEnable() {
        this.toggleFly(true);
    }
    
    public void onDisable() {
        this.toggleFly(false);
    }
    
    @Override
    public void onUpdate() {
        this.toggleFly(true);
    }
    
    private void toggleFly(final boolean b) {
        GMFly.mc.field_71439_g.field_71075_bZ.field_75100_b = b;
        if (GMFly.mc.field_71439_g.field_71075_bZ.field_75098_d) {
            return;
        }
        GMFly.mc.field_71439_g.field_71075_bZ.field_75101_c = b;
    }
}

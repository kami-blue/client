// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.movement;

import net.minecraft.init.Blocks;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "IceSpeed", description = "Changes how slippery ice is", category = Category.MOVEMENT)
public class IceSpeed extends Module
{
    private Setting<Float> slipperiness;
    
    public IceSpeed() {
        this.slipperiness = this.register((Setting<Float>)Settings.floatBuilder("Slipperiness").withMinimum(0.2f).withValue(0.4f).withMaximum(1.0f).build());
    }
    
    @Override
    public void onUpdate() {
        Blocks.field_150432_aD.field_149765_K = this.slipperiness.getValue();
        Blocks.field_150403_cj.field_149765_K = this.slipperiness.getValue();
        Blocks.field_185778_de.field_149765_K = this.slipperiness.getValue();
    }
    
    public void onDisable() {
        Blocks.field_150432_aD.field_149765_K = 0.98f;
        Blocks.field_150403_cj.field_149765_K = 0.98f;
        Blocks.field_185778_de.field_149765_K = 0.98f;
    }
}

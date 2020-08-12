// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "FovSlider", category = Category.RENDER)
public class FovSlider extends Module
{
    private Setting<Float> fov;
    
    public FovSlider() {
        this.fov = this.register((Setting<Float>)Settings.floatBuilder("Value").withMinimum(0.0f).withValue(130.0f).withMaximum(170.0f).build());
    }
    
    @Override
    public void onUpdate() {
        FovSlider.mc.field_71474_y.field_74334_X = (float)Math.round(this.fov.getValue());
    }
}

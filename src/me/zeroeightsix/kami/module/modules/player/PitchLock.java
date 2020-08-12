// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.player;

import net.minecraft.util.math.MathHelper;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "PitchLock", category = Category.PLAYER, description = "Locks your camera yaw")
public class PitchLock extends Module
{
    private Setting<Boolean> auto;
    private Setting<Float> pitch;
    private Setting<Integer> slice;
    
    public PitchLock() {
        this.auto = this.register(Settings.b("Auto", true));
        this.pitch = this.register(Settings.f("Pitch", 180.0f));
        this.slice = this.register(Settings.i("Slice", 8));
    }
    
    @Override
    public void onUpdate() {
        if (this.slice.getValue() == 0) {
            return;
        }
        if (this.auto.getValue()) {
            final int angle = 360 / this.slice.getValue();
            float yaw = PitchLock.mc.field_71439_g.field_70125_A;
            yaw = (float)(Math.round(yaw / angle) * angle);
            PitchLock.mc.field_71439_g.field_70125_A = yaw;
            if (PitchLock.mc.field_71439_g.func_184218_aH()) {
                PitchLock.mc.field_71439_g.func_184187_bx().field_70125_A = yaw;
            }
        }
        else {
            PitchLock.mc.field_71439_g.field_70125_A = MathHelper.func_76131_a(this.pitch.getValue() - 180.0f, -180.0f, 180.0f);
        }
    }
}

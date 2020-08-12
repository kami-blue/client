// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.player;

import net.minecraft.util.math.MathHelper;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "YawLock", category = Category.PLAYER, description = "Locks your camera yaw")
public class YawLock extends Module
{
    private Setting<Boolean> auto;
    private Setting<Float> yaw;
    private Setting<Integer> slice;
    
    public YawLock() {
        this.auto = this.register(Settings.b("Auto", true));
        this.yaw = this.register(Settings.f("Yaw", 180.0f));
        this.slice = this.register(Settings.i("Slice", 8));
    }
    
    @Override
    public void onUpdate() {
        if (this.slice.getValue() == 0) {
            return;
        }
        if (this.auto.getValue()) {
            final int angle = 360 / this.slice.getValue();
            float yaw = YawLock.mc.field_71439_g.field_70177_z;
            yaw = (float)(Math.round(yaw / angle) * angle);
            YawLock.mc.field_71439_g.field_70177_z = yaw;
            if (YawLock.mc.field_71439_g.func_184218_aH()) {
                YawLock.mc.field_71439_g.func_184187_bx().field_70177_z = yaw;
            }
        }
        else {
            YawLock.mc.field_71439_g.field_70177_z = MathHelper.func_76131_a(this.yaw.getValue() - 180.0f, -180.0f, 180.0f);
        }
    }
}

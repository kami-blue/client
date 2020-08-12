// 
// Decompiled by Procyon v0.5.36
// 

package net.noblesix.hyperlethal.module.modules.movement;

import net.noblesix.hyperlethal.setting.Settings;
import net.noblesix.hyperlethal.setting.Setting;
import net.noblesix.hyperlethal.module.Module;

@Module.Info(category = Module.Category.MOVEMENT, description = "AirJump", name = "AirJump")
public class AirJump extends Module
{
    private Boolean owo;
    private Setting<Float> speed;
    private Setting<Float> movementspeed;
    
    public AirJump() {
        this.owo = false;
        this.speed = (Setting<Float>)this.register(Settings.f("Speed", 5.0f));
        this.movementspeed = (Setting<Float>)this.register(Settings.f("MoveSpeed", 10.0f));
    }
    
    protected void onEnable() {
        if (AirJump.mc.field_71439_g == null) {}
    }
    
    public void onUpdate() {
        AirJump.mc.field_71439_g.field_71075_bZ.field_75100_b = false;
        AirJump.mc.field_71439_g.field_70747_aH = (float)this.movementspeed.getValue() / 100.0f;
        if (AirJump.mc.field_71474_y.field_74314_A.func_151470_d()) {
            if (!this.owo) {
                AirJump.mc.field_71439_g.field_70181_x = (float)this.speed.getValue() / 10.0f;
                this.owo = true;
            }
        }
        else if (!AirJump.mc.field_71474_y.field_74314_A.func_151470_d()) {
            this.owo = false;
        }
    }
}

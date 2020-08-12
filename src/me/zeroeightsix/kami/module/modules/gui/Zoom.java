// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.gui;

import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Zoom", category = Category.GUI, description = "Configures FOV", showOnArray = ShowOnArray.OFF)
public class Zoom extends Module
{
    private float fov;
    private float sensi;
    private Setting<Integer> fovChange;
    private Setting<Float> sensChange;
    private Setting<Boolean> smoothCamera;
    private Setting<Boolean> sens;
    
    public Zoom() {
        this.fov = 0.0f;
        this.sensi = 0.0f;
        this.fovChange = this.register((Setting<Integer>)Settings.integerBuilder("FOV").withMinimum(30).withValue(30).withMaximum(150).build());
        this.sensChange = this.register((Setting<Float>)Settings.floatBuilder("Sensitivity").withMinimum(0.25f).withValue(1.3f).withMaximum(2.0f).build());
        this.smoothCamera = this.register(Settings.b("Cinematic Camera", true));
        this.sens = this.register(Settings.b("Sensitivity", true));
    }
    
    public void onEnable() {
        if (Zoom.mc.field_71439_g == null) {
            return;
        }
        this.fov = Zoom.mc.field_71474_y.field_74334_X;
        this.sensi = Zoom.mc.field_71474_y.field_74341_c;
        if (this.smoothCamera.getValue()) {
            Zoom.mc.field_71474_y.field_74326_T = true;
        }
    }
    
    public void onDisable() {
        Zoom.mc.field_71474_y.field_74334_X = this.fov;
        Zoom.mc.field_71474_y.field_74341_c = this.sensi;
        if (this.smoothCamera.getValue()) {
            Zoom.mc.field_71474_y.field_74326_T = false;
        }
    }
    
    @Override
    public void onUpdate() {
        if (Zoom.mc.field_71439_g == null) {
            return;
        }
        Zoom.mc.field_71474_y.field_74334_X = this.fovChange.getValue();
        Zoom.mc.field_71474_y.field_74326_T = this.smoothCamera.getValue();
        if (this.sens.getValue()) {
            Zoom.mc.field_71474_y.field_74341_c = this.sensi * this.sensChange.getValue();
        }
    }
}

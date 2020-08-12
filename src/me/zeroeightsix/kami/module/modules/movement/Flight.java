// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.movement;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.client.Minecraft;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(category = Category.MOVEMENT, description = "Makes the player fly", name = "Flight")
public class Flight extends Module
{
    private Setting<Float> speed;
    private Setting<FlightMode> mode;
    
    public Flight() {
        this.speed = this.register(Settings.f("Speed", 10.0f));
        this.mode = this.register(Settings.e("Mode", FlightMode.VANILLA));
    }
    
    @Override
    protected void onEnable() {
        if (Flight.mc.field_71439_g == null) {
            return;
        }
        switch (this.mode.getValue()) {
            case VANILLA: {
                Flight.mc.field_71439_g.field_71075_bZ.field_75100_b = true;
                if (Flight.mc.field_71439_g.field_71075_bZ.field_75098_d) {
                    return;
                }
                Flight.mc.field_71439_g.field_71075_bZ.field_75101_c = true;
                break;
            }
        }
    }
    
    @Override
    public void onUpdate() {
        switch (this.mode.getValue()) {
            case STATIC: {
                Flight.mc.field_71439_g.field_71075_bZ.field_75100_b = false;
                Flight.mc.field_71439_g.field_70159_w = 0.0;
                Flight.mc.field_71439_g.field_70181_x = 0.0;
                Flight.mc.field_71439_g.field_70179_y = 0.0;
                Flight.mc.field_71439_g.field_70747_aH = this.speed.getValue();
                if (Flight.mc.field_71474_y.field_74314_A.func_151470_d()) {
                    final EntityPlayerSP field_71439_g = Flight.mc.field_71439_g;
                    field_71439_g.field_70181_x += this.speed.getValue();
                }
                if (Flight.mc.field_71474_y.field_74311_E.func_151470_d()) {
                    final EntityPlayerSP field_71439_g2 = Flight.mc.field_71439_g;
                    field_71439_g2.field_70181_x -= this.speed.getValue();
                    break;
                }
                break;
            }
            case VANILLA: {
                Flight.mc.field_71439_g.field_71075_bZ.func_75092_a(this.speed.getValue() / 100.0f);
                Flight.mc.field_71439_g.field_71075_bZ.field_75100_b = true;
                if (Flight.mc.field_71439_g.field_71075_bZ.field_75098_d) {
                    return;
                }
                Flight.mc.field_71439_g.field_71075_bZ.field_75101_c = true;
                break;
            }
            case PACKET: {
                final boolean forward = Flight.mc.field_71474_y.field_74351_w.func_151470_d();
                final boolean left = Flight.mc.field_71474_y.field_74370_x.func_151470_d();
                final boolean right = Flight.mc.field_71474_y.field_74366_z.func_151470_d();
                final boolean back = Flight.mc.field_71474_y.field_74368_y.func_151470_d();
                int angle;
                if (left && right) {
                    angle = (forward ? 0 : (back ? 180 : -1));
                }
                else if (forward && back) {
                    angle = (left ? -90 : (right ? 90 : -1));
                }
                else {
                    angle = (left ? -90 : (right ? 90 : 0));
                    if (forward) {
                        angle /= 2;
                    }
                    else if (back) {
                        angle = 180 - angle / 2;
                    }
                }
                if (angle != -1 && (forward || left || right || back)) {
                    final float yaw = Flight.mc.field_71439_g.field_70177_z + angle;
                    Flight.mc.field_71439_g.field_70159_w = EntityUtil.getRelativeX(yaw) * 0.20000000298023224;
                    Flight.mc.field_71439_g.field_70179_y = EntityUtil.getRelativeZ(yaw) * 0.20000000298023224;
                }
                Flight.mc.field_71439_g.field_70181_x = 0.0;
                Flight.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.PositionRotation(Flight.mc.field_71439_g.field_70165_t + Flight.mc.field_71439_g.field_70159_w, Flight.mc.field_71439_g.field_70163_u + (Minecraft.func_71410_x().field_71474_y.field_74314_A.func_151470_d() ? 0.0622 : 0.0) - (Minecraft.func_71410_x().field_71474_y.field_74311_E.func_151470_d() ? 0.0622 : 0.0), Flight.mc.field_71439_g.field_70161_v + Flight.mc.field_71439_g.field_70179_y, Flight.mc.field_71439_g.field_70177_z, Flight.mc.field_71439_g.field_70125_A, false));
                Flight.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.PositionRotation(Flight.mc.field_71439_g.field_70165_t + Flight.mc.field_71439_g.field_70159_w, Flight.mc.field_71439_g.field_70163_u - 42069.0, Flight.mc.field_71439_g.field_70161_v + Flight.mc.field_71439_g.field_70179_y, Flight.mc.field_71439_g.field_70177_z, Flight.mc.field_71439_g.field_70125_A, true));
                break;
            }
        }
    }
    
    @Override
    protected void onDisable() {
        switch (this.mode.getValue()) {
            case VANILLA: {
                Flight.mc.field_71439_g.field_71075_bZ.field_75100_b = false;
                Flight.mc.field_71439_g.field_71075_bZ.func_75092_a(0.05f);
                if (Flight.mc.field_71439_g.field_71075_bZ.field_75098_d) {
                    return;
                }
                Flight.mc.field_71439_g.field_71075_bZ.field_75101_c = false;
                break;
            }
        }
    }
    
    public double[] moveLooking() {
        return new double[] { Flight.mc.field_71439_g.field_70177_z * 360.0f / 360.0f * 180.0f / 180.0f, 0.0 };
    }
    
    public enum FlightMode
    {
        VANILLA, 
        STATIC, 
        PACKET;
    }
}

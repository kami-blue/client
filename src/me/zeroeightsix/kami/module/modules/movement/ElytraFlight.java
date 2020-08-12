// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.movement;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.MathHelper;
import net.minecraft.network.Packet;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketEntityAction;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "ElytraFlight", description = "Allows infinite elytra flying", category = Category.MOVEMENT)
public class ElytraFlight extends Module
{
    private Setting<ElytraFlightMode> mode;
    private Setting<Boolean> highway;
    private Setting<Boolean> defaultSetting;
    private Setting<Float> speed;
    private Setting<Float> upSpeed;
    private Setting<Float> downSpeed;
    private Setting<Float> fallSpeedHighway;
    private Setting<Float> fallspeed;
    
    public ElytraFlight() {
        this.mode = this.register(Settings.e("Mode", ElytraFlightMode.FLY));
        this.highway = this.register(Settings.b("Highway Mode", false));
        this.defaultSetting = this.register(Settings.b("Defaults", false));
        this.speed = this.register(Settings.f("Speed Highway", 1.8f));
        this.upSpeed = this.register(Settings.f("Up Speed", 0.08f));
        this.downSpeed = this.register(Settings.f("Down Speed", 0.04f));
        this.fallSpeedHighway = this.register(Settings.f("Fall Speed Highway", 5.0000002E-5f));
        this.fallspeed = this.register(Settings.f("Fall Speed", -0.003f));
    }
    
    @Override
    public void onUpdate() {
        if (this.defaultSetting.getValue()) {
            this.speed.setValue(1.8f);
            this.fallSpeedHighway.setValue(5.0000002E-5f);
            this.defaultSetting.setValue(false);
            Command.sendChatMessage("[ElytraFlight] Set to defaults!");
        }
        if (this.highway.getValue()) {
            this.mode.setValue(ElytraFlightMode.FLY);
        }
        if (ElytraFlight.mc.field_71439_g.field_71075_bZ.field_75100_b) {
            if (this.highway.getValue()) {
                ElytraFlight.mc.field_71439_g.func_70016_h(0.0, 0.0, 0.0);
                ElytraFlight.mc.field_71439_g.func_70107_b(ElytraFlight.mc.field_71439_g.field_70165_t, ElytraFlight.mc.field_71439_g.field_70163_u - this.fallSpeedHighway.getValue(), ElytraFlight.mc.field_71439_g.field_70161_v);
                ElytraFlight.mc.field_71439_g.field_71075_bZ.func_75092_a((float)this.speed.getValue());
                ElytraFlight.mc.field_71439_g.func_70031_b(false);
            }
            else {
                ElytraFlight.mc.field_71439_g.func_70016_h(0.0, 0.0, 0.0);
                ElytraFlight.mc.field_71439_g.field_71075_bZ.func_75092_a(0.915f);
                ElytraFlight.mc.field_71439_g.func_70107_b(ElytraFlight.mc.field_71439_g.field_70165_t, ElytraFlight.mc.field_71439_g.field_70163_u - this.fallspeed.getValue(), ElytraFlight.mc.field_71439_g.field_70161_v);
            }
        }
        if (ElytraFlight.mc.field_71439_g.field_70122_E) {
            ElytraFlight.mc.field_71439_g.field_71075_bZ.field_75101_c = false;
        }
        if (!ElytraFlight.mc.field_71439_g.func_184613_cA()) {
            return;
        }
        switch (this.mode.getValue()) {
            case BOOST: {
                if (ElytraFlight.mc.field_71439_g.func_70090_H()) {
                    ElytraFlight.mc.func_147114_u().func_147297_a((Packet)new CPacketEntityAction((Entity)ElytraFlight.mc.field_71439_g, CPacketEntityAction.Action.START_FALL_FLYING));
                    return;
                }
                if (ElytraFlight.mc.field_71474_y.field_74314_A.func_151470_d()) {
                    final EntityPlayerSP field_71439_g = ElytraFlight.mc.field_71439_g;
                    field_71439_g.field_70181_x += this.upSpeed.getValue();
                }
                else if (ElytraFlight.mc.field_71474_y.field_74311_E.func_151470_d()) {
                    final EntityPlayerSP field_71439_g2 = ElytraFlight.mc.field_71439_g;
                    field_71439_g2.field_70181_x -= this.downSpeed.getValue();
                }
                if (ElytraFlight.mc.field_71474_y.field_74351_w.func_151470_d()) {
                    final float yaw = (float)Math.toRadians(ElytraFlight.mc.field_71439_g.field_70177_z);
                    final EntityPlayerSP field_71439_g3 = ElytraFlight.mc.field_71439_g;
                    field_71439_g3.field_70159_w -= MathHelper.func_76126_a(yaw) * 0.05f;
                    final EntityPlayerSP field_71439_g4 = ElytraFlight.mc.field_71439_g;
                    field_71439_g4.field_70179_y += MathHelper.func_76134_b(yaw) * 0.05f;
                    break;
                }
                if (ElytraFlight.mc.field_71474_y.field_74368_y.func_151470_d()) {
                    final float yaw = (float)Math.toRadians(ElytraFlight.mc.field_71439_g.field_70177_z);
                    final EntityPlayerSP field_71439_g5 = ElytraFlight.mc.field_71439_g;
                    field_71439_g5.field_70159_w += MathHelper.func_76126_a(yaw) * 0.05f;
                    final EntityPlayerSP field_71439_g6 = ElytraFlight.mc.field_71439_g;
                    field_71439_g6.field_70179_y -= MathHelper.func_76134_b(yaw) * 0.05f;
                    break;
                }
                break;
            }
            case FLY: {
                ElytraFlight.mc.field_71439_g.field_71075_bZ.func_75092_a(0.915f);
                ElytraFlight.mc.field_71439_g.field_71075_bZ.field_75100_b = true;
                if (ElytraFlight.mc.field_71439_g.field_71075_bZ.field_75098_d) {
                    return;
                }
                ElytraFlight.mc.field_71439_g.field_71075_bZ.field_75101_c = true;
                break;
            }
        }
    }
    
    @Override
    protected void onDisable() {
        ElytraFlight.mc.field_71439_g.field_71075_bZ.field_75100_b = false;
        ElytraFlight.mc.field_71439_g.field_71075_bZ.func_75092_a(0.05f);
        if (ElytraFlight.mc.field_71439_g.field_71075_bZ.field_75098_d) {
            return;
        }
        ElytraFlight.mc.field_71439_g.field_71075_bZ.field_75101_c = false;
    }
    
    private enum ElytraFlightMode
    {
        BOOST, 
        FLY;
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.movement;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.MathHelper;
import net.minecraft.network.Packet;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketEntityAction;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Elytra+", description = "Allows infinite elytra flying", category = Category.MOVEMENT)
public class ElytraPlus extends Module
{
    private Setting<ElytraFlightMode> mode;
    
    public ElytraPlus() {
        this.mode = this.register(Settings.e("Mode", ElytraFlightMode.BOOST));
    }
    
    @Override
    public void onUpdate() {
        if (ElytraPlus.mc.field_71439_g.field_71075_bZ.field_75100_b) {
            ElytraPlus.mc.field_71439_g.func_70016_h(0.0, -0.003, 0.0);
            ElytraPlus.mc.field_71439_g.field_71075_bZ.func_75092_a(0.915f);
        }
        if (ElytraPlus.mc.field_71439_g.field_70122_E) {
            ElytraPlus.mc.field_71439_g.field_71075_bZ.field_75101_c = false;
        }
        if (!ElytraPlus.mc.field_71439_g.func_184613_cA()) {
            return;
        }
        switch (this.mode.getValue()) {
            case BOOST: {
                if (ElytraPlus.mc.field_71439_g.func_70090_H()) {
                    ElytraPlus.mc.func_147114_u().func_147297_a((Packet)new CPacketEntityAction((Entity)ElytraPlus.mc.field_71439_g, CPacketEntityAction.Action.START_FALL_FLYING));
                    return;
                }
                if (ElytraPlus.mc.field_71474_y.field_74314_A.func_151470_d()) {
                    final EntityPlayerSP field_71439_g = ElytraPlus.mc.field_71439_g;
                    field_71439_g.field_70181_x += 0.08;
                }
                else if (ElytraPlus.mc.field_71474_y.field_74311_E.func_151470_d()) {
                    final EntityPlayerSP field_71439_g2 = ElytraPlus.mc.field_71439_g;
                    field_71439_g2.field_70181_x -= 0.04;
                }
                if (ElytraPlus.mc.field_71474_y.field_74351_w.func_151470_d()) {
                    final float yaw = (float)Math.toRadians(ElytraPlus.mc.field_71439_g.field_70177_z);
                    final EntityPlayerSP field_71439_g3 = ElytraPlus.mc.field_71439_g;
                    field_71439_g3.field_70159_w -= MathHelper.func_76126_a(yaw) * 0.05f;
                    final EntityPlayerSP field_71439_g4 = ElytraPlus.mc.field_71439_g;
                    field_71439_g4.field_70179_y += MathHelper.func_76134_b(yaw) * 0.05f;
                    break;
                }
                if (ElytraPlus.mc.field_71474_y.field_74368_y.func_151470_d()) {
                    final float yaw = (float)Math.toRadians(ElytraPlus.mc.field_71439_g.field_70177_z);
                    final EntityPlayerSP field_71439_g5 = ElytraPlus.mc.field_71439_g;
                    field_71439_g5.field_70159_w += MathHelper.func_76126_a(yaw) * 0.05f;
                    final EntityPlayerSP field_71439_g6 = ElytraPlus.mc.field_71439_g;
                    field_71439_g6.field_70179_y -= MathHelper.func_76134_b(yaw) * 0.05f;
                    break;
                }
                break;
            }
            case FLY: {
                ElytraPlus.mc.field_71439_g.field_71075_bZ.func_75092_a(0.915f);
                ElytraPlus.mc.field_71439_g.field_71075_bZ.field_75100_b = true;
                if (ElytraPlus.mc.field_71439_g.field_71075_bZ.field_75098_d) {
                    return;
                }
                ElytraPlus.mc.field_71439_g.field_71075_bZ.field_75101_c = true;
                break;
            }
        }
    }
    
    @Override
    protected void onDisable() {
        ElytraPlus.mc.field_71439_g.field_71075_bZ.field_75100_b = false;
        ElytraPlus.mc.field_71439_g.field_71075_bZ.func_75092_a(0.05f);
        if (ElytraPlus.mc.field_71439_g.field_71075_bZ.field_75098_d) {
            return;
        }
        ElytraPlus.mc.field_71439_g.field_71075_bZ.field_75101_c = false;
    }
    
    private enum ElytraFlightMode
    {
        BOOST, 
        FLY;
    }
}

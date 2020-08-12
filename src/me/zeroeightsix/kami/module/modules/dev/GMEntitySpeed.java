// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.dev;

import net.minecraft.util.MovementInput;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.Entity;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "GMEntitySpeed", category = Category.DEV, description = "Godmode EntitySpeed")
public class GMEntitySpeed extends Module
{
    private Setting<Double> gmentityspeed;
    
    public GMEntitySpeed() {
        this.gmentityspeed = this.register((Setting<Double>)Settings.doubleBuilder("Speed").withRange(0.1, 10.0).withValue(1.0).build());
    }
    
    private static void speedEntity(final Entity entity, final Double speed) {
        if (entity instanceof EntityLlama) {
            entity.field_70177_z = GMEntitySpeed.mc.field_71439_g.field_70177_z;
            ((EntityLlama)entity).field_70759_as = GMEntitySpeed.mc.field_71439_g.field_70759_as;
        }
        final MovementInput movementInput = GMEntitySpeed.mc.field_71439_g.field_71158_b;
        double forward = movementInput.field_192832_b;
        double strafe = movementInput.field_78902_a;
        float yaw = GMEntitySpeed.mc.field_71439_g.field_70177_z;
        if (forward == 0.0 && strafe == 0.0) {
            entity.field_70159_w = 0.0;
            entity.field_70179_y = 0.0;
        }
        else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += ((forward > 0.0) ? -45 : 45);
                }
                else if (strafe < 0.0) {
                    yaw += ((forward > 0.0) ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                }
                else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            entity.field_70159_w = forward * speed * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0f));
            entity.field_70179_y = forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0f));
            if (entity instanceof EntityMinecart) {
                final EntityMinecart em = (EntityMinecart)entity;
                em.func_70016_h(forward * speed * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0f)), em.field_70181_x, forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0f)));
            }
        }
    }
    
    @Override
    public void onUpdate() {
        try {
            if (GMEntitySpeed.mc.field_71439_g.func_184187_bx() != null) {
                speedEntity(GMEntitySpeed.mc.field_71439_g.func_184187_bx(), this.gmentityspeed.getValue());
            }
        }
        catch (Exception e) {
            System.out.println("ERROR: Dude we kinda have a problem here:");
            e.printStackTrace();
        }
    }
}

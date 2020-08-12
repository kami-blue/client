// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.movement;

import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.util.MovementInput;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityPig;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "EntitySpeed", category = Category.MOVEMENT, description = "Abuse client-sided movement to shape sound barrier breaking rideables")
public class EntitySpeed extends Module
{
    private Setting<Float> speed;
    private Setting<Boolean> antiStuck;
    private Setting<Boolean> flight;
    private Setting<Boolean> wobble;
    private static Setting<Float> opacity;
    
    public EntitySpeed() {
        this.speed = this.register(Settings.f("Speed", 1.0f));
        this.antiStuck = this.register(Settings.b("AntiStuck"));
        this.flight = this.register(Settings.b("Flight", false));
        this.wobble = this.register(Settings.booleanBuilder("Wobble").withValue(true).withVisibility(b -> this.flight.getValue()).build());
        this.register(EntitySpeed.opacity);
    }
    
    @Override
    public void onUpdate() {
        if (EntitySpeed.mc.field_71441_e != null && EntitySpeed.mc.field_71439_g.func_184187_bx() != null) {
            final Entity riding = EntitySpeed.mc.field_71439_g.func_184187_bx();
            if (riding instanceof EntityPig || riding instanceof AbstractHorse) {
                this.steerEntity(riding);
            }
            else if (riding instanceof EntityBoat) {
                this.steerBoat(this.getBoat());
            }
        }
    }
    
    private void steerEntity(final Entity entity) {
        if (!this.flight.getValue()) {
            entity.field_70181_x = -0.4;
        }
        if (this.flight.getValue()) {
            if (EntitySpeed.mc.field_71474_y.field_74314_A.func_151470_d()) {
                entity.field_70181_x = this.speed.getValue();
            }
            else if (EntitySpeed.mc.field_71474_y.field_74351_w.func_151470_d() || EntitySpeed.mc.field_71474_y.field_74368_y.func_151470_d()) {
                entity.field_70181_x = (this.wobble.getValue() ? Math.sin(EntitySpeed.mc.field_71439_g.field_70173_aa) : 0.0);
            }
        }
        this.moveForward(entity, this.speed.getValue() * 3.8);
        if (entity instanceof EntityHorse) {
            entity.field_70177_z = EntitySpeed.mc.field_71439_g.field_70177_z;
        }
    }
    
    private void steerBoat(final EntityBoat boat) {
        if (boat == null) {
            return;
        }
        final boolean forward = EntitySpeed.mc.field_71474_y.field_74351_w.func_151470_d();
        final boolean left = EntitySpeed.mc.field_71474_y.field_74370_x.func_151470_d();
        final boolean right = EntitySpeed.mc.field_71474_y.field_74366_z.func_151470_d();
        final boolean back = EntitySpeed.mc.field_71474_y.field_74368_y.func_151470_d();
        if (!forward || !back) {
            boat.field_70181_x = 0.0;
        }
        if (EntitySpeed.mc.field_71474_y.field_74314_A.func_151470_d()) {
            boat.field_70181_x += this.speed.getValue() / 2.0f;
        }
        if (!forward && !left && !right && !back) {
            return;
        }
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
        if (angle == -1) {
            return;
        }
        final float yaw = EntitySpeed.mc.field_71439_g.field_70177_z + angle;
        boat.field_70159_w = EntityUtil.getRelativeX(yaw) * this.speed.getValue();
        boat.field_70179_y = EntityUtil.getRelativeZ(yaw) * this.speed.getValue();
    }
    
    @Override
    public void onRender() {
        final EntityBoat boat = this.getBoat();
        if (boat == null) {
            return;
        }
        boat.field_70177_z = EntitySpeed.mc.field_71439_g.field_70177_z;
        boat.func_184442_a(false, false, false, false);
    }
    
    private EntityBoat getBoat() {
        if (EntitySpeed.mc.field_71439_g.func_184187_bx() != null && EntitySpeed.mc.field_71439_g.func_184187_bx() instanceof EntityBoat) {
            return (EntityBoat)EntitySpeed.mc.field_71439_g.func_184187_bx();
        }
        return null;
    }
    
    private void moveForward(final Entity entity, final double speed) {
        if (entity != null) {
            final MovementInput movementInput = EntitySpeed.mc.field_71439_g.field_71158_b;
            double forward = movementInput.field_192832_b;
            double strafe = movementInput.field_78902_a;
            final boolean movingForward = forward != 0.0;
            final boolean movingStrafe = strafe != 0.0;
            float yaw = EntitySpeed.mc.field_71439_g.field_70177_z;
            if (!movingForward && !movingStrafe) {
                this.setEntitySpeed(entity, 0.0, 0.0);
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
                    else {
                        forward = -1.0;
                    }
                }
                double motX = forward * speed * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0f));
                double motZ = forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0f));
                if (this.isBorderingChunk(entity, motX, motZ)) {
                    motZ = (motX = 0.0);
                }
                this.setEntitySpeed(entity, motX, motZ);
            }
        }
    }
    
    private void setEntitySpeed(final Entity entity, final double motX, final double motZ) {
        entity.field_70159_w = motX;
        entity.field_70179_y = motZ;
    }
    
    private boolean isBorderingChunk(final Entity entity, final double motX, final double motZ) {
        return this.antiStuck.getValue() && EntitySpeed.mc.field_71441_e.func_72964_e((int)(entity.field_70165_t + motX) >> 4, (int)(entity.field_70161_v + motZ) >> 4) instanceof EmptyChunk;
    }
    
    public static float getOpacity() {
        return EntitySpeed.opacity.getValue();
    }
    
    static {
        EntitySpeed.opacity = Settings.f("Boat opacity", 0.5f);
    }
}

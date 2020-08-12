// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util.other;

import java.math.RoundingMode;
import java.math.BigDecimal;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;

public final class MathUtil
{
    public static Vec3d interpolateEntity(final Entity entity, final float time) {
        return new Vec3d(entity.field_70142_S + (entity.field_70165_t - entity.field_70142_S) * time, entity.field_70137_T + (entity.field_70163_u - entity.field_70137_T) * time, entity.field_70136_U + (entity.field_70161_v - entity.field_70136_U) * time);
    }
    
    public static double radToDeg(final double rad) {
        return rad * 57.295780181884766;
    }
    
    public static double degToRad(final double deg) {
        return deg * 0.01745329238474369;
    }
    
    public static Vec3d direction(final float yaw) {
        return new Vec3d(Math.cos(degToRad(yaw + 90.0f)), 0.0, Math.sin(degToRad(yaw + 90.0f)));
    }
    
    public static float[] calcAngle(final Vec3d from, final Vec3d to) {
        final double difX = to.field_72450_a - from.field_72450_a;
        final double difY = (to.field_72448_b - from.field_72448_b) * -1.0;
        final double difZ = to.field_72449_c - from.field_72449_c;
        final double dist = MathHelper.func_76133_a(difX * difX + difZ * difZ);
        return new float[] { (float)MathHelper.func_76138_g(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0), (float)MathHelper.func_76138_g(Math.toDegrees(Math.atan2(difY, dist))) };
    }
    
    public static double[] directionSpeed(final double speed) {
        final Minecraft mc = Minecraft.func_71410_x();
        float forward = mc.field_71439_g.field_71158_b.field_192832_b;
        float side = mc.field_71439_g.field_71158_b.field_78902_a;
        float yaw = mc.field_71439_g.field_70126_B + (mc.field_71439_g.field_70177_z - mc.field_71439_g.field_70126_B) * mc.func_184121_ak();
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
            }
            else if (side < 0.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            }
            else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        final double posX = forward * speed * Math.cos(Math.toRadians(yaw + 90.0f)) + side * speed * Math.sin(Math.toRadians(yaw + 90.0f));
        final double posZ = forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) - side * speed * Math.cos(Math.toRadians(yaw + 90.0f));
        return new double[] { posX, posZ };
    }
    
    public static Vec3d mult(final Vec3d factor, final Vec3d multiplier) {
        return new Vec3d(factor.field_72450_a * multiplier.field_72450_a, factor.field_72448_b * multiplier.field_72448_b, factor.field_72449_c * multiplier.field_72449_c);
    }
    
    public static Vec3d mult(final Vec3d factor, final float multiplier) {
        return new Vec3d(factor.field_72450_a * multiplier, factor.field_72448_b * multiplier, factor.field_72449_c * multiplier);
    }
    
    public static Vec3d div(final Vec3d factor, final Vec3d divisor) {
        return new Vec3d(factor.field_72450_a / divisor.field_72450_a, factor.field_72448_b / divisor.field_72448_b, factor.field_72449_c / divisor.field_72449_c);
    }
    
    public static Vec3d div(final Vec3d factor, final float divisor) {
        return new Vec3d(factor.field_72450_a / divisor, factor.field_72448_b / divisor, factor.field_72449_c / divisor);
    }
    
    public static double round(final double value, final int places) {
        if (places < 0) {
            return value;
        }
        return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
    
    public static float clamp(float val, final float min, final float max) {
        if (val <= min) {
            val = min;
        }
        if (val >= max) {
            val = max;
        }
        return val;
    }
}

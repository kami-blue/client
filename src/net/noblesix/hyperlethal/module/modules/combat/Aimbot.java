// 
// Decompiled by Procyon v0.5.36
// 

package net.noblesix.hyperlethal.module.modules.combat;

import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.noblesix.hyperlethal.module.Module;

@Module.Info(name = "Aimbot", description = "null", category = Module.Category.COMBAT)
public class Aimbot extends Module
{
    public void onUpdate() {
        final List list = Aimbot.mc.field_71441_e.field_73010_i;
        for (int k = 0; k < list.size(); ++k) {
            if (list.get(k).func_70005_c_() != Aimbot.mc.field_71439_g.func_70005_c_()) {
                EntityPlayer entPlayer = list.get(k);
                if (Aimbot.mc.field_71439_g.func_70032_d((Entity)entPlayer) > Aimbot.mc.field_71439_g.func_70032_d((Entity)list.get(k))) {
                    entPlayer = list.get(k);
                }
                final float f = Aimbot.mc.field_71439_g.func_70032_d((Entity)entPlayer);
                if (f < 6.0f && Aimbot.mc.field_71439_g.func_70685_l((Entity)entPlayer)) {
                    faceEntity((Entity)entPlayer);
                }
            }
        }
    }
    
    public static synchronized void faceEntity(final Entity entity) {
        final float[] rotations = getRotationsNeeded(entity);
        if (rotations != null) {
            Aimbot.mc.field_71439_g.field_70177_z = rotations[0];
            Aimbot.mc.field_71439_g.field_70125_A = rotations[1] + 1.0f;
        }
    }
    
    public static float[] getRotationsNeeded(final Entity entity) {
        if (entity == null) {
            return null;
        }
        final double diffX = entity.field_70165_t - Aimbot.mc.field_71439_g.field_70165_t;
        final double diffZ = entity.field_70161_v - Aimbot.mc.field_71439_g.field_70161_v;
        double diffY;
        if (entity instanceof EntityPlayer) {
            final EntityPlayer entityLivingBase = (EntityPlayer)entity;
            diffY = entityLivingBase.field_70163_u + entityLivingBase.func_70047_e() - (Aimbot.mc.field_71439_g.field_70163_u + Aimbot.mc.field_71439_g.func_70047_e());
        }
        else if (entity instanceof EntityMob) {
            final EntityMob entityLivingBase2 = (EntityMob)entity;
            diffY = (entity.field_70121_D.field_72338_b + entity.field_70121_D.field_72337_e) / 2.0 - (Aimbot.mc.field_71439_g.field_70163_u + Aimbot.mc.field_71439_g.func_70047_e());
        }
        else {
            final EntityLivingBase entityLivingBase3 = (EntityLivingBase)entity;
            diffY = (entity.field_70121_D.field_72338_b + entity.field_70121_D.field_72337_e) / 2.0 - (Aimbot.mc.field_71439_g.field_70163_u + Aimbot.mc.field_71439_g.func_70047_e());
        }
        final double dist = MathHelper.func_76133_a(diffX * diffX + diffZ * diffZ);
        final float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / 3.141592653589793) - 90.0f;
        final float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / 3.141592653589793));
        return new float[] { Aimbot.mc.field_71439_g.field_70177_z + MathHelper.func_76142_g(yaw - Aimbot.mc.field_71439_g.field_70177_z), Aimbot.mc.field_71439_g.field_70125_A + MathHelper.func_76142_g(pitch - Aimbot.mc.field_71439_g.field_70125_A) };
    }
}

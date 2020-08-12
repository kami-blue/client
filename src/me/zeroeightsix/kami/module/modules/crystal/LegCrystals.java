// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.crystal;

import net.minecraft.block.Block;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.init.Blocks;
import java.util.Iterator;
import net.minecraft.entity.Entity;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.init.Items;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "LegCrystals", category = Category.CRYSTAL)
public class LegCrystals extends Module
{
    private Setting<Double> range;
    private Setting<Boolean> autoEnableCrystalHit;
    private boolean switchCooldown;
    
    public LegCrystals() {
        this.range = this.register((Setting<Double>)Settings.doubleBuilder("Range").withMinimum(1.0).withValue(5.5).withMaximum(10.0).build());
        this.autoEnableCrystalHit = this.register(Settings.b("Auto enable Crystal Hit", false));
        this.switchCooldown = false;
    }
    
    public void onEnable() {
        if (this.autoEnableCrystalHit.getValue()) {
            ModuleManager.getModuleByName("CrystalHit").enable();
        }
    }
    
    public void onDisable() {
        if (this.autoEnableCrystalHit.getValue()) {
            ModuleManager.getModuleByName("CrystalHit").disable();
        }
    }
    
    @Override
    public void onUpdate() {
        if (LegCrystals.mc.field_71439_g == null) {
            return;
        }
        int crystalSlot = -1;
        if (LegCrystals.mc.field_71439_g.func_184614_ca().func_77973_b() == Items.field_185158_cP) {
            crystalSlot = LegCrystals.mc.field_71439_g.field_71071_by.field_70461_c;
        }
        else {
            for (int slot = 0; slot < 9; ++slot) {
                if (LegCrystals.mc.field_71439_g.field_71071_by.func_70301_a(slot).func_77973_b() == Items.field_185158_cP) {
                    crystalSlot = slot;
                    break;
                }
            }
        }
        if (crystalSlot == -1) {
            return;
        }
        final EntityPlayer closestTarget = this.findClosestTarget();
        if (closestTarget == null) {
            return;
        }
        final Vec3d targetVector = this.findPlaceableBlock(closestTarget.func_174791_d().func_72441_c(0.0, -1.0, 0.0));
        if (targetVector == null) {
            return;
        }
        final BlockPos targetBlock = new BlockPos(targetVector);
        if (LegCrystals.mc.field_71439_g.field_71071_by.field_70461_c != crystalSlot) {
            LegCrystals.mc.field_71439_g.field_71071_by.field_70461_c = crystalSlot;
            this.switchCooldown = true;
            return;
        }
        if (this.switchCooldown) {
            this.switchCooldown = false;
            return;
        }
        LegCrystals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayerTryUseItemOnBlock(targetBlock, EnumFacing.UP, EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
    }
    
    private Vec3d findPlaceableBlock(final Vec3d startPos) {
        if (this.canPlaceCrystal(startPos.func_178787_e(Offsets.NORTH2)) && !this.isExplosionProof(startPos.func_178787_e(Offsets.NORTH1).func_72441_c(0.0, 1.0, 0.0))) {
            return startPos.func_178787_e(Offsets.NORTH2);
        }
        if (this.canPlaceCrystal(startPos.func_178787_e(Offsets.NORTH1))) {
            return startPos.func_178787_e(Offsets.NORTH1);
        }
        if (this.canPlaceCrystal(startPos.func_178787_e(Offsets.EAST2)) && !this.isExplosionProof(startPos.func_178787_e(Offsets.EAST1).func_72441_c(0.0, 1.0, 0.0))) {
            return startPos.func_178787_e(Offsets.EAST2);
        }
        if (this.canPlaceCrystal(startPos.func_178787_e(Offsets.EAST1))) {
            return startPos.func_178787_e(Offsets.EAST1);
        }
        if (this.canPlaceCrystal(startPos.func_178787_e(Offsets.SOUTH2)) && !this.isExplosionProof(startPos.func_178787_e(Offsets.SOUTH1).func_72441_c(0.0, 1.0, 0.0))) {
            return startPos.func_178787_e(Offsets.SOUTH2);
        }
        if (this.canPlaceCrystal(startPos.func_178787_e(Offsets.SOUTH1))) {
            return startPos.func_178787_e(Offsets.SOUTH1);
        }
        if (this.canPlaceCrystal(startPos.func_178787_e(Offsets.WEST2)) && !this.isExplosionProof(startPos.func_178787_e(Offsets.WEST1).func_72441_c(0.0, 1.0, 0.0))) {
            return startPos.func_178787_e(Offsets.WEST2);
        }
        if (this.canPlaceCrystal(startPos.func_178787_e(Offsets.WEST1))) {
            return startPos.func_178787_e(Offsets.WEST1);
        }
        return null;
    }
    
    private EntityPlayer findClosestTarget() {
        EntityPlayer closestTarget = null;
        for (final EntityPlayer target : LegCrystals.mc.field_71441_e.field_73010_i) {
            if (target != LegCrystals.mc.field_71439_g && !Friends.isFriend(target.func_70005_c_()) && EntityUtil.isLiving((Entity)target) && target.func_110143_aJ() > 0.0f) {
                if (LegCrystals.mc.field_71439_g.func_70032_d((Entity)target) > this.range.getValue()) {
                    continue;
                }
                if (closestTarget == null) {
                    closestTarget = target;
                }
                else {
                    if (LegCrystals.mc.field_71439_g.func_70032_d((Entity)target) >= LegCrystals.mc.field_71439_g.func_70032_d((Entity)closestTarget)) {
                        continue;
                    }
                    closestTarget = target;
                }
            }
        }
        return closestTarget;
    }
    
    private boolean canPlaceCrystal(final Vec3d vec3d) {
        final BlockPos blockPos = new BlockPos(vec3d.field_72450_a, vec3d.field_72448_b, vec3d.field_72449_c);
        final BlockPos boost = blockPos.func_177982_a(0, 1, 0);
        final BlockPos boost2 = blockPos.func_177982_a(0, 2, 0);
        return (LegCrystals.mc.field_71441_e.func_180495_p(blockPos).func_177230_c() == Blocks.field_150357_h || LegCrystals.mc.field_71441_e.func_180495_p(blockPos).func_177230_c() == Blocks.field_150343_Z) && LegCrystals.mc.field_71441_e.func_180495_p(boost).func_177230_c() == Blocks.field_150350_a && LegCrystals.mc.field_71441_e.func_180495_p(boost2).func_177230_c() == Blocks.field_150350_a && LegCrystals.mc.field_71441_e.func_72872_a((Class)Entity.class, new AxisAlignedBB(boost)).isEmpty() && LegCrystals.mc.field_71441_e.func_72872_a((Class)Entity.class, new AxisAlignedBB(boost2)).isEmpty();
    }
    
    private boolean isExplosionProof(final Vec3d vec3d) {
        final BlockPos blockPos = new BlockPos(vec3d.field_72450_a, vec3d.field_72448_b, vec3d.field_72449_c);
        final Block block = LegCrystals.mc.field_71441_e.func_180495_p(blockPos).func_177230_c();
        return block == Blocks.field_150357_h || block == Blocks.field_150343_Z || block == Blocks.field_150467_bQ || block == Blocks.field_150477_bB || block == Blocks.field_180401_cv;
    }
    
    private static class Offsets
    {
        private static final Vec3d NORTH1;
        private static final Vec3d NORTH2;
        private static final Vec3d EAST1;
        private static final Vec3d EAST2;
        private static final Vec3d SOUTH1;
        private static final Vec3d SOUTH2;
        private static final Vec3d WEST1;
        private static final Vec3d WEST2;
        
        static {
            NORTH1 = new Vec3d(0.0, 0.0, -1.0);
            NORTH2 = new Vec3d(0.0, 0.0, -2.0);
            EAST1 = new Vec3d(1.0, 0.0, 0.0);
            EAST2 = new Vec3d(2.0, 0.0, 0.0);
            SOUTH1 = new Vec3d(0.0, 0.0, 1.0);
            SOUTH2 = new Vec3d(0.0, 0.0, 2.0);
            WEST1 = new Vec3d(-1.0, 0.0, 0.0);
            WEST2 = new Vec3d(-2.0, 0.0, 0.0);
        }
    }
}

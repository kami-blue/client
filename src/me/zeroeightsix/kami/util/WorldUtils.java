// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.Minecraft;

public class WorldUtils
{
    static Minecraft mc;
    
    public static void placeBlockMainHand(final BlockPos pos) {
        placeBlock(EnumHand.MAIN_HAND, pos);
    }
    
    public static void placeBlock(final EnumHand hand, final BlockPos pos) {
        final Vec3d eyesPos = new Vec3d(WorldUtils.mc.field_71439_g.field_70165_t, WorldUtils.mc.field_71439_g.field_70163_u + WorldUtils.mc.field_71439_g.func_70047_e(), WorldUtils.mc.field_71439_g.field_70161_v);
        for (final EnumFacing side : EnumFacing.values()) {
            final BlockPos neighbor = pos.func_177972_a(side);
            final EnumFacing side2 = side.func_176734_d();
            if (WorldUtils.mc.field_71441_e.func_180495_p(neighbor).func_177230_c().func_176209_a(WorldUtils.mc.field_71441_e.func_180495_p(neighbor), false)) {
                final Vec3d hitVec = new Vec3d((Vec3i)neighbor).func_72441_c(0.5, 0.5, 0.5).func_178787_e(new Vec3d(side2.func_176730_m()).func_186678_a(0.5));
                if (eyesPos.func_72436_e(hitVec) <= 18.0625) {
                    final double diffX = hitVec.field_72450_a - eyesPos.field_72450_a;
                    final double diffY = hitVec.field_72448_b - eyesPos.field_72448_b;
                    final double diffZ = hitVec.field_72449_c - eyesPos.field_72449_c;
                    final double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
                    final float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
                    final float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
                    final float[] rotations = { WorldUtils.mc.field_71439_g.field_70177_z + MathHelper.func_76142_g(yaw - WorldUtils.mc.field_71439_g.field_70177_z), WorldUtils.mc.field_71439_g.field_70125_A + MathHelper.func_76142_g(pitch - WorldUtils.mc.field_71439_g.field_70125_A) };
                    WorldUtils.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Rotation(rotations[0], rotations[1], WorldUtils.mc.field_71439_g.field_70122_E));
                    WorldUtils.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketEntityAction((Entity)WorldUtils.mc.field_71439_g, CPacketEntityAction.Action.START_SNEAKING));
                    WorldUtils.mc.field_71442_b.func_187099_a(WorldUtils.mc.field_71439_g, WorldUtils.mc.field_71441_e, neighbor, side2, hitVec, hand);
                    WorldUtils.mc.field_71439_g.func_184609_a(hand);
                    WorldUtils.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketEntityAction((Entity)WorldUtils.mc.field_71439_g, CPacketEntityAction.Action.STOP_SNEAKING));
                    return;
                }
            }
        }
    }
    
    public static int findBlock(final Block block) {
        return findItem(new ItemStack(block).func_77973_b());
    }
    
    public static int findItem(final Item item) {
        try {
            for (int i = 0; i < 9; ++i) {
                final ItemStack stack = WorldUtils.mc.field_71439_g.field_71071_by.func_70301_a(i);
                if (item == stack.func_77973_b()) {
                    return i;
                }
            }
        }
        catch (Exception ex) {}
        return -1;
    }
    
    public static double[] calculateLookAt(final double px, final double py, final double pz, final EntityPlayer me) {
        double dirx = me.field_70165_t - px;
        double diry = me.field_70163_u - py;
        double dirz = me.field_70161_v - pz;
        final double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);
        dirx /= len;
        diry /= len;
        dirz /= len;
        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);
        pitch = pitch * 180.0 / 3.141592653589793;
        yaw = yaw * 180.0 / 3.141592653589793;
        yaw += 90.0;
        return new double[] { yaw, pitch };
    }
    
    public static void rotate(final float yaw, final float pitch) {
        WorldUtils.mc.field_71439_g.field_70177_z = yaw;
        WorldUtils.mc.field_71439_g.field_70125_A = pitch;
    }
    
    public static void rotate(final double[] rotations) {
        WorldUtils.mc.field_71439_g.field_70177_z = (float)rotations[0];
        WorldUtils.mc.field_71439_g.field_70125_A = (float)rotations[1];
    }
    
    public static void lookAtBlock(final BlockPos blockToLookAt) {
        rotate(calculateLookAt(blockToLookAt.func_177958_n(), blockToLookAt.func_177956_o(), blockToLookAt.func_177952_p(), (EntityPlayer)WorldUtils.mc.field_71439_g));
    }
    
    public static BlockPos getRelativeBlockPos(final EntityPlayer player, final int ChangeX, final int ChangeY, final int ChangeZ) {
        final int[] playerCoords = { (int)player.field_70165_t, (int)player.field_70163_u, (int)player.field_70161_v };
        BlockPos pos;
        if (player.field_70165_t < 0.0 && player.field_70161_v < 0.0) {
            pos = new BlockPos(playerCoords[0] + ChangeX - 1, playerCoords[1] + ChangeY, playerCoords[2] + ChangeZ - 1);
        }
        else if (player.field_70165_t < 0.0 && player.field_70161_v > 0.0) {
            pos = new BlockPos(playerCoords[0] + ChangeX - 1, playerCoords[1] + ChangeY, playerCoords[2] + ChangeZ);
        }
        else if (player.field_70165_t > 0.0 && player.field_70161_v < 0.0) {
            pos = new BlockPos(playerCoords[0] + ChangeX, playerCoords[1] + ChangeY, playerCoords[2] + ChangeZ - 1);
        }
        else {
            pos = new BlockPos(playerCoords[0] + ChangeX, playerCoords[1] + ChangeY, playerCoords[2] + ChangeZ);
        }
        return pos;
    }
    
    static {
        WorldUtils.mc = Minecraft.func_71410_x();
    }
}

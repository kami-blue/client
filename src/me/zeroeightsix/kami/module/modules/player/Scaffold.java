// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.player;

import net.minecraft.util.math.MathHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockContainer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import me.zeroeightsix.kami.setting.builder.SettingBuilder;
import me.zeroeightsix.kami.setting.Settings;
import java.util.Arrays;
import net.minecraft.init.Blocks;
import me.zeroeightsix.kami.setting.Setting;
import net.minecraft.block.Block;
import java.util.List;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Scaffold", category = Category.PLAYER)
public class Scaffold extends Module
{
    private List<Block> blackList;
    private Setting<Integer> future;
    
    public Scaffold() {
        this.blackList = Arrays.asList(Blocks.field_150477_bB, (Block)Blocks.field_150486_ae, Blocks.field_150447_bR);
        this.future = this.register(Settings.integerBuilder("Ticks").withMinimum(0).withMaximum(60).withValue(2));
    }
    
    private boolean hasNeighbour(final BlockPos blockPos) {
        for (final EnumFacing side : EnumFacing.values()) {
            final BlockPos neighbour = blockPos.func_177972_a(side);
            if (!Wrapper.getWorld().func_180495_p(neighbour).func_185904_a().func_76222_j()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void onUpdate() {
        if (this.isDisabled() || Scaffold.mc.field_71439_g == null || ModuleManager.isModuleEnabled("Freecam")) {
            return;
        }
        final Vec3d vec3d = EntityUtil.getInterpolatedPos((Entity)Scaffold.mc.field_71439_g, this.future.getValue());
        BlockPos blockPos = new BlockPos(vec3d).func_177977_b();
        final BlockPos belowBlockPos = blockPos.func_177977_b();
        if (!Wrapper.getWorld().func_180495_p(blockPos).func_185904_a().func_76222_j()) {
            return;
        }
        int newSlot = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = Wrapper.getPlayer().field_71071_by.func_70301_a(i);
            if (stack != ItemStack.field_190927_a && stack.func_77973_b() instanceof ItemBlock) {
                final Block block = ((ItemBlock)stack.func_77973_b()).func_179223_d();
                if (!this.blackList.contains(block) && !(block instanceof BlockContainer) && Block.func_149634_a(stack.func_77973_b()).func_176223_P().func_185913_b() && (!(((ItemBlock)stack.func_77973_b()).func_179223_d() instanceof BlockFalling) || !Wrapper.getWorld().func_180495_p(belowBlockPos).func_185904_a().func_76222_j())) {
                    newSlot = i;
                    break;
                }
            }
        }
        if (newSlot == -1) {
            return;
        }
        final int oldSlot = Wrapper.getPlayer().field_71071_by.field_70461_c;
        Wrapper.getPlayer().field_71071_by.field_70461_c = newSlot;
        Label_0326: {
            if (!this.hasNeighbour(blockPos)) {
                for (final EnumFacing side : EnumFacing.values()) {
                    final BlockPos neighbour = blockPos.func_177972_a(side);
                    if (this.hasNeighbour(neighbour)) {
                        blockPos = neighbour;
                        break Label_0326;
                    }
                }
                return;
            }
        }
        placeBlockScaffold(blockPos);
        Wrapper.getPlayer().field_71071_by.field_70461_c = oldSlot;
    }
    
    public static boolean placeBlockScaffold(final BlockPos pos) {
        final Vec3d eyesPos = new Vec3d(Wrapper.getPlayer().field_70165_t, Wrapper.getPlayer().field_70163_u + Wrapper.getPlayer().func_70047_e(), Wrapper.getPlayer().field_70161_v);
        for (final EnumFacing side : EnumFacing.values()) {
            final BlockPos neighbor = pos.func_177972_a(side);
            final EnumFacing side2 = side.func_176734_d();
            if (eyesPos.func_72436_e(new Vec3d((Vec3i)pos).func_72441_c(0.5, 0.5, 0.5)) < eyesPos.func_72436_e(new Vec3d((Vec3i)neighbor).func_72441_c(0.5, 0.5, 0.5)) && canBeClicked(neighbor)) {
                final Vec3d hitVec = new Vec3d((Vec3i)neighbor).func_72441_c(0.5, 0.5, 0.5).func_178787_e(new Vec3d(side2.func_176730_m()).func_186678_a(0.5));
                if (eyesPos.func_72436_e(hitVec) <= 18.0625) {
                    faceVectorPacketInstant(hitVec);
                    processRightClickBlock(neighbor, side2, hitVec);
                    Wrapper.getPlayer().func_184609_a(EnumHand.MAIN_HAND);
                    Scaffold.mc.field_71467_ac = 4;
                    return true;
                }
            }
        }
        return false;
    }
    
    private static PlayerControllerMP getPlayerController() {
        return Minecraft.func_71410_x().field_71442_b;
    }
    
    public static void processRightClickBlock(final BlockPos pos, final EnumFacing side, final Vec3d hitVec) {
        getPlayerController().func_187099_a(Wrapper.getPlayer(), Scaffold.mc.field_71441_e, pos, side, hitVec, EnumHand.MAIN_HAND);
    }
    
    public static IBlockState getState(final BlockPos pos) {
        return Wrapper.getWorld().func_180495_p(pos);
    }
    
    public static Block getBlock(final BlockPos pos) {
        return getState(pos).func_177230_c();
    }
    
    public static boolean canBeClicked(final BlockPos pos) {
        return getBlock(pos).func_176209_a(getState(pos), false);
    }
    
    public static void faceVectorPacketInstant(final Vec3d vec) {
        final float[] rotations = getNeededRotations2(vec);
        Wrapper.getPlayer().field_71174_a.func_147297_a((Packet)new CPacketPlayer.Rotation(rotations[0], rotations[1], Wrapper.getPlayer().field_70122_E));
    }
    
    private static float[] getNeededRotations2(final Vec3d vec) {
        final Vec3d eyesPos = getEyesPos();
        final double diffX = vec.field_72450_a - eyesPos.field_72450_a;
        final double diffY = vec.field_72448_b - eyesPos.field_72448_b;
        final double diffZ = vec.field_72449_c - eyesPos.field_72449_c;
        final double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        final float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        final float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[] { Wrapper.getPlayer().field_70177_z + MathHelper.func_76142_g(yaw - Wrapper.getPlayer().field_70177_z), Wrapper.getPlayer().field_70125_A + MathHelper.func_76142_g(pitch - Wrapper.getPlayer().field_70125_A) };
    }
    
    public static Vec3d getEyesPos() {
        return new Vec3d(Wrapper.getPlayer().field_70165_t, Wrapper.getPlayer().field_70163_u + Wrapper.getPlayer().func_70047_e(), Wrapper.getPlayer().field_70161_v);
    }
}

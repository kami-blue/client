// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.combat;

import java.util.concurrent.TimeUnit;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import java.util.Iterator;
import net.minecraft.client.entity.EntityPlayerSP;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AutoWeb", category = Category.COMBAT)
public class AutoWeb extends Module
{
    BlockPos head;
    BlockPos feet;
    int delay;
    public static EntityPlayer target;
    public static List<EntityPlayer> targets;
    public static float yaw;
    public static float pitch;
    
    public boolean isInBlockRange(final Entity target) {
        return target.func_70032_d((Entity)AutoWeb.mc.field_71439_g) <= 4.0f;
    }
    
    public static boolean canBeClicked(final BlockPos pos) {
        return AutoWeb.mc.field_71441_e.func_180495_p(pos).func_177230_c().func_176209_a(AutoWeb.mc.field_71441_e.func_180495_p(pos), false);
    }
    
    private static void faceVectorPacket(final Vec3d vec) {
        final double diffX = vec.field_72450_a - AutoWeb.mc.field_71439_g.field_70165_t;
        final double diffY = vec.field_72448_b - AutoWeb.mc.field_71439_g.field_70163_u + AutoWeb.mc.field_71439_g.func_70047_e();
        final double diffZ = vec.field_72449_c - AutoWeb.mc.field_71439_g.field_70161_v;
        final double dist = MathHelper.func_76133_a(diffX * diffX + diffZ * diffZ);
        final float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        final float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, dist)));
        AutoWeb.mc.func_147114_u().func_147297_a((Packet)new CPacketPlayer.Rotation(AutoWeb.mc.field_71439_g.field_70177_z + MathHelper.func_76142_g(yaw - AutoWeb.mc.field_71439_g.field_70177_z), AutoWeb.mc.field_71439_g.field_70125_A + MathHelper.func_76142_g(pitch - AutoWeb.mc.field_71439_g.field_70125_A), AutoWeb.mc.field_71439_g.field_70122_E));
    }
    
    public boolean isValid(final EntityPlayer entity) {
        if (entity instanceof EntityPlayer) {
            final EntityPlayer animal = entity;
            if (this.isInBlockRange((Entity)animal) && animal.func_110143_aJ() > 0.0f && !animal.field_70128_L && !animal.func_70005_c_().startsWith("Body #") && !Friends.isFriend(animal.func_70005_c_())) {
                return true;
            }
        }
        return false;
    }
    
    public void loadTargets() {
        for (final EntityPlayer player : AutoWeb.mc.field_71441_e.field_73010_i) {
            if (!(player instanceof EntityPlayerSP)) {
                final EntityPlayer p = player;
                if (this.isValid(p)) {
                    AutoWeb.targets.add(p);
                }
                else {
                    if (!AutoWeb.targets.contains(p)) {
                        continue;
                    }
                    AutoWeb.targets.remove(p);
                }
            }
        }
    }
    
    private boolean isStackObby(final ItemStack stack) {
        return stack != null && stack.func_77973_b() == Item.func_150899_d(30);
    }
    
    private boolean doesHotbarHaveObby() {
        for (int i = 36; i < 45; ++i) {
            final ItemStack stack = AutoWeb.mc.field_71439_g.field_71069_bz.func_75139_a(i).func_75211_c();
            if (stack != null && this.isStackObby(stack)) {
                return true;
            }
        }
        return false;
    }
    
    public static Block getBlock(final BlockPos pos) {
        return getState(pos).func_177230_c();
    }
    
    public static IBlockState getState(final BlockPos pos) {
        return AutoWeb.mc.field_71441_e.func_180495_p(pos);
    }
    
    public static boolean placeBlockLegit(final BlockPos pos) {
        final Vec3d eyesPos = new Vec3d(AutoWeb.mc.field_71439_g.field_70165_t, AutoWeb.mc.field_71439_g.field_70163_u + AutoWeb.mc.field_71439_g.func_70047_e(), AutoWeb.mc.field_71439_g.field_70161_v);
        final Vec3d posVec = new Vec3d((Vec3i)pos).func_72441_c(0.5, 0.5, 0.5);
        for (final EnumFacing side : EnumFacing.values()) {
            final BlockPos neighbor = pos.func_177972_a(side);
            if (canBeClicked(neighbor)) {
                final Vec3d hitVec = posVec.func_178787_e(new Vec3d(side.func_176730_m()).func_186678_a(0.5));
                if (eyesPos.func_72436_e(hitVec) <= 36.0) {
                    AutoWeb.mc.field_71442_b.func_187099_a(AutoWeb.mc.field_71439_g, AutoWeb.mc.field_71441_e, neighbor, side.func_176734_d(), hitVec, EnumHand.MAIN_HAND);
                    AutoWeb.mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
                    try {
                        TimeUnit.MILLISECONDS.sleep(10L);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public void onUpdate() {
        if (AutoWeb.mc.field_71439_g.func_184587_cr()) {
            return;
        }
        if (!this.isValid(AutoWeb.target) || AutoWeb.target == null) {
            this.updateTarget();
        }
        for (final EntityPlayer player : AutoWeb.mc.field_71441_e.field_73010_i) {
            if (!(player instanceof EntityPlayerSP)) {
                final EntityPlayer e = player;
                if (this.isValid(e) && e.func_70032_d((Entity)AutoWeb.mc.field_71439_g) < AutoWeb.target.func_70032_d((Entity)AutoWeb.mc.field_71439_g)) {
                    AutoWeb.target = e;
                    return;
                }
                continue;
            }
        }
        if (this.isValid(AutoWeb.target) && AutoWeb.mc.field_71439_g.func_70032_d((Entity)AutoWeb.target) < 4.0f) {
            this.trap(AutoWeb.target);
        }
        else {
            this.delay = 0;
        }
    }
    
    public static double roundToHalf(final double d) {
        return Math.round(d * 2.0) / 2.0;
    }
    
    public void onEnable() {
        this.delay = 0;
    }
    
    private void trap(final EntityPlayer player) {
        if (player.field_191988_bg == 0.0 && player.field_70702_br == 0.0 && player.field_70701_bs == 0.0) {
            ++this.delay;
        }
        if (player.field_191988_bg != 0.0 || player.field_70702_br != 0.0 || player.field_70701_bs != 0.0) {
            this.delay = 0;
        }
        if (!this.doesHotbarHaveObby()) {
            this.delay = 0;
        }
        if (this.delay == 2 && this.doesHotbarHaveObby()) {
            this.head = new BlockPos(player.field_70165_t, player.field_70163_u + 1.0, player.field_70161_v);
            this.feet = new BlockPos(player.field_70165_t, player.field_70163_u, player.field_70161_v);
            for (int i = 36; i < 45; ++i) {
                final ItemStack stack = AutoWeb.mc.field_71439_g.field_71069_bz.func_75139_a(i).func_75211_c();
                if (stack != null && this.isStackObby(stack)) {
                    final int oldSlot = AutoWeb.mc.field_71439_g.field_71071_by.field_70461_c;
                    if (AutoWeb.mc.field_71441_e.func_180495_p(this.head).func_185904_a().func_76222_j() || AutoWeb.mc.field_71441_e.func_180495_p(this.feet).func_185904_a().func_76222_j()) {
                        AutoWeb.mc.field_71439_g.field_71071_by.field_70461_c = i - 36;
                        if (AutoWeb.mc.field_71441_e.func_180495_p(this.head).func_185904_a().func_76222_j()) {
                            placeBlockLegit(this.head);
                        }
                        if (AutoWeb.mc.field_71441_e.func_180495_p(this.feet).func_185904_a().func_76222_j()) {
                            placeBlockLegit(this.feet);
                        }
                        AutoWeb.mc.field_71439_g.field_71071_by.field_70461_c = oldSlot;
                        this.delay = 0;
                        break;
                    }
                    this.delay = 0;
                }
                this.delay = 0;
            }
        }
    }
    
    public void onDisable() {
        this.delay = 0;
        AutoWeb.yaw = AutoWeb.mc.field_71439_g.field_70177_z;
        AutoWeb.pitch = AutoWeb.mc.field_71439_g.field_70125_A;
        AutoWeb.target = null;
    }
    
    public void updateTarget() {
        for (final EntityPlayer player : AutoWeb.mc.field_71441_e.field_73010_i) {
            if (!(player instanceof EntityPlayerSP)) {
                final EntityPlayer entity = player;
                if (entity instanceof EntityPlayerSP) {
                    continue;
                }
                if (!this.isValid(entity)) {
                    continue;
                }
                AutoWeb.target = entity;
            }
        }
    }
    
    public EnumFacing getEnumFacing(final float posX, final float posY, final float posZ) {
        return EnumFacing.func_176737_a(posX, posY, posZ);
    }
    
    public BlockPos getBlockPos(final double x, final double y, final double z) {
        return new BlockPos(x, y, z);
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.combat;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3i;
import me.zeroeightsix.kami.util.BlockInteractionHelper;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockAir;
import me.zeroeightsix.kami.command.Command;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.math.Vec3d;
import me.zeroeightsix.kami.module.ModuleManager;
import net.minecraft.network.Packet;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AutoFeetPlace", category = Category.COMBAT)
public class AutoFeetPlace extends Module
{
    private Setting<Mode> mode;
    private Setting<Boolean> triggerable;
    private Setting<Boolean> disableNone;
    private Setting<Integer> timeoutTicks;
    private Setting<Integer> blocksPerTick;
    private Setting<Integer> tickDelay;
    private Setting<Boolean> rotate;
    private Setting<Boolean> infoMessage;
    private int offsetStep;
    private int delayStep;
    private int playerHotbarSlot;
    private int lastHotbarSlot;
    private boolean isSneaking;
    private int totalTicksRunning;
    private boolean firstRun;
    private boolean missingObiDisable;
    
    public AutoFeetPlace() {
        this.mode = this.register(Settings.e("Mode", Mode.FULL));
        this.triggerable = this.register(Settings.b("Triggerable", true));
        this.disableNone = this.register(Settings.b("DisableNoObby", true));
        this.timeoutTicks = this.register(Settings.integerBuilder("TimeoutTicks").withMinimum(1).withValue(40).withMaximum(100).withVisibility(b -> this.triggerable.getValue()).build());
        this.blocksPerTick = this.register((Setting<Integer>)Settings.integerBuilder("BlocksPerTick").withMinimum(1).withValue(4).withMaximum(9).build());
        this.tickDelay = this.register((Setting<Integer>)Settings.integerBuilder("TickDelay").withMinimum(0).withValue(0).withMaximum(10).build());
        this.rotate = this.register(Settings.b("Rotate", true));
        this.infoMessage = this.register(Settings.b("InfoMessage", false));
        this.offsetStep = 0;
        this.delayStep = 0;
        this.playerHotbarSlot = -1;
        this.lastHotbarSlot = -1;
        this.isSneaking = false;
        this.totalTicksRunning = 0;
        this.missingObiDisable = false;
    }
    
    private static EnumFacing getPlaceableSide(final BlockPos pos) {
        for (final EnumFacing side : EnumFacing.values()) {
            final BlockPos neighbour = pos.func_177972_a(side);
            if (AutoFeetPlace.mc.field_71441_e.func_180495_p(neighbour).func_177230_c().func_176209_a(AutoFeetPlace.mc.field_71441_e.func_180495_p(neighbour), false)) {
                final IBlockState blockState = AutoFeetPlace.mc.field_71441_e.func_180495_p(neighbour);
                if (!blockState.func_185904_a().func_76222_j()) {
                    return side;
                }
            }
        }
        return null;
    }
    
    @Override
    protected void onEnable() {
        if (AutoFeetPlace.mc.field_71439_g == null) {
            this.disable();
            return;
        }
        this.firstRun = true;
        this.playerHotbarSlot = AutoFeetPlace.mc.field_71439_g.field_71071_by.field_70461_c;
        this.lastHotbarSlot = -1;
    }
    
    @Override
    protected void onDisable() {
        if (AutoFeetPlace.mc.field_71439_g == null) {
            return;
        }
        if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
            AutoFeetPlace.mc.field_71439_g.field_71071_by.field_70461_c = this.playerHotbarSlot;
        }
        if (this.isSneaking) {
            AutoFeetPlace.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketEntityAction((Entity)AutoFeetPlace.mc.field_71439_g, CPacketEntityAction.Action.STOP_SNEAKING));
            this.isSneaking = false;
        }
        this.playerHotbarSlot = -1;
        this.lastHotbarSlot = -1;
        this.missingObiDisable = false;
    }
    
    @Override
    public void onUpdate() {
        if (AutoFeetPlace.mc.field_71439_g == null || ModuleManager.isModuleEnabled("Freecam")) {
            return;
        }
        if (this.triggerable.getValue() && this.totalTicksRunning >= this.timeoutTicks.getValue()) {
            this.totalTicksRunning = 0;
            this.disable();
            return;
        }
        if (!this.firstRun) {
            if (this.delayStep < this.tickDelay.getValue()) {
                ++this.delayStep;
                return;
            }
            this.delayStep = 0;
        }
        if (this.firstRun) {
            this.firstRun = false;
            if (this.findObiInHotbar() == -1) {
                this.missingObiDisable = true;
            }
        }
        Vec3d[] offsetPattern = new Vec3d[0];
        int maxSteps = 0;
        if (this.mode.getValue().equals(Mode.FULL)) {
            offsetPattern = Offsets.FULL;
            maxSteps = Offsets.FULL.length;
        }
        if (this.mode.getValue().equals(Mode.SURROUND)) {
            offsetPattern = Offsets.SURROUND;
            maxSteps = Offsets.SURROUND.length;
        }
        int blocksPlaced = 0;
        while (blocksPlaced < this.blocksPerTick.getValue()) {
            if (this.offsetStep >= maxSteps) {
                this.offsetStep = 0;
                break;
            }
            final BlockPos offsetPos = new BlockPos(offsetPattern[this.offsetStep]);
            final BlockPos targetPos = new BlockPos(AutoFeetPlace.mc.field_71439_g.func_174791_d()).func_177982_a(offsetPos.field_177962_a, offsetPos.field_177960_b, offsetPos.field_177961_c);
            if (this.placeBlock(targetPos)) {
                ++blocksPlaced;
            }
            ++this.offsetStep;
        }
        if (blocksPlaced > 0) {
            if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
                AutoFeetPlace.mc.field_71439_g.field_71071_by.field_70461_c = this.playerHotbarSlot;
                this.lastHotbarSlot = this.playerHotbarSlot;
            }
            if (this.isSneaking) {
                AutoFeetPlace.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketEntityAction((Entity)AutoFeetPlace.mc.field_71439_g, CPacketEntityAction.Action.STOP_SNEAKING));
                this.isSneaking = false;
            }
        }
        ++this.totalTicksRunning;
        if (this.missingObiDisable && this.disableNone.getValue()) {
            this.missingObiDisable = false;
            if (this.infoMessage.getValue()) {
                Command.sendChatMessage("[AutoFeetPlace] " + ChatFormatting.RED + "Disabled" + ChatFormatting.RESET + ", Obsidian missing!");
            }
            this.disable();
        }
    }
    
    private boolean placeBlock(final BlockPos pos) {
        final Block block = AutoFeetPlace.mc.field_71441_e.func_180495_p(pos).func_177230_c();
        if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid)) {
            return false;
        }
        for (final Entity entity : AutoFeetPlace.mc.field_71441_e.func_72839_b((Entity)null, new AxisAlignedBB(pos))) {
            if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb)) {
                return false;
            }
        }
        final EnumFacing side = getPlaceableSide(pos);
        if (side == null) {
            return false;
        }
        final BlockPos neighbour = pos.func_177972_a(side);
        final EnumFacing opposite = side.func_176734_d();
        if (!BlockInteractionHelper.canBeClicked(neighbour)) {
            return false;
        }
        final Vec3d hitVec = new Vec3d((Vec3i)neighbour).func_72441_c(0.5, 0.5, 0.5).func_178787_e(new Vec3d(opposite.func_176730_m()).func_186678_a(0.5));
        final Block neighbourBlock = AutoFeetPlace.mc.field_71441_e.func_180495_p(neighbour).func_177230_c();
        final int obiSlot = this.findObiInHotbar();
        if (obiSlot == -1) {
            this.missingObiDisable = true;
            return false;
        }
        if (this.lastHotbarSlot != obiSlot) {
            AutoFeetPlace.mc.field_71439_g.field_71071_by.field_70461_c = obiSlot;
            this.lastHotbarSlot = obiSlot;
        }
        if ((!this.isSneaking && BlockInteractionHelper.blackList.contains(neighbourBlock)) || BlockInteractionHelper.shulkerList.contains(neighbourBlock)) {
            AutoFeetPlace.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketEntityAction((Entity)AutoFeetPlace.mc.field_71439_g, CPacketEntityAction.Action.START_SNEAKING));
            this.isSneaking = true;
        }
        if (this.rotate.getValue()) {
            BlockInteractionHelper.faceVectorPacketInstant(hitVec);
        }
        AutoFeetPlace.mc.field_71442_b.func_187099_a(AutoFeetPlace.mc.field_71439_g, AutoFeetPlace.mc.field_71441_e, neighbour, opposite, hitVec, EnumHand.MAIN_HAND);
        AutoFeetPlace.mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
        AutoFeetPlace.mc.field_71467_ac = 4;
        return true;
    }
    
    private int findObiInHotbar() {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = AutoFeetPlace.mc.field_71439_g.field_71071_by.func_70301_a(i);
            if (stack != ItemStack.field_190927_a) {
                if (stack.func_77973_b() instanceof ItemBlock) {
                    final Block block = ((ItemBlock)stack.func_77973_b()).func_179223_d();
                    if (block instanceof Block) {
                        slot = i;
                        break;
                    }
                }
            }
        }
        return slot;
    }
    
    private enum Mode
    {
        SURROUND, 
        FULL;
    }
    
    private static class Offsets
    {
        private static final Vec3d[] SURROUND;
        private static final Vec3d[] FULL;
        
        static {
            SURROUND = new Vec3d[] { new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, -1.0), new Vec3d(1.0, -1.0, 0.0), new Vec3d(0.0, -1.0, 1.0), new Vec3d(-1.0, -1.0, 0.0), new Vec3d(0.0, -1.0, -1.0) };
            FULL = new Vec3d[] { new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, -1.0), new Vec3d(1.0, -1.0, 0.0), new Vec3d(0.0, -1.0, 1.0), new Vec3d(-1.0, -1.0, 0.0), new Vec3d(0.0, -1.0, -1.0), new Vec3d(0.0, -1.0, 0.0) };
        }
    }
}

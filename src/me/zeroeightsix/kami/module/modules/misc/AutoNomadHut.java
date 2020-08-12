// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import net.minecraft.util.math.MathHelper;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import me.zeroeightsix.kami.util.BlockInteractionHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.math.Vec3i;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.util.math.BlockPos;
import me.zeroeightsix.kami.setting.Setting;
import net.minecraft.util.math.Vec3d;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AutoNomadHut", category = Category.MISC, description = "Builds a NomadHut. Stand still in the middle of a block!")
public class AutoNomadHut extends Module
{
    private final Vec3d[] surroundTargets;
    private final Vec3d[] surroundTargetsCritical;
    private Setting<Boolean> toggleable;
    private Setting<Boolean> spoofRotations;
    private Setting<Boolean> spoofHotbar;
    private Setting<Double> blockPerTick;
    private Setting<Boolean> debugMessages;
    private BlockPos basePos;
    private int offsetStep;
    private int playerHotbarSlot;
    private int lastHotbarSlot;
    
    public AutoNomadHut() {
        this.surroundTargets = new Vec3d[] { new Vec3d(0.0, 0.0, 0.0), new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, -1.0), new Vec3d(1.0, 0.0, 1.0), new Vec3d(1.0, 0.0, -1.0), new Vec3d(-1.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, -1.0), new Vec3d(2.0, 0.0, 0.0), new Vec3d(2.0, 0.0, 1.0), new Vec3d(2.0, 0.0, -1.0), new Vec3d(-2.0, 0.0, 0.0), new Vec3d(-2.0, 0.0, 1.0), new Vec3d(-2.0, 0.0, -1.0), new Vec3d(0.0, 0.0, 2.0), new Vec3d(1.0, 0.0, 2.0), new Vec3d(-1.0, 0.0, 2.0), new Vec3d(0.0, 0.0, -2.0), new Vec3d(-1.0, 0.0, -2.0), new Vec3d(1.0, 0.0, -2.0), new Vec3d(2.0, 1.0, -1.0), new Vec3d(2.0, 1.0, 1.0), new Vec3d(-2.0, 1.0, 0.0), new Vec3d(-2.0, 1.0, 1.0), new Vec3d(-2.0, 1.0, -1.0), new Vec3d(0.0, 1.0, 2.0), new Vec3d(1.0, 1.0, 2.0), new Vec3d(-1.0, 1.0, 2.0), new Vec3d(0.0, 1.0, -2.0), new Vec3d(1.0, 1.0, -2.0), new Vec3d(-1.0, 1.0, -2.0), new Vec3d(2.0, 2.0, -1.0), new Vec3d(2.0, 2.0, 1.0), new Vec3d(-2.0, 2.0, 1.0), new Vec3d(-2.0, 2.0, -1.0), new Vec3d(1.0, 2.0, 2.0), new Vec3d(-1.0, 2.0, 2.0), new Vec3d(1.0, 2.0, -2.0), new Vec3d(-1.0, 2.0, -2.0), new Vec3d(2.0, 3.0, 0.0), new Vec3d(2.0, 3.0, -1.0), new Vec3d(2.0, 3.0, 1.0), new Vec3d(-2.0, 3.0, 0.0), new Vec3d(-2.0, 3.0, 1.0), new Vec3d(-2.0, 3.0, -1.0), new Vec3d(0.0, 3.0, 2.0), new Vec3d(1.0, 3.0, 2.0), new Vec3d(-1.0, 3.0, 2.0), new Vec3d(0.0, 3.0, -2.0), new Vec3d(1.0, 3.0, -2.0), new Vec3d(-1.0, 3.0, -2.0), new Vec3d(0.0, 4.0, 0.0), new Vec3d(1.0, 4.0, 0.0), new Vec3d(-1.0, 4.0, 0.0), new Vec3d(0.0, 4.0, 1.0), new Vec3d(0.0, 4.0, -1.0), new Vec3d(1.0, 4.0, 1.0), new Vec3d(-1.0, 4.0, 1.0), new Vec3d(-1.0, 4.0, -1.0), new Vec3d(1.0, 4.0, -1.0), new Vec3d(2.0, 4.0, 0.0), new Vec3d(2.0, 4.0, 1.0), new Vec3d(2.0, 4.0, -1.0) };
        this.surroundTargetsCritical = new Vec3d[] { new Vec3d(0.0, 0.0, 0.0), new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, -1.0), new Vec3d(1.0, 0.0, 1.0), new Vec3d(1.0, 0.0, -1.0), new Vec3d(-1.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, -1.0), new Vec3d(2.0, 0.0, 0.0), new Vec3d(2.0, 0.0, 1.0), new Vec3d(2.0, 0.0, -1.0), new Vec3d(-2.0, 0.0, 0.0), new Vec3d(-2.0, 0.0, 1.0), new Vec3d(-2.0, 0.0, -1.0), new Vec3d(0.0, 0.0, 2.0), new Vec3d(1.0, 0.0, 2.0), new Vec3d(-1.0, 0.0, 2.0), new Vec3d(0.0, 0.0, -2.0), new Vec3d(-1.0, 0.0, -2.0), new Vec3d(1.0, 0.0, -2.0), new Vec3d(2.0, 1.0, -1.0), new Vec3d(2.0, 1.0, 1.0), new Vec3d(-2.0, 1.0, 0.0), new Vec3d(-2.0, 1.0, 1.0), new Vec3d(-2.0, 1.0, -1.0), new Vec3d(0.0, 1.0, 2.0), new Vec3d(1.0, 1.0, 2.0), new Vec3d(-1.0, 1.0, 2.0), new Vec3d(0.0, 1.0, -2.0), new Vec3d(1.0, 1.0, -2.0), new Vec3d(-1.0, 1.0, -2.0), new Vec3d(2.0, 2.0, -1.0), new Vec3d(2.0, 2.0, 1.0), new Vec3d(-2.0, 2.0, 1.0), new Vec3d(-2.0, 2.0, -1.0), new Vec3d(1.0, 2.0, 2.0), new Vec3d(-1.0, 2.0, 2.0), new Vec3d(1.0, 2.0, -2.0), new Vec3d(-1.0, 2.0, -2.0), new Vec3d(2.0, 3.0, 0.0), new Vec3d(2.0, 3.0, -1.0), new Vec3d(2.0, 3.0, 1.0), new Vec3d(-2.0, 3.0, 0.0), new Vec3d(-2.0, 3.0, 1.0), new Vec3d(-2.0, 3.0, -1.0), new Vec3d(0.0, 3.0, 2.0), new Vec3d(1.0, 3.0, 2.0), new Vec3d(-1.0, 3.0, 2.0), new Vec3d(0.0, 3.0, -2.0), new Vec3d(1.0, 3.0, -2.0), new Vec3d(-1.0, 3.0, -2.0), new Vec3d(0.0, 4.0, 0.0), new Vec3d(1.0, 4.0, 0.0), new Vec3d(-1.0, 4.0, 0.0), new Vec3d(0.0, 4.0, 1.0), new Vec3d(0.0, 4.0, -1.0), new Vec3d(1.0, 4.0, 1.0), new Vec3d(-1.0, 4.0, 1.0), new Vec3d(-1.0, 4.0, -1.0), new Vec3d(1.0, 4.0, -1.0), new Vec3d(2.0, 4.0, 0.0), new Vec3d(2.0, 4.0, 1.0), new Vec3d(2.0, 4.0, -1.0) };
        this.toggleable = this.register(Settings.b("Toggleable", true));
        this.spoofRotations = this.register(Settings.b("Spoof Rotations", false));
        this.spoofHotbar = this.register(Settings.b("Spoof Hotbar", false));
        this.blockPerTick = this.register(Settings.d("Blocks per Tick", 1.0));
        this.debugMessages = this.register(Settings.b("Debug Messages", false));
        this.offsetStep = 0;
        this.playerHotbarSlot = -1;
        this.lastHotbarSlot = -1;
    }
    
    @Override
    public void onUpdate() {
        if (this.isDisabled() || AutoNomadHut.mc.field_71439_g == null || ModuleManager.isModuleEnabled("Freecam")) {
            return;
        }
        if (this.offsetStep == 0) {
            this.basePos = new BlockPos(AutoNomadHut.mc.field_71439_g.func_174791_d()).func_177977_b();
            this.playerHotbarSlot = Wrapper.getPlayer().field_71071_by.field_70461_c;
            if (this.debugMessages.getValue()) {
                Command.sendChatMessage("[AutoFeetPlace] Starting Loop, current Player Slot: " + this.playerHotbarSlot);
            }
            if (!this.spoofHotbar.getValue()) {
                this.lastHotbarSlot = AutoNomadHut.mc.field_71439_g.field_71071_by.field_70461_c;
            }
        }
        for (int i = 0; i < (int)Math.floor(this.blockPerTick.getValue()); ++i) {
            if (this.debugMessages.getValue()) {
                Command.sendChatMessage("[AutoFeetPlace] Loop iteration: " + this.offsetStep);
            }
            if (this.offsetStep >= this.surroundTargets.length) {
                this.endLoop();
                return;
            }
            final Vec3d offset = this.surroundTargets[this.offsetStep];
            this.placeBlock(new BlockPos((Vec3i)this.basePos.func_177963_a(offset.field_72450_a, offset.field_72448_b, offset.field_72449_c)));
            ++this.offsetStep;
        }
    }
    
    @Override
    protected void onEnable() {
        if (AutoNomadHut.mc.field_71439_g == null) {
            this.disable();
            return;
        }
        if (this.debugMessages.getValue()) {
            Command.sendChatMessage("[AutoFeetPlace] Enabling");
        }
        this.playerHotbarSlot = Wrapper.getPlayer().field_71071_by.field_70461_c;
        this.lastHotbarSlot = -1;
        if (this.debugMessages.getValue()) {
            Command.sendChatMessage("[AutoFeetPlace] Saving initial Slot  = " + this.playerHotbarSlot);
        }
    }
    
    @Override
    protected void onDisable() {
        if (AutoNomadHut.mc.field_71439_g == null) {
            return;
        }
        if (this.debugMessages.getValue()) {
            Command.sendChatMessage("[AutoFeetPlace] Disabling");
        }
        if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
            if (this.debugMessages.getValue()) {
                Command.sendChatMessage("[AutoFeetPlace] Setting Slot to  = " + this.playerHotbarSlot);
            }
            if (this.spoofHotbar.getValue()) {
                AutoNomadHut.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketHeldItemChange(this.playerHotbarSlot));
            }
            else {
                Wrapper.getPlayer().field_71071_by.field_70461_c = this.playerHotbarSlot;
            }
        }
        this.playerHotbarSlot = -1;
        this.lastHotbarSlot = -1;
    }
    
    private void endLoop() {
        this.offsetStep = 0;
        if (this.debugMessages.getValue()) {
            Command.sendChatMessage("[AutoFeetPlace] Ending Loop");
        }
        if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
            if (this.debugMessages.getValue()) {
                Command.sendChatMessage("[AutoFeetPlace] Setting Slot back to  = " + this.playerHotbarSlot);
            }
            if (this.spoofHotbar.getValue()) {
                AutoNomadHut.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketHeldItemChange(this.playerHotbarSlot));
            }
            else {
                Wrapper.getPlayer().field_71071_by.field_70461_c = this.playerHotbarSlot;
            }
            this.lastHotbarSlot = this.playerHotbarSlot;
        }
        if (!this.toggleable.getValue()) {
            this.disable();
        }
    }
    
    private void placeBlock(final BlockPos blockPos) {
        if (!Wrapper.getWorld().func_180495_p(blockPos).func_185904_a().func_76222_j()) {
            if (this.debugMessages.getValue()) {
                Command.sendChatMessage("[AutoFeetPlace] Block is already placed, skipping");
            }
            return;
        }
        if (!BlockInteractionHelper.checkForNeighbours(blockPos)) {
            if (this.debugMessages.getValue()) {
                Command.sendChatMessage("[AutoFeetPlace] !checkForNeighbours(blockPos), disabling! ");
            }
            return;
        }
        this.placeBlockExecute(blockPos);
    }
    
    private int findObiInHotbar() {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = Wrapper.getPlayer().field_71071_by.func_70301_a(i);
            if (stack != ItemStack.field_190927_a && stack.func_77973_b() instanceof ItemBlock) {
                final Block block = ((ItemBlock)stack.func_77973_b()).func_179223_d();
                if (block instanceof BlockObsidian) {
                    slot = i;
                    break;
                }
            }
        }
        return slot;
    }
    
    public void placeBlockExecute(final BlockPos pos) {
        final Vec3d eyesPos = new Vec3d(Wrapper.getPlayer().field_70165_t, Wrapper.getPlayer().field_70163_u + Wrapper.getPlayer().func_70047_e(), Wrapper.getPlayer().field_70161_v);
        for (final EnumFacing side : EnumFacing.values()) {
            final BlockPos neighbor = pos.func_177972_a(side);
            final EnumFacing side2 = side.func_176734_d();
            if (!canBeClicked(neighbor)) {
                if (this.debugMessages.getValue()) {
                    Command.sendChatMessage("[AutoFeetPlace] No neighbor to click at!");
                }
            }
            else {
                final Vec3d hitVec = new Vec3d((Vec3i)neighbor).func_72441_c(0.5, 0.5, 0.5).func_178787_e(new Vec3d(side2.func_176730_m()).func_186678_a(0.5));
                if (eyesPos.func_72436_e(hitVec) > 18.0625) {
                    if (this.debugMessages.getValue()) {
                        Command.sendChatMessage("[AutoFeetPlace] Distance > 4.25 blocks!");
                    }
                }
                else {
                    boolean needSneak = false;
                    final Block blockBelow = AutoNomadHut.mc.field_71441_e.func_180495_p(neighbor).func_177230_c();
                    if (BlockInteractionHelper.blackList.contains(blockBelow) || BlockInteractionHelper.shulkerList.contains(blockBelow)) {
                        if (this.debugMessages.getValue()) {
                            Command.sendChatMessage("[AutoFeetPlace] Sneak enabled!");
                        }
                        needSneak = true;
                    }
                    if (needSneak) {
                        AutoNomadHut.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketEntityAction((Entity)AutoNomadHut.mc.field_71439_g, CPacketEntityAction.Action.START_SNEAKING));
                    }
                    final int obiSlot = this.findObiInHotbar();
                    if (obiSlot == -1) {
                        if (this.debugMessages.getValue()) {
                            Command.sendChatMessage("[AutoFeetPlace] No Obi in Hotbar, disabling!");
                        }
                        this.disable();
                        return;
                    }
                    if (this.lastHotbarSlot != obiSlot) {
                        if (this.debugMessages.getValue()) {
                            Command.sendChatMessage("[AutoFeetPlace] Setting Slot to Obi at  = " + obiSlot);
                        }
                        if (this.spoofHotbar.getValue()) {
                            AutoNomadHut.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketHeldItemChange(obiSlot));
                        }
                        else {
                            Wrapper.getPlayer().field_71071_by.field_70461_c = obiSlot;
                        }
                        this.lastHotbarSlot = obiSlot;
                    }
                    AutoNomadHut.mc.field_71442_b.func_187099_a(Wrapper.getPlayer(), AutoNomadHut.mc.field_71441_e, neighbor, side2, hitVec, EnumHand.MAIN_HAND);
                    AutoNomadHut.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketAnimation(EnumHand.MAIN_HAND));
                    if (needSneak) {
                        if (this.debugMessages.getValue()) {
                            Command.sendChatMessage("[AutoFeetPlace] Sneak disabled!");
                        }
                        AutoNomadHut.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketEntityAction((Entity)AutoNomadHut.mc.field_71439_g, CPacketEntityAction.Action.STOP_SNEAKING));
                    }
                    return;
                }
            }
        }
    }
    
    private static boolean canBeClicked(final BlockPos pos) {
        return getBlock(pos).func_176209_a(getState(pos), false);
    }
    
    private static Block getBlock(final BlockPos pos) {
        return getState(pos).func_177230_c();
    }
    
    private static IBlockState getState(final BlockPos pos) {
        return Wrapper.getWorld().func_180495_p(pos);
    }
    
    private static void faceVectorPacketInstant(final Vec3d vec) {
        final float[] rotations = getLegitRotations(vec);
        Wrapper.getPlayer().field_71174_a.func_147297_a((Packet)new CPacketPlayer.Rotation(rotations[0], rotations[1], Wrapper.getPlayer().field_70122_E));
    }
    
    private static float[] getLegitRotations(final Vec3d vec) {
        final Vec3d eyesPos = getEyesPos();
        final double diffX = vec.field_72450_a - eyesPos.field_72450_a;
        final double diffY = vec.field_72448_b - eyesPos.field_72448_b;
        final double diffZ = vec.field_72449_c - eyesPos.field_72449_c;
        final double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        final float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        final float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[] { Wrapper.getPlayer().field_70177_z + MathHelper.func_76142_g(yaw - Wrapper.getPlayer().field_70177_z), Wrapper.getPlayer().field_70125_A + MathHelper.func_76142_g(pitch - Wrapper.getPlayer().field_70125_A) };
    }
    
    private static Vec3d getEyesPos() {
        return new Vec3d(Wrapper.getPlayer().field_70165_t, Wrapper.getPlayer().field_70163_u + Wrapper.getPlayer().func_70047_e(), Wrapper.getPlayer().field_70161_v);
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.combat;

import net.minecraft.network.Packet;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketEntityAction;
import me.zeroeightsix.kami.command.Command;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import me.zeroeightsix.kami.util.BlockInteractionHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.util.math.Vec3i;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Settings;
import java.util.Collections;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import me.zeroeightsix.kami.setting.Setting;
import java.util.List;
import net.minecraft.util.math.Vec3d;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Surround", category = Category.COMBAT)
public class Surround extends Module
{
    private final Vec3d[] surroundList;
    private final Vec3d[] surroundListFull;
    private final List obsidian;
    private Setting toggleable;
    private Setting slowmode;
    private Setting full;
    private Vec3d[] surroundTargets;
    private int blocksPerTick;
    private BlockPos basePos;
    private boolean slowModeSwitch;
    private int offsetStep;
    private int oldSlot;
    
    public Surround() {
        this.surroundList = new Vec3d[] { new Vec3d(0.0, 0.0, 0.0), new Vec3d(1.0, 0.0, 0.0), new Vec3d(1.0, 1.0, 0.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(0.0, 1.0, 1.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(-1.0, 1.0, 0.0), new Vec3d(0.0, 0.0, -1.0), new Vec3d(0.0, 1.0, -1.0) };
        this.surroundListFull = new Vec3d[] { new Vec3d(0.0, 0.0, 0.0), new Vec3d(1.0, 0.0, 0.0), new Vec3d(1.0, 1.0, 0.0), new Vec3d(1.0, 0.0, 1.0), new Vec3d(1.0, 1.0, 1.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(0.0, 1.0, 1.0), new Vec3d(-1.0, 0.0, 1.0), new Vec3d(-1.0, 1.0, 1.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(-1.0, 1.0, 0.0), new Vec3d(-1.0, 0.0, -1.0), new Vec3d(-1.0, 1.0, -1.0), new Vec3d(0.0, 0.0, -1.0), new Vec3d(0.0, 1.0, -1.0), new Vec3d(1.0, 0.0, -1.0), new Vec3d(1.0, 1.0, -1.0) };
        this.obsidian = Collections.singletonList(Blocks.field_150343_Z);
        this.toggleable = this.register(Settings.b("Toggleable", true));
        this.slowmode = this.register(Settings.b("Slow", false));
        this.full = this.register(Settings.b("Full", false));
        this.blocksPerTick = 3;
        this.slowModeSwitch = false;
        this.offsetStep = 0;
        this.oldSlot = 0;
    }
    
    @Override
    public void onUpdate() {
        if (!this.isDisabled() && Surround.mc.field_71439_g != null && !ModuleManager.isModuleEnabled("Freecam")) {
            if (this.slowModeSwitch) {
                this.slowModeSwitch = false;
            }
            else {
                if (this.offsetStep == 0) {
                    this.init();
                }
                for (int i = 0; i < this.blocksPerTick; ++i) {
                    if (this.offsetStep >= this.surroundTargets.length) {
                        this.end();
                        return;
                    }
                    final Vec3d offset = this.surroundTargets[this.offsetStep];
                    this.placeBlock(new BlockPos((Vec3i)this.basePos.func_177963_a(offset.field_72450_a, offset.field_72448_b, offset.field_72449_c)));
                    ++this.offsetStep;
                }
                this.slowModeSwitch = true;
            }
        }
    }
    
    private void placeBlock(final BlockPos blockPos) {
        if (Wrapper.getWorld().func_180495_p(blockPos).func_185904_a().func_76222_j()) {
            int newSlot = -1;
            for (int i = 0; i < 9; ++i) {
                final ItemStack stack = Wrapper.getPlayer().field_71071_by.func_70301_a(i);
                if (stack != ItemStack.field_190927_a && stack.func_77973_b() instanceof ItemBlock) {
                    final Block block = ((ItemBlock)stack.func_77973_b()).func_179223_d();
                    if (!BlockInteractionHelper.blackList.contains(block) && !(block instanceof BlockContainer) && Block.func_149634_a(stack.func_77973_b()).func_176223_P().func_185913_b() && (!(((ItemBlock)stack.func_77973_b()).func_179223_d() instanceof BlockFalling) || !Wrapper.getWorld().func_180495_p(blockPos.func_177977_b()).func_185904_a().func_76222_j()) && this.obsidian.contains(block)) {
                        newSlot = i;
                        break;
                    }
                }
            }
            if (newSlot == -1) {
                if (!this.toggleable.getValue()) {
                    Command.sendChatMessage("Surround: Please Put Obsidian in Hotbar");
                }
                this.end();
            }
            else {
                Wrapper.getPlayer().field_71071_by.field_70461_c = newSlot;
                if (BlockInteractionHelper.checkForNeighbours(blockPos)) {
                    BlockInteractionHelper.placeBlockScaffold(blockPos);
                }
            }
        }
    }
    
    private void init() {
        this.basePos = new BlockPos(Surround.mc.field_71439_g.func_174791_d()).func_177977_b();
        if (this.slowmode.getValue()) {
            this.blocksPerTick = 1;
        }
        if (this.full.getValue()) {
            this.surroundTargets = this.surroundListFull;
        }
        else {
            this.surroundTargets = this.surroundList;
        }
    }
    
    private void end() {
        this.offsetStep = 0;
        if (!this.toggleable.getValue()) {
            this.disable();
        }
    }
    
    @Override
    protected void onEnable() {
        Surround.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketEntityAction((Entity)Surround.mc.field_71439_g, CPacketEntityAction.Action.START_SNEAKING));
        this.oldSlot = Wrapper.getPlayer().field_71071_by.field_70461_c;
    }
    
    @Override
    protected void onDisable() {
        Surround.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketEntityAction((Entity)Surround.mc.field_71439_g, CPacketEntityAction.Action.STOP_SNEAKING));
        Wrapper.getPlayer().field_71071_by.field_70461_c = this.oldSlot;
    }
}

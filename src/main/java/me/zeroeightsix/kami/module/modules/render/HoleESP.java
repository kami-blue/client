package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.combat.AutoHoleFill;
import me.zeroeightsix.kami.module.modules.combat.CrystalAura;
import me.zeroeightsix.kami.module.modules.combat.Surround;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.GeometryMasks;
import me.zeroeightsix.kami.util.KamiTessellator;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static me.zeroeightsix.kami.module.modules.combat.CrystalAura.getPlayerPos;


/**
 * Created 16 November 2019 by hub
 * Updated by S-B99 on 15/12/19
 */
@Module.Info(name = "HoleESP", category = Module.Category.RENDER, description = "Show safe holes for crystal pvp")
public class HoleESP extends Module {

    private final BlockPos[] surroundOffset = {
            new BlockPos(0, -1, 0), // down
            new BlockPos(0, 0, -1), // north
            new BlockPos(1, 0, 0), // east
            new BlockPos(0, 0, 1), // south
            new BlockPos(-1, 0, 0) // west
    };

    private Setting<Double> renderDistance = register(Settings.d("Render Distance", 8.0d));
    private Setting<Integer> a0 = register(Settings.integerBuilder("Transparency").withMinimum(0).withValue(32).withMaximum(255).build());
    private Setting<Integer> r1 = register(Settings.integerBuilder("Red (Obby)").withMinimum(0).withValue(208).withMaximum(255).build()); // 144
    private Setting<Integer> g1 = register(Settings.integerBuilder("Green (Obby)").withMinimum(0).withValue(144).withMaximum(255).build());
    private Setting<Integer> b1 = register(Settings.integerBuilder("Blue (Obby)").withMinimum(0).withValue(255).withMaximum(255).build());
    private Setting<Integer> r2 = register(Settings.integerBuilder("Red (Bedrock)").withMinimum(0).withValue(144).withMaximum(255).build()); // 208
    private Setting<Integer> g2 = register(Settings.integerBuilder("Green (Bedrock)").withMinimum(0).withValue(144).withMaximum(255).build());
    private Setting<Integer> b2 = register(Settings.integerBuilder("Blue (Bedrock)").withMinimum(0).withValue(255).withMaximum(255).build());
    private Setting<RenderMode> renderModeSetting = register(Settings.e("Render Mode", RenderMode.BLOCK));
    private Setting<RenderBlocks> renderBlocksSetting = register(Settings.e("Render", RenderBlocks.BOTH));

    private ConcurrentHashMap<BlockPos, Boolean> safeHoles;

    private enum RenderMode {
        DOWN, BLOCK
    }

    private enum RenderBlocks {
        OBBY, BEDROCK, BOTH
    }

    @Override
    public void onUpdate() {
    	boolean surrounded = false;
        if (safeHoles == null) {
            safeHoles = new ConcurrentHashMap<>();
        } else {
            safeHoles.clear();
        }

        int range = (int) Math.ceil(renderDistance.getValue());

        CrystalAura crystalAura = (CrystalAura) ModuleManager.getModuleByName("CrystalAura");
        List<BlockPos> blockPosList = crystalAura.getSphere(getPlayerPos(), range, range, false, true, 0);

        for (BlockPos pos : blockPosList) {

            // block gotta be air
            if (!mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
                continue;
            }

            // block 1 above gotta be air
            if (!mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR)) {
                continue;
            }

            // block 2 above gotta be air
            if (!mc.world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR)) {
                continue;
            }

            boolean isSafe = true;
            boolean isBedrock = true;

            for (BlockPos offset : surroundOffset) {
                Block block = mc.world.getBlockState(pos.add(offset)).getBlock();
                if (block != Blocks.BEDROCK) {
                    isBedrock = false;
                }
                if (block != Blocks.BEDROCK && block != Blocks.OBSIDIAN && block != Blocks.ENDER_CHEST && block != Blocks.ANVIL) {
                    isSafe = false;
                    break;
                }
            }

            if (isSafe) {
                safeHoles.put(pos, isBedrock);
            }
            if (mc.player.getPosition() == pos) {
            	surrounded = true;
            }
            if (ModuleManager.isModuleEnabled("Auto Hole Fill") && mc.player != null) {
            	double distanceFromPlayer = mc.player.getPosition().distanceSq(pos.getX(), pos.getY(), pos.getZ());
            	if (distanceFromPlayer > 4.5 && surrounded == true) {
            		mc.player.connection.sendPacket(new CPacketAnimation(mc.player.getActiveHand()));
            		placeBlock(pos);
            	}
            }

        }

    }

    @Override
    public void onWorldRender(final RenderEvent event) {

        if (mc.player == null || safeHoles == null) {
            return;
        }

        if (safeHoles.isEmpty()) {
            return;
        }

        KamiTessellator.prepare(GL11.GL_QUADS);
            safeHoles.forEach((blockPos, isBedrock) -> {
                switch (renderBlocksSetting.getValue()) {
                    case BOTH:
                        if (isBedrock) {
                            drawBox(blockPos, r2.getValue(), g2.getValue(), b2.getValue());
                        }
                        else {
                            drawBox(blockPos, r1.getValue(), g1.getValue(), b1.getValue());
                        }
                        break;
                    case OBBY:
                        if (!isBedrock) {
                            drawBox(blockPos, r1.getValue(), g1.getValue(), b1.getValue());
                        }
                        break;
                    case BEDROCK:
                        if (isBedrock) {
                            drawBox(blockPos, r2.getValue(), g2.getValue(), b2.getValue());
                        }
                        break;
                }
            });
        KamiTessellator.release();
    }
    
    private int findObiInHotbar() {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = Wrapper.getPlayer().inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) stack.getItem()).getBlock();
                if (block instanceof BlockObsidian) {
                    slot = i;
                    break;
                }
            }
        }
        return slot;
    }
    
    private void drawBox(BlockPos blockPos, int r, int g, int b) {
        Color color = new Color(r, g, b, a0.getValue());
        if (renderModeSetting.getValue().equals(RenderMode.DOWN)) {
            KamiTessellator.drawBox(blockPos, color.getRGB(), GeometryMasks.Quad.DOWN);
        } else if (renderModeSetting.getValue().equals(RenderMode.BLOCK)) {
            KamiTessellator.drawBox(blockPos, color.getRGB(), GeometryMasks.Quad.ALL);
        }
    }
    public void placeBlock(BlockPos pos) {
    	Vec3d eyesPos = new Vec3d(Wrapper.getPlayer().posX, Wrapper.getPlayer().posY + (double) Wrapper.getPlayer().getEyeHeight(), Wrapper.getPlayer().posZ);
        EnumFacing[] var3 = EnumFacing.values();
        for (EnumFacing side : var3) {
            BlockPos neighbor = pos.offset(side);
            EnumFacing side2 = side.getOpposite();
            Vec3d hitVec = (new Vec3d(neighbor)).add(0.5D, 0.5D, 0.5D).add((new Vec3d(side2.getDirectionVec())).scale(0.5D));
            int obiSlot = findObiInHotbar();
            if (obiSlot == -1) {
            	return;
            }
            mc.player.connection.sendPacket(new CPacketHeldItemChange(obiSlot));
            mc.playerController.processRightClickBlock(Wrapper.getPlayer(), mc.world, neighbor, side2, hitVec, EnumHand.MAIN_HAND);
            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
        }
    }
}

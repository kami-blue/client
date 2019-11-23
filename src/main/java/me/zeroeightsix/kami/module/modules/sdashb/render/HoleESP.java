package me.zeroeightsix.kami.module.modules.sdashb.render;

import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.GeometryMasks;
import me.zeroeightsix.kami.util.KamiTessellator;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import sun.awt.image.PixelConverter;

import java.util.ArrayList;

/***
 * @author Waizy
 * S-B99 added modes
 */
@Module.Info(name = "HoleESP", category = Module.Category.RENDER)
public class HoleESP
        extends Module {
    private ArrayList<BlockPos> holes = new ArrayList();

    //private Setting<Integer> range = register(Settings.i("Range", 15));
    private Setting<Distance> distance = register(Settings.e("Distance", Distance.CLOSE));
    private Setting<Integer> red = register(Settings.integerBuilder("Red").withRange(0, 255).withValue(150));
    private Setting<Integer> green = register(Settings.integerBuilder("Green").withRange(0, 255).withValue(255));
    private Setting<Integer> blue = register(Settings.integerBuilder("Blue").withRange(0, 255).withValue(255));
    private Setting<Integer> alpha = register(Settings.integerBuilder("Opacity").withRange(0, 255).withValue(50));
//    private Setting<Distance> color = register(Settings.e("Color", Color.KAMI));


    private enum Distance {
        CLOSE, FAR
    }

//    private enum Color {
//        WHITE, KAMI
//    }

    BlockPos pos;

    public void onUpdate() {
        this.holes = new ArrayList();

        if (distance.getValue().equals(Distance.CLOSE)) {
            Iterable<BlockPos> blocks = BlockPos.getAllInBox(mc.player.getPosition().add(-5, -5, -5), mc.player.getPosition().add(5, 5, 5));
            for (BlockPos pos : blocks) {
                if (!mc.world.getBlockState(pos).getMaterial().blocksMovement() && !mc.world.getBlockState(pos.add(0, 1, 0)).getMaterial().blocksMovement()) {
                    boolean solidNeighbours = (
                            mc.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.BEDROCK | mc.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.OBSIDIAN
                                    && mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.BEDROCK | mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.OBSIDIAN
                                    && mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.BEDROCK | mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.OBSIDIAN
                                    && mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.BEDROCK | mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.OBSIDIAN
                                    && mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.BEDROCK | mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.OBSIDIAN
                                    && mc.world.getBlockState(pos.add(0, 0, 0)).getMaterial() == Material.AIR
                                    && mc.world.getBlockState(pos.add(0, 1, 0)).getMaterial() == Material.AIR
                                    && mc.world.getBlockState(pos.add(0, 2, 0)).getMaterial() == Material.AIR);
                    if (solidNeighbours) {
                        this.holes.add(pos);
                    }
                }
            }
        }
        if (distance.getValue().equals(Distance.FAR)) {
            Iterable<BlockPos> blocks = BlockPos.getAllInBox(mc.player.getPosition().add(-20, -20, -20), mc.player.getPosition().add(20, 20, 20));
            for (BlockPos pos : blocks) {
                if (!mc.world.getBlockState(pos).getMaterial().blocksMovement() && !mc.world.getBlockState(pos.add(0, 1, 0)).getMaterial().blocksMovement()) {
                    boolean solidNeighbours = (
                            mc.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.BEDROCK | mc.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.OBSIDIAN
                                    && mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.BEDROCK | mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.OBSIDIAN
                                    && mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.BEDROCK | mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.OBSIDIAN
                                    && mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.BEDROCK | mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.OBSIDIAN
                                    && mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.BEDROCK | mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.OBSIDIAN
                                    && mc.world.getBlockState(pos.add(0, 0, 0)).getMaterial() == Material.AIR
                                    && mc.world.getBlockState(pos.add(0, 1, 0)).getMaterial() == Material.AIR
                                    && mc.world.getBlockState(pos.add(0, 2, 0)).getMaterial() == Material.AIR);
                    if (solidNeighbours) {
                        this.holes.add(pos);
                    }
                }
            }
        }
    }


    public void onWorldRender(RenderEvent event) {
        int redS = red.getValue();
        int greenS = green.getValue();
        int blueS = blue.getValue();
        int alphaS = alpha.getValue();
//        int realColor = 0;
//        if (color.getValue().equals(Color.WHITE)) {
//            realColor = 0x33ffffff;
//        }
//        if (color.getValue().equals(Color.KAMI)) {
//            realColor = 0x339B90FF;
//        }
//        int finalRealColor = realColor;
        KamiTessellator.prepare(GL11.GL_QUADS);
        this.holes.forEach(blockPos -> KamiTessellator.drawBox((BlockPos) blockPos, redS, greenS, blueS, alphaS, GeometryMasks.Quad.ALL));
        KamiTessellator.release();
    }
}

package me.zeroeightsix.kami.util.astar;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;

/**
 * Created by fred41 on 18/07/2020.
 */
public class AstarNode {
    public boolean blocked;
    public double g = 0;
    public AstarNode pred;
    public static HashSet<Block> noclipblocks = new HashSet<>();

    static {
        noclipblocks.add(Blocks.AIR);
        noclipblocks.add(Blocks.PORTAL);
        noclipblocks.add(Blocks.STANDING_SIGN);
        noclipblocks.add(Blocks.WALL_SIGN);
        noclipblocks.add(Blocks.FIRE);
        noclipblocks.add(Blocks.TORCH);
        noclipblocks.add(Blocks.REDSTONE_TORCH);
        noclipblocks.add(Blocks.REDSTONE_WIRE);
        noclipblocks.add(Blocks.END_ROD);
        noclipblocks.add(Blocks.VINE);
        noclipblocks.add(Blocks.DEADBUSH);
        noclipblocks.add(Blocks.CARPET);
        noclipblocks.add(Blocks.WHEAT);
        noclipblocks.add(Blocks.STANDING_BANNER);
        noclipblocks.add(Blocks.WALL_BANNER);
        noclipblocks.add(Blocks.SAPLING);
        noclipblocks.add(Blocks.GOLDEN_RAIL);
        noclipblocks.add(Blocks.DETECTOR_RAIL);
        noclipblocks.add(Blocks.RAIL);
        noclipblocks.add(Blocks.ACTIVATOR_RAIL);
        noclipblocks.add(Blocks.TALLGRASS);
        noclipblocks.add(Blocks.YELLOW_FLOWER);
        noclipblocks.add(Blocks.RED_FLOWER);
        noclipblocks.add(Blocks.LADDER);
        noclipblocks.add(Blocks.STONE_PRESSURE_PLATE);
        noclipblocks.add(Blocks.WOODEN_PRESSURE_PLATE);
        noclipblocks.add(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
        noclipblocks.add(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
        noclipblocks.add(Blocks.LIT_REDSTONE_ORE);
        noclipblocks.add(Blocks.UNLIT_REDSTONE_TORCH);
        noclipblocks.add(Blocks.STONE_BUTTON);
        noclipblocks.add(Blocks.WOODEN_BUTTON);
        noclipblocks.add(Blocks.REEDS);
        noclipblocks.add(Blocks.BROWN_MUSHROOM);
        noclipblocks.add(Blocks.RED_MUSHROOM);
        noclipblocks.add(Blocks.NETHER_WART);
    }

    public BlockPos pos;
    public static Minecraft mc = Minecraft.getMinecraft();
    public double f;

    public AstarNode(BlockPos pos, int addlayer) {
        this.pos = pos;
        Block b = mc.world.getBlockState(pos).getBlock();
        if (addlayer != 0) {
            Block b1 = mc.world.getBlockState(pos.add(0, addlayer, 0)).getBlock();
            blocked = !(b.isPassable(mc.world, pos) || noclipblocks.contains(b)) || !(b.isPassable(mc.world, pos.add(0, addlayer, 0)) || noclipblocks.contains(b1));
        } else {
            blocked = !(b.isPassable(mc.world, pos) || noclipblocks.contains(b));
        }
    }
}

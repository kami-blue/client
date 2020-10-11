package me.zeroeightsix.kami.util.astar;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

/**
 * Created by fred41 on 18/07/2020.
 */
public class AstarGrid {
    public HashMap<BlockPos, AstarNode> nodes;

    public AstarGrid() {
        nodes = new HashMap<>();
    }
}

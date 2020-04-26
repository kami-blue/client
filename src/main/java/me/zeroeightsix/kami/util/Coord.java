package me.zeroeightsix.kami.util;

import net.minecraft.util.math.BlockPos;

/**
 * @author wnuke
 * Created by wnuke on 17/04/20
 */

public class Coord {
    public int x;
    public int y;
    public int z;
    public boolean chunk;
    public String name;
    public String time;
    public String date;

    public BlockPos getPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public boolean isChunkCoord() {
        return this.chunk;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }
}

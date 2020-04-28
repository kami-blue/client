package me.zeroeightsix.kami.util;

import net.minecraft.util.math.BlockPos;

/**
 * @author wnuke
 * Created by wnuke on 17/04/20
 */

public class CoordinateInfo {
    public BlockPos xyz;
    public String name;
    public String time;
    public String date;

    public CoordinateInfo(int x, int y, int z, String nameSet, String timeSet) {
        xyz = new BlockPos(x, y, z);
        name = nameSet;
        time = timeSet;
    }

    public CoordinateInfo(BlockPos pos, String nameSet, String timeSet) {
        xyz = pos;
        name = nameSet;
        time = timeSet;
    }

    public BlockPos getPos() {
        return xyz;
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

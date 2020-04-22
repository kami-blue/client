package me.zeroeightsix.kami.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for managing and transforming blockPos positions
 *
 * @author Qther / Vonr
 * Updated by dominikaaaa on 21/04/20
 */
public class VectorUtil {

    /**
     * Gets distance between two vectors
     *
     * @param vecA First Vector
     * @param vecB Second Vector
     * @return the distance between two vectors
     */
    public static double getDistance(Vec3d vecA, Vec3d vecB) {
        return Math.sqrt(Math.pow(vecA.x - vecB.x, 2) + Math.pow(vecA.y - vecB.y, 2) + Math.pow(vecA.z - vecB.z, 2));
    }

    /**
     * Gets vectors between two given vectors (startVec and destinationVec) every (distance between the given vectors) / steps
     *
     * @param startVec Beginning vector
     * @param destinationVec Ending vector
     * @param steps distance between given vectors
     * @return all vectors between startVec and destinationVec divided by steps
     */
    public static ArrayList<Vec3d> extendVec(Vec3d startVec, Vec3d destinationVec, int steps) {
        ArrayList<Vec3d> returnList = new ArrayList<>(steps + 1);
        double stepDistance = getDistance(startVec, destinationVec) / steps;

        for (int i = 0; i < Math.max(steps, 1) + 1; i++) {
            returnList.add(advanceVec(startVec, destinationVec, stepDistance * i));
        }

        return returnList;
    }

    // Returns

    /**
     * Moves a vector towards a destination based on distance
     *
     * @param startVec Starting vector
     * @param destinationVec returned vector
     * @param distance distance to move startVec by
     * @return vector based on startVec that is moved towards destinationVec by distance
     */
    public static Vec3d advanceVec(Vec3d startVec, Vec3d destinationVec, double distance) {
        Vec3d advanceDirection = destinationVec.subtract(startVec).normalize();
        if (destinationVec.distanceTo(startVec) < distance) return destinationVec;
        return advanceDirection.scale(distance);
    }

    /**
     * Get all rounded block positions inside a 3-dimensional area between pos1 and pos2.
     *
     * @param pos1 Starting vector
     * @param pos2 Ending vector
     * @return rounded block positions inside a 3d area between pos1 and pos2
     */
    public static List<BlockPos> getBlockPositionsInArea(Vec3d pos1, Vec3d pos2) {
        int minX = (int) Math.round(Math.min(pos1.x, pos2.x));
        int maxX = (int) Math.round(Math.max(pos1.x, pos2.x));

        int minY = (int) Math.round(Math.min(pos1.y, pos2.y));
        int maxY = (int) Math.round(Math.max(pos1.y, pos2.y));

        int minZ = (int) Math.round(Math.min(pos1.z, pos2.z));
        int maxZ = (int) Math.round(Math.max(pos1.z, pos2.z));

        return getBlockPos(minX, maxX, minY, maxY, minZ, maxZ);
    }

    /**
     * Get all block positions inside a 3d area between pos1 and pos2
     *
     * @param pos1 Starting blockPos
     * @param pos2 Ending blockPos
     * @return block positions inside a 3d area between pos1 and pos2
     */
    public static List<BlockPos> getBlockPositionsInArea(BlockPos pos1, BlockPos pos2) {
        int minX = Math.min(pos1.x, pos2.x);
        int maxX = Math.max(pos1.x, pos2.x);

        int minY = Math.min(pos1.y, pos2.y);
        int maxY = Math.max(pos1.y, pos2.y);

        int minZ = Math.min(pos1.z, pos2.z);
        int maxZ = Math.max(pos1.z, pos2.z);

        return getBlockPos(minX, maxX, minY, maxY, minZ, maxZ);
    }

    /**
     * Get a block pos with the Y level as the highest terrain level
     *
     * @param pos blockPos
     * @return blockPos with highest Y level terrain
     */
    public static BlockPos getHighestTerrainPos(BlockPos pos) {
        for (int i = pos.y ; i >= 0 ; i--) {
            Block block = Wrapper.getWorld().getBlockState(new BlockPos(pos.getX(), i, pos.getZ())).getBlock();
            boolean replaceable = Wrapper.getWorld().getBlockState(new BlockPos(pos.getX(), i, pos.getZ())).getMaterial().isReplaceable();
            if (!(block instanceof BlockAir) && !replaceable) {
                return new BlockPos(pos.getX(), i, pos.getZ());
            }
        }
        return new BlockPos(pos.getX(), 0, pos.getZ());
    }

    private static List<BlockPos> getBlockPos(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        ArrayList<BlockPos> returnList = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    returnList.add(new BlockPos(x, y, z));
                }
            }
        }

        return returnList;
    }
}

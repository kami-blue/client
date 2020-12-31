package me.zeroeightsix.kami.util.math

import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.kamiblue.commons.extension.ceilToInt
import org.kamiblue.commons.extension.floorToInt
import kotlin.math.*

/**
 * Utilities for managing and transforming blockPos positions
 *
 * @author Qther / Vonr
 * Updated by l1ving on 21/04/20.
 * Updated by Xiaro on 09/09/20
 */
object VectorUtils {

    /**
     * Get all block positions inside a 3d area between pos1 and pos2
     *
     * @param pos1 Starting blockPos
     * @param pos2 Ending blockPos
     * @return block positions inside a 3d area between pos1 and pos2
     */
    fun getBlockPositionsInArea(pos1: BlockPos, pos2: BlockPos): List<BlockPos> {
        val minX = min(pos1.x, pos2.x)
        val maxX = max(pos1.x, pos2.x)
        val minY = min(pos1.y, pos2.y)
        val maxY = max(pos1.y, pos2.y)
        val minZ = min(pos1.z, pos2.z)
        val maxZ = max(pos1.z, pos2.z)
        return getBlockPos(minX, maxX, minY, maxY, minZ, maxZ)
    }

    private fun getBlockPos(minX: Int, maxX: Int, minY: Int, maxY: Int, minZ: Int, maxZ: Int): List<BlockPos> {
        val returnList = ArrayList<BlockPos>()
        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                for (y in minY..maxY) {
                    returnList.add(BlockPos(x, y, z))
                }
            }
        }
        return returnList
    }

    /**
     * Get all block positions inside a sphere with given [radius]
     *
     * @param center Center of the sphere
     * @param radius Radius of the sphere
     * @return block positions inside a sphere with given [radius]
     */
    fun getBlockPosInSphere(center: Vec3d, radius: Float): ArrayList<BlockPos> {
        val squaredRadius = radius.pow(2)
        val posList = ArrayList<BlockPos>()
        for (x in getAxisRange(center.x, radius)) for (y in getAxisRange(center.y, radius)) for (z in getAxisRange(center.z, radius)) {
            /* Valid position check */
            val blockPos = BlockPos(x, y, z)
            if (blockPos.distanceSqToCenter(center.x, center.y, center.z) > squaredRadius) continue
            posList.add(blockPos)
        }
        return posList
    }

    private fun getAxisRange(d1: Double, d2: Float): IntRange {
        return IntRange((d1 - d2).floorToInt(), (d1 + d2).ceilToInt())
    }

    fun Vec3d.toBlockPos(): BlockPos {
        return BlockPos(x.floorToInt(), y.floorToInt(), z.floorToInt())
    }

    fun BlockPos.toVec3d(): Vec3d {
        return toVec3d(0.0, 0.0, 0.0)
    }

    fun BlockPos.toVec3d(xOffset: Double, yOffset: Double, zOffset: Double): Vec3d {
        return Vec3d(x + 0.5 + xOffset, y + 0.5 + yOffset, z + 0.5 + zOffset)
    }

    fun Vec3i.distanceTo(vec3i: Vec3i): Double {
        val xDiff = vec3i.x - x
        val yDiff = vec3i.y - y
        val zDiff = vec3i.z - z
        return sqrt((xDiff * xDiff + yDiff * yDiff + zDiff * zDiff).toDouble())
    }

    fun Vec3d.distanceTo(vec3i: Vec3i): Double {
        val xDiff = vec3i.x + 0.5 - x
        val yDiff = vec3i.y + 0.5 - y
        val zDiff = vec3i.z + 0.5 - z
        return sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff)
    }

    fun Entity.distanceTo(vec3i: Vec3i): Double {
        val xDiff = vec3i.x + 0.5 - posX
        val yDiff = vec3i.y + 0.5 - posY
        val zDiff = vec3i.z + 0.5 - posZ
        return sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff)
    }

    fun Entity.distanceTo(vec3d: Vec3d): Double {
        val xDiff = vec3d.x - posX
        val yDiff = vec3d.y - posY
        val zDiff = vec3d.z - posZ
        return sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff)
    }

    fun Entity.distanceTo(chunkPos: ChunkPos): Double {
        return hypot(chunkPos.x * 16 + 8 - posX, chunkPos.z * 16 + 8 - posZ)
    }
}

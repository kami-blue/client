package org.kamiblue.client.util.world

import net.minecraft.entity.Entity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.util.Wrapper
import org.kamiblue.client.util.math.VectorUtils.toVec3dCenter
import kotlin.math.floor

fun World.isLiquidBelow(entity: Entity): Boolean {
    val results = rayTraceBoundingBoxToGround(entity, true)
    if (results.all { it.typeOfHit == RayTraceResult.Type.MISS || it.hitVec?.y ?: 911.0 < 0.0 }) {
        return true
    }

    val pos = results.maxByOrNull { it.hitVec?.y ?: -69420.0 }?.blockPos ?: return false
    return isLiquid(pos)
}

private fun World.rayTraceBoundingBoxToGround(entity: Entity, stopOnLiquid: Boolean): List<RayTraceResult> {
    val boundingBox = entity.entityBoundingBox
    val xArray = arrayOf(floor(boundingBox.minX), floor(boundingBox.maxX))
    val zArray = arrayOf(floor(boundingBox.minZ), floor(boundingBox.maxZ))

    val results = ArrayList<RayTraceResult>(4)

    for (x in xArray) {
        for (z in zArray) {
            val result = rayTraceToGround(Vec3d(x, boundingBox.minY, z), stopOnLiquid)
            if (result != null) {
                results.add(result)
            }
        }
    }

    return results
}

fun World.getGroundPos(entity: Entity): Vec3d {
    val results = rayTraceBoundingBoxToGround(entity, false)
    if (results.all { it.typeOfHit == RayTraceResult.Type.MISS || it.hitVec?.y ?: 911.0 < 0.0 }) {
        return Vec3d(0.0, -999.0, 0.0)
    }

    return results.maxByOrNull { it.hitVec?.y ?: -69420.0 }?.hitVec ?: Vec3d(0.0, -69420.0, 0.0)
}

private fun World.rayTraceToGround(vec3d: Vec3d, stopOnLiquid: Boolean): RayTraceResult? {
    return this.rayTrace(
        vec3d,
        Vec3d(vec3d.x, -1.0, vec3d.z),
        stopOnLiquid,
        ignoreBlockWithoutBoundingBox = true,
        returnLastUncollidableBlock = false
    )
}

fun World.isVisible(
    pos: BlockPos,
    tolerance: Double = 1.0
) = Wrapper.player?.let {
    val center = pos.toVec3dCenter()
    val result = rayTrace(
        it.getPositionEyes(1.0f),
        center,
        stopOnLiquid = false,
        ignoreBlockWithoutBoundingBox = true
    )

    result != null
        && (result.blockPos == pos
        || (result.hitVec != null && result.hitVec.distanceTo(center) <= tolerance))
} ?: false

fun World.rayTrace(
    start: Vec3d,
    end: Vec3d,
    stopOnLiquid: Boolean = false,
    ignoreBlockWithoutBoundingBox: Boolean = false,
    returnLastUncollidableBlock: Boolean = false
): RayTraceResult? =
    this.rayTraceBlocks(start, end, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock)

fun World.isLiquid(pos: BlockPos): Boolean {
    return this.getBlockState(pos).isLiquid
}

fun World.isWater(pos: BlockPos): Boolean {
    return this.getBlockState(pos).isWater
}

fun SafeClientEvent.hasNeighbour(pos: BlockPos): Boolean {
    return EnumFacing.values().any {
        !world.getBlockState(pos.offset(it)).isReplaceable
    }
}

/**
 * Checks if given [pos] is able to place block in it
 *
 * @return true playing is not colliding with [pos] and there is block below it
 */
fun World.isPlaceable(pos: BlockPos, ignoreSelfCollide: Boolean = false) =
    this.getBlockState(pos).isReplaceable
        && this.checkNoEntityCollision(AxisAlignedBB(pos), if (ignoreSelfCollide) Wrapper.player else null)
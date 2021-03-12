package org.kamiblue.client.util.math

import org.kamiblue.client.util.math.VectorUtils.plus
import org.kamiblue.client.util.math.VectorUtils.times
import org.kamiblue.client.util.math.VectorUtils.toVec3d
import org.kamiblue.client.util.math.VectorUtils.toViewVec
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import org.kamiblue.client.util.Wrapper

val AxisAlignedBB.xLength get() = maxX - minX

val AxisAlignedBB.yLength get() = maxY - minY

val AxisAlignedBB.zLength get() = maxY - minY

val AxisAlignedBB.lengths get() = Vec3d(xLength, yLength, zLength)

fun AxisAlignedBB.corners(scale: Double): Array<Vec3d> {
    val growSizes = lengths * (scale - 1.0)
    return grow(growSizes.x, growSizes.y, growSizes.z).corners()
}

fun AxisAlignedBB.corners() = arrayOf(
    Vec3d(minX, minY, minZ),
    Vec3d(minX, minY, maxZ),
    Vec3d(minX, maxY, minZ),
    Vec3d(minX, maxY, maxZ),
    Vec3d(maxX, minY, minZ),
    Vec3d(maxX, minY, maxZ),
    Vec3d(maxX, maxY, minZ),
    Vec3d(maxX, maxY, maxZ),
)

fun AxisAlignedBB.side(side: EnumFacing, scale: Double = 0.5): Vec3d {
    val lengths = lengths
    val sideDirectionVec = side.directionVec.toVec3d()
    return lengths * sideDirectionVec * scale + center
}

/**
 * Check if a box is in sight
 */
fun AxisAlignedBB.isInSight(
    posFrom: Vec3d = Wrapper.player?.getPositionEyes(1.0f) ?: Vec3d.ZERO,
    rotation: Vec2f = Wrapper.player?.let { Vec2f(it) } ?: Vec2f.ZERO,
    range: Double = 4.25,
    tolerance: Double = 1.05
) = isInSight(posFrom, rotation.toViewVec(), range, tolerance)

/**
 * Check if a box is in sight
 */
fun AxisAlignedBB.isInSight(
    posFrom: Vec3d,
    viewVec: Vec3d,
    range: Double = 4.25,
    tolerance: Double = 1.05
): RayTraceResult? {
    val sightEnd = posFrom.add(viewVec.scale(range))

    return grow(tolerance - 1.0).calculateIntercept(posFrom, sightEnd)
}
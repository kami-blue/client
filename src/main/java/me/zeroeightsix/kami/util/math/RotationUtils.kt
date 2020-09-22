package me.zeroeightsix.kami.util.math

import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.entity.Entity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.*

/**
 * Utils for calculating angles and rotations
 */
object RotationUtils {
    val mc = Wrapper.minecraft

    fun faceEntityClosest(entity: Entity, pTicks: Float = 1f) {
        val rotation = getRotationToEntityClosest(entity, pTicks)
        mc.player.rotationYaw = rotation.x.toFloat()
        mc.player.rotationPitch = rotation.y.toFloat()
    }

    fun faceEntity(entity: Entity, pTicks: Float = 1f) {
        val rotation = getRotationToEntity(entity, pTicks)
        mc.player.rotationYaw = rotation.x.toFloat()
        mc.player.rotationPitch = rotation.y.toFloat()
    }

    fun getRelativeRotation(entity: Entity, pTicks: Float = 1f): Double {
        return getRotationDiff(getRotationToEntity(entity, pTicks), getPlayerRotation())
    }

    fun getRelativeRotation(posTo: Vec3d, pTicks: Float = 1f): Double {
        return getRotationDiff(getRotationTo(posTo, true, pTicks), getPlayerRotation())
    }

    fun getPlayerRotation(pTicks: Float = 1f): Vec2d {
        val rotation = Vec2d(mc.player.rotationYaw.toDouble(), mc.player.rotationPitch.toDouble())
        val prevRotation = Vec2d(mc.player.rotationYaw.toDouble(), mc.player.rotationPitch.toDouble())
        return prevRotation.add(rotation.subtract(prevRotation).multiply(pTicks.toDouble()))
    }

    fun getRotationDiff(r1: Vec2d, r2: Vec2d): Double {
        val r1Radians = r1.toRadians()
        val r2Radians = r2.toRadians()
        return Math.toDegrees(acos(cos(r1Radians.y) * cos(r2Radians.y) * cos(r1Radians.x - r2Radians.x) + sin(r1Radians.y) * sin(r2Radians.y)))
    }

    fun getRotationToEntityClosest(entity: Entity, pTicks: Float = 1f): Vec2d {
        val box = entity.boundingBox
        val eyePos = mc.player.getPositionEyes(1f)
        val x = MathHelper.clamp(eyePos.x, box.minX + 0.1, box.maxX - 0.1)
        val y = MathHelper.clamp(eyePos.y, box.minY + 0.1, box.maxY - 0.1)
        val z = MathHelper.clamp(eyePos.z, box.minZ + 0.1, box.maxZ - 0.1)
        val hitVec = Vec3d(x, y, z)
        return getRotationTo(hitVec, true, pTicks)
    }

    fun getRotationToEntity(entity: Entity, pTicks: Float = 1f): Vec2d {
        val posTo = EntityUtils.getInterpolatedPos(entity, pTicks)
        return getRotationTo(posTo, true)
    }

    /**
     * Get rotation from a player position to another position vector
     *
     * @param posTo Calculate rotation to this position vector
     * @param eyeHeight Use player eye position to calculate
     * @return [Pair]<Yaw, Pitch>
     */
    @JvmStatic
    fun getRotationTo(posTo: Vec3d, eyeHeight: Boolean, pTicks: Float = 1f): Vec2d {
        val player = mc.player
        val posFrom = if (eyeHeight) player.getPositionEyes(pTicks)
        else EntityUtils.getInterpolatedPos(player, pTicks)
        return getRotationTo(posFrom, posTo)
    }

    /**
     * Get rotation from a position vector to another position vector
     *
     * @param posFrom Calculate rotation from this position vector
     * @param posTo Calculate rotation to this position vector
     * @return [Pair]<Yaw, Pitch>
     */
    fun getRotationTo(posFrom: Vec3d, posTo: Vec3d): Vec2d {
        return getRotationFromVec(posTo.subtract(posFrom))
    }

    fun getRotationFromVec(vec: Vec3d): Vec2d {
        val xz = sqrt(vec.x * vec.x + vec.z * vec.z)
        val yaw = normalizeAngle(Math.toDegrees(atan2(vec.z, vec.x)) - 90.0)
        val pitch = normalizeAngle(Math.toDegrees(-atan2(vec.y, xz)))
        return Vec2d(yaw, pitch)
    }

    @JvmStatic
    fun normalizeAngle(angleIn: Double): Double {
        var angle = angleIn
        angle %= 360.0
        if (angle >= 180.0) {
            angle -= 360.0
        }
        if (angle < -180.0) {
            angle += 360.0
        }
        return angle
    }

    @JvmStatic
    fun normalizeAngle(angleIn: Float): Float {
        var angle = angleIn
        angle %= 360f
        if (angle >= 180f) {
            angle -= 360f
        }
        if (angle < -180f) {
            angle += 360f
        }
        return angle
    }
}
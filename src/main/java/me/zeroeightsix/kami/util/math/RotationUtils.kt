package me.zeroeightsix.kami.util.math

import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import org.kamiblue.commons.extension.toDegree
import kotlin.math.*

/**
 * Utils for calculating angles and rotations
 */
object RotationUtils {
    val mc = Wrapper.minecraft

    fun faceEntityClosest(entity: Entity) {
        val rotation = getRotationToEntityClosest(entity)
        mc.player.rotationYaw = rotation.x
        mc.player.rotationPitch = rotation.y
    }

    fun getRelativeRotation(entity: Entity): Float {
        return getRelativeRotation(entity.entityBoundingBox.center)
    }

    fun getRelativeRotation(posTo: Vec3d): Float {
        return getRotationDiff(getRotationTo(posTo), Vec2f(mc.player))
    }

    private fun getRotationDiff(r1: Vec2f, r2: Vec2f): Float {
        val r1Radians = r1.toRadians()
        val r2Radians = r2.toRadians()
        return acos(cos(r1Radians.y) * cos(r2Radians.y) * cos(r1Radians.x - r2Radians.x) + sin(r1Radians.y) * sin(r2Radians.y)).toDegree()
    }

    fun getRotationToEntityClosest(entity: Entity): Vec2f {
        val box = entity.entityBoundingBox
        val eyePos = mc.player.getPositionEyes(1f)
        val x = eyePos.x.coerceIn(box.minX + 0.1, box.maxX - 0.1)
        val y = eyePos.y.coerceIn(box.minY + 0.1, box.maxY - 0.1)
        val z = eyePos.z.coerceIn(box.minZ + 0.1, box.maxZ - 0.1)
        val hitVec = Vec3d(x, y, z)
        return getRotationTo(hitVec)
    }

    fun getRotationToEntity(entity: Entity): Vec2f {
        return getRotationTo(entity.positionVector)
    }

    /**
     * Get rotation from a player position to another position vector
     *
     * @param posTo Calculate rotation to this position vector
     * @param eyeHeight Use player eye position to calculate
     * @return [Pair]<Yaw, Pitch>
     */
    fun getRotationTo(posTo: Vec3d): Vec2f {
        return getRotationTo(mc.player.getPositionEyes(1f), posTo)
    }

    /**
     * Get rotation from a position vector to another position vector
     *
     * @param posFrom Calculate rotation from this position vector
     * @param posTo Calculate rotation to this position vector
     * @return [Pair]<Yaw, Pitch>
     */
    fun getRotationTo(posFrom: Vec3d, posTo: Vec3d): Vec2f {
        return getRotationFromVec(posTo.subtract(posFrom))
    }

    fun getRotationFromVec(vec: Vec3d): Vec2f {
        val xz = hypot(vec.x, vec.z)
        val yaw = normalizeAngle(Math.toDegrees(atan2(vec.z, vec.x)) - 90.0)
        val pitch = normalizeAngle(Math.toDegrees(-atan2(vec.y, xz)))
        return Vec2f(yaw, pitch)
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
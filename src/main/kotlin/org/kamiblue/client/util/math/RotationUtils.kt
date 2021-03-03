package org.kamiblue.client.util.math

import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.commons.extension.toDegree
import kotlin.math.*

/**
 * Utils for calculating angles and rotations
 */
object RotationUtils {
    fun SafeClientEvent.faceEntityClosest(entity: Entity) {
        val rotation = getRotationToEntityClosest(entity)
        player.rotationYaw = rotation.x
        player.rotationPitch = rotation.y
    }

    fun SafeClientEvent.getRelativeRotation(entity: Entity): Float {
        return getRelativeRotation(entity.entityBoundingBox.center)
    }

    fun SafeClientEvent.getRelativeRotation(posTo: Vec3d): Float {
        return getRotationDiff(getRotationTo(posTo), Vec2f(player))
    }

    private fun getRotationDiff(r1: Vec2f, r2: Vec2f): Float {
        val r1Radians = r1.toRadians()
        val r2Radians = r2.toRadians()
        return acos(cos(r1Radians.y) * cos(r2Radians.y) * cos(r1Radians.x - r2Radians.x) + sin(r1Radians.y) * sin(r2Radians.y)).toDegree()
    }

    fun SafeClientEvent.getRotationToEntityClosest(entity: Entity): Vec2f {
        val box = entity.entityBoundingBox

        val eyePos = player.getPositionEyes(1f)

        if (player.entityBoundingBox.intersects(box)) {
            return getRotationTo(eyePos, box.center)
        }

        val x = eyePos.x.coerceIn(box.minX, box.maxX)
        val y = eyePos.y.coerceIn(box.minY, box.maxY)
        val z = eyePos.z.coerceIn(box.minZ, box.maxZ)

        val hitVec = Vec3d(x, y, z)
        return getRotationTo(eyePos, hitVec)
    }

    fun SafeClientEvent.getRotationToEntity(entity: Entity): Vec2f {
        return getRotationTo(entity.positionVector)
    }

    /**
     * Get rotation from a player position to another position vector
     *
     * @param posTo Calculate rotation to this position vector
     */
    fun SafeClientEvent.getRotationTo(posTo: Vec3d): Vec2f {
        return getRotationTo(player.getPositionEyes(1f), posTo)
    }

    /**
     * Get rotation from a position vector to another position vector
     *
     * @param posFrom Calculate rotation from this position vector
     * @param posTo Calculate rotation to this position vector
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
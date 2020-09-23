package me.zeroeightsix.kami.util.math

import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Utils for calculating angles and rotations
 */
object RotationUtils {
    val mc = Wrapper.minecraft

    fun faceEntity(entity: Entity) {
        val rotation = getRotationToEntity(entity)
        mc.player.rotationYaw = rotation.x.toFloat()
        mc.player.rotationPitch = rotation.y.toFloat()
    }

    fun getRotationToEntity(entity: Entity): Vec2d {
        val posTo = EntityUtils.getInterpolatedPos(entity, KamiTessellator.pTicks())
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
    fun getRotationTo(posTo: Vec3d, eyeHeight: Boolean): Vec2d {
        val player = mc.player
        val posFrom = if (eyeHeight) player.getPositionEyes(KamiTessellator.pTicks())
        else EntityUtils.getInterpolatedPos(player, KamiTessellator.pTicks())
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
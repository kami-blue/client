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
 *
 * Created by Xiaro on 20/08/20
 */
object RotationUtils {
    val mc = Wrapper.minecraft

    fun faceEntity(entity: Entity) {
        val rotation = getRotationToEntity(entity)
        mc.player.rotationYaw = rotation.first.toFloat()
        mc.player.rotationPitch = rotation.second.toFloat()
    }

    fun getRotationToEntity(entity: Entity): Pair<Double, Double> {
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
    fun getRotationTo(posTo: Vec3d, eyeHeight: Boolean): Pair<Double, Double> {
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
    fun getRotationTo(posFrom: Vec3d, posTo: Vec3d): Pair<Double, Double> {
        return getRotationFromVec(posTo.subtract(posFrom))
    }

    fun getRotationFromVec(vec: Vec3d): Pair<Double, Double> {
        val xz = sqrt(vec.x * vec.x + vec.z * vec.z)
        val yaw = normalizeAngle(Math.toDegrees(atan2(vec.z, vec.x)) - 90.0f)
        val pitch = normalizeAngle(Math.toDegrees(-atan2(vec.y, xz)))
        return Pair(yaw, pitch)
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
}
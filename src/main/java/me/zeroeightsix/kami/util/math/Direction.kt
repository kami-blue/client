package me.zeroeightsix.kami.util.math

import net.minecraft.entity.Entity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.Vec3i
import kotlin.math.roundToInt

/**
 * [EnumFacing] but with 45° directions
 */
@Suppress("UNUSED")
enum class Direction(val displayName: String, val displayNameXY: String, val directionVec: Vec3i) {
    NORTH("North", "-Z", Vec3i(0, 0, -1)),
    NORTH_EAST("North East", "+X -Z", Vec3i(1, 0, -1)),
    EAST("East", "+X", Vec3i(1, 0, 0)),
    SOUTH_EAST("South East", "+X +Z", Vec3i(1, 0, 1)),
    SOUTH("South", "+Z", Vec3i(0, 0, 1)),
    SOUTH_WEST("South West", "-X +Z", Vec3i(-1, 0, 1)),
    WEST("West", "-X", Vec3i(-1, 0, 0)),
    NORTH_WEST("North West", "-X -Z", Vec3i(-1, 0, -1));

    companion object {

        @JvmStatic
        fun fromEntity(entity: Entity?) = entity?.let {
            fromYaw(it.rotationYaw)
        } ?: NORTH

        fun fromYaw(yaw: Float): Direction {
            val normalizedYaw = (RotationUtils.normalizeAngle(yaw) + 180.0f).coerceIn(0.0f, 360.0f)
            val index = (normalizedYaw / 45.0f).roundToInt() % 8
            return values()[index]
        }

        fun EnumFacing.toDirection() = when (this) {
            EnumFacing.NORTH -> NORTH
            EnumFacing.EAST -> EAST
            EnumFacing.SOUTH -> SOUTH
            EnumFacing.WEST -> WEST
            else -> null
        }
    }
}
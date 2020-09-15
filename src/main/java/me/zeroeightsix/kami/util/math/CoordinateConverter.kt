package me.zeroeightsix.kami.util.math

import me.zeroeightsix.kami.util.Waypoint
import net.minecraft.util.math.BlockPos

object CoordinateConverter {
    /**
     * More efficient impl of [BlockPos.toString]
     */
    fun BlockPos.asString(): String {
        return "${this.x}, ${this.y}, ${this.z}"
    }

    fun toCurrent(dimension: Int, pos: BlockPos): BlockPos {
        return if (dimension == Waypoint.genDimension()) {
            pos
        } else {
            if (dimension == -1) {
                toOverworld(pos)
            } else {
                toNether(pos)
            }
        }
    }

    fun bothConverted(dimension: Int, pos: BlockPos): String {
        return if (dimension == -1) {
            "${toOverworld(pos).asString()} (${pos.asString()})"
        } else {
            "${pos.asString()} (${toNether(pos).asString()})"
        }
    }

    fun toNether(pos: BlockPos): BlockPos {
        return BlockPos(pos.x / 8, pos.y, pos.z / 8)
    }

    fun toOverworld(pos: BlockPos): BlockPos {
        return BlockPos(pos.x * 8, pos.y, pos.z * 8)
    }
}
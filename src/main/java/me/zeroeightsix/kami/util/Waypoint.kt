package me.zeroeightsix.kami.util

import com.google.gson.annotations.SerializedName
import me.zeroeightsix.kami.manager.mangers.WaypointManager
import me.zeroeightsix.kami.util.math.CoordinateConverter
import net.minecraft.util.math.BlockPos

class Waypoint(
        @SerializedName("position")
        val pos: BlockPos,

        @SerializedName("name")
        val name: String,

        @SerializedName("time") // NEEDS to stay "time" to maintain backwards compat
        val date: String
) {

    @SerializedName("id")
    val id: Int = genID()

    @SerializedName("server")
    val server: String? = WaypointManager.genServer() /* can be null from old configs */

    @SerializedName("dimension")
    val dimension: Int = WaypointManager.genDimension()

    fun asString(currentDimension: Boolean): String {
        return if (currentDimension) {
            "${currentPos().x}, ${currentPos().y}, ${currentPos().z}"
        } else {
            "${pos.x}, ${pos.y}, ${pos.z}"
        }
    }

    fun currentPos() = CoordinateConverter.toCurrent(dimension, pos)

    private fun genID(): Int = WaypointManager.waypoints.lastOrNull()?.id?.plus(1) ?: 0
}
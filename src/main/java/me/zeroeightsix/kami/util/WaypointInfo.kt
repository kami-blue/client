package me.zeroeightsix.kami.util

import com.google.gson.annotations.SerializedName
import me.zeroeightsix.kami.manager.mangers.FileInstanceManager
import me.zeroeightsix.kami.util.Waypoint.genDimension
import me.zeroeightsix.kami.util.Waypoint.genServer
import me.zeroeightsix.kami.util.math.CoordinateConverter
import net.minecraft.util.math.BlockPos

/**
 * @author wnuke
 * Created by wnuke on 17/04/20
 * Updated by Xiaro on 20/08/20
 */
class WaypointInfo(
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
    val server: String? = genServer() /* can be null from old configs */

    @SerializedName("dimension")
    val dimension: Int = genDimension()

    fun asString(currentDimension: Boolean): String {
        return if (currentDimension) {
            "${currentPos().x}, ${currentPos().y}, ${currentPos().z}"
        } else {
            "${pos.x}, ${pos.y}, ${pos.z}"
        }
    }

    fun currentPos(): BlockPos {
        return CoordinateConverter.toCurrent(dimension, pos)
    }

    private fun genID(): Int {
        return try {
            FileInstanceManager.waypoints[FileInstanceManager.waypoints.size - 1].id + 1
        } catch (ignored: ArrayIndexOutOfBoundsException) {
            0 // if you haven't saved coords before, this will throw, because the size() is 0
        }
    }
}
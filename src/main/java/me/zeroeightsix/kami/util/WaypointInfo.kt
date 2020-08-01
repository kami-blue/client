package me.zeroeightsix.kami.util

import com.google.gson.annotations.SerializedName
import me.zeroeightsix.kami.module.FileInstanceManager

/**
 * @author wnuke
 * Created by wnuke on 17/04/20
 */
class WaypointInfo {
    @JvmField
    @SerializedName("position")
    var pos: Coordinate

    @JvmField
    @SerializedName("name")
    var name: String

    @JvmField
    @SerializedName("time") // NEEDS to stay "time" to maintain backwards compat
    var date: String

    @JvmField
    @SerializedName("id")
    var id: Int

    @JvmField
    @SerializedName("type")
    var wpType: Int

    /**
     * Waypoint types:
     * 0 - Waypoint
     * 1 - LogSpot
     * 2 - Stash
     * 3 - TeleportSpot
     * 4 - Other
     * 5 - Death
     */

    constructor(x: Int, y: Int, z: Int, nameSet: String, timeSet: String, type: Int) {
        pos = Coordinate(x, y, z)
        name = nameSet
        date = timeSet
        id = genID()
        wpType = type
    }

    constructor(posSet: Coordinate, nameSet: String, timeSet: String, type: Int) {
        pos = posSet
        name = nameSet
        date = timeSet
        id = genID()
        wpType = type
    }

    private fun genID(): Int {
        return try {
            FileInstanceManager.waypoints[FileInstanceManager.waypoints.size - 1].id + 1
        } catch (ignored: ArrayIndexOutOfBoundsException) {
            0 // if you haven't saved coords before, this will throw, because the size() is 0
        }
    }

    val idString: String
        get() = id.toString()
}

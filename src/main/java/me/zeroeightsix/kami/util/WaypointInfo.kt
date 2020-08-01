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

    constructor(x: Int, y: Int, z: Int, nameSet: String, timeSet: String) {
        pos = Coordinate(x, y, z)
        name = nameSet
        date = timeSet
        id = genID()
    }

    constructor(posSet: Coordinate, nameSet: String, timeSet: String) {
        pos = posSet
        name = nameSet
        date = timeSet
        id = genID()
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
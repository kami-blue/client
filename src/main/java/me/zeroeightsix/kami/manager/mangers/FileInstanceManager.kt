package me.zeroeightsix.kami.manager.mangers

import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.util.Macro
import me.zeroeightsix.kami.util.Waypoint
import me.zeroeightsix.kami.util.WaypointInfo
import java.io.FileWriter
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author dominikaaaa
 */
object FileInstanceManager : Manager() {
    /*
     * Map of all the macros.
     * KeyCode, Actions
     */
    var macros = LinkedHashMap<Int, ArrayList<String>>()

    /*
     * ArrayList of all Waypoints
     */
    var waypoints = ArrayList<WaypointInfo>()

    /**
     * Super lazy fix for Windows users sometimes saving empty files
     */
    @JvmStatic
    fun fixEmptyFiles() {
        if (!Waypoint.file.exists()) {
            try {
                val w = FileWriter(Waypoint.file)
                w.write("[]")
                w.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (!Macro.file.exists()) {
            try {
                val w = FileWriter(Macro.file)
                w.write("{}")
                w.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    init {
        fixEmptyFiles()
    }
}
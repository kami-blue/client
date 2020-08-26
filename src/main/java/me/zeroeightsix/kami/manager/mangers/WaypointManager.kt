package me.zeroeightsix.kami.manager.mangers

import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.util.Waypoint

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 31/07/20
 */
object WaypointManager : Manager() {

    /**
     * Reads waypoints from KAMIBlueWaypoints.json into the waypoints ArrayList
     */
    fun loadWaypoints(): Boolean {
        return Waypoint.readFileToMemory()
    }

    /**
     * Saves waypoints from the waypoints ArrayList into KAMIBlueWaypoints.json
     */
    fun saveWaypoints(): Boolean {
        return Waypoint.writeMemoryToFile()
    }
}
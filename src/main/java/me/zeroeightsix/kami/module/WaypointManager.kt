package me.zeroeightsix.kami.module

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.util.Waypoint

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 31/07/20
 */
object WaypointManager {

    /**
     * Reads waypoints from KAMIBlueWaypoints.json into the waypoints ArrayList
     */
    fun loadWaypoints(): Boolean {
        KamiMod.log.info("Loading waypoints...")
        return Waypoint.readFileToMemory()
    }

    /**
     * Saves waypoints from the waypoints ArrayList into KAMIBlueWaypoints.json
     */
    fun saveWaypoints(): Boolean {
        KamiMod.log.info("Saving waypoints...")
        return Waypoint.writeMemoryToFile()
    }
}
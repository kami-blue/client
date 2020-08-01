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
    fun registerWaypoints() {
        KamiMod.log.info("Registering waypoints...")
        Waypoint.readFileToMemory()
        KamiMod.log.info("Waypoints registered")
    }

    /**
     * Saves waypoints from the waypoints ArrayList into KAMIBlueWaypoints.json
     */
    fun saveWaypoints() {
        KamiMod.log.info("Saving waypoints...")
        Waypoint.writeMemoryToFile()
        KamiMod.log.info("Waypoints saved")
    }
}
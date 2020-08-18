package me.zeroeightsix.kami.manager.mangers

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.util.Waypoint

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 31/07/20
 */
object WaypointManager : Manager() {

    /**
     * Saves waypoints from the waypoints ArrayList into KAMIBlueWaypoints.json
     */
    fun saveWaypoints() {
        KamiMod.log.info("Saving waypoints...")
        Waypoint.writeMemoryToFile()
        KamiMod.log.info("Waypoints saved")
    }

    /**
     * Reads waypoints from KAMIBlueWaypoints.json into the waypoints ArrayList
     */
    init {
        Waypoint.readFileToMemory()
    }
}
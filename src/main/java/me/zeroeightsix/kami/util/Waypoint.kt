package me.zeroeightsix.kami.util

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.WaypointUpdateEvent
import me.zeroeightsix.kami.module.FileInstanceManager
import net.minecraft.client.Minecraft
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 31/07/20
 *
 * Rewritten from former CoordUtil by wnuke (formerly LogUtil)
 */
object Waypoint {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private const val oldConfigName = "KAMIBlueCoords.json" /* maintain backwards compat with old format */
    private const val configName = "KAMIBlueWaypoints.json"
    private val oldFile = File(oldConfigName)
    val file = File(configName)
    private val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy")

    fun writeMemoryToFile(): Boolean {
        return try {
            val fw = FileWriter(file, false)
            gson.toJson(FileInstanceManager.waypoints, fw)
            fw.flush()
            fw.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun readFileToMemory(): Boolean {
        var success = false
        var localFile = file
        /* backwards compatibility for older configs */
        if (legacyFormat()) {
            localFile = oldFile
        }
        try {
            try {
                FileInstanceManager.waypoints = gson.fromJson(FileReader(localFile), object : TypeToken<ArrayList<WaypointInfo>?>() {}.type)!!
                KamiMod.log.info("Waypoint loaded")
                success = true
            } catch (e: FileNotFoundException) {
                KamiMod.log.warn("Could not find file $configName, clearing the waypoints list")
                FileInstanceManager.waypoints.clear()
            }
        } catch (e: IllegalStateException) {
            KamiMod.log.warn("$configName is empty!")
            FileInstanceManager.waypoints.clear()
        }

        if (legacyFormat()) {
            oldFile.delete()
        }
        return success
    }

    fun getCurrentCoord(): Coordinate {
        val mc = Minecraft.getMinecraft()
        return Coordinate(mc.player.posX.toInt(), mc.player.posY.toInt(), mc.player.posZ.toInt())
    }

    fun writePlayerCoords(locationName: String): Coordinate {
        val coord = getCurrentCoord()
        createWaypoint(coord, locationName)
        return coord
    }

    fun createWaypoint(xyz: Coordinate, locationName: String): Coordinate {
        FileInstanceManager.waypoints.add(dateFormatter(xyz, locationName))

        KamiMod.EVENT_BUS.post(WaypointUpdateEvent.UpdateType.CREATE)
        return xyz
    }

    fun removeWaypoint(coordinate: Coordinate): Boolean {
        var removed = false
        val waypoints = FileInstanceManager.waypoints

        for (waypoint in waypoints) {
            if (waypoint.pos.x == coordinate.x && waypoint.pos.y == coordinate.y && waypoint.pos.z == coordinate.z) {
                waypoints.remove(waypoint)
                removed = true
                break
            }
        }
        return removed
    }

    fun removeWaypoint(id: String): Boolean {
        var removed = false
        val waypoints = FileInstanceManager.waypoints

        for (waypoint in waypoints) if (waypoint.idString == id) {
            waypoints.remove(waypoint)
            removed = true
            break
        }

        KamiMod.EVENT_BUS.post(WaypointUpdateEvent.UpdateType.REMOVE)
        return removed
    }

    fun getWaypoint(id: String): Coordinate? {
        val waypoints = FileInstanceManager.waypoints
        for (waypoint in waypoints) {
            if (waypoint.idString == id) {
                return waypoint.pos
            }
        }

        KamiMod.EVENT_BUS.post(WaypointUpdateEvent.UpdateType.REMOVE)
        return null
    }

    /**
     * file deletion does not work on OSX, issue #1044
     * because of this, we must also check if they've used the new format
     */
    private fun legacyFormat(): Boolean {
        return oldFile.exists() && !file.exists()
    }

    private fun dateFormatter(xyz: Coordinate, locationName: String): WaypointInfo {
        val date = sdf.format(Date())
        return WaypointInfo(xyz, locationName, date)
    }
}
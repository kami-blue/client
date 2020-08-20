package me.zeroeightsix.kami.util

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.WaypointUpdateEvent
import me.zeroeightsix.kami.manager.mangers.FileInstanceManager
import me.zeroeightsix.kami.util.math.MathUtils
import net.minecraft.client.Minecraft
import net.minecraft.util.math.BlockPos
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

    fun getCurrentCoord(): BlockPos {
        val mc = Minecraft.getMinecraft()
        return MathUtils.mcPlayerPosFloored(mc)
    }

    fun writePlayerCoords(locationName: String): BlockPos {
        val coords = getCurrentCoord()
        createWaypoint(coords, locationName)
        return coords
    }

    fun createWaypoint(pos: BlockPos, locationName: String): BlockPos {
        FileInstanceManager.waypoints.add(dateFormatter(pos, locationName))

        KamiMod.EVENT_BUS.post(WaypointUpdateEvent.UpdateType.CREATE)
        return pos
    }

    fun removeWaypoint(pos: BlockPos): Boolean {
        var removed = false
        val waypoints = FileInstanceManager.waypoints

        for (waypoint in waypoints) {
            if (waypoint.pos.x == pos.x && waypoint.pos.y == pos.y && waypoint.pos.z == pos.z) {
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

    fun getWaypoint(id: String): BlockPos? {
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

    private fun dateFormatter(pos: BlockPos, locationName: String): WaypointInfo {
        val date = sdf.format(Date())
        return WaypointInfo(pos, locationName, date)
    }
}
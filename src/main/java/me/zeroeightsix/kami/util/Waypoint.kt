package me.zeroeightsix.kami.util

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.WaypointUpdateEvent
import me.zeroeightsix.kami.manager.mangers.FileInstanceManager
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import net.minecraft.util.math.BlockPos
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

//TODO: Merge this into WaypointManager.kt
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
            KamiMod.log.info("Friend saved")
            true
        } catch (e: IOException) {
            KamiMod.log.info("Failed saving friend")
            e.printStackTrace()
            false
        }
    }

    fun readFileToMemory(): Boolean {
        /* backwards compatibility for older configs */
        val localFile = if (legacyFormat()) oldFile else file
        var success = false
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

    fun writePlayerCoords(locationName: String): WaypointInfo {
        val coords = Wrapper.player?.positionVector?.toBlockPos() ?: BlockPos(0, -6969, 0) // This shouldn't happen
        val waypoint = createWaypoint(coords, locationName)
        KamiMod.EVENT_BUS.post(WaypointUpdateEvent.Create())
        KamiMod.EVENT_BUS.post(WaypointUpdateEvent.Update())
        return waypoint
    }

    fun createWaypoint(pos: BlockPos, locationName: String): WaypointInfo {
        val waypoint = dateFormatter(pos, locationName)
        FileInstanceManager.waypoints.add(waypoint)
        KamiMod.EVENT_BUS.post(WaypointUpdateEvent.Create())
        KamiMod.EVENT_BUS.post(WaypointUpdateEvent.Update())
        return waypoint
    }

    fun removeWaypoint(pos: BlockPos, currentDimension: Boolean = false): Boolean {
        val waypoint = getWaypoint(pos, currentDimension)
        val removed = FileInstanceManager.waypoints.remove(waypoint)
        KamiMod.EVENT_BUS.post(WaypointUpdateEvent.Remove())
        KamiMod.EVENT_BUS.post(WaypointUpdateEvent.Update())
        return removed
    }

    fun removeWaypoint(id: String): Boolean {
        val waypoint = getWaypoint(id)
        val removed = FileInstanceManager.waypoints.remove(waypoint)
        KamiMod.EVENT_BUS.post(WaypointUpdateEvent.Remove())
        KamiMod.EVENT_BUS.post(WaypointUpdateEvent.Update())
        return removed
    }

    fun getWaypoint(id: String): WaypointInfo? {
        val waypoint = FileInstanceManager.waypoints.firstOrNull { it.id.toString() == id }
        KamiMod.EVENT_BUS.post(WaypointUpdateEvent.Get())
        return waypoint
    }

    fun getWaypoint(pos: BlockPos, currentDimension: Boolean = false): WaypointInfo? {
        val waypoint = FileInstanceManager.waypoints.firstOrNull { (if (currentDimension) it.currentPos() else it.pos) == pos }
        KamiMod.EVENT_BUS.post(WaypointUpdateEvent.Get())
        return waypoint
    }

    fun clearWaypoint() {
        FileInstanceManager.waypoints.clear()
        KamiMod.EVENT_BUS.post(WaypointUpdateEvent.Remove())
        KamiMod.EVENT_BUS.post(WaypointUpdateEvent.Update())
    }

    fun genServer(): String? {
        return Wrapper.minecraft.currentServerData?.serverIP
                ?: if (Wrapper.minecraft.isIntegratedServerRunning) "Singleplayer"
                else null
    }

    fun genDimension(): Int {
        return Wrapper.player?.dimension ?: -2 /* this shouldn't ever happen at all */
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
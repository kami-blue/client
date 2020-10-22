package me.zeroeightsix.kami.manager.managers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.event.events.WaypointUpdateEvent
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.util.ConfigUtils.fixEmptyFile
import me.zeroeightsix.kami.util.Waypoint
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import net.minecraft.util.math.BlockPos
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.LinkedHashSet

object WaypointManager : Manager() {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private const val oldConfigName = "KAMIBlueCoords.json" /* maintain backwards compat with old format */
    private const val configName = "KAMIBlueWaypoints.json"
    private val oldFile = File(oldConfigName)
    val file = File(configName)
    private val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy")
    private val mainThread = Thread.currentThread()

    /**
     * LinkedHashSet for all waypoints
     * Since LinkedHashSet isn't thread safe, getting this from another thread will
     * returns a read only copy
     */
    var waypoints = LinkedHashSet<Waypoint>()
        get() {
            return if (Thread.currentThread() != mainThread) {
                synchronized(field) { LinkedHashSet(field) }
            } else field
        }

    /**
     * Reads waypoints from KAMIBlueWaypoints.json into the waypoints ArrayList
     */
    fun loadWaypoints(): Boolean {
        /* backwards compatibility for older configs */
        val localFile = if (legacyFormat()) oldFile else file
        val success = try {
            waypoints = gson.fromJson(FileReader(localFile), object : TypeToken<LinkedHashSet<Waypoint>?>() {}.type)
            KamiMod.log.info("Waypoint loaded")
            if (legacyFormat()) oldFile.delete()
            true
        } catch (e: FileNotFoundException) {
            KamiMod.log.warn("Could not find file $configName, clearing the waypoints list")
            waypoints.clear()
            false
        } catch (e: IllegalStateException) {
            KamiMod.log.warn("$configName is empty!")
            waypoints.clear()
            false
        }
        KamiEventBus.post(WaypointUpdateEvent(WaypointUpdateEvent.Type.CLEAR, null))
        return success
    }

    /**
     * Saves waypoints from the waypoints ArrayList into KAMIBlueWaypoints.json
     */
    fun saveWaypoints(): Boolean {
        return try {
            val fileWriter = FileWriter(file, false)
            gson.toJson(waypoints, fileWriter)
            fileWriter.flush()
            fileWriter.close()
            KamiMod.log.info("Waypoint saved")
            true
        } catch (e: IOException) {
            KamiMod.log.info("Failed saving waypoint")
            e.printStackTrace()
            false
        }
    }

    /**
     * file deletion does not work on OSX, issue #1044
     * because of this, we must also check if they've used the new format
     */
    private fun legacyFormat(): Boolean {
        return oldFile.exists() && !file.exists()
    }

    fun get(id: String): Waypoint? {
        val waypoint = waypoints.firstOrNull { it.id.toString() == id }
        KamiEventBus.post(WaypointUpdateEvent(WaypointUpdateEvent.Type.GET, waypoint))
        return waypoint
    }

    fun get(pos: BlockPos, currentDimension: Boolean = false): Waypoint? {
        val waypoint = waypoints.firstOrNull { (if (currentDimension) it.currentPos() else it.pos) == pos }
        KamiEventBus.post(WaypointUpdateEvent(WaypointUpdateEvent.Type.GET, waypoint))
        return waypoint
    }

    fun add(locationName: String): Waypoint {
        val pos = Wrapper.player?.positionVector?.toBlockPos()
        return if (pos != null) {
            val waypoint = add(pos, locationName)
            KamiEventBus.post(WaypointUpdateEvent(WaypointUpdateEvent.Type.ADD, waypoint))
            waypoint
        } else {
            KamiMod.log.error("Error during waypoint adding")
            dateFormatter(BlockPos(0, 0, 0), locationName) // This shouldn't happen
        }
    }

    fun add(pos: BlockPos, locationName: String): Waypoint {
        val waypoint = dateFormatter(pos, locationName)
        waypoints.add(waypoint)
        KamiEventBus.post(WaypointUpdateEvent(WaypointUpdateEvent.Type.ADD, waypoint))
        return waypoint
    }

    fun remove(pos: BlockPos, currentDimension: Boolean = false): Boolean {
        val waypoint = get(pos, currentDimension)
        val removed = waypoints.remove(waypoint)
        KamiEventBus.post(WaypointUpdateEvent(WaypointUpdateEvent.Type.REMOVE, waypoint))
        return removed
    }

    fun remove(id: String): Boolean {
        val waypoint = get(id)?: return false
        val removed = waypoints.remove(waypoint)
        KamiEventBus.post(WaypointUpdateEvent(WaypointUpdateEvent.Type.REMOVE, waypoint))
        return removed
    }

    fun clear() {
        waypoints.clear()
        KamiEventBus.post(WaypointUpdateEvent(WaypointUpdateEvent.Type.CLEAR, null))
    }

    fun genServer(): String? {
        return Wrapper.minecraft.currentServerData?.serverIP
                ?: if (Wrapper.minecraft.isIntegratedServerRunning) "Singleplayer"
                else null
    }

    fun genDimension(): Int {
        return Wrapper.player?.dimension ?: -2 /* this shouldn't ever happen at all */
    }

    private fun dateFormatter(pos: BlockPos, locationName: String): Waypoint {
        val date = sdf.format(Date())
        return Waypoint(pos, locationName, date)
    }

    init {
        fixEmptyFile(file)
    }
}

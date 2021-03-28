package org.kamiblue.client.manager.managers

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import net.minecraft.util.math.BlockPos
import org.kamiblue.client.KamiMod
import org.kamiblue.client.event.KamiEventBus
import org.kamiblue.client.event.events.WaypointUpdateEvent
import org.kamiblue.client.manager.Manager
import org.kamiblue.client.util.ConfigUtils
import org.kamiblue.client.util.Wrapper
import org.kamiblue.client.util.math.CoordinateConverter
import org.kamiblue.client.util.math.CoordinateConverter.asString
import org.kamiblue.client.util.math.VectorUtils.toBlockPos
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentSkipListSet

object WaypointManager : Manager {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File(KamiMod.DIRECTORY + "waypoints.json")
    private val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy")

    val waypoints = ConcurrentSkipListSet<Waypoint>(compareBy { it.id })

    fun loadWaypoints(): Boolean {
        ConfigUtils.fixEmptyJson(file, true)

        val success = try {
            val cacheArray = FileReader(file).buffered().use {
                gson.fromJson(it, Array<Waypoint>::class.java)
            }

            waypoints.clear()
            waypoints.addAll(cacheArray)

            KamiMod.LOG.info("Waypoint loaded")
            true
        } catch (e: Exception) {
            KamiMod.LOG.warn("Failed loading waypoints", e)
            false
        }

        KamiEventBus.post(WaypointUpdateEvent(WaypointUpdateEvent.Type.CLEAR, null))
        return success
    }

    fun saveWaypoints(): Boolean {
        return try {
            FileWriter(file, false).buffered().use {
                gson.toJson(waypoints, it)
            }
            KamiMod.LOG.info("Waypoint saved")
            true
        } catch (e: Exception) {
            KamiMod.LOG.warn("Failed saving waypoint", e)
            false
        }
    }

    fun get(id: Int): Waypoint? {
        val waypoint = waypoints.firstOrNull { it.id == id }
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
            KamiMod.LOG.error("Error during waypoint adding")
            dateFormatter(BlockPos(0, 0, 0), locationName) // This shouldn't happen
        }
    }

    fun add(pos: BlockPos, locationName: String): Waypoint {
        val waypoint = dateFormatter(pos, locationName)
        waypoints.add(waypoint)
        KamiEventBus.post(WaypointUpdateEvent(WaypointUpdateEvent.Type.ADD, waypoint))
        return waypoint
    }

    fun add(waypoint: Waypoint) {
        waypoints.add(waypoint)
        KamiEventBus.post(WaypointUpdateEvent(WaypointUpdateEvent.Type.ADD, waypoint))
    }

    fun remove(pos: BlockPos, currentDimension: Boolean = false): Boolean {
        val waypoint = get(pos, currentDimension)
        val removed = waypoints.remove(waypoint)
        KamiEventBus.post(WaypointUpdateEvent(WaypointUpdateEvent.Type.REMOVE, waypoint))
        return removed
    }

    fun remove(id: Int): Boolean {
        val waypoint = get(id) ?: return false
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

    class Waypoint(
        @SerializedName("position")
        val pos: BlockPos,
        val name: String,

        @SerializedName(value = "date", alternate = ["time"])
        val date: String
    ) {
        val id: Int = genID()
        val server: String? = genServer() /* can be null from old configs */
        val dimension: Int = genDimension()

        fun currentPos() = CoordinateConverter.toCurrent(dimension, pos)

        private fun genID(): Int = waypoints.lastOrNull()?.id?.plus(1) ?: 0

        override fun toString() = currentPos().asString()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Waypoint) return false

            if (pos != other.pos) return false
            if (name != other.name) return false
            if (date != other.date) return false
            if (id != other.id) return false
            if (server != other.server) return false
            if (dimension != other.dimension) return false

            return true
        }

        override fun hashCode(): Int {
            var result = pos.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + date.hashCode()
            result = 31 * result + id
            result = 31 * result + (server?.hashCode() ?: 0)
            result = 31 * result + dimension
            return result
        }
    }
}
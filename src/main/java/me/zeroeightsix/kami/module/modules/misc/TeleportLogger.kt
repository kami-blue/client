package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.CoordUtil
import me.zeroeightsix.kami.util.Coordinate
import me.zeroeightsix.kami.util.MathsUtils
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraft.entity.player.EntityPlayer

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 29/07/20
 */
@Module.Info(
        name = "TeleportLogger",
        category = Module.Category.MISC,
        description = "Logs when a player teleports somewhere"
)
class TeleportLogger : Module() {
    private var saveToFile = register(Settings.b("SaveToFile", true))
    private var remove = register(Settings.b("RemoveInRange", true))
    private var printAdd = register(Settings.b("PrintAdd", true))
    private var printRemove = register(Settings.booleanBuilder("PrintRemove").withValue(true).withVisibility { remove.value }.build())
    private var minimumDistance = register(Settings.integerBuilder("MinimumDistance").withValue(512).withMinimum(128).build())

    private val teleportedPlayers = HashMap<String, Coordinate>()

    override fun onUpdate() {
        if (mc.player == null) return
        for (player in mc.world.loadedEntityList.filterIsInstance<EntityPlayer>()) {
            if (player.name == mc.player.name) continue

            /* 8 chunk render distance * 16 */
            if (remove.value && 128 > player.getDistance(mc.player)) {
                if (teleportedPlayers.contains(player.name)) {
                    val removed = CoordUtil.removeCoord(teleportedPlayers[player.name], CoordUtil.coordsLogFilename)
                    teleportedPlayers.remove(player.name)

                    if (removed) {
                        if (printRemove.value) MessageSendHelper.sendChatMessage("$chatName Removed ${player.name}, they are now ${MathsUtils.round(player.getDistance(mc.player), 1)} blocks away")
                    } else {
                        if (printRemove.value) MessageSendHelper.sendErrorMessage("$chatName Error removing ${player.name} from coords, their position wasn't saved anymore")
                    }
                }
                continue
            }

            if (minimumDistance.value > player.getDistance(mc.player) || teleportedPlayers.containsKey(player.name)) {
                continue
            }

            val coords = logCoordinates(Coordinate(player.posX.toInt(), player.posY.toInt(), player.posZ.toInt()), "${player.name} Teleport Spot")
            teleportedPlayers[player.name] = coords
            if (printAdd.value) MessageSendHelper.sendChatMessage("$chatName ${player.name} teleported, ${getSaveText()} ${coords(coords)}")
        }
    }

    private fun coords(coordinate: Coordinate): String {
        return coordinate.x.toString() + ", " + coordinate.y + ", " + coordinate.z
    }

    private fun logCoordinates(coordinate: Coordinate, name: String): Coordinate {
        return if (saveToFile.value) {
            CoordUtil.writeCustomCoords(coordinate, name)
        } else {
            coordinate
        }
    }

    private fun getSaveText(): String {
        return if (saveToFile.value) {
            "saved their coordinates at"
        } else {
            "their coordinates are"
        }
    }
}
package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Waypoint
import me.zeroeightsix.kami.util.math.MathUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos

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

    private val teleportedPlayers = HashMap<String, BlockPos>()

    override fun onUpdate() {
        if (mc.player == null) return
        for (player in mc.world.loadedEntityList.filterIsInstance<EntityPlayer>()) {
            if (player.name == mc.player.name) continue

            /* 8 chunk render distance * 16 */
            if (remove.value && 128 > player.getDistance(mc.player)) {
                if (teleportedPlayers.contains(player.name)) {
                    val removed = Waypoint.removeWaypoint(teleportedPlayers[player.name]!!)
                    teleportedPlayers.remove(player.name)

                    if (removed) {
                        if (printRemove.value) MessageSendHelper.sendChatMessage("$chatName Removed ${player.name}, they are now ${MathUtils.round(player.getDistance(mc.player), 1)} blocks away")
                    } else {
                        if (printRemove.value) MessageSendHelper.sendErrorMessage("$chatName Error removing ${player.name} from coords, their position wasn't saved anymore")
                    }
                }
                continue
            }

            if (minimumDistance.value > player.getDistance(mc.player) || teleportedPlayers.containsKey(player.name)) {
                continue
            }

            val coords = logCoordinates(player.position, "${player.name} Teleport Spot")
            teleportedPlayers[player.name] = coords
            if (printAdd.value) MessageSendHelper.sendChatMessage("$chatName ${player.name} teleported, ${getSaveText()} ${coords.x}, ${coords.y}, ${coords.z}")
        }
    }

    private fun logCoordinates(coordinate: BlockPos, name: String): BlockPos {
        return if (saveToFile.value) {
            Waypoint.createWaypoint(coordinate, name)
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
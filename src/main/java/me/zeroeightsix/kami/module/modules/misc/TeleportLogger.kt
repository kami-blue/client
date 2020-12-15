package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import org.kamiblue.event.listener.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.util.math.BlockPos
import org.kamiblue.commons.utils.MathUtils

@Module.Info(
        name = "TeleportLogger",
        category = Module.Category.MISC,
        description = "Logs when a player teleports somewhere"
)
object TeleportLogger : Module() {
    private val saveToFile = register(Settings.b("SaveToFile", true))
    private val remove = register(Settings.b("RemoveInRange", true))
    private val printAdd = register(Settings.b("PrintAdd", true))
    private val printRemove = register(Settings.booleanBuilder("PrintRemove").withValue(true).withVisibility { remove.value })
    private val minimumDistance = register(Settings.integerBuilder("MinimumDistance").withValue(512).withRange(128, 2048).withStep(128))

    private val teleportedPlayers = HashMap<String, BlockPos>()

    init {
        listener<SafeTickEvent> {
            for (player in mc.world.playerEntities) {
                if (player == mc.player) continue

                /* 8 chunk render distance * 16 */
                if (remove.value && player.getDistance(mc.player) < 128) {
                    if (teleportedPlayers.contains(player.name)) {
                        val removed = WaypointManager.remove(teleportedPlayers[player.name]!!)
                        teleportedPlayers.remove(player.name)

                        if (removed) {
                            if (printRemove.value) MessageSendHelper.sendChatMessage("$chatName Removed ${player.name}, they are now ${MathUtils.round(player.getDistance(mc.player), 1)} blocks away")
                        } else {
                            if (printRemove.value) MessageSendHelper.sendErrorMessage("$chatName Error removing ${player.name} from coords, their position wasn't saved anymore")
                        }
                    }
                    continue
                }

                if (player.getDistance(mc.player) < minimumDistance.value || teleportedPlayers.containsKey(player.name)) {
                    continue
                }

                val coords = logCoordinates(player.position, "${player.name} Teleport Spot")
                teleportedPlayers[player.name] = coords
                if (printAdd.value) MessageSendHelper.sendChatMessage("$chatName ${player.name} teleported, ${getSaveText()} ${coords.x}, ${coords.y}, ${coords.z}")
            }
        }
    }

    private fun logCoordinates(coordinate: BlockPos, name: String): BlockPos {
        return if (saveToFile.value) WaypointManager.add(coordinate, name).pos
        else coordinate
    }

    private fun getSaveText(): String {
        return if (saveToFile.value) "saved their coordinates at"
        else "their coordinates are"
    }
}
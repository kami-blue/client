package org.kamiblue.client.module.modules.misc

import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.manager.managers.WaypointManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.EntityUtils.isFakeOrSelf
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.commons.utils.MathUtils

internal object TeleportLogger : Module(
    name = "TeleportLogger",
    category = Category.MISC,
    description = "Logs when a player teleports somewhere"
) {
    private val saveToWaypoints = setting("Save To Waypoints", true)
    private val remove = setting("Remove In Range", true)
    private val printAdd = setting("Print Add", true)
    private val printRemove = setting("Print Remove", true, { remove.value })
    private val minimumDistance = setting("Minimum Distance", 512, 128..2048, 128)

    private val teleportedPlayers = HashMap<String, BlockPos>()

    init {
        safeListener<TickEvent.ClientTickEvent> {
            for (worldPlayer in world.playerEntities) {
                if (worldPlayer.isFakeOrSelf) continue

                /* 8 chunk render distance * 16 */
                if (remove.value && worldPlayer.getDistance(player) < 128) {
                    if (teleportedPlayers.contains(worldPlayer.name)) {
                        val removed = WaypointManager.remove(teleportedPlayers[worldPlayer.name]!!)
                        teleportedPlayers.remove(worldPlayer.name)

                        if (removed) {
                            if (printRemove.value) MessageSendHelper.sendChatMessage("$chatName Removed ${worldPlayer.name}, they are now ${MathUtils.round(worldPlayer.getDistance(mc.player), 1)} blocks away")
                        } else {
                            if (printRemove.value) MessageSendHelper.sendErrorMessage("$chatName Error removing ${worldPlayer.name} from coords, their position wasn't saved anymore")
                        }
                    }
                    continue
                }

                if (worldPlayer.getDistance(player) < minimumDistance.value || teleportedPlayers.containsKey(worldPlayer.name)) {
                    continue
                }

                val coords = logCoordinates(worldPlayer.position, "${worldPlayer.name} Teleport Spot")
                teleportedPlayers[worldPlayer.name] = coords
                if (printAdd.value) MessageSendHelper.sendChatMessage("$chatName ${worldPlayer.name} teleported, ${getSaveText()} ${coords.x}, ${coords.y}, ${coords.z}")
            }
        }
    }

    private fun logCoordinates(coordinate: BlockPos, name: String): BlockPos {
        return if (saveToWaypoints.value) WaypointManager.add(coordinate, name).pos
        else coordinate
    }

    private fun getSaveText(): String {
        return if (saveToWaypoints.value) "saved their coordinates at"
        else "their coordinates are"
    }
}

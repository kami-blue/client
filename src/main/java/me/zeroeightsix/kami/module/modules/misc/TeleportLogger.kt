package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.utils.MathUtils

@Module.Info(
    name = "TeleportLogger",
    category = Module.Category.MISC,
    description = "Logs when a player teleports somewhere"
)
object TeleportLogger : Module() {
    private val saveToFile = setting("SaveToFile", true)
    private val remove = setting("RemoveInRange", true)
    private val printAdd = setting("PrintAdd", true)
    private val printRemove = setting("PrintRemove", true, { remove.value })
    private val minimumDistance = setting("MinimumDistance", 512, 128..2048, 128)

    private val teleportedPlayers = HashMap<String, BlockPos>()

    init {
        safeListener<TickEvent.ClientTickEvent> {
            for (worldPlayer in world.playerEntities) {
                if (worldPlayer == player) continue

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
        return if (saveToFile.value) WaypointManager.add(coordinate, name).pos
        else coordinate
    }

    private fun getSaveText(): String {
        return if (saveToFile.value) "saved their coordinates at"
        else "their coordinates are"
    }
}
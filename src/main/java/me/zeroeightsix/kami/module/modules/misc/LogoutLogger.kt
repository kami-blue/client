package me.zeroeightsix.kami.module.modules.misc

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Waypoint
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 30/07/20
 */
@Module.Info(
        name = "LogoutLogger",
        category = Module.Category.MISC,
        description = "Logs when a player leaves the game"
)
object LogoutLogger : Module() {
    private var saveToFile = register(Settings.b("SaveToFile", true))
    private var print = register(Settings.b("PrintToChat", true))

    private var loggedPlayers = HashMap<String, BlockPos>()
    private var onlinePlayers = mutableListOf<NetworkPlayerInfo>()
    private var ticks = 0

    @EventHandler
    private val disconnectListener = Listener(EventHook { event: ConnectionEvent.Disconnect ->
        loggedPlayers.clear()
        onlinePlayers.clear()
    })

    override fun onUpdate() {
        ticks++

        for (player in mc.world.loadedEntityList.filterIsInstance<EntityPlayer>()) {
            if (player.name == mc.player.name) continue
            loggedPlayers[player.name] = BlockPos(player.posX.toInt(), player.posY.toInt(), player.posZ.toInt())
        }

        if (ticks >= 20) {
            Thread {
                updateOnlinePlayers()
                loggedPlayers.forEach { loggedPlayer ->
                    var found = false
                    onlinePlayers.forEach { onlinePlayer ->
                        if (onlinePlayer.gameProfile.name == loggedPlayer.key) {
                            found = true
                        }
                    }

                    if (!found) {
                        val posString = "${loggedPlayer.value.x}, ${loggedPlayer.value.y}, ${loggedPlayer.value.z}"
                        if (print.value) MessageSendHelper.sendChatMessage("${loggedPlayer.key} logged out at $posString")
                        logCoordinates(loggedPlayer.value, "${loggedPlayer.key} Logout Spot")
                        loggedPlayers.remove(loggedPlayer.key)
                    }
                }
            }.start()
            ticks = 0
        }
    }

    private fun logCoordinates(coordinate: BlockPos, name: String): BlockPos {
        return if (saveToFile.value) {
            Waypoint.createWaypoint(coordinate, name)
        } else {
            coordinate
        }
    }

    private fun updateOnlinePlayers() {
        if (mc.player == null) return
        onlinePlayers = mc.player.connection.playerInfoMap.toMutableList()
    }
}
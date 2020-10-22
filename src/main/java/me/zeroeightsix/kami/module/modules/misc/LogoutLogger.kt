package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.CoordinateConverter.asString
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos

@Module.Info(
        name = "LogoutLogger",
        category = Module.Category.MISC,
        description = "Logs when a player leaves the game"
)
object LogoutLogger : Module() {
    private val saveToFile = register(Settings.b("SaveToFile", true))
    private val print = register(Settings.b("PrintToChat", true))

    private val loggedPlayers = HashMap<String, BlockPos>()
    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)

    init {
        listener<ConnectionEvent.Disconnect> {
            loggedPlayers.clear()
        }

        listener<SafeTickEvent> {
            for (player in mc.world.loadedEntityList) {
                if (player !is EntityPlayer) continue
                if (player == mc.player) continue
                loggedPlayers[player.name] = player.positionVector.toBlockPos()
            }

            if (timer.tick(1L)) {
                val toRemove = ArrayList<String>()
                for ((name, pos) in loggedPlayers) {
                    if (mc.connection!!.getPlayerInfo(name) != null) continue
                    if (print.value) MessageSendHelper.sendChatMessage("$name logged out at ${pos.asString()}")
                    if (saveToFile.value) WaypointManager.add(pos, "$name Logout Spot")
                    toRemove.add(name)
                }
                loggedPlayers.keys.removeAll(toRemove)
            }
        }
    }
}
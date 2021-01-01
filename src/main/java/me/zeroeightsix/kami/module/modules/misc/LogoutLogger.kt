package me.zeroeightsix.kami.module.modules.misc

import com.mojang.authlib.GameProfile
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.math.CoordinateConverter.asString
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.math.BlockPos
import org.kamiblue.event.listener.listener

@Module.Info(
        name = "LogoutLogger",
        category = Module.Category.MISC,
        description = "Logs when a player leaves the game"
)
object LogoutLogger : Module() {
    private val saveToFile = register(Settings.b("SaveToFile", true))
    private val print = register(Settings.b("PrintToChat", true))

    private val loggedPlayers = HashMap<GameProfile, BlockPos>()
    private val timer = TickTimer(TimeUnit.SECONDS)

    init {
        listener<ConnectionEvent.Disconnect> {
            loggedPlayers.clear()
        }

        listener<SafeTickEvent> {
            for (player in mc.world.loadedEntityList) {
                if (player !is EntityOtherPlayerMP) continue
                mc.connection?.getPlayerInfo(player.gameProfile.id)?.let {
                    loggedPlayers[it.gameProfile] = player.positionVector.toBlockPos()
                }
            }

            if (timer.tick(1L)) {
                val toRemove = ArrayList<GameProfile>()
                for ((profile, pos) in loggedPlayers) {
                    if (mc.connection?.getPlayerInfo(profile.id) != null) continue
                    if (print.value) MessageSendHelper.sendChatMessage("${profile.name} logged out at ${pos.asString()}")
                    if (saveToFile.value) WaypointManager.add(pos, "${profile.name} Logout Spot")
                    toRemove.add(profile)
                }
                loggedPlayers.keys.removeAll(toRemove)
            }
        }
    }
}
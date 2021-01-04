package me.zeroeightsix.kami.module.modules.misc

import com.mojang.authlib.GameProfile
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.math.CoordinateConverter.asString
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent
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

        safeListener<TickEvent.ClientTickEvent> {
            for (loadedPlayer in world.loadedEntityList) {
                if (loadedPlayer !is EntityOtherPlayerMP) continue
                connection.getPlayerInfo(loadedPlayer.gameProfile.id).let {
                    loggedPlayers[it.gameProfile] = loadedPlayer.positionVector.toBlockPos()
                }
            }

            if (timer.tick(1L)) {
                val toRemove = ArrayList<GameProfile>()
                for ((profile, pos) in loggedPlayers) {
                    if (connection.getPlayerInfo(profile.id) != null) continue
                    if (print.value) MessageSendHelper.sendChatMessage("${profile.name} logged out at ${pos.asString()}")
                    if (saveToFile.value) WaypointManager.add(pos, "${profile.name} Logout Spot")
                    toRemove.add(profile)
                }
                loggedPlayers.keys.removeAll(toRemove)
            }
        }
    }
}
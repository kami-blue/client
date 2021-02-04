package org.kamiblue.client.module.modules.misc

import com.mojang.authlib.GameProfile
import org.kamiblue.client.event.events.ConnectionEvent
import org.kamiblue.client.manager.managers.WaypointManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.EntityUtils.flooredPosition
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.math.CoordinateConverter.asString
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.onMainThread
import org.kamiblue.client.util.threads.safeListener
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.asyncListener
import org.kamiblue.event.listener.listener

internal object LogoutLogger : Module(
    name = "LogoutLogger",
    category = Category.MISC,
    description = "Logs when a player leaves the game"
) {
    private val saveToFile = setting("SaveToFile", true)
    private val print = setting("PrintToChat", true)

    private val loggedPlayers = HashMap<GameProfile, BlockPos>()
    private val timer = TickTimer(TimeUnit.SECONDS)

    init {
        asyncListener<ConnectionEvent.Disconnect> {
            onMainThread {
                loggedPlayers.clear()
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            for (loadedPlayer in world.playerEntities) {
                if (loadedPlayer !is EntityOtherPlayerMP) continue

                val info = connection.getPlayerInfo(loadedPlayer.gameProfile.id) ?: continue
                loggedPlayers[info.gameProfile] = loadedPlayer.flooredPosition
            }

            if (timer.tick(1L)) {
                val toRemove = ArrayList<GameProfile>()

                for ((profile, pos) in loggedPlayers) {
                    if (print.value) MessageSendHelper.sendChatMessage("${profile.name} logged out at ${pos.asString()}")
                    if (saveToFile.value) WaypointManager.add(pos, "${profile.name} Logout Spot")
                    toRemove.add(profile)
                }

                loggedPlayers.keys.removeAll(toRemove)
            }
        }
    }
}
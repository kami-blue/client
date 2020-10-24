package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.module.Module
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.scoreboard.ScorePlayerTeam

@Module.Info(
        name = "TabFriends",
        description = "Highlights friends in the tab menu",
        category = Module.Category.RENDER,
        showOnArray = Module.ShowOnArray.OFF
)
object TabFriends : Module() {
    @JvmStatic
    fun getPlayerName(networkPlayerInfoIn: NetworkPlayerInfo): String {
        val name = if (networkPlayerInfoIn.displayName != null) networkPlayerInfoIn.displayName!!.formattedText else ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.playerTeam, networkPlayerInfoIn.gameProfile.name)
        return if (FriendManager.isFriend(name)) String.format("%sa%s", KamiMod.color, name) else name
    }
}
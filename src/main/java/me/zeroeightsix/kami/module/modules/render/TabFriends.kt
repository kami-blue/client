package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.Friends
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.scoreboard.ScorePlayerTeam

@Module.Info(
        name = "TabFriends",
        description = "Highlights friends in the tab menu",
        category = Module.Category.RENDER,
        showOnArray = Module.ShowOnArray.OFF
)
class TabFriends : Module() {
    companion object {
        @JvmField
        var INSTANCE: TabFriends? = null

        @JvmStatic
        fun getPlayerName(networkPlayerInfoIn: NetworkPlayerInfo): String {
            val dname = if (networkPlayerInfoIn.displayName != null) networkPlayerInfoIn.displayName!!.formattedText else ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.playerTeam, networkPlayerInfoIn.gameProfile.name)
            return if (Friends.isFriend(dname)) String.format("%sa%s", KamiMod.colour, dname) else dname
        }
    }

    init {
        INSTANCE = this
    }
}
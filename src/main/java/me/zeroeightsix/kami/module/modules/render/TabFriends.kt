package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.module.Module
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.text.TextFormatting

@Module.Info(
        name = "TabFriends",
        description = "Highlights friends in the tab menu",
        category = Module.Category.RENDER,
        showOnArray = Module.ShowOnArray.OFF
)
object TabFriends : Module() {
    @JvmStatic
    fun getPlayerName(info: NetworkPlayerInfo): String {
        val name = info.displayName?.formattedText
            ?: ScorePlayerTeam.formatPlayerName(info.playerTeam, info.gameProfile.name)
        return if (FriendManager.isFriend(name)) "${TextFormatting.GREEN}$name" else name
    }
}
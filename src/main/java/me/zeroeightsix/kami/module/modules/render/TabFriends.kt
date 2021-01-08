package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.color.EnumTextColor
import me.zeroeightsix.kami.util.text.format
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.scoreboard.ScorePlayerTeam

internal object TabFriends : Module(
    name = "TabFriends",
    description = "Highlights friends in the tab menu",
    category = Category.RENDER,
    showOnArray = false
) {
    private val color = setting("Color", EnumTextColor.GREEN)

    @JvmStatic
    fun getPlayerName(info: NetworkPlayerInfo): String {
        val name = info.displayName?.formattedText
            ?: ScorePlayerTeam.formatPlayerName(info.playerTeam, info.gameProfile.name)

        return if (FriendManager.isFriend(name)) {
            color.value format name
        } else {
            name
        }
    }
}
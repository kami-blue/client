package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;

@Module.Info(
        name = "TabFriends",
        description = "Highlights friends in the tab menu",
        category = Module.Category.RENDER,
        showOnArray = Module.ShowOnArray.OFF
)
public class TabFriends extends Module {

    public static TabFriends INSTANCE;

    public TabFriends() {
        TabFriends.INSTANCE = this;
    }

    public static String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn) {
        String dname = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
        if (Friends.isFriend(dname)) return String.format("%sa%s", KamiMod.colour, dname);
        return dname;
    }
}

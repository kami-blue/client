package me.zeroeightsix.kami.module.modules.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendServerMessage;

/**
 * Created on 26 October 2019 by hub
 * Updated 12 January 2020 by hub
 * Updated by polymer on 23/02/20
 */
@Module.Info(
        name = "VisualRange",
        description = "Shows players who enter and leave range in chat",
        category = Module.Category.COMBAT
)
public class VisualRange extends Module {
    private Setting<Boolean> leaving = register(Settings.b("Leaving", false));
    private Setting<Boolean> uwuAura = register(Settings.b("UwU Aura", false));

    private List<String> knownPlayers;
    
    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        List<String> tickPlayerList = new ArrayList<>();

        for (Entity entity : mc.world.getLoadedEntityList()) {
            if (entity instanceof EntityPlayer) tickPlayerList.add(entity.getName());
        }

        if (tickPlayerList.size() > 0) {
            for (String playerName : tickPlayerList) {
                if (playerName.equals(mc.player.getName())) continue;

                if (!knownPlayers.contains(playerName)) {
                    knownPlayers.add(playerName);

                    if (Friends.isFriend(playerName)) {
                        sendNotification(ChatFormatting.GREEN.toString() + playerName + ChatFormatting.RESET.toString() + " entered the Battlefield!");
                    } else {
                        sendNotification(ChatFormatting.RED.toString() + playerName + ChatFormatting.RESET.toString() + " entered the Battlefield!");
                    }
                    if (uwuAura.getValue()) sendServerMessage("/w "+ playerName + " hi uwu");

                    return;
                }
            }
        }

        if (knownPlayers.size() > 0) {
            for (String playerName : knownPlayers) {
                if (!tickPlayerList.contains(playerName)) {
                    knownPlayers.remove(playerName);

                    if (leaving.getValue()) {
                        if (Friends.isFriend(playerName)) {
                            sendNotification(ChatFormatting.GREEN.toString() + playerName + ChatFormatting.RESET.toString() + " left the Battlefield!");
                        } else {
                            sendNotification(ChatFormatting.RED.toString() + playerName + ChatFormatting.RESET.toString() + " left the Battlefield!");
                        }
                        if (uwuAura.getValue()) sendServerMessage(("/w "+ playerName + " bye uwu"));
                    }

                    return;
                }
            }
        }

    }

    private void sendNotification(String s) {
        sendChatMessage(s);
    }

    @Override
    public void onEnable() {
        this.knownPlayers = new ArrayList<>();
    }
}

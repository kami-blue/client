// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.command.Command;
import java.util.Iterator;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import java.util.ArrayList;
import me.zeroeightsix.kami.setting.Settings;
import java.util.List;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "VisualRange", description = "Shows players who enter and leave range in chat", category = Category.COMBAT)
public class VisualRange extends Module
{
    private Setting<Boolean> leaving;
    private List<String> knownPlayers;
    
    public VisualRange() {
        this.leaving = this.register(Settings.b("Leaving", false));
    }
    
    @Override
    public void onUpdate() {
        if (VisualRange.mc.field_71439_g == null) {
            return;
        }
        final List<String> tickPlayerList = new ArrayList<String>();
        for (final Entity entity : VisualRange.mc.field_71441_e.func_72910_y()) {
            if (entity instanceof EntityPlayer) {
                tickPlayerList.add(entity.func_70005_c_());
            }
        }
        if (tickPlayerList.size() > 0) {
            for (final String playerName : tickPlayerList) {
                if (playerName.equals(VisualRange.mc.field_71439_g.func_70005_c_())) {
                    continue;
                }
                if (!this.knownPlayers.contains(playerName)) {
                    this.knownPlayers.add(playerName);
                    if (Friends.isFriend(playerName)) {
                        this.sendNotification(ChatFormatting.GREEN.toString() + playerName + ChatFormatting.RESET.toString() + " entered the Battlefield!");
                    }
                    else {
                        this.sendNotification(ChatFormatting.RED.toString() + playerName + ChatFormatting.RESET.toString() + " entered the Battlefield!");
                    }
                    return;
                }
            }
        }
        if (this.knownPlayers.size() > 0) {
            for (final String playerName : this.knownPlayers) {
                if (!tickPlayerList.contains(playerName)) {
                    this.knownPlayers.remove(playerName);
                    if (this.leaving.getValue()) {
                        if (Friends.isFriend(playerName)) {
                            this.sendNotification(ChatFormatting.GREEN.toString() + playerName + ChatFormatting.RESET.toString() + " left the Battlefield!");
                        }
                        else {
                            this.sendNotification(ChatFormatting.RED.toString() + playerName + ChatFormatting.RESET.toString() + " left the Battlefield!");
                        }
                    }
                }
            }
        }
    }
    
    private void sendNotification(final String s) {
        Command.sendChatMessage(s);
    }
    
    public void onEnable() {
        this.knownPlayers = new ArrayList<String>();
    }
}

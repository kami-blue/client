// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "XormiosHand", category = Category.MISC, description = "Gives you Xormios Hand")
public class XormiosHand extends Module
{
    private Setting<Boolean> alert;
    
    public XormiosHand() {
        this.alert = this.register(Settings.b("Chat Alerts", true));
    }
    
    @Override
    protected void onEnable() {
        if (XormiosHand.mc.field_71441_e != null && this.alert.getValue()) {
            Command.sendChatMessage("§2XORMIOS ENABLED");
        }
    }
    
    public void onDisable() {
        if (XormiosHand.mc.field_71441_e != null && this.alert.getValue()) {
            Command.sendChatMessage("§4XORMIOS DISABLED");
        }
    }
}

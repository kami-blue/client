// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.chat;

import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Auto-KitCreator", category = Category.CHAT, description = "Sends a message when enabled")
public class AutoKitcreator extends Module
{
    protected int onEnable() {
        if (AutoKitcreator.mc.field_71439_g != null) {
            Wrapper.getPlayer().func_71165_d("/kitcreator");
            this.disable();
        }
        return 0;
    }
    
    @Override
    protected void onDisable() {
    }
}

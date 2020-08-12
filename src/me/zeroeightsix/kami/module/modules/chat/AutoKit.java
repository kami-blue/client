// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.chat;

import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Auto-Kit", category = Category.CHAT, description = "Sends a message when enabled")
public class AutoKit extends Module
{
    protected int onEnable() {
        if (AutoKit.mc.field_71439_g != null) {
            Wrapper.getPlayer().func_71165_d("/kit");
            this.disable();
        }
        return 0;
    }
    
    @Override
    protected void onDisable() {
    }
}

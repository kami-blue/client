// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.gui;

import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "CleanGUI", category = Category.GUI, showOnArray = ShowOnArray.OFF)
public class CleanGUI extends Module
{
    public Setting<Boolean> inventoryGlobal;
    public static Setting<Boolean> chatGlobal;
    private static CleanGUI INSTANCE;
    
    public CleanGUI() {
        this.inventoryGlobal = this.register(Settings.b("Inventory", true));
        (CleanGUI.INSTANCE = this).register(CleanGUI.chatGlobal);
    }
    
    public static boolean enabled() {
        return CleanGUI.INSTANCE.isEnabled();
    }
    
    static {
        CleanGUI.chatGlobal = Settings.b("Chat", true);
        CleanGUI.INSTANCE = new CleanGUI();
    }
}

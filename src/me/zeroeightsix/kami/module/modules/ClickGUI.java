// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules;

import net.minecraft.client.gui.GuiScreen;
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen;
import me.zeroeightsix.kami.module.Module;

@Info(name = "clickGUI", description = "Opens the Click GUI", category = Category.HIDDEN)
public class ClickGUI extends Module
{
    public ClickGUI() {
        this.getBind().setKey(21);
    }
    
    @Override
    protected void onEnable() {
        if (!(ClickGUI.mc.field_71462_r instanceof DisplayGuiScreen)) {
            ClickGUI.mc.func_147108_a((GuiScreen)new DisplayGuiScreen(ClickGUI.mc.field_71462_r));
        }
        this.disable();
    }
}

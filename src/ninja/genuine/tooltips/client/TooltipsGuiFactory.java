// 
// Decompiled by Procyon v0.5.36
// 

package ninja.genuine.tooltips.client;

import ninja.genuine.tooltips.client.gui.GuiConfigTooltips;
import net.minecraft.client.gui.GuiScreen;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.IModGuiFactory;

public class TooltipsGuiFactory implements IModGuiFactory
{
    public void initialize(final Minecraft minecraftInstance) {
    }
    
    public Set<IModGuiFactory.RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
    
    public boolean hasConfigGui() {
        return true;
    }
    
    public GuiScreen createConfigGui(final GuiScreen parentScreen) {
        return (GuiScreen)new GuiConfigTooltips(parentScreen);
    }
}

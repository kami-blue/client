// 
// Decompiled by Procyon v0.5.36
// 

package ninja.genuine.tooltips.client.gui;

import java.util.Collection;
import net.minecraftforge.common.config.ConfigElement;
import ninja.genuine.tooltips.client.config.Config;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;

public class GuiConfigTooltips extends GuiConfig
{
    public GuiConfigTooltips(final GuiScreen parent) {
        super(parent, (List)new ArrayList(), "worldtooltips", "worldtooltipsgui", false, false, "World Tooltips", "Appearance configuration");
        final ConfigElement elementGeneral = new ConfigElement(Config.getCategory("General"));
        final ConfigElement elementAppearance = new ConfigElement(Config.getCategory("Appearance"));
        final ConfigElement elementBehavior = new ConfigElement(Config.getCategory("Behavior"));
        this.configElements.addAll(elementGeneral.getChildElements());
        this.configElements.addAll(elementAppearance.getChildElements());
        this.configElements.addAll(elementBehavior.getChildElements());
    }
}

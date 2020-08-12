// 
// Decompiled by Procyon v0.5.36
// 

package com.dazo66.shulkerboxshower.config;

import java.util.Iterator;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import com.dazo66.shulkerboxshower.ShulkerBoxViewer;
import net.minecraftforge.fml.client.config.GuiConfig;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.IConfigElement;
import java.util.List;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

public class ConfigFactory implements IModGuiFactory
{
    public static final String CLASS_NAME = "com.dazo66.shulkerboxshower.config.ConfigFactory";
    
    public GuiScreen createConfigGui(final GuiScreen parent) {
        return (GuiScreen)new ShulkerBoxViewerConfigGui(parent, new ConfigElement(ConfigLoader.config.getCategory("common")).getChildElements(), "shulkerboxviewer", "ShulkerBoxViewerConfigGui.Common");
    }
    
    public boolean hasConfigGui() {
        return true;
    }
    
    public void initialize(final Minecraft minecraftInstance) {
    }
    
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return (Class<? extends GuiScreen>)ShulkerBoxViewerConfigGui.class;
    }
    
    public Set<IModGuiFactory.RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
    
    public static class ShulkerBoxViewerConfigGui extends GuiConfig
    {
        public ShulkerBoxViewerConfigGui(final GuiScreen parent) {
            this(parent, new ConfigElement(ConfigLoader.config.getCategory("common")).getChildElements(), "shulkerboxviewer", "ShulkerBoxViewerConfigGui.Common");
        }
        
        public ShulkerBoxViewerConfigGui(final GuiScreen parentScreen, final List<IConfigElement> list, final String modid, final String title) {
            super(parentScreen, (List)list, modid, false, false, title);
        }
        
        public void func_146281_b() {
            ShulkerBoxViewer.config.configSave();
        }
        
        public GuiConfigEntries.IConfigEntry getCategoryEntry() {
            for (final GuiConfigEntries.IConfigEntry entry : this.entryList.listEntries) {
                if ("common".equals(entry.getName())) {
                    return entry;
                }
            }
            return null;
        }
    }
}

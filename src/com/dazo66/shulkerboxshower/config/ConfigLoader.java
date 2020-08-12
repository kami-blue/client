// 
// Decompiled by Procyon v0.5.36
// 

package com.dazo66.shulkerboxshower.config;

import java.io.File;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.Configuration;

public class ConfigLoader
{
    public static Configuration config;
    
    public ConfigLoader(final FMLPreInitializationEvent event) {
        final File configFile = event.getSuggestedConfigurationFile();
        ConfigLoader.config = new Configuration(configFile);
        this.configLoader();
        this.initialize();
    }
    
    private void configLoader() {
        ConfigLoader.config.load();
    }
    
    private void initialize() {
        this.isOrganizing();
        this.configSave();
    }
    
    public void configSave() {
        ConfigLoader.config.save();
    }
    
    public boolean isOrganizing() {
        final String comment = "Organizing the items in one stack or not";
        return ConfigLoader.config.getBoolean("Organizing The Items", "Common", false, comment);
    }
}

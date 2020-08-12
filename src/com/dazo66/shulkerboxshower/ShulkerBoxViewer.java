// 
// Decompiled by Procyon v0.5.36
// 

package com.dazo66.shulkerboxshower;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import com.dazo66.shulkerboxshower.eventhandler.ShulkerBoxViewerEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import com.dazo66.shulkerboxshower.config.ConfigLoader;
import net.minecraftforge.fml.common.Mod;

@Mod(modid = "shulkerboxviewer", version = "1.5", acceptedMinecraftVersions = "[1.11, 1.12.2]", clientSideOnly = true, guiFactory = "com.dazo66.shulkerboxshower.config.ConfigFactory")
public class ShulkerBoxViewer
{
    public static final String MODID = "shulkerboxviewer";
    public static final String NAME = "ShulkerBoxViewer";
    public static final String VERSION = "1.5";
    public static final String MCVersion = "[1.11, 1.12.2]";
    public static ConfigLoader config;
    
    @Mod.EventHandler
    public void initialize(final FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register((Object)ShulkerBoxViewerEventHandler.instance);
    }
    
    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        ShulkerBoxViewer.config = new ConfigLoader(event);
    }
}

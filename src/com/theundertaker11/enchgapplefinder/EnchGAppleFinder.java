// 
// Decompiled by Procyon v0.5.36
// 

package com.theundertaker11.enchgapplefinder;

import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(modid = "enchgapplefinder", name = "Enchanted Gold Apple Finder", version = "1.0", acceptedMinecraftVersions = "[1.12.2]")
public class EnchGAppleFinder
{
    public static final String MODID = "enchgapplefinder";
    public static final String NAME = "Enchanted Gold Apple Finder";
    public static final String VERSION = "1.0";
    
    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
    }
    
    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
    }
    
    @Mod.EventHandler
    public void postInit(final FMLPostInitializationEvent event) {
        ChestDetection.writeToFile("Start of new MC instance");
    }
}

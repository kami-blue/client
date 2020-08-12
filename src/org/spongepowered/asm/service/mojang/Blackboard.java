// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.service.mojang;

import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.service.IGlobalPropertyService;

public class Blackboard implements IGlobalPropertyService
{
    @Override
    public final <T> T getProperty(final String key) {
        return Launch.blackboard.get(key);
    }
    
    @Override
    public final void setProperty(final String key, final Object value) {
        Launch.blackboard.put(key, value);
    }
    
    @Override
    public final <T> T getProperty(final String key, final T defaultValue) {
        final Object value = Launch.blackboard.get(key);
        return (T)((value != null) ? value : defaultValue);
    }
    
    @Override
    public final String getPropertyString(final String key, final String defaultValue) {
        final Object value = Launch.blackboard.get(key);
        return (value != null) ? value.toString() : defaultValue;
    }
}

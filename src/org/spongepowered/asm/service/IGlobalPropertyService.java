// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.service;

public interface IGlobalPropertyService
{
     <T> T getProperty(final String p0);
    
    void setProperty(final String p0, final Object p1);
    
     <T> T getProperty(final String p0, final T p1);
    
    String getPropertyString(final String p0, final String p1);
}

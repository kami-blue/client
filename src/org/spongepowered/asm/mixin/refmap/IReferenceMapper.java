// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.refmap;

public interface IReferenceMapper
{
    boolean isDefault();
    
    String getResourceName();
    
    String getStatus();
    
    String getContext();
    
    void setContext(final String p0);
    
    String remap(final String p0, final String p1);
    
    String remapWithContext(final String p0, final String p1, final String p2);
}

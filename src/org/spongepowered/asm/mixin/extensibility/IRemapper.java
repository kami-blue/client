// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.extensibility;

public interface IRemapper
{
    String mapMethodName(final String p0, final String p1, final String p2);
    
    String mapFieldName(final String p0, final String p1, final String p2);
    
    String map(final String p0);
    
    String unmap(final String p0);
    
    String mapDesc(final String p0);
    
    String unmapDesc(final String p0);
}

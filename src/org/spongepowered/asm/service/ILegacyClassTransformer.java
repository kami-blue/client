// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.service;

public interface ILegacyClassTransformer extends ITransformer
{
    String getName();
    
    boolean isDelegationExcluded();
    
    byte[] transformClassBytes(final String p0, final String p1, final byte[] p2);
}

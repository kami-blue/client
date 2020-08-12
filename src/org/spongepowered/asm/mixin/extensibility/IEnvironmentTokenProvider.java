// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.extensibility;

import org.spongepowered.asm.mixin.MixinEnvironment;

public interface IEnvironmentTokenProvider
{
    public static final int DEFAULT_PRIORITY = 1000;
    
    int getPriority();
    
    Integer getToken(final String p0, final MixinEnvironment p1);
}

// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.launch.platform;

public interface IMixinPlatformAgent
{
    String getPhaseProvider();
    
    void prepare();
    
    void initPrimaryContainer();
    
    void inject();
    
    String getLaunchTarget();
}

// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.service;

import java.io.InputStream;
import java.util.Collection;
import org.spongepowered.asm.util.ReEntranceLock;
import org.spongepowered.asm.mixin.MixinEnvironment;

public interface IMixinService
{
    String getName();
    
    boolean isValid();
    
    void prepare();
    
    MixinEnvironment.Phase getInitialPhase();
    
    void init();
    
    void beginPhase();
    
    void checkEnv(final Object p0);
    
    ReEntranceLock getReEntranceLock();
    
    IClassProvider getClassProvider();
    
    IClassBytecodeProvider getBytecodeProvider();
    
    Collection<String> getPlatformAgents();
    
    InputStream getResourceAsStream(final String p0);
    
    void registerInvalidClass(final String p0);
    
    boolean isClassLoaded(final String p0);
    
    Collection<ITransformer> getTransformers();
    
    String getSideName();
}

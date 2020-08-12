// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.extensibility;

import org.spongepowered.asm.lib.tree.ClassNode;
import java.util.List;
import java.util.Set;

public interface IMixinConfigPlugin
{
    void onLoad(final String p0);
    
    String getRefMapperConfig();
    
    boolean shouldApplyMixin(final String p0, final String p1);
    
    void acceptTargets(final Set<String> p0, final Set<String> p1);
    
    List<String> getMixins();
    
    void preApply(final String p0, final ClassNode p1, final String p2, final IMixinInfo p3);
    
    void postApply(final String p0, final ClassNode p1, final String p2, final IMixinInfo p3);
}

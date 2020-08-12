// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.refmap;

import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public interface IMixinContext
{
    IMixinInfo getMixin();
    
    Extensions getExtensions();
    
    String getClassRef();
    
    String getTargetClassRef();
    
    IReferenceMapper getReferenceMapper();
    
    boolean getOption(final MixinEnvironment.Option p0);
    
    int getPriority();
    
    Target getTargetMethod(final MethodNode p0);
}

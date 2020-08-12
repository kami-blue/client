// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer.throwables;

import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinTargetAlreadyLoadedException extends InvalidMixinException
{
    private static final long serialVersionUID = 1L;
    private final String target;
    
    public MixinTargetAlreadyLoadedException(final IMixinInfo mixin, final String message, final String target) {
        super(mixin, message);
        this.target = target;
    }
    
    public MixinTargetAlreadyLoadedException(final IMixinInfo mixin, final String message, final String target, final Throwable cause) {
        super(mixin, message, cause);
        this.target = target;
    }
    
    public String getTarget() {
        return this.target;
    }
}

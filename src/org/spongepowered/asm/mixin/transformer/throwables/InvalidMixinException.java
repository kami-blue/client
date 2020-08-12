// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer.throwables;

import org.spongepowered.asm.mixin.refmap.IMixinContext;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.throwables.MixinException;

public class InvalidMixinException extends MixinException
{
    private static final long serialVersionUID = 2L;
    private final IMixinInfo mixin;
    
    public InvalidMixinException(final IMixinInfo mixin, final String message) {
        super(message);
        this.mixin = mixin;
    }
    
    public InvalidMixinException(final IMixinContext context, final String message) {
        this(context.getMixin(), message);
    }
    
    public InvalidMixinException(final IMixinInfo mixin, final Throwable cause) {
        super(cause);
        this.mixin = mixin;
    }
    
    public InvalidMixinException(final IMixinContext context, final Throwable cause) {
        this(context.getMixin(), cause);
    }
    
    public InvalidMixinException(final IMixinInfo mixin, final String message, final Throwable cause) {
        super(message, cause);
        this.mixin = mixin;
    }
    
    public InvalidMixinException(final IMixinContext context, final String message, final Throwable cause) {
        super(message, cause);
        this.mixin = context.getMixin();
    }
    
    public IMixinInfo getMixin() {
        return this.mixin;
    }
}

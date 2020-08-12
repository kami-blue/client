// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer.throwables;

import org.spongepowered.asm.mixin.refmap.IMixinContext;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class InvalidInterfaceMixinException extends InvalidMixinException
{
    private static final long serialVersionUID = 2L;
    
    public InvalidInterfaceMixinException(final IMixinInfo mixin, final String message) {
        super(mixin, message);
    }
    
    public InvalidInterfaceMixinException(final IMixinContext context, final String message) {
        super(context, message);
    }
    
    public InvalidInterfaceMixinException(final IMixinInfo mixin, final Throwable cause) {
        super(mixin, cause);
    }
    
    public InvalidInterfaceMixinException(final IMixinContext context, final Throwable cause) {
        super(context, cause);
    }
    
    public InvalidInterfaceMixinException(final IMixinInfo mixin, final String message, final Throwable cause) {
        super(mixin, message, cause);
    }
    
    public InvalidInterfaceMixinException(final IMixinContext context, final String message, final Throwable cause) {
        super(context, message, cause);
    }
}

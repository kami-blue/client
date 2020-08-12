// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.throwables;

public class MixinException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    
    public MixinException() {
    }
    
    public MixinException(final String message) {
        super(message);
    }
    
    public MixinException(final Throwable cause) {
        super(cause);
    }
    
    public MixinException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

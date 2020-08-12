// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.callback;

public class CancellationException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    
    public CancellationException() {
    }
    
    public CancellationException(final String message) {
        super(message);
    }
    
    public CancellationException(final Throwable cause) {
        super(cause);
    }
    
    public CancellationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

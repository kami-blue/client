// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.modify;

import org.spongepowered.asm.mixin.throwables.MixinException;

public class InvalidImplicitDiscriminatorException extends MixinException
{
    private static final long serialVersionUID = 1L;
    
    public InvalidImplicitDiscriminatorException(final String message) {
        super(message);
    }
    
    public InvalidImplicitDiscriminatorException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

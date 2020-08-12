// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.util.throwables;

import org.spongepowered.asm.mixin.throwables.MixinException;

public class LVTGeneratorException extends MixinException
{
    private static final long serialVersionUID = 1L;
    
    public LVTGeneratorException(final String message) {
        super(message);
    }
    
    public LVTGeneratorException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.throwables;

import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.refmap.IMixinContext;

public class InvalidInjectionPointException extends InvalidInjectionException
{
    private static final long serialVersionUID = 2L;
    
    public InvalidInjectionPointException(final IMixinContext context, final String format, final Object... args) {
        super(context, String.format(format, args));
    }
    
    public InvalidInjectionPointException(final InjectionInfo info, final String format, final Object... args) {
        super(info, String.format(format, args));
    }
    
    public InvalidInjectionPointException(final IMixinContext context, final Throwable cause, final String format, final Object... args) {
        super(context, String.format(format, args), cause);
    }
    
    public InvalidInjectionPointException(final InjectionInfo info, final Throwable cause, final String format, final Object... args) {
        super(info, String.format(format, args), cause);
    }
}

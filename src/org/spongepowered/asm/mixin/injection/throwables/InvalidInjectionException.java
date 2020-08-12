// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.throwables;

import org.spongepowered.asm.mixin.refmap.IMixinContext;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;

public class InvalidInjectionException extends InvalidMixinException
{
    private static final long serialVersionUID = 2L;
    private final InjectionInfo info;
    
    public InvalidInjectionException(final IMixinContext context, final String message) {
        super(context, message);
        this.info = null;
    }
    
    public InvalidInjectionException(final InjectionInfo info, final String message) {
        super(info.getContext(), message);
        this.info = info;
    }
    
    public InvalidInjectionException(final IMixinContext context, final Throwable cause) {
        super(context, cause);
        this.info = null;
    }
    
    public InvalidInjectionException(final InjectionInfo info, final Throwable cause) {
        super(info.getContext(), cause);
        this.info = info;
    }
    
    public InvalidInjectionException(final IMixinContext context, final String message, final Throwable cause) {
        super(context, message, cause);
        this.info = null;
    }
    
    public InvalidInjectionException(final InjectionInfo info, final String message, final Throwable cause) {
        super(info.getContext(), message, cause);
        this.info = info;
    }
    
    public InjectionInfo getInjectionInfo() {
        return this.info;
    }
}

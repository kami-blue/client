// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer.ext.extensions;

import org.spongepowered.asm.mixin.throwables.MixinException;
import org.spongepowered.asm.lib.ClassVisitor;
import org.spongepowered.asm.lib.util.CheckClassAdapter;
import org.spongepowered.asm.transformers.MixinClassWriter;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;

public class ExtensionCheckClass implements IExtension
{
    @Override
    public boolean checkActive(final MixinEnvironment environment) {
        return environment.getOption(MixinEnvironment.Option.DEBUG_VERIFY);
    }
    
    @Override
    public void preApply(final ITargetClassContext context) {
    }
    
    @Override
    public void postApply(final ITargetClassContext context) {
        try {
            context.getClassNode().accept(new CheckClassAdapter(new MixinClassWriter(2)));
        }
        catch (RuntimeException ex) {
            throw new ValidationFailedException(ex.getMessage(), ex);
        }
    }
    
    @Override
    public void export(final MixinEnvironment env, final String name, final boolean force, final byte[] bytes) {
    }
    
    public static class ValidationFailedException extends MixinException
    {
        private static final long serialVersionUID = 1L;
        
        public ValidationFailedException(final String message, final Throwable cause) {
            super(message, cause);
        }
        
        public ValidationFailedException(final String message) {
            super(message);
        }
        
        public ValidationFailedException(final Throwable cause) {
            super(cause);
        }
    }
}

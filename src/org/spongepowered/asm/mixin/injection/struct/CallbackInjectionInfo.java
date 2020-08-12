// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.struct;

import com.google.common.base.Strings;
import org.spongepowered.asm.mixin.injection.callback.CallbackInjector;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

public class CallbackInjectionInfo extends InjectionInfo
{
    protected CallbackInjectionInfo(final MixinTargetContext mixin, final MethodNode method, final AnnotationNode annotation) {
        super(mixin, method, annotation);
    }
    
    @Override
    protected Injector parseInjector(final AnnotationNode injectAnnotation) {
        final boolean cancellable = Annotations.getValue(injectAnnotation, "cancellable", Boolean.FALSE);
        final LocalCapture locals = Annotations.getValue(injectAnnotation, "locals", LocalCapture.class, LocalCapture.NO_CAPTURE);
        final String identifier = Annotations.getValue(injectAnnotation, "id", "");
        return new CallbackInjector(this, cancellable, locals, identifier);
    }
    
    @Override
    public String getSliceId(final String id) {
        return Strings.nullToEmpty(id);
    }
}

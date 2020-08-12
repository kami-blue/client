// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.struct;

import org.spongepowered.asm.mixin.injection.invoke.ModifyArgInjector;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

public class ModifyArgInjectionInfo extends InjectionInfo
{
    public ModifyArgInjectionInfo(final MixinTargetContext mixin, final MethodNode method, final AnnotationNode annotation) {
        super(mixin, method, annotation);
    }
    
    @Override
    protected Injector parseInjector(final AnnotationNode injectAnnotation) {
        final int index = Annotations.getValue(injectAnnotation, "index", -1);
        return new ModifyArgInjector(this, index);
    }
    
    @Override
    protected String getDescription() {
        return "Argument modifier method";
    }
}

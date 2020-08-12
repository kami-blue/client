// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.struct;

import org.spongepowered.asm.mixin.injection.modify.ModifyVariableInjector;
import org.spongepowered.asm.mixin.injection.modify.LocalVariableDiscriminator;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

public class ModifyVariableInjectionInfo extends InjectionInfo
{
    public ModifyVariableInjectionInfo(final MixinTargetContext mixin, final MethodNode method, final AnnotationNode annotation) {
        super(mixin, method, annotation);
    }
    
    @Override
    protected Injector parseInjector(final AnnotationNode injectAnnotation) {
        return new ModifyVariableInjector(this, LocalVariableDiscriminator.parse(injectAnnotation));
    }
    
    @Override
    protected String getDescription() {
        return "Variable modifier method";
    }
}

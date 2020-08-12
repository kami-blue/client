// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer;

import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.FieldNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidInterfaceMixinException;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.util.Bytecode;

class MixinPreProcessorInterface extends MixinPreProcessorStandard
{
    MixinPreProcessorInterface(final MixinInfo mixin, final MixinInfo.MixinClassNode classNode) {
        super(mixin, classNode);
    }
    
    @Override
    protected void prepareMethod(final MixinInfo.MixinMethodNode mixinMethod, final ClassInfo.Method method) {
        if (!Bytecode.hasFlag(mixinMethod, 1) && !Bytecode.hasFlag(mixinMethod, 4096)) {
            throw new InvalidInterfaceMixinException(this.mixin, "Interface mixin contains a non-public method! Found " + method + " in " + this.mixin);
        }
        super.prepareMethod(mixinMethod, method);
    }
    
    @Override
    protected boolean validateField(final MixinTargetContext context, final FieldNode field, final AnnotationNode shadow) {
        if (!Bytecode.hasFlag(field, 8)) {
            throw new InvalidInterfaceMixinException(this.mixin, "Interface mixin contains an instance field! Found " + field.name + " in " + this.mixin);
        }
        return super.validateField(context, field, shadow);
    }
}

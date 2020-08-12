// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.gen;

import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import org.spongepowered.asm.lib.Type;
import java.lang.annotation.Annotation;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

public class InvokerInfo extends AccessorInfo
{
    public InvokerInfo(final MixinTargetContext mixin, final MethodNode method) {
        super(mixin, method, Invoker.class);
    }
    
    @Override
    protected AccessorType initType() {
        return AccessorType.METHOD_PROXY;
    }
    
    @Override
    protected Type initTargetFieldType() {
        return null;
    }
    
    @Override
    protected MemberInfo initTarget() {
        return new MemberInfo(this.getTargetName(), null, this.method.desc);
    }
    
    @Override
    public void locate() {
        this.targetMethod = this.findTargetMethod();
    }
    
    private MethodNode findTargetMethod() {
        return this.findTarget(this.classNode.methods);
    }
}

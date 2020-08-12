// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.struct;

import org.spongepowered.asm.mixin.injection.Constant;
import com.google.common.base.Strings;
import org.spongepowered.asm.mixin.injection.invoke.ModifyConstantInjector;
import org.spongepowered.asm.mixin.injection.code.Injector;
import java.util.Iterator;
import org.spongepowered.asm.mixin.injection.points.BeforeConstant;
import org.spongepowered.asm.lib.Type;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

public class ModifyConstantInjectionInfo extends InjectionInfo
{
    private static final String CONSTANT_ANNOTATION_CLASS;
    
    public ModifyConstantInjectionInfo(final MixinTargetContext mixin, final MethodNode method, final AnnotationNode annotation) {
        super(mixin, method, annotation, "constant");
    }
    
    @Override
    protected List<AnnotationNode> readInjectionPoints(final String type) {
        List<AnnotationNode> ats = super.readInjectionPoints(type);
        if (ats.isEmpty()) {
            final AnnotationNode c = new AnnotationNode(ModifyConstantInjectionInfo.CONSTANT_ANNOTATION_CLASS);
            c.visit("log", Boolean.TRUE);
            ats = (List<AnnotationNode>)ImmutableList.of((Object)c);
        }
        return ats;
    }
    
    @Override
    protected void parseInjectionPoints(final List<AnnotationNode> ats) {
        final Type returnType = Type.getReturnType(this.method.desc);
        for (final AnnotationNode at : ats) {
            this.injectionPoints.add(new BeforeConstant(this.getContext(), at, returnType.getDescriptor()));
        }
    }
    
    @Override
    protected Injector parseInjector(final AnnotationNode injectAnnotation) {
        return new ModifyConstantInjector(this);
    }
    
    @Override
    protected String getDescription() {
        return "Constant modifier method";
    }
    
    @Override
    public String getSliceId(final String id) {
        return Strings.nullToEmpty(id);
    }
    
    static {
        CONSTANT_ANNOTATION_CLASS = Constant.class.getName().replace('.', '/');
    }
}

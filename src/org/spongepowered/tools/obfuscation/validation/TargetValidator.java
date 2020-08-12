// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation.validation;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.DeclaredType;
import org.spongepowered.tools.obfuscation.mirror.TypeUtils;
import javax.lang.model.type.TypeMirror;
import java.util.Iterator;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.lang.annotation.Annotation;
import org.spongepowered.asm.mixin.gen.Accessor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.spongepowered.tools.obfuscation.mirror.TypeHandle;
import java.util.Collection;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;
import javax.lang.model.element.TypeElement;
import org.spongepowered.tools.obfuscation.interfaces.IMixinValidator;
import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;
import org.spongepowered.tools.obfuscation.MixinValidator;

public class TargetValidator extends MixinValidator
{
    public TargetValidator(final IMixinAnnotationProcessor ap) {
        super(ap, IMixinValidator.ValidationPass.LATE);
    }
    
    public boolean validate(final TypeElement mixin, final AnnotationHandle annotation, final Collection<TypeHandle> targets) {
        if ("true".equalsIgnoreCase(this.options.getOption("disableTargetValidator"))) {
            return true;
        }
        if (mixin.getKind() == ElementKind.INTERFACE) {
            this.validateInterfaceMixin(mixin, targets);
        }
        else {
            this.validateClassMixin(mixin, targets);
        }
        return true;
    }
    
    private void validateInterfaceMixin(final TypeElement mixin, final Collection<TypeHandle> targets) {
        boolean containsNonAccessorMethod = false;
        for (final Element element : mixin.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                final boolean isAccessor = AnnotationHandle.of(element, Accessor.class).exists();
                final boolean isInvoker = AnnotationHandle.of(element, Invoker.class).exists();
                containsNonAccessorMethod |= (!isAccessor && !isInvoker);
            }
        }
        if (!containsNonAccessorMethod) {
            return;
        }
        for (final TypeHandle target : targets) {
            final TypeElement targetType = target.getElement();
            if (targetType != null && targetType.getKind() != ElementKind.INTERFACE) {
                this.error("Targetted type '" + target + " of " + mixin + " is not an interface", mixin);
            }
        }
    }
    
    private void validateClassMixin(final TypeElement mixin, final Collection<TypeHandle> targets) {
        final TypeMirror superClass = mixin.getSuperclass();
        for (final TypeHandle target : targets) {
            final TypeMirror targetType = target.getType();
            if (targetType != null && !this.validateSuperClass(targetType, superClass)) {
                this.error("Superclass " + superClass + " of " + mixin + " was not found in the hierarchy of target class " + targetType, mixin);
            }
        }
    }
    
    private boolean validateSuperClass(final TypeMirror targetType, final TypeMirror superClass) {
        return TypeUtils.isAssignable(this.processingEnv, targetType, superClass) || this.validateSuperClassRecursive(targetType, superClass);
    }
    
    private boolean validateSuperClassRecursive(final TypeMirror targetType, final TypeMirror superClass) {
        if (!(targetType instanceof DeclaredType)) {
            return false;
        }
        if (TypeUtils.isAssignable(this.processingEnv, targetType, superClass)) {
            return true;
        }
        final TypeElement targetElement = (TypeElement)((DeclaredType)targetType).asElement();
        final TypeMirror targetSuper = targetElement.getSuperclass();
        return targetSuper.getKind() != TypeKind.NONE && (this.checkMixinsFor(targetSuper, superClass) || this.validateSuperClassRecursive(targetSuper, superClass));
    }
    
    private boolean checkMixinsFor(final TypeMirror targetType, final TypeMirror superClass) {
        for (final TypeMirror mixinType : this.getMixinsTargeting(targetType)) {
            if (TypeUtils.isAssignable(this.processingEnv, mixinType, superClass)) {
                return true;
            }
        }
        return false;
    }
}

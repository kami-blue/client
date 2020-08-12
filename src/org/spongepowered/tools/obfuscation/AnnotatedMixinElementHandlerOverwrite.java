// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation;

import java.lang.reflect.Method;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import javax.annotation.processing.Messager;
import java.util.Iterator;
import javax.tools.Diagnostic;
import org.spongepowered.tools.obfuscation.mirror.TypeHandle;
import javax.lang.model.element.Element;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;
import javax.lang.model.element.ExecutableElement;
import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;

class AnnotatedMixinElementHandlerOverwrite extends AnnotatedMixinElementHandler
{
    AnnotatedMixinElementHandlerOverwrite(final IMixinAnnotationProcessor ap, final AnnotatedMixin mixin) {
        super(ap, mixin);
    }
    
    public void registerMerge(final ExecutableElement method) {
        this.validateTargetMethod(method, null, new AliasedElementName(method, AnnotationHandle.MISSING), "overwrite", true, true);
    }
    
    public void registerOverwrite(final AnnotatedElementOverwrite elem) {
        final AliasedElementName name = new AliasedElementName(((AnnotatedElement<Element>)elem).getElement(), elem.getAnnotation());
        this.validateTargetMethod(elem.getElement(), elem.getAnnotation(), name, "@Overwrite", true, false);
        this.checkConstraints(elem.getElement(), elem.getAnnotation());
        if (elem.shouldRemap()) {
            for (final TypeHandle target : this.mixin.getTargets()) {
                if (!this.registerOverwriteForTarget(elem, target)) {
                    return;
                }
            }
        }
        if (!"true".equalsIgnoreCase(this.ap.getOption("disableOverwriteChecker"))) {
            final Diagnostic.Kind overwriteErrorKind = "error".equalsIgnoreCase(this.ap.getOption("overwriteErrorLevel")) ? Diagnostic.Kind.ERROR : Diagnostic.Kind.WARNING;
            final String javadoc = this.ap.getJavadocProvider().getJavadoc(((AnnotatedElement<Element>)elem).getElement());
            if (javadoc == null) {
                this.ap.printMessage(overwriteErrorKind, "@Overwrite is missing javadoc comment", ((AnnotatedElement<Element>)elem).getElement());
                return;
            }
            if (!javadoc.toLowerCase().contains("@author")) {
                this.ap.printMessage(overwriteErrorKind, "@Overwrite is missing an @author tag", ((AnnotatedElement<Element>)elem).getElement());
            }
            if (!javadoc.toLowerCase().contains("@reason")) {
                this.ap.printMessage(overwriteErrorKind, "@Overwrite is missing an @reason tag", ((AnnotatedElement<Element>)elem).getElement());
            }
        }
    }
    
    private boolean registerOverwriteForTarget(final AnnotatedElementOverwrite elem, final TypeHandle target) {
        final MappingMethod targetMethod = target.getMappingMethod(elem.getSimpleName(), elem.getDesc());
        final ObfuscationData<MappingMethod> obfData = this.obf.getDataProvider().getObfMethod(targetMethod);
        if (obfData.isEmpty()) {
            Diagnostic.Kind error = Diagnostic.Kind.ERROR;
            try {
                final Method md = elem.getElement().getClass().getMethod("isStatic", (Class<?>[])new Class[0]);
                if (md.invoke(((AnnotatedElement<Object>)elem).getElement(), new Object[0])) {
                    error = Diagnostic.Kind.WARNING;
                }
            }
            catch (Exception ex2) {}
            this.ap.printMessage(error, "No obfuscation mapping for @Overwrite method", ((AnnotatedElement<Element>)elem).getElement());
            return false;
        }
        try {
            this.addMethodMappings(elem.getSimpleName(), elem.getDesc(), obfData);
        }
        catch (Mappings.MappingConflictException ex) {
            elem.printMessage(this.ap, Diagnostic.Kind.ERROR, "Mapping conflict for @Overwrite method: " + ex.getNew().getSimpleName() + " for target " + target + " conflicts with existing mapping " + ex.getOld().getSimpleName());
            return false;
        }
        return true;
    }
    
    static class AnnotatedElementOverwrite extends AnnotatedElement<ExecutableElement>
    {
        private final boolean shouldRemap;
        
        public AnnotatedElementOverwrite(final ExecutableElement element, final AnnotationHandle annotation, final boolean shouldRemap) {
            super(element, annotation);
            this.shouldRemap = shouldRemap;
        }
        
        public boolean shouldRemap() {
            return this.shouldRemap;
        }
    }
}

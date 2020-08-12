// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation;

import java.util.Iterator;
import javax.tools.Diagnostic;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ElementKind;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;
import org.spongepowered.tools.obfuscation.mirror.TypeUtils;
import javax.lang.model.element.Element;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import java.lang.annotation.Annotation;
import org.spongepowered.asm.mixin.injection.Inject;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import javax.annotation.processing.SupportedAnnotationTypes;

@SupportedAnnotationTypes({ "org.spongepowered.asm.mixin.injection.Inject", "org.spongepowered.asm.mixin.injection.ModifyArg", "org.spongepowered.asm.mixin.injection.ModifyArgs", "org.spongepowered.asm.mixin.injection.Redirect", "org.spongepowered.asm.mixin.injection.At" })
public class MixinObfuscationProcessorInjection extends MixinObfuscationProcessor
{
    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            this.postProcess(roundEnv);
            return true;
        }
        this.processMixins(roundEnv);
        this.processInjectors(roundEnv, Inject.class);
        this.processInjectors(roundEnv, ModifyArg.class);
        this.processInjectors(roundEnv, ModifyArgs.class);
        this.processInjectors(roundEnv, Redirect.class);
        this.processInjectors(roundEnv, ModifyVariable.class);
        this.processInjectors(roundEnv, ModifyConstant.class);
        this.postProcess(roundEnv);
        return true;
    }
    
    @Override
    protected void postProcess(final RoundEnvironment roundEnv) {
        super.postProcess(roundEnv);
        try {
            this.mixins.writeReferences();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void processInjectors(final RoundEnvironment roundEnv, final Class<? extends Annotation> injectorClass) {
        for (final Element elem : roundEnv.getElementsAnnotatedWith(injectorClass)) {
            final Element parent = elem.getEnclosingElement();
            if (!(parent instanceof TypeElement)) {
                throw new IllegalStateException("@" + injectorClass.getSimpleName() + " element has unexpected parent with type " + TypeUtils.getElementType(parent));
            }
            final AnnotationHandle inject = AnnotationHandle.of(elem, injectorClass);
            if (elem.getKind() == ElementKind.METHOD) {
                this.mixins.registerInjector((TypeElement)parent, (ExecutableElement)elem, inject);
            }
            else {
                this.mixins.printMessage(Diagnostic.Kind.WARNING, "Found an @" + injectorClass.getSimpleName() + " annotation on an element which is not a method: " + elem.toString());
            }
        }
    }
}

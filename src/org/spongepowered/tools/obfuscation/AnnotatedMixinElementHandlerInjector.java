// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation;

import java.util.HashMap;
import java.util.Map;
import java.lang.annotation.Annotation;
import javax.lang.model.element.Element;
import org.spongepowered.asm.mixin.injection.Coerce;
import javax.lang.model.element.VariableElement;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;
import org.spongepowered.tools.obfuscation.struct.InjectorRemap;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import javax.lang.model.element.AnnotationMirror;
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;
import org.spongepowered.tools.obfuscation.interfaces.IReferenceManager;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import java.util.Iterator;
import org.spongepowered.tools.obfuscation.mirror.TypeHandle;
import javax.lang.model.element.ExecutableElement;
import org.spongepowered.asm.mixin.injection.struct.InvalidMemberDescriptorException;
import javax.annotation.processing.Messager;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import javax.tools.Diagnostic;
import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;

class AnnotatedMixinElementHandlerInjector extends AnnotatedMixinElementHandler
{
    AnnotatedMixinElementHandlerInjector(final IMixinAnnotationProcessor ap, final AnnotatedMixin mixin) {
        super(ap, mixin);
    }
    
    public void registerInjector(final AnnotatedElementInjector elem) {
        if (this.mixin.isInterface()) {
            this.ap.printMessage(Diagnostic.Kind.ERROR, "Injector in interface is unsupported", ((AnnotatedElement<Element>)elem).getElement());
        }
        for (final String reference : elem.getAnnotation().getList("method")) {
            final MemberInfo targetMember = MemberInfo.parse(reference);
            if (targetMember.name == null) {
                continue;
            }
            try {
                targetMember.validate();
            }
            catch (InvalidMemberDescriptorException ex) {
                elem.printMessage(this.ap, Diagnostic.Kind.ERROR, ex.getMessage());
            }
            if (targetMember.desc != null) {
                this.validateReferencedTarget(elem.getElement(), elem.getAnnotation(), targetMember, elem.toString());
            }
            if (!elem.shouldRemap()) {
                continue;
            }
            for (final TypeHandle target : this.mixin.getTargets()) {
                if (!this.registerInjector(elem, reference, targetMember, target)) {
                    break;
                }
            }
        }
    }
    
    private boolean registerInjector(final AnnotatedElementInjector elem, final String reference, final MemberInfo targetMember, final TypeHandle target) {
        final String desc = target.findDescriptor(targetMember);
        if (desc == null) {
            final Diagnostic.Kind error = this.mixin.isMultiTarget() ? Diagnostic.Kind.ERROR : Diagnostic.Kind.WARNING;
            if (target.isSimulated()) {
                elem.printMessage(this.ap, Diagnostic.Kind.NOTE, elem + " target '" + reference + "' in @Pseudo mixin will not be obfuscated");
            }
            else if (target.isImaginary()) {
                elem.printMessage(this.ap, error, elem + " target requires method signature because enclosing type information for " + target + " is unavailable");
            }
            else if (!targetMember.isInitialiser()) {
                elem.printMessage(this.ap, error, "Unable to determine signature for " + elem + " target method");
            }
            return true;
        }
        final String targetName = elem + " target " + targetMember.name;
        final MappingMethod targetMethod = target.getMappingMethod(targetMember.name, desc);
        ObfuscationData<MappingMethod> obfData = this.obf.getDataProvider().getObfMethod(targetMethod);
        if (obfData.isEmpty()) {
            if (target.isSimulated()) {
                obfData = this.obf.getDataProvider().getRemappedMethod(targetMethod);
            }
            else {
                if (targetMember.isClassInitialiser()) {
                    return true;
                }
                final Diagnostic.Kind error2 = targetMember.isConstructor() ? Diagnostic.Kind.WARNING : Diagnostic.Kind.ERROR;
                elem.addMessage(error2, "No obfuscation mapping for " + targetName, ((AnnotatedElement<Element>)elem).getElement(), elem.getAnnotation());
                return false;
            }
        }
        final IReferenceManager refMap = this.obf.getReferenceManager();
        try {
            if ((targetMember.owner == null && this.mixin.isMultiTarget()) || target.isSimulated()) {
                obfData = AnnotatedMixinElementHandler.stripOwnerData(obfData);
            }
            refMap.addMethodMapping(this.classRef, reference, obfData);
        }
        catch (ReferenceManager.ReferenceConflictException ex) {
            final String conflictType = this.mixin.isMultiTarget() ? "Multi-target" : "Target";
            if (elem.hasCoerceArgument() && targetMember.owner == null && targetMember.desc == null) {
                final MemberInfo oldMember = MemberInfo.parse(ex.getOld());
                final MemberInfo newMember = MemberInfo.parse(ex.getNew());
                if (oldMember.name.equals(newMember.name)) {
                    obfData = AnnotatedMixinElementHandler.stripDescriptors(obfData);
                    refMap.setAllowConflicts(true);
                    refMap.addMethodMapping(this.classRef, reference, obfData);
                    refMap.setAllowConflicts(false);
                    elem.printMessage(this.ap, Diagnostic.Kind.WARNING, "Coerced " + conflictType + " reference has conflicting descriptors for " + targetName + ": Storing bare references " + obfData.values() + " in refMap");
                    return true;
                }
            }
            elem.printMessage(this.ap, Diagnostic.Kind.ERROR, conflictType + " reference conflict for " + targetName + ": " + reference + " -> " + ex.getNew() + " previously defined as " + ex.getOld());
        }
        return true;
    }
    
    public void registerInjectionPoint(final AnnotatedElementInjectionPoint elem, final String format) {
        if (this.mixin.isInterface()) {
            this.ap.printMessage(Diagnostic.Kind.ERROR, "Injector in interface is unsupported", ((AnnotatedElement<Element>)elem).getElement());
        }
        if (!elem.shouldRemap()) {
            return;
        }
        final String type = InjectionPointData.parseType(elem.getAt().getValue("value"));
        final String target = elem.getAt().getValue("target");
        if ("NEW".equals(type)) {
            this.remapNewTarget(String.format(format, type + ".<target>"), target, elem);
            this.remapNewTarget(String.format(format, type + ".args[class]"), elem.getAtArg("class"), elem);
        }
        else {
            this.remapReference(String.format(format, type + ".<target>"), target, elem);
        }
    }
    
    protected final void remapNewTarget(final String subject, final String reference, final AnnotatedElementInjectionPoint elem) {
        if (reference == null) {
            return;
        }
        final MemberInfo member = MemberInfo.parse(reference);
        final String target = member.toCtorType();
        if (target != null) {
            final String desc = member.toCtorDesc();
            final MappingMethod m = new MappingMethod(target, ".", (desc != null) ? desc : "()V");
            final ObfuscationData<MappingMethod> remapped = this.obf.getDataProvider().getRemappedMethod(m);
            if (remapped.isEmpty()) {
                this.ap.printMessage(Diagnostic.Kind.WARNING, "Cannot find class mapping for " + subject + " '" + target + "'", ((AnnotatedElement<Element>)elem).getElement(), elem.getAnnotation().asMirror());
                return;
            }
            final ObfuscationData<String> mappings = new ObfuscationData<String>();
            for (final ObfuscationType type : remapped) {
                final MappingMethod mapping = remapped.get(type);
                if (desc == null) {
                    mappings.put(type, mapping.getOwner());
                }
                else {
                    mappings.put(type, mapping.getDesc().replace(")V", ")L" + mapping.getOwner() + ";"));
                }
            }
            this.obf.getReferenceManager().addClassMapping(this.classRef, reference, mappings);
        }
        elem.notifyRemapped();
    }
    
    protected final void remapReference(final String subject, final String reference, final AnnotatedElementInjectionPoint elem) {
        if (reference == null) {
            return;
        }
        final AnnotationMirror errorsOn = ((this.ap.getCompilerEnvironment() == IMixinAnnotationProcessor.CompilerEnvironment.JDT) ? elem.getAt() : elem.getAnnotation()).asMirror();
        final MemberInfo targetMember = MemberInfo.parse(reference);
        if (!targetMember.isFullyQualified()) {
            final String missing = (targetMember.owner == null) ? ((targetMember.desc == null) ? "owner and signature" : "owner") : "signature";
            this.ap.printMessage(Diagnostic.Kind.ERROR, subject + " is not fully qualified, missing " + missing, ((AnnotatedElement<Element>)elem).getElement(), errorsOn);
            return;
        }
        try {
            targetMember.validate();
        }
        catch (InvalidMemberDescriptorException ex) {
            this.ap.printMessage(Diagnostic.Kind.ERROR, ex.getMessage(), ((AnnotatedElement<Element>)elem).getElement(), errorsOn);
        }
        try {
            if (targetMember.isField()) {
                final ObfuscationData<MappingField> obfFieldData = this.obf.getDataProvider().getObfFieldRecursive(targetMember);
                if (obfFieldData.isEmpty()) {
                    this.ap.printMessage(Diagnostic.Kind.WARNING, "Cannot find field mapping for " + subject + " '" + reference + "'", ((AnnotatedElement<Element>)elem).getElement(), errorsOn);
                    return;
                }
                this.obf.getReferenceManager().addFieldMapping(this.classRef, reference, targetMember, obfFieldData);
            }
            else {
                final ObfuscationData<MappingMethod> obfMethodData = this.obf.getDataProvider().getObfMethodRecursive(targetMember);
                if (obfMethodData.isEmpty() && (targetMember.owner == null || !targetMember.owner.startsWith("java/lang/"))) {
                    this.ap.printMessage(Diagnostic.Kind.WARNING, "Cannot find method mapping for " + subject + " '" + reference + "'", ((AnnotatedElement<Element>)elem).getElement(), errorsOn);
                    return;
                }
                this.obf.getReferenceManager().addMethodMapping(this.classRef, reference, targetMember, obfMethodData);
            }
        }
        catch (ReferenceManager.ReferenceConflictException ex2) {
            this.ap.printMessage(Diagnostic.Kind.ERROR, "Unexpected reference conflict for " + subject + ": " + reference + " -> " + ex2.getNew() + " previously defined as " + ex2.getOld(), ((AnnotatedElement<Element>)elem).getElement(), errorsOn);
            return;
        }
        elem.notifyRemapped();
    }
    
    static class AnnotatedElementInjector extends AnnotatedElement<ExecutableElement>
    {
        private final InjectorRemap state;
        
        public AnnotatedElementInjector(final ExecutableElement element, final AnnotationHandle annotation, final InjectorRemap shouldRemap) {
            super(element, annotation);
            this.state = shouldRemap;
        }
        
        public boolean shouldRemap() {
            return this.state.shouldRemap();
        }
        
        public boolean hasCoerceArgument() {
            if (!this.annotation.toString().equals("@Inject")) {
                return false;
            }
            final Iterator<? extends VariableElement> iterator = ((ExecutableElement)this.element).getParameters().iterator();
            if (iterator.hasNext()) {
                final VariableElement param = (VariableElement)iterator.next();
                return AnnotationHandle.of(param, Coerce.class).exists();
            }
            return false;
        }
        
        public void addMessage(final Diagnostic.Kind kind, final CharSequence msg, final Element element, final AnnotationHandle annotation) {
            this.state.addMessage(kind, msg, element, annotation);
        }
        
        @Override
        public String toString() {
            return this.getAnnotation().toString();
        }
    }
    
    static class AnnotatedElementInjectionPoint extends AnnotatedElement<ExecutableElement>
    {
        private final AnnotationHandle at;
        private Map<String, String> args;
        private final InjectorRemap state;
        
        public AnnotatedElementInjectionPoint(final ExecutableElement element, final AnnotationHandle inject, final AnnotationHandle at, final InjectorRemap state) {
            super(element, inject);
            this.at = at;
            this.state = state;
        }
        
        public boolean shouldRemap() {
            return this.at.getBoolean("remap", this.state.shouldRemap());
        }
        
        public AnnotationHandle getAt() {
            return this.at;
        }
        
        public String getAtArg(final String key) {
            if (this.args == null) {
                this.args = new HashMap<String, String>();
                for (final String arg : this.at.getList("args")) {
                    if (arg == null) {
                        continue;
                    }
                    final int eqPos = arg.indexOf(61);
                    if (eqPos > -1) {
                        this.args.put(arg.substring(0, eqPos), arg.substring(eqPos + 1));
                    }
                    else {
                        this.args.put(arg, "");
                    }
                }
            }
            return this.args.get(key);
        }
        
        public void notifyRemapped() {
            this.state.notifyRemapped();
        }
    }
}

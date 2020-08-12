// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation;

import javax.annotation.processing.Messager;
import org.spongepowered.asm.obfuscation.mapping.IMapping;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import java.util.List;
import org.spongepowered.tools.obfuscation.mirror.FieldHandle;
import org.spongepowered.tools.obfuscation.mirror.Visibility;
import org.spongepowered.tools.obfuscation.mirror.MethodHandle;
import org.spongepowered.tools.obfuscation.mirror.TypeHandle;
import org.spongepowered.tools.obfuscation.mirror.TypeUtils;
import javax.lang.model.element.VariableElement;
import org.spongepowered.asm.util.throwables.InvalidConstraintException;
import org.spongepowered.asm.util.throwables.ConstraintViolationException;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import org.spongepowered.asm.util.ConstraintParser;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;
import javax.lang.model.element.ExecutableElement;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import java.util.Iterator;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.tools.obfuscation.mapping.IMappingConsumer;
import org.spongepowered.tools.obfuscation.interfaces.IObfuscationManager;
import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;

abstract class AnnotatedMixinElementHandler
{
    protected final AnnotatedMixin mixin;
    protected final String classRef;
    protected final IMixinAnnotationProcessor ap;
    protected final IObfuscationManager obf;
    private IMappingConsumer mappings;
    
    AnnotatedMixinElementHandler(final IMixinAnnotationProcessor ap, final AnnotatedMixin mixin) {
        this.ap = ap;
        this.mixin = mixin;
        this.classRef = mixin.getClassRef();
        this.obf = ap.getObfuscationManager();
    }
    
    private IMappingConsumer getMappings() {
        if (this.mappings == null) {
            final IMappingConsumer mappingConsumer = this.mixin.getMappings();
            if (mappingConsumer instanceof Mappings) {
                this.mappings = ((Mappings)mappingConsumer).asUnique();
            }
            else {
                this.mappings = mappingConsumer;
            }
        }
        return this.mappings;
    }
    
    protected final void addFieldMappings(final String mcpName, final String mcpSignature, final ObfuscationData<MappingField> obfData) {
        for (final ObfuscationType type : obfData) {
            final MappingField obfField = obfData.get(type);
            this.addFieldMapping(type, mcpName, obfField.getSimpleName(), mcpSignature, obfField.getDesc());
        }
    }
    
    protected final void addFieldMapping(final ObfuscationType type, final ShadowElementName name, final String mcpSignature, final String obfSignature) {
        this.addFieldMapping(type, name.name(), name.obfuscated(), mcpSignature, obfSignature);
    }
    
    protected final void addFieldMapping(final ObfuscationType type, final String mcpName, final String obfName, final String mcpSignature, final String obfSignature) {
        final MappingField from = new MappingField(this.classRef, mcpName, mcpSignature);
        final MappingField to = new MappingField(this.classRef, obfName, obfSignature);
        this.getMappings().addFieldMapping(type, from, to);
    }
    
    protected final void addMethodMappings(final String mcpName, final String mcpSignature, final ObfuscationData<MappingMethod> obfData) {
        for (final ObfuscationType type : obfData) {
            final MappingMethod obfMethod = obfData.get(type);
            this.addMethodMapping(type, mcpName, obfMethod.getSimpleName(), mcpSignature, obfMethod.getDesc());
        }
    }
    
    protected final void addMethodMapping(final ObfuscationType type, final ShadowElementName name, final String mcpSignature, final String obfSignature) {
        this.addMethodMapping(type, name.name(), name.obfuscated(), mcpSignature, obfSignature);
    }
    
    protected final void addMethodMapping(final ObfuscationType type, final String mcpName, final String obfName, final String mcpSignature, final String obfSignature) {
        final MappingMethod from = new MappingMethod(this.classRef, mcpName, mcpSignature);
        final MappingMethod to = new MappingMethod(this.classRef, obfName, obfSignature);
        this.getMappings().addMethodMapping(type, from, to);
    }
    
    protected final void checkConstraints(final ExecutableElement method, final AnnotationHandle annotation) {
        try {
            final ConstraintParser.Constraint constraint = ConstraintParser.parse(annotation.getValue("constraints"));
            try {
                constraint.check(this.ap.getTokenProvider());
            }
            catch (ConstraintViolationException ex) {
                this.ap.printMessage(Diagnostic.Kind.ERROR, ex.getMessage(), method, annotation.asMirror());
            }
        }
        catch (InvalidConstraintException ex2) {
            this.ap.printMessage(Diagnostic.Kind.WARNING, ex2.getMessage(), method, annotation.asMirror());
        }
    }
    
    protected final void validateTarget(final Element element, final AnnotationHandle annotation, final AliasedElementName name, final String type) {
        if (element instanceof ExecutableElement) {
            this.validateTargetMethod((ExecutableElement)element, annotation, name, type, false, false);
        }
        else if (element instanceof VariableElement) {
            this.validateTargetField((VariableElement)element, annotation, name, type);
        }
    }
    
    protected final void validateTargetMethod(final ExecutableElement method, final AnnotationHandle annotation, final AliasedElementName name, final String type, final boolean overwrite, final boolean merge) {
        final String signature = TypeUtils.getJavaSignature(method);
        for (final TypeHandle target : this.mixin.getTargets()) {
            if (target.isImaginary()) {
                continue;
            }
            MethodHandle targetMethod = target.findMethod(method);
            if (targetMethod == null && name.hasPrefix()) {
                targetMethod = target.findMethod(name.baseName(), signature);
            }
            if (targetMethod == null && name.hasAliases()) {
                for (final String alias : name.getAliases()) {
                    if ((targetMethod = target.findMethod(alias, signature)) != null) {
                        break;
                    }
                }
            }
            if (targetMethod != null) {
                if (!overwrite) {
                    continue;
                }
                this.validateMethodVisibility(method, annotation, type, target, targetMethod);
            }
            else {
                if (merge) {
                    continue;
                }
                this.printMessage(Diagnostic.Kind.WARNING, "Cannot find target for " + type + " method in " + target, method, annotation);
            }
        }
    }
    
    private void validateMethodVisibility(final ExecutableElement method, final AnnotationHandle annotation, final String type, final TypeHandle target, final MethodHandle targetMethod) {
        final Visibility visTarget = targetMethod.getVisibility();
        if (visTarget == null) {
            return;
        }
        final Visibility visMethod = TypeUtils.getVisibility(method);
        final String visibility = "visibility of " + visTarget + " method in " + target;
        if (visTarget.ordinal() > visMethod.ordinal()) {
            this.printMessage(Diagnostic.Kind.WARNING, visMethod + " " + type + " method cannot reduce " + visibility, method, annotation);
        }
        else if (visTarget == Visibility.PRIVATE && visMethod.ordinal() > visTarget.ordinal()) {
            this.printMessage(Diagnostic.Kind.WARNING, visMethod + " " + type + " method will upgrade " + visibility, method, annotation);
        }
    }
    
    protected final void validateTargetField(final VariableElement field, final AnnotationHandle annotation, final AliasedElementName name, final String type) {
        final String fieldType = field.asType().toString();
        for (final TypeHandle target : this.mixin.getTargets()) {
            if (target.isImaginary()) {
                continue;
            }
            FieldHandle targetField = target.findField(field);
            if (targetField != null) {
                continue;
            }
            final List<String> aliases = name.getAliases();
            for (final String alias : aliases) {
                if ((targetField = target.findField(alias, fieldType)) != null) {
                    break;
                }
            }
            if (targetField != null) {
                continue;
            }
            this.ap.printMessage(Diagnostic.Kind.WARNING, "Cannot find target for " + type + " field in " + target, field, annotation.asMirror());
        }
    }
    
    protected final void validateReferencedTarget(final ExecutableElement method, final AnnotationHandle inject, final MemberInfo reference, final String type) {
        final String signature = reference.toDescriptor();
        for (final TypeHandle target : this.mixin.getTargets()) {
            if (target.isImaginary()) {
                continue;
            }
            final MethodHandle targetMethod = target.findMethod(reference.name, signature);
            if (targetMethod != null) {
                continue;
            }
            this.ap.printMessage(Diagnostic.Kind.WARNING, "Cannot find target method for " + type + " in " + target, method, inject.asMirror());
        }
    }
    
    private void printMessage(final Diagnostic.Kind kind, final String msg, final Element e, final AnnotationHandle annotation) {
        if (annotation == null) {
            this.ap.printMessage(kind, msg, e);
        }
        else {
            this.ap.printMessage(kind, msg, e, annotation.asMirror());
        }
    }
    
    protected static <T extends IMapping<T>> ObfuscationData<T> stripOwnerData(final ObfuscationData<T> data) {
        final ObfuscationData<T> stripped = new ObfuscationData<T>();
        for (final ObfuscationType type : data) {
            final T mapping = data.get(type);
            stripped.put(type, mapping.move(null));
        }
        return stripped;
    }
    
    protected static <T extends IMapping<T>> ObfuscationData<T> stripDescriptors(final ObfuscationData<T> data) {
        final ObfuscationData<T> stripped = new ObfuscationData<T>();
        for (final ObfuscationType type : data) {
            final T mapping = data.get(type);
            stripped.put(type, mapping.transform(null));
        }
        return stripped;
    }
    
    abstract static class AnnotatedElement<E extends Element>
    {
        protected final E element;
        protected final AnnotationHandle annotation;
        private final String desc;
        
        public AnnotatedElement(final E element, final AnnotationHandle annotation) {
            this.element = element;
            this.annotation = annotation;
            this.desc = TypeUtils.getDescriptor(element);
        }
        
        public E getElement() {
            return this.element;
        }
        
        public AnnotationHandle getAnnotation() {
            return this.annotation;
        }
        
        public String getSimpleName() {
            return this.getElement().getSimpleName().toString();
        }
        
        public String getDesc() {
            return this.desc;
        }
        
        public final void printMessage(final Messager messager, final Diagnostic.Kind kind, final CharSequence msg) {
            messager.printMessage(kind, msg, this.element, this.annotation.asMirror());
        }
    }
    
    static class AliasedElementName
    {
        protected final String originalName;
        private final List<String> aliases;
        private boolean caseSensitive;
        
        public AliasedElementName(final Element element, final AnnotationHandle annotation) {
            this.originalName = element.getSimpleName().toString();
            this.aliases = annotation.getList("aliases");
        }
        
        public AliasedElementName setCaseSensitive(final boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }
        
        public boolean isCaseSensitive() {
            return this.caseSensitive;
        }
        
        public boolean hasAliases() {
            return this.aliases.size() > 0;
        }
        
        public List<String> getAliases() {
            return this.aliases;
        }
        
        public String elementName() {
            return this.originalName;
        }
        
        public String baseName() {
            return this.originalName;
        }
        
        public boolean hasPrefix() {
            return false;
        }
    }
    
    static class ShadowElementName extends AliasedElementName
    {
        private final boolean hasPrefix;
        private final String prefix;
        private final String baseName;
        private String obfuscated;
        
        ShadowElementName(final Element element, final AnnotationHandle shadow) {
            super(element, shadow);
            this.prefix = shadow.getValue("prefix", "shadow$");
            boolean hasPrefix = false;
            String name = this.originalName;
            if (name.startsWith(this.prefix)) {
                hasPrefix = true;
                name = name.substring(this.prefix.length());
            }
            this.hasPrefix = hasPrefix;
            final String s = name;
            this.baseName = s;
            this.obfuscated = s;
        }
        
        @Override
        public String toString() {
            return this.baseName;
        }
        
        @Override
        public String baseName() {
            return this.baseName;
        }
        
        public ShadowElementName setObfuscatedName(final IMapping<?> name) {
            this.obfuscated = name.getName();
            return this;
        }
        
        public ShadowElementName setObfuscatedName(final String name) {
            this.obfuscated = name;
            return this;
        }
        
        @Override
        public boolean hasPrefix() {
            return this.hasPrefix;
        }
        
        public String prefix() {
            return this.hasPrefix ? this.prefix : "";
        }
        
        public String name() {
            return this.prefix(this.baseName);
        }
        
        public String obfuscated() {
            return this.prefix(this.obfuscated);
        }
        
        public String prefix(final String name) {
            return this.hasPrefix ? (this.prefix + name) : name;
        }
    }
}

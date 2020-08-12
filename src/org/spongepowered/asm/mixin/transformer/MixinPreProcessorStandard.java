// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer;

import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.Overwrite;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.lib.Type;
import com.google.common.base.Strings;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.util.throwables.SyntheticBridgeException;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;
import org.spongepowered.asm.mixin.refmap.IMixinContext;
import org.spongepowered.asm.mixin.gen.throwables.InvalidAccessorException;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.mixin.transformer.meta.MixinRenamed;
import java.lang.annotation.Annotation;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.mixin.Shadow;
import java.util.Iterator;
import org.spongepowered.asm.lib.tree.FieldNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.util.perf.Profiler;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.apache.logging.log4j.Logger;

class MixinPreProcessorStandard
{
    private static final Logger logger;
    protected final MixinInfo mixin;
    protected final MixinInfo.MixinClassNode classNode;
    protected final MixinEnvironment env;
    protected final Profiler profiler;
    private final boolean verboseLogging;
    private final boolean strictUnique;
    private boolean prepared;
    private boolean attached;
    
    MixinPreProcessorStandard(final MixinInfo mixin, final MixinInfo.MixinClassNode classNode) {
        this.profiler = MixinEnvironment.getProfiler();
        this.mixin = mixin;
        this.classNode = classNode;
        this.env = mixin.getParent().getEnvironment();
        this.verboseLogging = this.env.getOption(MixinEnvironment.Option.DEBUG_VERBOSE);
        this.strictUnique = this.env.getOption(MixinEnvironment.Option.DEBUG_UNIQUE);
    }
    
    final MixinPreProcessorStandard prepare() {
        if (this.prepared) {
            return this;
        }
        this.prepared = true;
        final Profiler.Section prepareTimer = this.profiler.begin("prepare");
        for (final MixinInfo.MixinMethodNode mixinMethod : this.classNode.mixinMethods) {
            final ClassInfo.Method method = this.mixin.getClassInfo().findMethod(mixinMethod);
            this.prepareMethod(mixinMethod, method);
        }
        for (final FieldNode mixinField : this.classNode.fields) {
            this.prepareField(mixinField);
        }
        prepareTimer.end();
        return this;
    }
    
    protected void prepareMethod(final MixinInfo.MixinMethodNode mixinMethod, final ClassInfo.Method method) {
        this.prepareShadow(mixinMethod, method);
        this.prepareSoftImplements(mixinMethod, method);
    }
    
    protected void prepareShadow(final MixinInfo.MixinMethodNode mixinMethod, final ClassInfo.Method method) {
        final AnnotationNode shadowAnnotation = Annotations.getVisible(mixinMethod, Shadow.class);
        if (shadowAnnotation == null) {
            return;
        }
        final String prefix = Annotations.getValue(shadowAnnotation, "prefix", (Class<?>)Shadow.class);
        if (mixinMethod.name.startsWith(prefix)) {
            Annotations.setVisible(mixinMethod, MixinRenamed.class, "originalName", mixinMethod.name);
            final String newName = mixinMethod.name.substring(prefix.length());
            mixinMethod.name = method.renameTo(newName);
        }
    }
    
    protected void prepareSoftImplements(final MixinInfo.MixinMethodNode mixinMethod, final ClassInfo.Method method) {
        for (final InterfaceInfo iface : this.mixin.getSoftImplements()) {
            if (iface.renameMethod(mixinMethod)) {
                method.renameTo(mixinMethod.name);
            }
        }
    }
    
    protected void prepareField(final FieldNode mixinField) {
    }
    
    final MixinPreProcessorStandard conform(final TargetClassContext target) {
        return this.conform(target.getClassInfo());
    }
    
    final MixinPreProcessorStandard conform(final ClassInfo target) {
        final Profiler.Section conformTimer = this.profiler.begin("conform");
        for (final MixinInfo.MixinMethodNode mixinMethod : this.classNode.mixinMethods) {
            if (mixinMethod.isInjector()) {
                final ClassInfo.Method method = this.mixin.getClassInfo().findMethod(mixinMethod, 10);
                this.conformInjector(target, mixinMethod, method);
            }
        }
        conformTimer.end();
        return this;
    }
    
    private void conformInjector(final ClassInfo targetClass, final MixinInfo.MixinMethodNode mixinMethod, final ClassInfo.Method method) {
        final MethodMapper methodMapper = targetClass.getMethodMapper();
        methodMapper.remapHandlerMethod(this.mixin, mixinMethod, method);
    }
    
    MixinTargetContext createContextFor(final TargetClassContext target) {
        final MixinTargetContext context = new MixinTargetContext(this.mixin, this.classNode, target);
        this.conform(target);
        this.attach(context);
        return context;
    }
    
    final MixinPreProcessorStandard attach(final MixinTargetContext context) {
        if (this.attached) {
            throw new IllegalStateException("Preprocessor was already attached");
        }
        this.attached = true;
        final Profiler.Section attachTimer = this.profiler.begin("attach");
        Profiler.Section timer = this.profiler.begin("methods");
        this.attachMethods(context);
        timer = timer.next("fields");
        this.attachFields(context);
        timer = timer.next("transform");
        this.transform(context);
        timer.end();
        attachTimer.end();
        return this;
    }
    
    protected void attachMethods(final MixinTargetContext context) {
        final Iterator<MixinInfo.MixinMethodNode> iter = this.classNode.mixinMethods.iterator();
        while (iter.hasNext()) {
            final MixinInfo.MixinMethodNode mixinMethod = iter.next();
            if (!this.validateMethod(context, mixinMethod)) {
                iter.remove();
            }
            else if (this.attachInjectorMethod(context, mixinMethod)) {
                context.addMixinMethod(mixinMethod);
            }
            else if (this.attachAccessorMethod(context, mixinMethod)) {
                iter.remove();
            }
            else if (this.attachShadowMethod(context, mixinMethod)) {
                context.addShadowMethod(mixinMethod);
                iter.remove();
            }
            else if (this.attachOverwriteMethod(context, mixinMethod)) {
                context.addMixinMethod(mixinMethod);
            }
            else if (this.attachUniqueMethod(context, mixinMethod)) {
                iter.remove();
            }
            else {
                this.attachMethod(context, mixinMethod);
                context.addMixinMethod(mixinMethod);
            }
        }
    }
    
    protected boolean validateMethod(final MixinTargetContext context, final MixinInfo.MixinMethodNode mixinMethod) {
        return true;
    }
    
    protected boolean attachInjectorMethod(final MixinTargetContext context, final MixinInfo.MixinMethodNode mixinMethod) {
        return mixinMethod.isInjector();
    }
    
    protected boolean attachAccessorMethod(final MixinTargetContext context, final MixinInfo.MixinMethodNode mixinMethod) {
        return this.attachAccessorMethod(context, mixinMethod, SpecialMethod.ACCESSOR) || this.attachAccessorMethod(context, mixinMethod, SpecialMethod.INVOKER);
    }
    
    protected boolean attachAccessorMethod(final MixinTargetContext context, final MixinInfo.MixinMethodNode mixinMethod, final SpecialMethod type) {
        final AnnotationNode annotation = mixinMethod.getVisibleAnnotation(type.annotation);
        if (annotation == null) {
            return false;
        }
        final String description = type + " method " + mixinMethod.name;
        final ClassInfo.Method method = this.getSpecialMethod(mixinMethod, type);
        if (MixinEnvironment.getCompatibilityLevel().isAtLeast(MixinEnvironment.CompatibilityLevel.JAVA_8) && method.isStatic()) {
            if (this.mixin.getTargets().size() > 1) {
                throw new InvalidAccessorException(context, description + " in multi-target mixin is invalid. Mixin must have exactly 1 target.");
            }
            final String uniqueName = context.getUniqueName(mixinMethod, true);
            MixinPreProcessorStandard.logger.log(this.mixin.getLoggingLevel(), "Renaming @Unique method {}{} to {} in {}", new Object[] { mixinMethod.name, mixinMethod.desc, uniqueName, this.mixin });
            mixinMethod.name = method.renameTo(uniqueName);
        }
        else {
            if (!method.isAbstract()) {
                throw new InvalidAccessorException(context, description + " is not abstract");
            }
            if (method.isStatic()) {
                throw new InvalidAccessorException(context, description + " cannot be static");
            }
        }
        context.addAccessorMethod(mixinMethod, type.annotation);
        return true;
    }
    
    protected boolean attachShadowMethod(final MixinTargetContext context, final MixinInfo.MixinMethodNode mixinMethod) {
        return this.attachSpecialMethod(context, mixinMethod, SpecialMethod.SHADOW);
    }
    
    protected boolean attachOverwriteMethod(final MixinTargetContext context, final MixinInfo.MixinMethodNode mixinMethod) {
        return this.attachSpecialMethod(context, mixinMethod, SpecialMethod.OVERWRITE);
    }
    
    protected boolean attachSpecialMethod(final MixinTargetContext context, final MixinInfo.MixinMethodNode mixinMethod, final SpecialMethod type) {
        final AnnotationNode annotation = mixinMethod.getVisibleAnnotation(type.annotation);
        if (annotation == null) {
            return false;
        }
        if (type.isOverwrite) {
            this.checkMixinNotUnique(mixinMethod, type);
        }
        final ClassInfo.Method method = this.getSpecialMethod(mixinMethod, type);
        MethodNode target = context.findMethod(mixinMethod, annotation);
        if (target == null) {
            if (type.isOverwrite) {
                return false;
            }
            target = context.findRemappedMethod(mixinMethod);
            if (target == null) {
                throw new InvalidMixinException(this.mixin, String.format("%s method %s in %s was not located in the target class %s. %s%s", type, mixinMethod.name, this.mixin, context.getTarget(), context.getReferenceMapper().getStatus(), getDynamicInfo(mixinMethod)));
            }
            mixinMethod.name = method.renameTo(target.name);
        }
        if ("<init>".equals(target.name)) {
            throw new InvalidMixinException(this.mixin, String.format("Nice try! %s in %s cannot alias a constructor", mixinMethod.name, this.mixin));
        }
        if (!Bytecode.compareFlags(mixinMethod, target, 8)) {
            throw new InvalidMixinException(this.mixin, String.format("STATIC modifier of %s method %s in %s does not match the target", type, mixinMethod.name, this.mixin));
        }
        this.conformVisibility(context, mixinMethod, type, target);
        if (!target.name.equals(mixinMethod.name)) {
            if (type.isOverwrite && (target.access & 0x2) == 0x0) {
                throw new InvalidMixinException(this.mixin, "Non-private method cannot be aliased. Found " + target.name);
            }
            mixinMethod.name = method.renameTo(target.name);
        }
        return true;
    }
    
    private void conformVisibility(final MixinTargetContext context, final MixinInfo.MixinMethodNode mixinMethod, final SpecialMethod type, final MethodNode target) {
        final Bytecode.Visibility visTarget = Bytecode.getVisibility(target);
        final Bytecode.Visibility visMethod = Bytecode.getVisibility(mixinMethod);
        if (visMethod.ordinal() >= visTarget.ordinal()) {
            if (visTarget == Bytecode.Visibility.PRIVATE && visMethod.ordinal() > Bytecode.Visibility.PRIVATE.ordinal()) {
                context.getTarget().addUpgradedMethod(target);
            }
            return;
        }
        final String message = String.format("%s %s method %s in %s cannot reduce visibiliy of %s target method", visMethod, type, mixinMethod.name, this.mixin, visTarget);
        if (type.isOverwrite && !this.mixin.getParent().conformOverwriteVisibility()) {
            throw new InvalidMixinException(this.mixin, message);
        }
        if (visMethod == Bytecode.Visibility.PRIVATE) {
            if (type.isOverwrite) {
                MixinPreProcessorStandard.logger.warn("Static binding violation: {}, visibility will be upgraded.", new Object[] { message });
            }
            context.addUpgradedMethod(mixinMethod);
            Bytecode.setVisibility(mixinMethod, visTarget);
        }
    }
    
    protected ClassInfo.Method getSpecialMethod(final MixinInfo.MixinMethodNode mixinMethod, final SpecialMethod type) {
        final ClassInfo.Method method = this.mixin.getClassInfo().findMethod(mixinMethod, 10);
        this.checkMethodNotUnique(method, type);
        return method;
    }
    
    protected void checkMethodNotUnique(final ClassInfo.Method method, final SpecialMethod type) {
        if (method.isUnique()) {
            throw new InvalidMixinException(this.mixin, String.format("%s method %s in %s cannot be @Unique", type, method.getName(), this.mixin));
        }
    }
    
    protected void checkMixinNotUnique(final MixinInfo.MixinMethodNode mixinMethod, final SpecialMethod type) {
        if (this.mixin.isUnique()) {
            throw new InvalidMixinException(this.mixin, String.format("%s method %s found in a @Unique mixin %s", type, mixinMethod.name, this.mixin));
        }
    }
    
    protected boolean attachUniqueMethod(final MixinTargetContext context, final MixinInfo.MixinMethodNode mixinMethod) {
        final ClassInfo.Method method = this.mixin.getClassInfo().findMethod(mixinMethod, 10);
        if (method == null || (!method.isUnique() && !this.mixin.isUnique() && !method.isSynthetic())) {
            return false;
        }
        if (method.isSynthetic()) {
            context.transformDescriptor(mixinMethod);
            method.remapTo(mixinMethod.desc);
        }
        final MethodNode target = context.findMethod(mixinMethod, null);
        if (target == null) {
            return false;
        }
        final String type = method.isSynthetic() ? "synthetic" : "@Unique";
        if (Bytecode.getVisibility(mixinMethod).ordinal() < Bytecode.Visibility.PUBLIC.ordinal()) {
            final String uniqueName = context.getUniqueName(mixinMethod, false);
            MixinPreProcessorStandard.logger.log(this.mixin.getLoggingLevel(), "Renaming {} method {}{} to {} in {}", new Object[] { type, mixinMethod.name, mixinMethod.desc, uniqueName, this.mixin });
            mixinMethod.name = method.renameTo(uniqueName);
            return false;
        }
        if (this.strictUnique) {
            throw new InvalidMixinException(this.mixin, String.format("Method conflict, %s method %s in %s cannot overwrite %s%s in %s", type, mixinMethod.name, this.mixin, target.name, target.desc, context.getTarget()));
        }
        final AnnotationNode unique = Annotations.getVisible(mixinMethod, Unique.class);
        if (unique == null || !Annotations.getValue(unique, "silent", Boolean.FALSE)) {
            if (Bytecode.hasFlag(mixinMethod, 64)) {
                try {
                    Bytecode.compareBridgeMethods(target, mixinMethod);
                    MixinPreProcessorStandard.logger.debug("Discarding sythetic bridge method {} in {} because existing method in {} is compatible", new Object[] { type, mixinMethod.name, this.mixin, context.getTarget() });
                    return true;
                }
                catch (SyntheticBridgeException ex) {
                    if (this.verboseLogging || this.env.getOption(MixinEnvironment.Option.DEBUG_VERIFY)) {
                        ex.printAnalysis(context, target, mixinMethod);
                    }
                    throw new InvalidMixinException(this.mixin, ex.getMessage());
                }
            }
            MixinPreProcessorStandard.logger.warn("Discarding {} public method {} in {} because it already exists in {}", new Object[] { type, mixinMethod.name, this.mixin, context.getTarget() });
            return true;
        }
        context.addMixinMethod(mixinMethod);
        return true;
    }
    
    protected void attachMethod(final MixinTargetContext context, final MixinInfo.MixinMethodNode mixinMethod) {
        final ClassInfo.Method method = this.mixin.getClassInfo().findMethod(mixinMethod);
        if (method == null) {
            return;
        }
        final ClassInfo.Method parentMethod = this.mixin.getClassInfo().findMethodInHierarchy(mixinMethod, ClassInfo.SearchType.SUPER_CLASSES_ONLY);
        if (parentMethod != null && parentMethod.isRenamed()) {
            mixinMethod.name = method.renameTo(parentMethod.getName());
        }
        final MethodNode target = context.findMethod(mixinMethod, null);
        if (target != null) {
            this.conformVisibility(context, mixinMethod, SpecialMethod.MERGE, target);
        }
    }
    
    protected void attachFields(final MixinTargetContext context) {
        final Iterator<FieldNode> iter = this.classNode.fields.iterator();
        while (iter.hasNext()) {
            final FieldNode mixinField = iter.next();
            final AnnotationNode shadow = Annotations.getVisible(mixinField, Shadow.class);
            final boolean isShadow = shadow != null;
            if (!this.validateField(context, mixinField, shadow)) {
                iter.remove();
            }
            else {
                final ClassInfo.Field field = this.mixin.getClassInfo().findField(mixinField);
                context.transformDescriptor(mixinField);
                field.remapTo(mixinField.desc);
                if (field.isUnique() && isShadow) {
                    throw new InvalidMixinException(this.mixin, String.format("@Shadow field %s cannot be @Unique", mixinField.name));
                }
                FieldNode target = context.findField(mixinField, shadow);
                if (target == null) {
                    if (shadow == null) {
                        continue;
                    }
                    target = context.findRemappedField(mixinField);
                    if (target == null) {
                        throw new InvalidMixinException(this.mixin, String.format("Shadow field %s was not located in the target class %s. %s%s", mixinField.name, context.getTarget(), context.getReferenceMapper().getStatus(), getDynamicInfo(mixinField)));
                    }
                    mixinField.name = field.renameTo(target.name);
                }
                if (!Bytecode.compareFlags(mixinField, target, 8)) {
                    throw new InvalidMixinException(this.mixin, String.format("STATIC modifier of @Shadow field %s in %s does not match the target", mixinField.name, this.mixin));
                }
                if (field.isUnique()) {
                    if ((mixinField.access & 0x6) != 0x0) {
                        final String uniqueName = context.getUniqueName(mixinField);
                        MixinPreProcessorStandard.logger.log(this.mixin.getLoggingLevel(), "Renaming @Unique field {}{} to {} in {}", new Object[] { mixinField.name, mixinField.desc, uniqueName, this.mixin });
                        mixinField.name = field.renameTo(uniqueName);
                    }
                    else {
                        if (this.strictUnique) {
                            throw new InvalidMixinException(this.mixin, String.format("Field conflict, @Unique field %s in %s cannot overwrite %s%s in %s", mixinField.name, this.mixin, target.name, target.desc, context.getTarget()));
                        }
                        MixinPreProcessorStandard.logger.warn("Discarding @Unique public field {} in {} because it already exists in {}. Note that declared FIELD INITIALISERS will NOT be removed!", new Object[] { mixinField.name, this.mixin, context.getTarget() });
                        iter.remove();
                    }
                }
                else {
                    if (!target.desc.equals(mixinField.desc)) {
                        throw new InvalidMixinException(this.mixin, String.format("The field %s in the target class has a conflicting signature", mixinField.name));
                    }
                    if (!target.name.equals(mixinField.name)) {
                        if ((target.access & 0x2) == 0x0 && (target.access & 0x1000) == 0x0) {
                            throw new InvalidMixinException(this.mixin, "Non-private field cannot be aliased. Found " + target.name);
                        }
                        mixinField.name = field.renameTo(target.name);
                    }
                    iter.remove();
                    if (!isShadow) {
                        continue;
                    }
                    final boolean isFinal = field.isDecoratedFinal();
                    if (this.verboseLogging && Bytecode.hasFlag(target, 16) != isFinal) {
                        final String message = isFinal ? "@Shadow field {}::{} is decorated with @Final but target is not final" : "@Shadow target {}::{} is final but shadow is not decorated with @Final";
                        MixinPreProcessorStandard.logger.warn(message, new Object[] { this.mixin, mixinField.name });
                    }
                    context.addShadowField(mixinField, field);
                }
            }
        }
    }
    
    protected boolean validateField(final MixinTargetContext context, final FieldNode field, final AnnotationNode shadow) {
        if (Bytecode.hasFlag(field, 8) && !Bytecode.hasFlag(field, 2) && !Bytecode.hasFlag(field, 4096) && shadow == null) {
            throw new InvalidMixinException(context, String.format("Mixin %s contains non-private static field %s:%s", context, field.name, field.desc));
        }
        final String prefix = Annotations.getValue(shadow, "prefix", (Class<?>)Shadow.class);
        if (field.name.startsWith(prefix)) {
            throw new InvalidMixinException(context, String.format("@Shadow field %s.%s has a shadow prefix. This is not allowed.", context, field.name));
        }
        if (!"super$".equals(field.name)) {
            return true;
        }
        if (field.access != 2) {
            throw new InvalidMixinException(this.mixin, String.format("Imaginary super field %s.%s must be private and non-final", context, field.name));
        }
        if (!field.desc.equals("L" + this.mixin.getClassRef() + ";")) {
            throw new InvalidMixinException(this.mixin, String.format("Imaginary super field %s.%s must have the same type as the parent mixin (%s)", context, field.name, this.mixin.getClassName()));
        }
        return false;
    }
    
    protected void transform(final MixinTargetContext context) {
        for (final MethodNode mixinMethod : this.classNode.methods) {
            for (final AbstractInsnNode insn : mixinMethod.instructions) {
                if (insn instanceof MethodInsnNode) {
                    this.transformMethod((MethodInsnNode)insn);
                }
                else {
                    if (!(insn instanceof FieldInsnNode)) {
                        continue;
                    }
                    this.transformField((FieldInsnNode)insn);
                }
            }
        }
    }
    
    protected void transformMethod(final MethodInsnNode methodNode) {
        final Profiler.Section metaTimer = this.profiler.begin("meta");
        final ClassInfo owner = ClassInfo.forName(methodNode.owner);
        if (owner == null) {
            throw new RuntimeException(new ClassNotFoundException(methodNode.owner.replace('/', '.')));
        }
        final ClassInfo.Method method = owner.findMethodInHierarchy(methodNode, ClassInfo.SearchType.ALL_CLASSES, 2);
        metaTimer.end();
        if (method != null && method.isRenamed()) {
            methodNode.name = method.getName();
        }
    }
    
    protected void transformField(final FieldInsnNode fieldNode) {
        final Profiler.Section metaTimer = this.profiler.begin("meta");
        final ClassInfo owner = ClassInfo.forName(fieldNode.owner);
        if (owner == null) {
            throw new RuntimeException(new ClassNotFoundException(fieldNode.owner.replace('/', '.')));
        }
        final ClassInfo.Field field = owner.findField(fieldNode, 2);
        metaTimer.end();
        if (field != null && field.isRenamed()) {
            fieldNode.name = field.getName();
        }
    }
    
    protected static String getDynamicInfo(final MethodNode method) {
        return getDynamicInfo("Method", Annotations.getInvisible(method, Dynamic.class));
    }
    
    protected static String getDynamicInfo(final FieldNode method) {
        return getDynamicInfo("Field", Annotations.getInvisible(method, Dynamic.class));
    }
    
    private static String getDynamicInfo(final String targetType, final AnnotationNode annotation) {
        String description = Strings.nullToEmpty((String)Annotations.getValue(annotation));
        final Type upstream = Annotations.getValue(annotation, "mixin");
        if (upstream != null) {
            description = String.format("{%s} %s", upstream.getClassName(), description).trim();
        }
        return (description.length() > 0) ? String.format(" %s is @Dynamic(%s)", targetType, description) : "";
    }
    
    static {
        logger = LogManager.getLogger("mixin");
    }
    
    enum SpecialMethod
    {
        MERGE(true), 
        OVERWRITE(true, (Class<? extends Annotation>)Overwrite.class), 
        SHADOW(false, (Class<? extends Annotation>)Shadow.class), 
        ACCESSOR(false, (Class<? extends Annotation>)Accessor.class), 
        INVOKER(false, (Class<? extends Annotation>)Invoker.class);
        
        final boolean isOverwrite;
        final Class<? extends Annotation> annotation;
        final String description;
        
        private SpecialMethod(final boolean isOverwrite, final Class<? extends Annotation> type) {
            this.isOverwrite = isOverwrite;
            this.annotation = type;
            this.description = "@" + Bytecode.getSimpleName(type);
        }
        
        private SpecialMethod(final boolean isOverwrite) {
            this.isOverwrite = isOverwrite;
            this.annotation = null;
            this.description = "overwrite";
        }
        
        @Override
        public String toString() {
            return this.description;
        }
    }
}

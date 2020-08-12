// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer;

import org.spongepowered.asm.mixin.transformer.throwables.MixinReloadException;
import org.spongepowered.asm.lib.tree.InnerClassNode;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.lib.tree.FieldNode;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.lib.ClassVisitor;
import org.spongepowered.asm.lib.ClassReader;
import java.util.HashSet;
import org.spongepowered.asm.lib.MethodVisitor;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.service.MixinService;
import java.io.IOException;
import java.util.Set;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.Pseudo;
import java.util.Iterator;
import org.spongepowered.asm.mixin.transformer.throwables.MixinTargetAlreadyLoadedException;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import java.util.Collection;
import com.google.common.base.Function;
import org.spongepowered.asm.lib.Type;
import java.util.ArrayList;
import java.lang.annotation.Annotation;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.mixin.Mixin;
import java.util.Collections;
import com.google.common.collect.Lists;
import com.google.common.base.Functions;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import java.util.List;
import org.spongepowered.asm.util.perf.Profiler;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

class MixinInfo implements Comparable<MixinInfo>, IMixinInfo
{
    private static final IMixinService classLoaderUtil;
    static int mixinOrder;
    private final transient Logger logger;
    private final transient Profiler profiler;
    private final transient MixinConfig parent;
    private final String name;
    private final String className;
    private final int priority;
    private final boolean virtual;
    private final List<ClassInfo> targetClasses;
    private final List<String> targetClassNames;
    private final transient int order;
    private final transient IMixinService service;
    private final transient IMixinConfigPlugin plugin;
    private final transient MixinEnvironment.Phase phase;
    private final transient ClassInfo info;
    private final transient SubType type;
    private final transient boolean strict;
    private transient State pendingState;
    private transient State state;
    
    MixinInfo(final IMixinService service, final MixinConfig parent, final String name, final boolean runTransformers, final IMixinConfigPlugin plugin, final boolean suppressPlugin) {
        this.logger = LogManager.getLogger("mixin");
        this.profiler = MixinEnvironment.getProfiler();
        this.order = MixinInfo.mixinOrder++;
        this.service = service;
        this.parent = parent;
        this.name = name;
        this.className = parent.getMixinPackage() + name;
        this.plugin = plugin;
        this.phase = parent.getEnvironment().getPhase();
        this.strict = parent.getEnvironment().getOption(MixinEnvironment.Option.DEBUG_TARGETS);
        try {
            final byte[] mixinBytes = this.loadMixinClass(this.className, runTransformers);
            this.pendingState = new State(mixinBytes);
            this.info = this.pendingState.getClassInfo();
            this.type = SubType.getTypeFor(this);
        }
        catch (InvalidMixinException ex) {
            throw ex;
        }
        catch (Exception ex2) {
            throw new InvalidMixinException(this, ex2);
        }
        if (!this.type.isLoadable()) {
            MixinInfo.classLoaderUtil.registerInvalidClass(this.className);
        }
        try {
            this.priority = this.readPriority(this.pendingState.getClassNode());
            this.virtual = this.readPseudo(this.pendingState.getClassNode());
            this.targetClasses = this.readTargetClasses(this.pendingState.getClassNode(), suppressPlugin);
            this.targetClassNames = Collections.unmodifiableList((List<? extends String>)Lists.transform((List)this.targetClasses, Functions.toStringFunction()));
        }
        catch (InvalidMixinException ex) {
            throw ex;
        }
        catch (Exception ex2) {
            throw new InvalidMixinException(this, ex2);
        }
    }
    
    void validate() {
        if (this.pendingState == null) {
            throw new IllegalStateException("No pending validation state for " + this);
        }
        try {
            this.pendingState.validate(this.type, this.targetClasses);
            this.state = this.pendingState;
        }
        finally {
            this.pendingState = null;
        }
    }
    
    protected List<ClassInfo> readTargetClasses(final MixinClassNode classNode, final boolean suppressPlugin) {
        if (classNode == null) {
            return Collections.emptyList();
        }
        final AnnotationNode mixin = Annotations.getInvisible(classNode, Mixin.class);
        if (mixin == null) {
            throw new InvalidMixinException(this, String.format("The mixin '%s' is missing an @Mixin annotation", this.className));
        }
        final List<ClassInfo> targets = new ArrayList<ClassInfo>();
        final List<Type> publicTargets = Annotations.getValue(mixin, "value");
        final List<String> privateTargets = Annotations.getValue(mixin, "targets");
        if (publicTargets != null) {
            this.readTargets(targets, Lists.transform((List)publicTargets, (Function)new Function<Type, String>() {
                public String apply(final Type input) {
                    return input.getClassName();
                }
            }), suppressPlugin, false);
        }
        if (privateTargets != null) {
            this.readTargets(targets, Lists.transform((List)privateTargets, (Function)new Function<String, String>() {
                public String apply(final String input) {
                    return MixinInfo.this.getParent().remapClassName(MixinInfo.this.getClassRef(), input);
                }
            }), suppressPlugin, true);
        }
        return targets;
    }
    
    private void readTargets(final Collection<ClassInfo> outTargets, final Collection<String> inTargets, final boolean suppressPlugin, final boolean checkPublic) {
        for (final String targetRef : inTargets) {
            final String targetName = targetRef.replace('/', '.');
            if (MixinInfo.classLoaderUtil.isClassLoaded(targetName) && !this.isReloading()) {
                final String message = String.format("Critical problem: %s target %s was already transformed.", this, targetName);
                if (this.parent.isRequired()) {
                    throw new MixinTargetAlreadyLoadedException(this, message, targetName);
                }
                this.logger.error(message);
            }
            if (this.shouldApplyMixin(suppressPlugin, targetName)) {
                final ClassInfo targetInfo = this.getTarget(targetName, checkPublic);
                if (targetInfo == null || outTargets.contains(targetInfo)) {
                    continue;
                }
                outTargets.add(targetInfo);
                targetInfo.addMixin(this);
            }
        }
    }
    
    private boolean shouldApplyMixin(final boolean suppressPlugin, final String targetName) {
        final Profiler.Section pluginTimer = this.profiler.begin("plugin");
        final boolean result = this.plugin == null || suppressPlugin || this.plugin.shouldApplyMixin(targetName, this.className);
        pluginTimer.end();
        return result;
    }
    
    private ClassInfo getTarget(final String targetName, final boolean checkPublic) throws InvalidMixinException {
        final ClassInfo targetInfo = ClassInfo.forName(targetName);
        if (targetInfo == null) {
            if (this.isVirtual()) {
                this.logger.debug("Skipping virtual target {} for {}", new Object[] { targetName, this });
            }
            else {
                this.handleTargetError(String.format("@Mixin target %s was not found %s", targetName, this));
            }
            return null;
        }
        this.type.validateTarget(targetName, targetInfo);
        if (checkPublic && targetInfo.isPublic() && !this.isVirtual()) {
            this.handleTargetError(String.format("@Mixin target %s is public in %s and should be specified in value", targetName, this));
        }
        return targetInfo;
    }
    
    private void handleTargetError(final String message) {
        if (this.strict) {
            this.logger.error(message);
            throw new InvalidMixinException(this, message);
        }
        this.logger.warn(message);
    }
    
    protected int readPriority(final ClassNode classNode) {
        if (classNode == null) {
            return this.parent.getDefaultMixinPriority();
        }
        final AnnotationNode mixin = Annotations.getInvisible(classNode, Mixin.class);
        if (mixin == null) {
            throw new InvalidMixinException(this, String.format("The mixin '%s' is missing an @Mixin annotation", this.className));
        }
        final Integer priority = Annotations.getValue(mixin, "priority");
        return (priority == null) ? this.parent.getDefaultMixinPriority() : priority;
    }
    
    protected boolean readPseudo(final ClassNode classNode) {
        return Annotations.getInvisible(classNode, Pseudo.class) != null;
    }
    
    private boolean isReloading() {
        return this.pendingState instanceof Reloaded;
    }
    
    private State getState() {
        return (this.state != null) ? this.state : this.pendingState;
    }
    
    ClassInfo getClassInfo() {
        return this.info;
    }
    
    @Override
    public IMixinConfig getConfig() {
        return this.parent;
    }
    
    MixinConfig getParent() {
        return this.parent;
    }
    
    @Override
    public int getPriority() {
        return this.priority;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public String getClassName() {
        return this.className;
    }
    
    @Override
    public String getClassRef() {
        return this.getClassInfo().getName();
    }
    
    @Override
    public byte[] getClassBytes() {
        return this.getState().getClassBytes();
    }
    
    @Override
    public boolean isDetachedSuper() {
        return this.getState().isDetachedSuper();
    }
    
    public boolean isUnique() {
        return this.getState().isUnique();
    }
    
    public boolean isVirtual() {
        return this.virtual;
    }
    
    public boolean isAccessor() {
        return this.type instanceof SubType.Accessor;
    }
    
    public boolean isLoadable() {
        return this.type.isLoadable();
    }
    
    public Level getLoggingLevel() {
        return this.parent.getLoggingLevel();
    }
    
    @Override
    public MixinEnvironment.Phase getPhase() {
        return this.phase;
    }
    
    @Override
    public MixinClassNode getClassNode(final int flags) {
        return this.getState().createClassNode(flags);
    }
    
    @Override
    public List<String> getTargetClasses() {
        return this.targetClassNames;
    }
    
    List<InterfaceInfo> getSoftImplements() {
        return Collections.unmodifiableList(this.getState().getSoftImplements());
    }
    
    Set<String> getSyntheticInnerClasses() {
        return Collections.unmodifiableSet((Set<? extends String>)this.getState().getSyntheticInnerClasses());
    }
    
    Set<String> getInnerClasses() {
        return Collections.unmodifiableSet((Set<? extends String>)this.getState().getInnerClasses());
    }
    
    List<ClassInfo> getTargets() {
        return Collections.unmodifiableList((List<? extends ClassInfo>)this.targetClasses);
    }
    
    Set<String> getInterfaces() {
        return this.getState().getInterfaces();
    }
    
    MixinTargetContext createContextFor(final TargetClassContext target) {
        final MixinClassNode classNode = this.getClassNode(8);
        final Profiler.Section preTimer = this.profiler.begin("pre");
        final MixinTargetContext preProcessor = this.type.createPreProcessor(classNode).prepare().createContextFor(target);
        preTimer.end();
        return preProcessor;
    }
    
    private byte[] loadMixinClass(final String mixinClassName, final boolean runTransformers) throws ClassNotFoundException {
        byte[] mixinBytes = null;
        try {
            mixinBytes = this.service.getBytecodeProvider().getClassBytes(mixinClassName, runTransformers);
        }
        catch (ClassNotFoundException ex2) {
            throw new ClassNotFoundException(String.format("The specified mixin '%s' was not found", mixinClassName));
        }
        catch (IOException ex) {
            this.logger.warn("Failed to load mixin %s, the specified mixin will not be applied", new Object[] { mixinClassName });
            throw new InvalidMixinException(this, "An error was encountered whilst loading the mixin class", ex);
        }
        return mixinBytes;
    }
    
    void reloadMixin(final byte[] mixinBytes) {
        if (this.pendingState != null) {
            throw new IllegalStateException("Cannot reload mixin while it is initialising");
        }
        this.pendingState = new Reloaded(this.state, mixinBytes);
        this.validate();
    }
    
    @Override
    public int compareTo(final MixinInfo other) {
        if (other == null) {
            return 0;
        }
        if (other.priority == this.priority) {
            return this.order - other.order;
        }
        return this.priority - other.priority;
    }
    
    public void preApply(final String transformedName, final ClassNode targetClass) {
        if (this.plugin != null) {
            final Profiler.Section pluginTimer = this.profiler.begin("plugin");
            this.plugin.preApply(transformedName, targetClass, this.className, this);
            pluginTimer.end();
        }
    }
    
    public void postApply(final String transformedName, final ClassNode targetClass) {
        if (this.plugin != null) {
            final Profiler.Section pluginTimer = this.profiler.begin("plugin");
            this.plugin.postApply(transformedName, targetClass, this.className, this);
            pluginTimer.end();
        }
        this.parent.postApply(transformedName, targetClass);
    }
    
    @Override
    public String toString() {
        return String.format("%s:%s", this.parent.getName(), this.name);
    }
    
    static {
        classLoaderUtil = MixinService.getService();
        MixinInfo.mixinOrder = 0;
    }
    
    class MixinMethodNode extends MethodNode
    {
        private final String originalName;
        
        public MixinMethodNode(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
            super(327680, access, name, desc, signature, exceptions);
            this.originalName = name;
        }
        
        @Override
        public String toString() {
            return String.format("%s%s", this.originalName, this.desc);
        }
        
        public String getOriginalName() {
            return this.originalName;
        }
        
        public boolean isInjector() {
            return this.getInjectorAnnotation() != null || this.isSurrogate();
        }
        
        public boolean isSurrogate() {
            return this.getVisibleAnnotation(Surrogate.class) != null;
        }
        
        public boolean isSynthetic() {
            return Bytecode.hasFlag(this, 4096);
        }
        
        public AnnotationNode getVisibleAnnotation(final Class<? extends Annotation> annotationClass) {
            return Annotations.getVisible(this, annotationClass);
        }
        
        public AnnotationNode getInjectorAnnotation() {
            return InjectionInfo.getInjectorAnnotation(MixinInfo.this, this);
        }
        
        public IMixinInfo getOwner() {
            return MixinInfo.this;
        }
    }
    
    class MixinClassNode extends ClassNode
    {
        public final List<MixinMethodNode> mixinMethods;
        
        public MixinClassNode(final MixinInfo this$0, final MixinInfo mixin) {
            this(this$0, 327680);
        }
        
        public MixinClassNode(final int api) {
            super(api);
            this.mixinMethods = (List<MixinMethodNode>)this.methods;
        }
        
        public MixinInfo getMixin() {
            return MixinInfo.this;
        }
        
        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
            final MethodNode method = new MixinMethodNode(access, name, desc, signature, exceptions);
            this.methods.add(method);
            return method;
        }
    }
    
    class State
    {
        private byte[] mixinBytes;
        private final ClassInfo classInfo;
        private boolean detachedSuper;
        private boolean unique;
        protected final Set<String> interfaces;
        protected final List<InterfaceInfo> softImplements;
        protected final Set<String> syntheticInnerClasses;
        protected final Set<String> innerClasses;
        protected MixinClassNode classNode;
        
        State(final MixinInfo this$0, final byte[] mixinBytes) {
            this(this$0, mixinBytes, null);
        }
        
        State(final byte[] mixinBytes, final ClassInfo classInfo) {
            this.interfaces = new HashSet<String>();
            this.softImplements = new ArrayList<InterfaceInfo>();
            this.syntheticInnerClasses = new HashSet<String>();
            this.innerClasses = new HashSet<String>();
            this.mixinBytes = mixinBytes;
            this.connect();
            this.classInfo = ((classInfo != null) ? classInfo : ClassInfo.fromClassNode(this.getClassNode()));
        }
        
        private void connect() {
            this.classNode = this.createClassNode(0);
        }
        
        private void complete() {
            this.classNode = null;
        }
        
        ClassInfo getClassInfo() {
            return this.classInfo;
        }
        
        byte[] getClassBytes() {
            return this.mixinBytes;
        }
        
        MixinClassNode getClassNode() {
            return this.classNode;
        }
        
        boolean isDetachedSuper() {
            return this.detachedSuper;
        }
        
        boolean isUnique() {
            return this.unique;
        }
        
        List<? extends InterfaceInfo> getSoftImplements() {
            return this.softImplements;
        }
        
        Set<String> getSyntheticInnerClasses() {
            return this.syntheticInnerClasses;
        }
        
        Set<String> getInnerClasses() {
            return this.innerClasses;
        }
        
        Set<String> getInterfaces() {
            return this.interfaces;
        }
        
        MixinClassNode createClassNode(final int flags) {
            final MixinClassNode classNode = new MixinClassNode(MixinInfo.this);
            final ClassReader classReader = new ClassReader(this.mixinBytes);
            classReader.accept(classNode, flags);
            return classNode;
        }
        
        void validate(final SubType type, final List<ClassInfo> targetClasses) {
            final MixinPreProcessorStandard preProcessor = type.createPreProcessor(this.getClassNode()).prepare();
            for (final ClassInfo target : targetClasses) {
                preProcessor.conform(target);
            }
            type.validate(this, targetClasses);
            this.detachedSuper = type.isDetachedSuper();
            this.unique = (Annotations.getVisible(this.getClassNode(), Unique.class) != null);
            this.validateInner();
            this.validateClassVersion();
            this.validateRemappables(targetClasses);
            this.readImplementations(type);
            this.readInnerClasses();
            this.validateChanges(type, targetClasses);
            this.complete();
        }
        
        private void validateInner() {
            if (!this.classInfo.isProbablyStatic()) {
                throw new InvalidMixinException(MixinInfo.this, "Inner class mixin must be declared static");
            }
        }
        
        private void validateClassVersion() {
            if (this.classNode.version > MixinEnvironment.getCompatibilityLevel().classVersion()) {
                String helpText = ".";
                for (final MixinEnvironment.CompatibilityLevel level : MixinEnvironment.CompatibilityLevel.values()) {
                    if (level.classVersion() >= this.classNode.version) {
                        helpText = String.format(". Mixin requires compatibility level %s or above.", level.name());
                    }
                }
                throw new InvalidMixinException(MixinInfo.this, "Unsupported mixin class version " + this.classNode.version + helpText);
            }
        }
        
        private void validateRemappables(final List<ClassInfo> targetClasses) {
            if (targetClasses.size() > 1) {
                for (final FieldNode field : this.classNode.fields) {
                    this.validateRemappable(Shadow.class, field.name, Annotations.getVisible(field, Shadow.class));
                }
                for (final MethodNode method : this.classNode.methods) {
                    this.validateRemappable(Shadow.class, method.name, Annotations.getVisible(method, Shadow.class));
                    final AnnotationNode overwrite = Annotations.getVisible(method, Overwrite.class);
                    if (overwrite != null && ((method.access & 0x8) == 0x0 || (method.access & 0x1) == 0x0)) {
                        throw new InvalidMixinException(MixinInfo.this, "Found @Overwrite annotation on " + method.name + " in " + MixinInfo.this);
                    }
                }
            }
        }
        
        private void validateRemappable(final Class<Shadow> annotationClass, final String name, final AnnotationNode annotation) {
            if (annotation != null && Annotations.getValue(annotation, "remap", Boolean.TRUE)) {
                throw new InvalidMixinException(MixinInfo.this, "Found a remappable @" + annotationClass.getSimpleName() + " annotation on " + name + " in " + this);
            }
        }
        
        void readImplementations(final SubType type) {
            this.interfaces.addAll(this.classNode.interfaces);
            this.interfaces.addAll(type.getInterfaces());
            final AnnotationNode implementsAnnotation = Annotations.getInvisible(this.classNode, Implements.class);
            if (implementsAnnotation == null) {
                return;
            }
            final List<AnnotationNode> interfaces = Annotations.getValue(implementsAnnotation);
            if (interfaces == null) {
                return;
            }
            for (final AnnotationNode interfaceNode : interfaces) {
                final InterfaceInfo interfaceInfo = InterfaceInfo.fromAnnotation(MixinInfo.this, interfaceNode);
                this.softImplements.add(interfaceInfo);
                this.interfaces.add(interfaceInfo.getInternalName());
                if (!(this instanceof Reloaded)) {
                    this.classInfo.addInterface(interfaceInfo.getInternalName());
                }
            }
        }
        
        void readInnerClasses() {
            for (final InnerClassNode inner : this.classNode.innerClasses) {
                final ClassInfo innerClass = ClassInfo.forName(inner.name);
                if ((inner.outerName != null && inner.outerName.equals(this.classInfo.getName())) || inner.name.startsWith(this.classNode.name + "$")) {
                    if (innerClass.isProbablyStatic() && innerClass.isSynthetic()) {
                        this.syntheticInnerClasses.add(inner.name);
                    }
                    else {
                        this.innerClasses.add(inner.name);
                    }
                }
            }
        }
        
        protected void validateChanges(final SubType type, final List<ClassInfo> targetClasses) {
            type.createPreProcessor(this.classNode).prepare();
        }
    }
    
    class Reloaded extends State
    {
        private final State previous;
        
        Reloaded(final State previous, final byte[] mixinBytes) {
            super(mixinBytes, previous.getClassInfo());
            this.previous = previous;
        }
        
        @Override
        protected void validateChanges(final SubType type, final List<ClassInfo> targetClasses) {
            if (!this.syntheticInnerClasses.equals(this.previous.syntheticInnerClasses)) {
                throw new MixinReloadException(MixinInfo.this, "Cannot change inner classes");
            }
            if (!this.interfaces.equals(this.previous.interfaces)) {
                throw new MixinReloadException(MixinInfo.this, "Cannot change interfaces");
            }
            if (!new HashSet(this.softImplements).equals(new HashSet(this.previous.softImplements))) {
                throw new MixinReloadException(MixinInfo.this, "Cannot change soft interfaces");
            }
            final List<ClassInfo> targets = MixinInfo.this.readTargetClasses(this.classNode, true);
            if (!new HashSet(targets).equals(new HashSet(targetClasses))) {
                throw new MixinReloadException(MixinInfo.this, "Cannot change target classes");
            }
            final int priority = MixinInfo.this.readPriority(this.classNode);
            if (priority != MixinInfo.this.getPriority()) {
                throw new MixinReloadException(MixinInfo.this, "Cannot change mixin priority");
            }
        }
    }
    
    abstract static class SubType
    {
        protected final MixinInfo mixin;
        protected final String annotationType;
        protected final boolean targetMustBeInterface;
        protected boolean detached;
        
        SubType(final MixinInfo info, final String annotationType, final boolean targetMustBeInterface) {
            this.mixin = info;
            this.annotationType = annotationType;
            this.targetMustBeInterface = targetMustBeInterface;
        }
        
        Collection<String> getInterfaces() {
            return (Collection<String>)Collections.emptyList();
        }
        
        boolean isDetachedSuper() {
            return this.detached;
        }
        
        boolean isLoadable() {
            return false;
        }
        
        void validateTarget(final String targetName, final ClassInfo targetInfo) {
            final boolean targetIsInterface = targetInfo.isInterface();
            if (targetIsInterface != this.targetMustBeInterface) {
                final String not = targetIsInterface ? "" : "not ";
                throw new InvalidMixinException(this.mixin, this.annotationType + " target type mismatch: " + targetName + " is " + not + "an interface in " + this);
            }
        }
        
        abstract void validate(final State p0, final List<ClassInfo> p1);
        
        abstract MixinPreProcessorStandard createPreProcessor(final MixinClassNode p0);
        
        static SubType getTypeFor(final MixinInfo mixin) {
            if (!mixin.getClassInfo().isInterface()) {
                return new Standard(mixin);
            }
            boolean containsNonAccessorMethod = false;
            for (final ClassInfo.Method method : mixin.getClassInfo().getMethods()) {
                containsNonAccessorMethod |= !method.isAccessor();
            }
            if (containsNonAccessorMethod) {
                return new Interface(mixin);
            }
            return new Accessor(mixin);
        }
        
        static class Standard extends SubType
        {
            Standard(final MixinInfo info) {
                super(info, "@Mixin", false);
            }
            
            @Override
            void validate(final State state, final List<ClassInfo> targetClasses) {
                final ClassNode classNode = state.getClassNode();
                for (final ClassInfo targetClass : targetClasses) {
                    if (classNode.superName.equals(targetClass.getSuperName())) {
                        continue;
                    }
                    if (!targetClass.hasSuperClass(classNode.superName, ClassInfo.Traversal.SUPER)) {
                        final ClassInfo superClass = ClassInfo.forName(classNode.superName);
                        if (superClass.isMixin()) {
                            for (final ClassInfo superTarget : superClass.getTargets()) {
                                if (targetClasses.contains(superTarget)) {
                                    throw new InvalidMixinException(this.mixin, "Illegal hierarchy detected. Derived mixin " + this + " targets the same class " + superTarget.getClassName() + " as its superclass " + superClass.getClassName());
                                }
                            }
                        }
                        throw new InvalidMixinException(this.mixin, "Super class '" + classNode.superName.replace('/', '.') + "' of " + this.mixin.getName() + " was not found in the hierarchy of target class '" + targetClass + "'");
                    }
                    this.detached = true;
                }
            }
            
            @Override
            MixinPreProcessorStandard createPreProcessor(final MixinClassNode classNode) {
                return new MixinPreProcessorStandard(this.mixin, classNode);
            }
        }
        
        static class Interface extends SubType
        {
            Interface(final MixinInfo info) {
                super(info, "@Mixin", true);
            }
            
            @Override
            void validate(final State state, final List<ClassInfo> targetClasses) {
                if (!MixinEnvironment.getCompatibilityLevel().supportsMethodsInInterfaces()) {
                    throw new InvalidMixinException(this.mixin, "Interface mixin not supported in current enviromnment");
                }
                final ClassNode classNode = state.getClassNode();
                if (!"java/lang/Object".equals(classNode.superName)) {
                    throw new InvalidMixinException(this.mixin, "Super class of " + this + " is invalid, found " + classNode.superName.replace('/', '.'));
                }
            }
            
            @Override
            MixinPreProcessorStandard createPreProcessor(final MixinClassNode classNode) {
                return new MixinPreProcessorInterface(this.mixin, classNode);
            }
        }
        
        static class Accessor extends SubType
        {
            private final Collection<String> interfaces;
            
            Accessor(final MixinInfo info) {
                super(info, "@Mixin", false);
                (this.interfaces = new ArrayList<String>()).add(info.getClassRef());
            }
            
            @Override
            boolean isLoadable() {
                return true;
            }
            
            @Override
            Collection<String> getInterfaces() {
                return this.interfaces;
            }
            
            @Override
            void validateTarget(final String targetName, final ClassInfo targetInfo) {
                final boolean targetIsInterface = targetInfo.isInterface();
                if (targetIsInterface && !MixinEnvironment.getCompatibilityLevel().supportsMethodsInInterfaces()) {
                    throw new InvalidMixinException(this.mixin, "Accessor mixin targetting an interface is not supported in current enviromnment");
                }
            }
            
            @Override
            void validate(final State state, final List<ClassInfo> targetClasses) {
                final ClassNode classNode = state.getClassNode();
                if (!"java/lang/Object".equals(classNode.superName)) {
                    throw new InvalidMixinException(this.mixin, "Super class of " + this + " is invalid, found " + classNode.superName.replace('/', '.'));
                }
            }
            
            @Override
            MixinPreProcessorStandard createPreProcessor(final MixinClassNode classNode) {
                return new MixinPreProcessorAccessor(this.mixin, classNode);
            }
        }
    }
}

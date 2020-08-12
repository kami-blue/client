// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer;

import com.google.common.collect.ImmutableList;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.util.throwables.InvalidConstraintException;
import org.spongepowered.asm.util.throwables.ConstraintViolationException;
import org.spongepowered.asm.util.ITokenProvider;
import org.spongepowered.asm.util.ConstraintParser;
import java.util.Set;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import java.util.HashSet;
import org.spongepowered.asm.lib.tree.JumpInsnNode;
import java.util.ArrayDeque;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.lib.Label;
import java.util.Deque;
import org.spongepowered.asm.lib.tree.LineNumberNode;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.mixin.transformer.meta.MixinRenamed;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;
import org.spongepowered.asm.lib.signature.SignatureVisitor;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.signature.SignatureReader;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.FieldNode;
import java.util.Map;
import org.spongepowered.asm.util.Bytecode;
import java.util.Iterator;
import org.spongepowered.asm.mixin.refmap.IMixinContext;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;
import java.util.ArrayList;
import java.util.SortedSet;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.util.perf.Profiler;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.apache.logging.log4j.Logger;
import java.lang.annotation.Annotation;
import java.util.List;

class MixinApplicatorStandard
{
    protected static final List<Class<? extends Annotation>> CONSTRAINED_ANNOTATIONS;
    protected static final int[] INITIALISER_OPCODE_BLACKLIST;
    protected final Logger logger;
    protected final TargetClassContext context;
    protected final String targetName;
    protected final ClassNode targetClass;
    protected final Profiler profiler;
    
    MixinApplicatorStandard(final TargetClassContext context) {
        this.logger = LogManager.getLogger("mixin");
        this.profiler = MixinEnvironment.getProfiler();
        this.context = context;
        this.targetName = context.getClassName();
        this.targetClass = context.getClassNode();
    }
    
    void apply(final SortedSet<MixinInfo> mixins) {
        final List<MixinTargetContext> mixinContexts = new ArrayList<MixinTargetContext>();
        for (final MixinInfo mixin : mixins) {
            this.logger.log(mixin.getLoggingLevel(), "Mixing {} from {} into {}", new Object[] { mixin.getName(), mixin.getParent(), this.targetName });
            mixinContexts.add(mixin.createContextFor(this.context));
        }
        MixinTargetContext current = null;
        try {
            for (final MixinTargetContext context : mixinContexts) {
                (current = context).preApply(this.targetName, this.targetClass);
            }
            for (final ApplicatorPass pass : ApplicatorPass.values()) {
                final Profiler.Section timer = this.profiler.begin("pass", pass.name().toLowerCase());
                for (final MixinTargetContext context2 : mixinContexts) {
                    this.applyMixin(current = context2, pass);
                }
                timer.end();
            }
            for (final MixinTargetContext context : mixinContexts) {
                (current = context).postApply(this.targetName, this.targetClass);
            }
        }
        catch (InvalidMixinException ex) {
            throw ex;
        }
        catch (Exception ex2) {
            throw new InvalidMixinException(current, "Unexpecteded " + ex2.getClass().getSimpleName() + " whilst applying the mixin class: " + ex2.getMessage(), ex2);
        }
        this.applySourceMap(this.context);
        this.context.processDebugTasks();
    }
    
    protected final void applyMixin(final MixinTargetContext mixin, final ApplicatorPass pass) {
        switch (pass) {
            case MAIN: {
                this.applySignature(mixin);
                this.applyInterfaces(mixin);
                this.applyAttributes(mixin);
                this.applyAnnotations(mixin);
                this.applyFields(mixin);
                this.applyMethods(mixin);
                this.applyInitialisers(mixin);
                break;
            }
            case PREINJECT: {
                this.prepareInjections(mixin);
                break;
            }
            case INJECT: {
                this.applyAccessors(mixin);
                this.applyInjections(mixin);
                break;
            }
            default: {
                throw new IllegalStateException("Invalid pass specified " + pass);
            }
        }
    }
    
    protected void applySignature(final MixinTargetContext mixin) {
        this.context.mergeSignature(mixin.getSignature());
    }
    
    protected void applyInterfaces(final MixinTargetContext mixin) {
        for (final String interfaceName : mixin.getInterfaces()) {
            if (!this.targetClass.interfaces.contains(interfaceName)) {
                this.targetClass.interfaces.add(interfaceName);
                mixin.getTargetClassInfo().addInterface(interfaceName);
            }
        }
    }
    
    protected void applyAttributes(final MixinTargetContext mixin) {
        if (mixin.shouldSetSourceFile()) {
            this.targetClass.sourceFile = mixin.getSourceFile();
        }
        this.targetClass.version = Math.max(this.targetClass.version, mixin.getMinRequiredClassVersion());
    }
    
    protected void applyAnnotations(final MixinTargetContext mixin) {
        final ClassNode sourceClass = mixin.getClassNode();
        Bytecode.mergeAnnotations(sourceClass, this.targetClass);
    }
    
    protected void applyFields(final MixinTargetContext mixin) {
        this.mergeShadowFields(mixin);
        this.mergeNewFields(mixin);
    }
    
    protected void mergeShadowFields(final MixinTargetContext mixin) {
        for (final Map.Entry<FieldNode, ClassInfo.Field> entry : mixin.getShadowFields()) {
            final FieldNode shadow = entry.getKey();
            final FieldNode target = this.findTargetField(shadow);
            if (target != null) {
                Bytecode.mergeAnnotations(shadow, target);
                if (!entry.getValue().isDecoratedMutable() || Bytecode.hasFlag(target, 2)) {
                    continue;
                }
                final FieldNode fieldNode = target;
                fieldNode.access &= 0xFFFFFFEF;
            }
        }
    }
    
    protected void mergeNewFields(final MixinTargetContext mixin) {
        for (final FieldNode field : mixin.getFields()) {
            final FieldNode target = this.findTargetField(field);
            if (target == null) {
                this.targetClass.fields.add(field);
            }
        }
    }
    
    protected void applyMethods(final MixinTargetContext mixin) {
        for (final MethodNode shadow : mixin.getShadowMethods()) {
            this.applyShadowMethod(mixin, shadow);
        }
        for (final MethodNode mixinMethod : mixin.getMethods()) {
            this.applyNormalMethod(mixin, mixinMethod);
        }
    }
    
    protected void applyShadowMethod(final MixinTargetContext mixin, final MethodNode shadow) {
        final MethodNode target = this.findTargetMethod(shadow);
        if (target != null) {
            Bytecode.mergeAnnotations(shadow, target);
        }
    }
    
    protected void applyNormalMethod(final MixinTargetContext mixin, final MethodNode mixinMethod) {
        mixin.transformMethod(mixinMethod);
        if (!mixinMethod.name.startsWith("<")) {
            this.checkMethodVisibility(mixin, mixinMethod);
            this.checkMethodConstraints(mixin, mixinMethod);
            this.mergeMethod(mixin, mixinMethod);
        }
        else if ("<clinit>".equals(mixinMethod.name)) {
            this.appendInsns(mixin, mixinMethod);
        }
    }
    
    protected void mergeMethod(final MixinTargetContext mixin, final MethodNode method) {
        final boolean isOverwrite = Annotations.getVisible(method, Overwrite.class) != null;
        final MethodNode target = this.findTargetMethod(method);
        if (target != null) {
            if (this.isAlreadyMerged(mixin, method, isOverwrite, target)) {
                return;
            }
            final AnnotationNode intrinsic = Annotations.getInvisible(method, Intrinsic.class);
            if (intrinsic != null) {
                if (this.mergeIntrinsic(mixin, method, isOverwrite, target, intrinsic)) {
                    mixin.getTarget().methodMerged(method);
                    return;
                }
            }
            else {
                if (mixin.requireOverwriteAnnotations() && !isOverwrite) {
                    throw new InvalidMixinException(mixin, String.format("%s%s in %s cannot overwrite method in %s because @Overwrite is required by the parent configuration", method.name, method.desc, mixin, mixin.getTarget().getClassName()));
                }
                this.targetClass.methods.remove(target);
            }
        }
        else if (isOverwrite) {
            throw new InvalidMixinException(mixin, String.format("Overwrite target \"%s\" was not located in target class %s", method.name, mixin.getTargetClassRef()));
        }
        this.targetClass.methods.add(method);
        mixin.methodMerged(method);
        if (method.signature != null) {
            final SignatureVisitor sv = mixin.getSignature().getRemapper();
            new SignatureReader(method.signature).accept(sv);
            method.signature = sv.toString();
        }
    }
    
    protected boolean isAlreadyMerged(final MixinTargetContext mixin, final MethodNode method, final boolean isOverwrite, final MethodNode target) {
        final AnnotationNode merged = Annotations.getVisible(target, MixinMerged.class);
        if (merged == null) {
            if (Annotations.getVisible(target, Final.class) != null) {
                this.logger.warn("Overwrite prohibited for @Final method {} in {}. Skipping method.", new Object[] { method.name, mixin });
                return true;
            }
            return false;
        }
        else {
            final String sessionId = Annotations.getValue(merged, "sessionId");
            if (!this.context.getSessionId().equals(sessionId)) {
                throw new ClassFormatError("Invalid @MixinMerged annotation found in" + mixin + " at " + method.name + " in " + this.targetClass.name);
            }
            if (Bytecode.hasFlag(target, 4160) && Bytecode.hasFlag(method, 4160)) {
                if (mixin.getEnvironment().getOption(MixinEnvironment.Option.DEBUG_VERBOSE)) {
                    this.logger.warn("Synthetic bridge method clash for {} in {}", new Object[] { method.name, mixin });
                }
                return true;
            }
            final String owner = Annotations.getValue(merged, "mixin");
            final int priority = Annotations.getValue(merged, "priority");
            if (priority >= mixin.getPriority() && !owner.equals(mixin.getClassName())) {
                this.logger.warn("Method overwrite conflict for {} in {}, previously written by {}. Skipping method.", new Object[] { method.name, mixin, owner });
                return true;
            }
            if (Annotations.getVisible(target, Final.class) != null) {
                this.logger.warn("Method overwrite conflict for @Final method {} in {} declared by {}. Skipping method.", new Object[] { method.name, mixin, owner });
                return true;
            }
            return false;
        }
    }
    
    protected boolean mergeIntrinsic(final MixinTargetContext mixin, final MethodNode method, final boolean isOverwrite, final MethodNode target, final AnnotationNode intrinsic) {
        if (isOverwrite) {
            throw new InvalidMixinException(mixin, "@Intrinsic is not compatible with @Overwrite, remove one of these annotations on " + method.name + " in " + mixin);
        }
        final String methodName = method.name + method.desc;
        if (Bytecode.hasFlag(method, 8)) {
            throw new InvalidMixinException(mixin, "@Intrinsic method cannot be static, found " + methodName + " in " + mixin);
        }
        if (!Bytecode.hasFlag(method, 4096)) {
            final AnnotationNode renamed = Annotations.getVisible(method, MixinRenamed.class);
            if (renamed == null || !Annotations.getValue(renamed, "isInterfaceMember", Boolean.FALSE)) {
                throw new InvalidMixinException(mixin, "@Intrinsic method must be prefixed interface method, no rename encountered on " + methodName + " in " + mixin);
            }
        }
        if (!Annotations.getValue(intrinsic, "displace", Boolean.FALSE)) {
            this.logger.log(mixin.getLoggingLevel(), "Skipping Intrinsic mixin method {} for {}", new Object[] { methodName, mixin.getTargetClassRef() });
            return true;
        }
        this.displaceIntrinsic(mixin, method, target);
        return false;
    }
    
    protected void displaceIntrinsic(final MixinTargetContext mixin, final MethodNode method, final MethodNode target) {
        final String proxyName = "proxy+" + target.name;
        for (final AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode && insn.getOpcode() != 184) {
                final MethodInsnNode methodNode = (MethodInsnNode)insn;
                if (!methodNode.owner.equals(this.targetClass.name) || !methodNode.name.equals(target.name) || !methodNode.desc.equals(target.desc)) {
                    continue;
                }
                methodNode.name = proxyName;
            }
        }
        target.name = proxyName;
    }
    
    protected final void appendInsns(final MixinTargetContext mixin, final MethodNode method) {
        if (Type.getReturnType(method.desc) != Type.VOID_TYPE) {
            throw new IllegalArgumentException("Attempted to merge insns from a method which does not return void");
        }
        final MethodNode target = this.findTargetMethod(method);
        if (target != null) {
            final AbstractInsnNode returnNode = Bytecode.findInsn(target, 177);
            if (returnNode != null) {
                for (final AbstractInsnNode insn : method.instructions) {
                    if (!(insn instanceof LineNumberNode) && insn.getOpcode() != 177) {
                        target.instructions.insertBefore(returnNode, insn);
                    }
                }
                target.maxLocals = Math.max(target.maxLocals, method.maxLocals);
                target.maxStack = Math.max(target.maxStack, method.maxStack);
            }
            return;
        }
        this.targetClass.methods.add(method);
    }
    
    protected void applyInitialisers(final MixinTargetContext mixin) {
        final MethodNode ctor = this.getConstructor(mixin);
        if (ctor == null) {
            return;
        }
        final Deque<AbstractInsnNode> initialiser = this.getInitialiser(mixin, ctor);
        if (initialiser == null || initialiser.size() == 0) {
            return;
        }
        for (final MethodNode method : this.targetClass.methods) {
            if ("<init>".equals(method.name)) {
                method.maxStack = Math.max(method.maxStack, ctor.maxStack);
                this.injectInitialiser(mixin, method, initialiser);
            }
        }
    }
    
    protected MethodNode getConstructor(final MixinTargetContext mixin) {
        MethodNode ctor = null;
        for (final MethodNode mixinMethod : mixin.getMethods()) {
            if ("<init>".equals(mixinMethod.name) && Bytecode.methodHasLineNumbers(mixinMethod)) {
                if (ctor == null) {
                    ctor = mixinMethod;
                }
                else {
                    this.logger.warn(String.format("Mixin %s has multiple constructors, %s was selected\n", mixin, ctor.desc));
                }
            }
        }
        return ctor;
    }
    
    private Range getConstructorRange(final MethodNode ctor) {
        boolean lineNumberIsValid = false;
        AbstractInsnNode endReturn = null;
        int line = 0;
        int start = 0;
        int end = 0;
        int superIndex = -1;
        for (final AbstractInsnNode insn : ctor.instructions) {
            if (insn instanceof LineNumberNode) {
                line = ((LineNumberNode)insn).line;
                lineNumberIsValid = true;
            }
            else if (insn instanceof MethodInsnNode) {
                if (insn.getOpcode() != 183 || !"<init>".equals(((MethodInsnNode)insn).name) || superIndex != -1) {
                    continue;
                }
                superIndex = ctor.instructions.indexOf(insn);
                start = line;
            }
            else if (insn.getOpcode() == 181) {
                lineNumberIsValid = false;
            }
            else {
                if (insn.getOpcode() != 177) {
                    continue;
                }
                if (lineNumberIsValid) {
                    end = line;
                }
                else {
                    end = start;
                    endReturn = insn;
                }
            }
        }
        if (endReturn != null) {
            final LabelNode label = new LabelNode(new Label());
            ctor.instructions.insertBefore(endReturn, label);
            ctor.instructions.insertBefore(endReturn, new LineNumberNode(start, label));
        }
        return new Range(start, end, superIndex);
    }
    
    protected final Deque<AbstractInsnNode> getInitialiser(final MixinTargetContext mixin, final MethodNode ctor) {
        final Range init = this.getConstructorRange(ctor);
        if (!init.isValid()) {
            return null;
        }
        int line = 0;
        final Deque<AbstractInsnNode> initialiser = new ArrayDeque<AbstractInsnNode>();
        boolean gatherNodes = false;
        int trimAtOpcode = -1;
        LabelNode optionalInsn = null;
        final Iterator<AbstractInsnNode> iter = ctor.instructions.iterator(init.marker);
        while (iter.hasNext()) {
            final AbstractInsnNode insn = iter.next();
            if (insn instanceof LineNumberNode) {
                line = ((LineNumberNode)insn).line;
                final AbstractInsnNode next = ctor.instructions.get(ctor.instructions.indexOf(insn) + 1);
                if (line == init.end && next.getOpcode() != 177) {
                    gatherNodes = true;
                    trimAtOpcode = 177;
                }
                else {
                    gatherNodes = init.excludes(line);
                    trimAtOpcode = -1;
                }
            }
            else {
                if (!gatherNodes) {
                    continue;
                }
                if (optionalInsn != null) {
                    initialiser.add(optionalInsn);
                    optionalInsn = null;
                }
                if (insn instanceof LabelNode) {
                    optionalInsn = (LabelNode)insn;
                }
                else {
                    final int opcode = insn.getOpcode();
                    if (opcode == trimAtOpcode) {
                        trimAtOpcode = -1;
                    }
                    else {
                        for (final int ivalidOp : MixinApplicatorStandard.INITIALISER_OPCODE_BLACKLIST) {
                            if (opcode == ivalidOp) {
                                throw new InvalidMixinException(mixin, "Cannot handle " + Bytecode.getOpcodeName(opcode) + " opcode (0x" + Integer.toHexString(opcode).toUpperCase() + ") in class initialiser");
                            }
                        }
                        initialiser.add(insn);
                    }
                }
            }
        }
        final AbstractInsnNode last = initialiser.peekLast();
        if (last != null && last.getOpcode() != 181) {
            throw new InvalidMixinException(mixin, "Could not parse initialiser, expected 0xB5, found 0x" + Integer.toHexString(last.getOpcode()) + " in " + mixin);
        }
        return initialiser;
    }
    
    protected final void injectInitialiser(final MixinTargetContext mixin, final MethodNode ctor, final Deque<AbstractInsnNode> initialiser) {
        final Map<LabelNode, LabelNode> labels = Bytecode.cloneLabels(ctor.instructions);
        AbstractInsnNode insn = this.findInitialiserInjectionPoint(mixin, ctor, initialiser);
        if (insn == null) {
            this.logger.warn("Failed to locate initialiser injection point in <init>{}, initialiser was not mixed in.", new Object[] { ctor.desc });
            return;
        }
        for (final AbstractInsnNode node : initialiser) {
            if (node instanceof LabelNode) {
                continue;
            }
            if (node instanceof JumpInsnNode) {
                throw new InvalidMixinException(mixin, "Unsupported JUMP opcode in initialiser in " + mixin);
            }
            final AbstractInsnNode imACloneNow = node.clone(labels);
            ctor.instructions.insert(insn, imACloneNow);
            insn = imACloneNow;
        }
    }
    
    protected AbstractInsnNode findInitialiserInjectionPoint(final MixinTargetContext mixin, final MethodNode ctor, final Deque<AbstractInsnNode> initialiser) {
        final Set<String> initialisedFields = new HashSet<String>();
        for (final AbstractInsnNode initialiserInsn : initialiser) {
            if (initialiserInsn.getOpcode() == 181) {
                initialisedFields.add(fieldKey((FieldInsnNode)initialiserInsn));
            }
        }
        final InitialiserInjectionMode mode = this.getInitialiserInjectionMode(mixin.getEnvironment());
        final String targetName = mixin.getTargetClassInfo().getName();
        final String targetSuperName = mixin.getTargetClassInfo().getSuperName();
        AbstractInsnNode targetInsn = null;
        for (final AbstractInsnNode insn : ctor.instructions) {
            if (insn.getOpcode() == 183 && "<init>".equals(((MethodInsnNode)insn).name)) {
                final String owner = ((MethodInsnNode)insn).owner;
                if (!owner.equals(targetName) && !owner.equals(targetSuperName)) {
                    continue;
                }
                targetInsn = insn;
                if (mode == InitialiserInjectionMode.SAFE) {
                    break;
                }
                continue;
            }
            else {
                if (insn.getOpcode() != 181 || mode != InitialiserInjectionMode.DEFAULT) {
                    continue;
                }
                final String key = fieldKey((FieldInsnNode)insn);
                if (!initialisedFields.contains(key)) {
                    continue;
                }
                targetInsn = insn;
            }
        }
        return targetInsn;
    }
    
    private InitialiserInjectionMode getInitialiserInjectionMode(final MixinEnvironment environment) {
        final String strMode = environment.getOptionValue(MixinEnvironment.Option.INITIALISER_INJECTION_MODE);
        if (strMode == null) {
            return InitialiserInjectionMode.DEFAULT;
        }
        try {
            return InitialiserInjectionMode.valueOf(strMode.toUpperCase());
        }
        catch (Exception ex) {
            this.logger.warn("Could not parse unexpected value \"{}\" for mixin.initialiserInjectionMode, reverting to DEFAULT", new Object[] { strMode });
            return InitialiserInjectionMode.DEFAULT;
        }
    }
    
    private static String fieldKey(final FieldInsnNode fieldNode) {
        return String.format("%s:%s", fieldNode.desc, fieldNode.name);
    }
    
    protected void prepareInjections(final MixinTargetContext mixin) {
        mixin.prepareInjections();
    }
    
    protected void applyInjections(final MixinTargetContext mixin) {
        mixin.applyInjections();
    }
    
    protected void applyAccessors(final MixinTargetContext mixin) {
        final List<MethodNode> accessorMethods = mixin.generateAccessors();
        for (final MethodNode method : accessorMethods) {
            if (!method.name.startsWith("<")) {
                this.mergeMethod(mixin, method);
            }
        }
    }
    
    protected void checkMethodVisibility(final MixinTargetContext mixin, final MethodNode mixinMethod) {
        if (Bytecode.hasFlag(mixinMethod, 8) && !Bytecode.hasFlag(mixinMethod, 2) && !Bytecode.hasFlag(mixinMethod, 4096) && Annotations.getVisible(mixinMethod, Overwrite.class) == null) {
            throw new InvalidMixinException(mixin, String.format("Mixin %s contains non-private static method %s", mixin, mixinMethod));
        }
    }
    
    protected void applySourceMap(final TargetClassContext context) {
        this.targetClass.sourceDebug = context.getSourceMap().toString();
    }
    
    protected void checkMethodConstraints(final MixinTargetContext mixin, final MethodNode method) {
        for (final Class<? extends Annotation> annotationType : MixinApplicatorStandard.CONSTRAINED_ANNOTATIONS) {
            final AnnotationNode annotation = Annotations.getVisible(method, annotationType);
            if (annotation != null) {
                this.checkConstraints(mixin, method, annotation);
            }
        }
    }
    
    protected final void checkConstraints(final MixinTargetContext mixin, final MethodNode method, final AnnotationNode annotation) {
        try {
            final ConstraintParser.Constraint constraint = ConstraintParser.parse(annotation);
            try {
                constraint.check(mixin.getEnvironment());
            }
            catch (ConstraintViolationException ex) {
                final String message = String.format("Constraint violation: %s on %s in %s", ex.getMessage(), method, mixin);
                this.logger.warn(message);
                if (!mixin.getEnvironment().getOption(MixinEnvironment.Option.IGNORE_CONSTRAINTS)) {
                    throw new InvalidMixinException(mixin, message, ex);
                }
            }
        }
        catch (InvalidConstraintException ex2) {
            throw new InvalidMixinException(mixin, ex2.getMessage());
        }
    }
    
    protected final MethodNode findTargetMethod(final MethodNode searchFor) {
        for (final MethodNode target : this.targetClass.methods) {
            if (target.name.equals(searchFor.name) && target.desc.equals(searchFor.desc)) {
                return target;
            }
        }
        return null;
    }
    
    protected final FieldNode findTargetField(final FieldNode searchFor) {
        for (final FieldNode target : this.targetClass.fields) {
            if (target.name.equals(searchFor.name)) {
                return target;
            }
        }
        return null;
    }
    
    static {
        CONSTRAINED_ANNOTATIONS = (List)ImmutableList.of((Object)Overwrite.class, (Object)Inject.class, (Object)ModifyArg.class, (Object)ModifyArgs.class, (Object)Redirect.class, (Object)ModifyVariable.class, (Object)ModifyConstant.class);
        INITIALISER_OPCODE_BLACKLIST = new int[] { 177, 21, 22, 23, 24, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 79, 80, 81, 82, 83, 84, 85, 86 };
    }
    
    enum ApplicatorPass
    {
        MAIN, 
        PREINJECT, 
        INJECT;
    }
    
    enum InitialiserInjectionMode
    {
        DEFAULT, 
        SAFE;
    }
    
    class Range
    {
        final int start;
        final int end;
        final int marker;
        
        Range(final int start, final int end, final int marker) {
            this.start = start;
            this.end = end;
            this.marker = marker;
        }
        
        boolean isValid() {
            return this.start != 0 && this.end != 0 && this.end >= this.start;
        }
        
        boolean contains(final int value) {
            return value >= this.start && value <= this.end;
        }
        
        boolean excludes(final int value) {
            return value < this.start || value > this.end;
        }
        
        @Override
        public String toString() {
            return String.format("Range[%d-%d,%d,valid=%s)", this.start, this.end, this.marker, this.isValid());
        }
    }
}

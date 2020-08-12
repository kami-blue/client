// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer;

import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.injection.throwables.InjectionValidationException;
import org.spongepowered.asm.mixin.injection.throwables.InjectionError;
import org.spongepowered.asm.mixin.refmap.IReferenceMapper;
import org.apache.logging.log4j.Level;
import java.util.Set;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.obfuscation.RemapperChain;
import java.util.Deque;
import java.util.Collection;
import java.util.LinkedList;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.mixin.transformer.throwables.MixinTransformerError;
import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.lib.Handle;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.LocalVariableNode;
import org.spongepowered.asm.mixin.SoftOverride;
import org.spongepowered.asm.lib.tree.InvokeDynamicInsnNode;
import org.spongepowered.asm.lib.tree.LdcInsnNode;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.mixin.struct.MemberRef;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;
import org.spongepowered.asm.util.ClassSignature;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import org.spongepowered.asm.mixin.transformer.ext.IClassGenerator;
import org.spongepowered.asm.mixin.MixinEnvironment;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import com.google.common.collect.HashBiMap;
import org.spongepowered.asm.mixin.struct.SourceMap;
import org.spongepowered.asm.mixin.gen.AccessorInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;
import org.spongepowered.asm.lib.tree.FieldNode;
import java.util.Map;
import org.spongepowered.asm.lib.tree.MethodNode;
import java.util.List;
import com.google.common.collect.BiMap;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.refmap.IMixinContext;

public class MixinTargetContext extends ClassContext implements IMixinContext
{
    private static final Logger logger;
    private final MixinInfo mixin;
    private final ClassNode classNode;
    private final TargetClassContext targetClass;
    private final String sessionId;
    private final ClassInfo targetClassInfo;
    private final BiMap<String, String> innerClasses;
    private final List<MethodNode> shadowMethods;
    private final Map<FieldNode, ClassInfo.Field> shadowFields;
    private final List<MethodNode> mergedMethods;
    private final InjectorGroupInfo.Map injectorGroups;
    private final List<InjectionInfo> injectors;
    private final List<AccessorInfo> accessors;
    private final boolean inheritsFromMixin;
    private final boolean detachedSuper;
    private final SourceMap.File stratum;
    private int minRequiredClassVersion;
    
    MixinTargetContext(final MixinInfo mixin, final ClassNode classNode, final TargetClassContext context) {
        this.innerClasses = (BiMap<String, String>)HashBiMap.create();
        this.shadowMethods = new ArrayList<MethodNode>();
        this.shadowFields = new LinkedHashMap<FieldNode, ClassInfo.Field>();
        this.mergedMethods = new ArrayList<MethodNode>();
        this.injectorGroups = new InjectorGroupInfo.Map();
        this.injectors = new ArrayList<InjectionInfo>();
        this.accessors = new ArrayList<AccessorInfo>();
        this.minRequiredClassVersion = MixinEnvironment.CompatibilityLevel.JAVA_6.classVersion();
        this.mixin = mixin;
        this.classNode = classNode;
        this.targetClass = context;
        this.targetClassInfo = ClassInfo.forName(this.getTarget().getClassRef());
        this.stratum = context.getSourceMap().addFile(this.classNode);
        this.inheritsFromMixin = (mixin.getClassInfo().hasMixinInHierarchy() || this.targetClassInfo.hasMixinTargetInHierarchy());
        this.detachedSuper = !this.classNode.superName.equals(this.getTarget().getClassNode().superName);
        this.sessionId = context.getSessionId();
        this.requireVersion(classNode.version);
        final InnerClassGenerator icg = context.getExtensions().getGenerator(InnerClassGenerator.class);
        for (final String innerClass : this.mixin.getInnerClasses()) {
            this.innerClasses.put((Object)innerClass, (Object)icg.registerInnerClass(this.mixin, innerClass, this));
        }
    }
    
    void addShadowMethod(final MethodNode method) {
        this.shadowMethods.add(method);
    }
    
    void addShadowField(final FieldNode fieldNode, final ClassInfo.Field fieldInfo) {
        this.shadowFields.put(fieldNode, fieldInfo);
    }
    
    void addAccessorMethod(final MethodNode method, final Class<? extends Annotation> type) {
        this.accessors.add(AccessorInfo.of(this, method, type));
    }
    
    void addMixinMethod(final MethodNode method) {
        Annotations.setVisible(method, MixinMerged.class, "mixin", this.getClassName());
        this.getTarget().addMixinMethod(method);
    }
    
    void methodMerged(final MethodNode method) {
        this.mergedMethods.add(method);
        this.targetClassInfo.addMethod(method);
        this.getTarget().methodMerged(method);
        Annotations.setVisible(method, MixinMerged.class, "mixin", this.getClassName(), "priority", this.getPriority(), "sessionId", this.sessionId);
    }
    
    @Override
    public String toString() {
        return this.mixin.toString();
    }
    
    public MixinEnvironment getEnvironment() {
        return this.mixin.getParent().getEnvironment();
    }
    
    @Override
    public boolean getOption(final MixinEnvironment.Option option) {
        return this.getEnvironment().getOption(option);
    }
    
    public ClassNode getClassNode() {
        return this.classNode;
    }
    
    public String getClassName() {
        return this.mixin.getClassName();
    }
    
    @Override
    public String getClassRef() {
        return this.mixin.getClassRef();
    }
    
    public TargetClassContext getTarget() {
        return this.targetClass;
    }
    
    @Override
    public String getTargetClassRef() {
        return this.getTarget().getClassRef();
    }
    
    public ClassNode getTargetClassNode() {
        return this.getTarget().getClassNode();
    }
    
    public ClassInfo getTargetClassInfo() {
        return this.targetClassInfo;
    }
    
    protected ClassInfo getClassInfo() {
        return this.mixin.getClassInfo();
    }
    
    public ClassSignature getSignature() {
        return this.getClassInfo().getSignature();
    }
    
    public SourceMap.File getStratum() {
        return this.stratum;
    }
    
    public int getMinRequiredClassVersion() {
        return this.minRequiredClassVersion;
    }
    
    public int getDefaultRequiredInjections() {
        return this.mixin.getParent().getDefaultRequiredInjections();
    }
    
    public String getDefaultInjectorGroup() {
        return this.mixin.getParent().getDefaultInjectorGroup();
    }
    
    public int getMaxShiftByValue() {
        return this.mixin.getParent().getMaxShiftByValue();
    }
    
    public InjectorGroupInfo.Map getInjectorGroups() {
        return this.injectorGroups;
    }
    
    public boolean requireOverwriteAnnotations() {
        return this.mixin.getParent().requireOverwriteAnnotations();
    }
    
    public ClassInfo findRealType(final ClassInfo mixin) {
        if (mixin == this.getClassInfo()) {
            return this.targetClassInfo;
        }
        final ClassInfo type = this.targetClassInfo.findCorrespondingType(mixin);
        if (type == null) {
            throw new InvalidMixinException(this, "Resolution error: unable to find corresponding type for " + mixin + " in hierarchy of " + this.targetClassInfo);
        }
        return type;
    }
    
    public void transformMethod(final MethodNode method) {
        this.validateMethod(method);
        this.transformDescriptor(method);
        this.transformLVT(method);
        this.stratum.applyOffset(method);
        AbstractInsnNode lastInsn = null;
        final Iterator<AbstractInsnNode> iter = method.instructions.iterator();
        while (iter.hasNext()) {
            final AbstractInsnNode insn = iter.next();
            if (insn instanceof MethodInsnNode) {
                this.transformMethodRef(method, iter, new MemberRef.Method((MethodInsnNode)insn));
            }
            else if (insn instanceof FieldInsnNode) {
                this.transformFieldRef(method, iter, new MemberRef.Field((FieldInsnNode)insn));
                this.checkFinal(method, iter, (FieldInsnNode)insn);
            }
            else if (insn instanceof TypeInsnNode) {
                this.transformTypeNode(method, iter, (TypeInsnNode)insn, lastInsn);
            }
            else if (insn instanceof LdcInsnNode) {
                this.transformConstantNode(method, iter, (LdcInsnNode)insn);
            }
            else if (insn instanceof InvokeDynamicInsnNode) {
                this.transformInvokeDynamicNode(method, iter, (InvokeDynamicInsnNode)insn);
            }
            lastInsn = insn;
        }
    }
    
    private void validateMethod(final MethodNode method) {
        if (Annotations.getInvisible(method, SoftOverride.class) != null) {
            final ClassInfo.Method superMethod = this.targetClassInfo.findMethodInHierarchy(method.name, method.desc, ClassInfo.SearchType.SUPER_CLASSES_ONLY, ClassInfo.Traversal.SUPER);
            if (superMethod == null || !superMethod.isInjected()) {
                throw new InvalidMixinException(this, "Mixin method " + method.name + method.desc + " is tagged with @SoftOverride but no valid method was found in superclasses of " + this.getTarget().getClassName());
            }
        }
    }
    
    private void transformLVT(final MethodNode method) {
        if (method.localVariables == null) {
            return;
        }
        for (final LocalVariableNode local : method.localVariables) {
            if (local != null) {
                if (local.desc == null) {
                    continue;
                }
                local.desc = this.transformSingleDescriptor(Type.getType(local.desc));
            }
        }
    }
    
    private void transformMethodRef(final MethodNode method, final Iterator<AbstractInsnNode> iter, final MemberRef methodRef) {
        this.transformDescriptor(methodRef);
        if (methodRef.getOwner().equals(this.getClassRef())) {
            methodRef.setOwner(this.getTarget().getClassRef());
            final ClassInfo.Method md = this.getClassInfo().findMethod(methodRef.getName(), methodRef.getDesc(), 10);
            if (md != null && md.isRenamed() && md.getOriginalName().equals(methodRef.getName()) && md.isSynthetic()) {
                methodRef.setName(md.getName());
            }
            this.upgradeMethodRef(method, methodRef, md);
        }
        else if (this.innerClasses.containsKey((Object)methodRef.getOwner())) {
            methodRef.setOwner((String)this.innerClasses.get((Object)methodRef.getOwner()));
            methodRef.setDesc(this.transformMethodDescriptor(methodRef.getDesc()));
        }
        else if (this.detachedSuper || this.inheritsFromMixin) {
            if (methodRef.getOpcode() == 183) {
                this.updateStaticBinding(method, methodRef);
            }
            else if (methodRef.getOpcode() == 182 && ClassInfo.forName(methodRef.getOwner()).isMixin()) {
                this.updateDynamicBinding(method, methodRef);
            }
        }
    }
    
    private void transformFieldRef(final MethodNode method, final Iterator<AbstractInsnNode> iter, final MemberRef fieldRef) {
        if ("super$".equals(fieldRef.getName())) {
            if (!(fieldRef instanceof MemberRef.Field)) {
                throw new InvalidMixinException(this.mixin, "Cannot call imaginary super from method handle.");
            }
            this.processImaginarySuper(method, ((MemberRef.Field)fieldRef).insn);
            iter.remove();
        }
        this.transformDescriptor(fieldRef);
        if (fieldRef.getOwner().equals(this.getClassRef())) {
            fieldRef.setOwner(this.getTarget().getClassRef());
            final ClassInfo.Field field = this.getClassInfo().findField(fieldRef.getName(), fieldRef.getDesc(), 10);
            if (field != null && field.isRenamed() && field.getOriginalName().equals(fieldRef.getName()) && field.isStatic()) {
                fieldRef.setName(field.getName());
            }
        }
        else {
            final ClassInfo fieldOwner = ClassInfo.forName(fieldRef.getOwner());
            if (fieldOwner.isMixin()) {
                final ClassInfo actualOwner = this.targetClassInfo.findCorrespondingType(fieldOwner);
                fieldRef.setOwner((actualOwner != null) ? actualOwner.getName() : this.getTarget().getClassRef());
            }
        }
    }
    
    private void checkFinal(final MethodNode method, final Iterator<AbstractInsnNode> iter, final FieldInsnNode fieldNode) {
        if (!fieldNode.owner.equals(this.getTarget().getClassRef())) {
            return;
        }
        final int opcode = fieldNode.getOpcode();
        if (opcode == 180 || opcode == 178) {
            return;
        }
        for (final Map.Entry<FieldNode, ClassInfo.Field> shadow : this.shadowFields.entrySet()) {
            final FieldNode shadowFieldNode = shadow.getKey();
            if (shadowFieldNode.desc.equals(fieldNode.desc)) {
                if (!shadowFieldNode.name.equals(fieldNode.name)) {
                    continue;
                }
                final ClassInfo.Field shadowField = shadow.getValue();
                if (shadowField.isDecoratedFinal()) {
                    if (shadowField.isDecoratedMutable()) {
                        if (this.mixin.getParent().getEnvironment().getOption(MixinEnvironment.Option.DEBUG_VERBOSE)) {
                            MixinTargetContext.logger.warn("Write access to @Mutable @Final field {} in {}::{}", new Object[] { shadowField, this.mixin, method.name });
                        }
                    }
                    else if ("<init>".equals(method.name) || "<clinit>".equals(method.name)) {
                        MixinTargetContext.logger.warn("@Final field {} in {} should be final", new Object[] { shadowField, this.mixin });
                    }
                    else {
                        MixinTargetContext.logger.error("Write access detected to @Final field {} in {}::{}", new Object[] { shadowField, this.mixin, method.name });
                        if (this.mixin.getParent().getEnvironment().getOption(MixinEnvironment.Option.DEBUG_VERIFY)) {
                            throw new InvalidMixinException(this.mixin, "Write access detected to @Final field " + shadowField + " in " + this.mixin + "::" + method.name);
                        }
                    }
                }
            }
        }
    }
    
    private void transformTypeNode(final MethodNode method, final Iterator<AbstractInsnNode> iter, final TypeInsnNode typeInsn, final AbstractInsnNode lastNode) {
        if (typeInsn.getOpcode() == 192 && typeInsn.desc.equals(this.getTarget().getClassRef()) && lastNode.getOpcode() == 25 && ((VarInsnNode)lastNode).var == 0) {
            iter.remove();
            return;
        }
        if (typeInsn.desc.equals(this.getClassRef())) {
            typeInsn.desc = this.getTarget().getClassRef();
        }
        else {
            final String newName = (String)this.innerClasses.get((Object)typeInsn.desc);
            if (newName != null) {
                typeInsn.desc = newName;
            }
        }
        this.transformDescriptor(typeInsn);
    }
    
    private void transformConstantNode(final MethodNode method, final Iterator<AbstractInsnNode> iter, final LdcInsnNode ldcInsn) {
        ldcInsn.cst = this.transformConstant(method, iter, ldcInsn.cst);
    }
    
    private void transformInvokeDynamicNode(final MethodNode method, final Iterator<AbstractInsnNode> iter, final InvokeDynamicInsnNode dynInsn) {
        this.requireVersion(51);
        dynInsn.desc = this.transformMethodDescriptor(dynInsn.desc);
        dynInsn.bsm = this.transformHandle(method, iter, dynInsn.bsm);
        for (int i = 0; i < dynInsn.bsmArgs.length; ++i) {
            dynInsn.bsmArgs[i] = this.transformConstant(method, iter, dynInsn.bsmArgs[i]);
        }
    }
    
    private Object transformConstant(final MethodNode method, final Iterator<AbstractInsnNode> iter, final Object constant) {
        if (constant instanceof Type) {
            final Type type = (Type)constant;
            final String desc = this.transformDescriptor(type);
            if (!type.toString().equals(desc)) {
                return Type.getType(desc);
            }
            return constant;
        }
        else {
            if (constant instanceof Handle) {
                return this.transformHandle(method, iter, (Handle)constant);
            }
            return constant;
        }
    }
    
    private Handle transformHandle(final MethodNode method, final Iterator<AbstractInsnNode> iter, final Handle handle) {
        final MemberRef.Handle memberRef = new MemberRef.Handle(handle);
        if (memberRef.isField()) {
            this.transformFieldRef(method, iter, memberRef);
        }
        else {
            this.transformMethodRef(method, iter, memberRef);
        }
        return memberRef.getMethodHandle();
    }
    
    private void processImaginarySuper(final MethodNode method, final FieldInsnNode fieldInsn) {
        if (fieldInsn.getOpcode() != 180) {
            if ("<init>".equals(method.name)) {
                throw new InvalidMixinException(this, "Illegal imaginary super declaration: field " + fieldInsn.name + " must not specify an initialiser");
            }
            throw new InvalidMixinException(this, "Illegal imaginary super access: found " + Bytecode.getOpcodeName(fieldInsn.getOpcode()) + " opcode in " + method.name + method.desc);
        }
        else {
            if ((method.access & 0x2) != 0x0 || (method.access & 0x8) != 0x0) {
                throw new InvalidMixinException(this, "Illegal imaginary super access: method " + method.name + method.desc + " is private or static");
            }
            if (Annotations.getInvisible(method, SoftOverride.class) == null) {
                throw new InvalidMixinException(this, "Illegal imaginary super access: method " + method.name + method.desc + " is not decorated with @SoftOverride");
            }
            final Iterator<AbstractInsnNode> methodIter = method.instructions.iterator(method.instructions.indexOf(fieldInsn));
            while (methodIter.hasNext()) {
                final AbstractInsnNode insn = methodIter.next();
                if (insn instanceof MethodInsnNode) {
                    final MethodInsnNode methodNode = (MethodInsnNode)insn;
                    if (methodNode.owner.equals(this.getClassRef()) && methodNode.name.equals(method.name) && methodNode.desc.equals(method.desc)) {
                        methodNode.setOpcode(183);
                        this.updateStaticBinding(method, new MemberRef.Method(methodNode));
                        return;
                    }
                    continue;
                }
            }
            throw new InvalidMixinException(this, "Illegal imaginary super access: could not find INVOKE for " + method.name + method.desc);
        }
    }
    
    private void updateStaticBinding(final MethodNode method, final MemberRef methodRef) {
        this.updateBinding(method, methodRef, ClassInfo.Traversal.SUPER);
    }
    
    private void updateDynamicBinding(final MethodNode method, final MemberRef methodRef) {
        this.updateBinding(method, methodRef, ClassInfo.Traversal.ALL);
    }
    
    private void updateBinding(final MethodNode method, final MemberRef methodRef, final ClassInfo.Traversal traversal) {
        if ("<init>".equals(method.name) || methodRef.getOwner().equals(this.getTarget().getClassRef()) || this.getTarget().getClassRef().startsWith("<")) {
            return;
        }
        final ClassInfo.Method superMethod = this.targetClassInfo.findMethodInHierarchy(methodRef.getName(), methodRef.getDesc(), traversal.getSearchType(), traversal);
        if (superMethod != null) {
            if (superMethod.getOwner().isMixin()) {
                throw new InvalidMixinException(this, "Invalid " + methodRef + " in " + this + " resolved " + superMethod.getOwner() + " but is mixin.");
            }
            methodRef.setOwner(superMethod.getImplementor().getName());
        }
        else if (ClassInfo.forName(methodRef.getOwner()).isMixin()) {
            throw new MixinTransformerError("Error resolving " + methodRef + " in " + this);
        }
    }
    
    public void transformDescriptor(final FieldNode field) {
        if (!this.inheritsFromMixin && this.innerClasses.size() == 0) {
            return;
        }
        field.desc = this.transformSingleDescriptor(field.desc, false);
    }
    
    public void transformDescriptor(final MethodNode method) {
        if (!this.inheritsFromMixin && this.innerClasses.size() == 0) {
            return;
        }
        method.desc = this.transformMethodDescriptor(method.desc);
    }
    
    public void transformDescriptor(final MemberRef member) {
        if (!this.inheritsFromMixin && this.innerClasses.size() == 0) {
            return;
        }
        if (member.isField()) {
            member.setDesc(this.transformSingleDescriptor(member.getDesc(), false));
        }
        else {
            member.setDesc(this.transformMethodDescriptor(member.getDesc()));
        }
    }
    
    public void transformDescriptor(final TypeInsnNode typeInsn) {
        if (!this.inheritsFromMixin && this.innerClasses.size() == 0) {
            return;
        }
        typeInsn.desc = this.transformSingleDescriptor(typeInsn.desc, true);
    }
    
    private String transformDescriptor(final Type type) {
        if (type.getSort() == 11) {
            return this.transformMethodDescriptor(type.getDescriptor());
        }
        return this.transformSingleDescriptor(type);
    }
    
    private String transformSingleDescriptor(final Type type) {
        if (type.getSort() < 9) {
            return type.toString();
        }
        return this.transformSingleDescriptor(type.toString(), false);
    }
    
    private String transformSingleDescriptor(final String desc, boolean isObject) {
        String type = desc;
        while (type.startsWith("[") || type.startsWith("L")) {
            if (type.startsWith("[")) {
                type = type.substring(1);
            }
            else {
                type = type.substring(1, type.indexOf(";"));
                isObject = true;
            }
        }
        if (!isObject) {
            return desc;
        }
        final String innerClassName = (String)this.innerClasses.get((Object)type);
        if (innerClassName != null) {
            return desc.replace(type, innerClassName);
        }
        if (this.innerClasses.inverse().containsKey((Object)type)) {
            return desc;
        }
        final ClassInfo typeInfo = ClassInfo.forName(type);
        if (!typeInfo.isMixin()) {
            return desc;
        }
        return desc.replace(type, this.findRealType(typeInfo).toString());
    }
    
    private String transformMethodDescriptor(final String desc) {
        final StringBuilder newDesc = new StringBuilder();
        newDesc.append('(');
        for (final Type arg : Type.getArgumentTypes(desc)) {
            newDesc.append(this.transformSingleDescriptor(arg));
        }
        return newDesc.append(')').append(this.transformSingleDescriptor(Type.getReturnType(desc))).toString();
    }
    
    @Override
    public Target getTargetMethod(final MethodNode method) {
        return this.getTarget().getTargetMethod(method);
    }
    
    MethodNode findMethod(final MethodNode method, final AnnotationNode annotation) {
        final Deque<String> aliases = new LinkedList<String>();
        aliases.add(method.name);
        if (annotation != null) {
            final List<String> aka = Annotations.getValue(annotation, "aliases");
            if (aka != null) {
                aliases.addAll(aka);
            }
        }
        return this.getTarget().findMethod(aliases, method.desc);
    }
    
    MethodNode findRemappedMethod(final MethodNode method) {
        final RemapperChain remapperChain = this.getEnvironment().getRemappers();
        final String remappedName = remapperChain.mapMethodName(this.getTarget().getClassRef(), method.name, method.desc);
        if (remappedName.equals(method.name)) {
            return null;
        }
        final Deque<String> aliases = new LinkedList<String>();
        aliases.add(remappedName);
        return this.getTarget().findAliasedMethod(aliases, method.desc);
    }
    
    FieldNode findField(final FieldNode field, final AnnotationNode shadow) {
        final Deque<String> aliases = new LinkedList<String>();
        aliases.add(field.name);
        if (shadow != null) {
            final List<String> aka = Annotations.getValue(shadow, "aliases");
            if (aka != null) {
                aliases.addAll(aka);
            }
        }
        return this.getTarget().findAliasedField(aliases, field.desc);
    }
    
    FieldNode findRemappedField(final FieldNode field) {
        final RemapperChain remapperChain = this.getEnvironment().getRemappers();
        final String remappedName = remapperChain.mapFieldName(this.getTarget().getClassRef(), field.name, field.desc);
        if (remappedName.equals(field.name)) {
            return null;
        }
        final Deque<String> aliases = new LinkedList<String>();
        aliases.add(remappedName);
        return this.getTarget().findAliasedField(aliases, field.desc);
    }
    
    protected void requireVersion(final int version) {
        this.minRequiredClassVersion = Math.max(this.minRequiredClassVersion, version);
        if (version > MixinEnvironment.getCompatibilityLevel().classVersion()) {
            throw new InvalidMixinException(this, "Unsupported mixin class version " + version);
        }
    }
    
    @Override
    public Extensions getExtensions() {
        return this.targetClass.getExtensions();
    }
    
    @Override
    public IMixinInfo getMixin() {
        return this.mixin;
    }
    
    MixinInfo getInfo() {
        return this.mixin;
    }
    
    @Override
    public int getPriority() {
        return this.mixin.getPriority();
    }
    
    public Set<String> getInterfaces() {
        return this.mixin.getInterfaces();
    }
    
    public Collection<MethodNode> getShadowMethods() {
        return this.shadowMethods;
    }
    
    public List<MethodNode> getMethods() {
        return this.classNode.methods;
    }
    
    public Set<Map.Entry<FieldNode, ClassInfo.Field>> getShadowFields() {
        return this.shadowFields.entrySet();
    }
    
    public List<FieldNode> getFields() {
        return this.classNode.fields;
    }
    
    public Level getLoggingLevel() {
        return this.mixin.getLoggingLevel();
    }
    
    public boolean shouldSetSourceFile() {
        return this.mixin.getParent().shouldSetSourceFile();
    }
    
    public String getSourceFile() {
        return this.classNode.sourceFile;
    }
    
    @Override
    public IReferenceMapper getReferenceMapper() {
        return this.mixin.getParent().getReferenceMapper();
    }
    
    public void preApply(final String transformedName, final ClassNode targetClass) {
        this.mixin.preApply(transformedName, targetClass);
    }
    
    public void postApply(final String transformedName, final ClassNode targetClass) {
        try {
            this.injectorGroups.validateAll();
        }
        catch (InjectionValidationException ex) {
            final InjectorGroupInfo group = ex.getGroup();
            throw new InjectionError(String.format("Critical injection failure: Callback group %s in %s failed injection check: %s", group, this.mixin, ex.getMessage()));
        }
        this.mixin.postApply(transformedName, targetClass);
    }
    
    public String getUniqueName(final MethodNode method, final boolean preservePrefix) {
        return this.getTarget().getUniqueName(method, preservePrefix);
    }
    
    public String getUniqueName(final FieldNode field) {
        return this.getTarget().getUniqueName(field);
    }
    
    public void prepareInjections() {
        this.injectors.clear();
        for (final MethodNode method : this.mergedMethods) {
            final InjectionInfo injectInfo = InjectionInfo.parse(this, method);
            if (injectInfo == null) {
                continue;
            }
            if (injectInfo.isValid()) {
                injectInfo.prepare();
                this.injectors.add(injectInfo);
            }
            method.visibleAnnotations.remove(injectInfo.getAnnotation());
        }
    }
    
    public void applyInjections() {
        for (final InjectionInfo injectInfo : this.injectors) {
            injectInfo.inject();
        }
        for (final InjectionInfo injectInfo : this.injectors) {
            injectInfo.postInject();
        }
        this.injectors.clear();
    }
    
    public List<MethodNode> generateAccessors() {
        for (final AccessorInfo accessor : this.accessors) {
            accessor.locate();
        }
        final List<MethodNode> methods = new ArrayList<MethodNode>();
        for (final AccessorInfo accessor2 : this.accessors) {
            final MethodNode generated = accessor2.generate();
            this.getTarget().addMixinMethod(generated);
            methods.add(generated);
        }
        return methods;
    }
    
    static {
        logger = LogManager.getLogger("mixin");
    }
}

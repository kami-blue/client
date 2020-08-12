// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer;

import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.lang.annotation.Annotation;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.lib.tree.FrameNode;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.lib.Type;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.lib.tree.FieldNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import java.util.Collection;
import org.spongepowered.asm.lib.tree.ClassNode;
import java.util.Collections;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import org.spongepowered.asm.util.ClassSignature;
import java.util.Set;
import java.util.Map;
import org.spongepowered.asm.util.perf.Profiler;
import org.apache.logging.log4j.Logger;

public final class ClassInfo
{
    public static final int INCLUDE_PRIVATE = 2;
    public static final int INCLUDE_STATIC = 8;
    public static final int INCLUDE_ALL = 10;
    private static final Logger logger;
    private static final Profiler profiler;
    private static final String JAVA_LANG_OBJECT = "java/lang/Object";
    private static final Map<String, ClassInfo> cache;
    private static final ClassInfo OBJECT;
    private final String name;
    private final String superName;
    private final String outerName;
    private final boolean isProbablyStatic;
    private final Set<String> interfaces;
    private final Set<Method> methods;
    private final Set<Field> fields;
    private final Set<MixinInfo> mixins;
    private final Map<ClassInfo, ClassInfo> correspondingTypes;
    private final MixinInfo mixin;
    private final MethodMapper methodMapper;
    private final boolean isMixin;
    private final boolean isInterface;
    private final int access;
    private ClassInfo superClass;
    private ClassInfo outerClass;
    private ClassSignature signature;
    
    private ClassInfo() {
        this.mixins = new HashSet<MixinInfo>();
        this.correspondingTypes = new HashMap<ClassInfo, ClassInfo>();
        this.name = "java/lang/Object";
        this.superName = null;
        this.outerName = null;
        this.isProbablyStatic = true;
        this.methods = (Set<Method>)ImmutableSet.of((Object)new Method("getClass", "()Ljava/lang/Class;"), (Object)new Method("hashCode", "()I"), (Object)new Method("equals", "(Ljava/lang/Object;)Z"), (Object)new Method("clone", "()Ljava/lang/Object;"), (Object)new Method("toString", "()Ljava/lang/String;"), (Object)new Method("notify", "()V"), (Object[])new Method[] { new Method("notifyAll", "()V"), new Method("wait", "(J)V"), new Method("wait", "(JI)V"), new Method("wait", "()V"), new Method("finalize", "()V") });
        this.fields = Collections.emptySet();
        this.isInterface = false;
        this.interfaces = Collections.emptySet();
        this.access = 1;
        this.isMixin = false;
        this.mixin = null;
        this.methodMapper = null;
    }
    
    private ClassInfo(final ClassNode classNode) {
        this.mixins = new HashSet<MixinInfo>();
        this.correspondingTypes = new HashMap<ClassInfo, ClassInfo>();
        final Profiler.Section timer = ClassInfo.profiler.begin(1, "class.meta");
        try {
            this.name = classNode.name;
            this.superName = ((classNode.superName != null) ? classNode.superName : "java/lang/Object");
            this.methods = new HashSet<Method>();
            this.fields = new HashSet<Field>();
            this.isInterface = ((classNode.access & 0x200) != 0x0);
            this.interfaces = new HashSet<String>();
            this.access = classNode.access;
            this.isMixin = (classNode instanceof MixinInfo.MixinClassNode);
            this.mixin = (this.isMixin ? ((MixinInfo.MixinClassNode)classNode).getMixin() : null);
            this.interfaces.addAll(classNode.interfaces);
            for (final MethodNode method : classNode.methods) {
                this.addMethod(method, this.isMixin);
            }
            boolean isProbablyStatic = true;
            String outerName = classNode.outerClass;
            for (final FieldNode field : classNode.fields) {
                if ((field.access & 0x1000) != 0x0 && field.name.startsWith("this$")) {
                    isProbablyStatic = false;
                    if (outerName == null) {
                        outerName = field.desc;
                        if (outerName != null && outerName.startsWith("L")) {
                            outerName = outerName.substring(1, outerName.length() - 1);
                        }
                    }
                }
                this.fields.add(new Field(field, this.isMixin));
            }
            this.isProbablyStatic = isProbablyStatic;
            this.outerName = outerName;
            this.methodMapper = new MethodMapper(MixinEnvironment.getCurrentEnvironment(), this);
            this.signature = ClassSignature.ofLazy(classNode);
        }
        finally {
            timer.end();
        }
    }
    
    void addInterface(final String iface) {
        this.interfaces.add(iface);
        this.getSignature().addInterface(iface);
    }
    
    void addMethod(final MethodNode method) {
        this.addMethod(method, true);
    }
    
    private void addMethod(final MethodNode method, final boolean injected) {
        if (!method.name.startsWith("<")) {
            this.methods.add(new Method(method, injected));
        }
    }
    
    void addMixin(final MixinInfo mixin) {
        if (this.isMixin) {
            throw new IllegalArgumentException("Cannot add target " + this.name + " for " + mixin.getClassName() + " because the target is a mixin");
        }
        this.mixins.add(mixin);
    }
    
    public Set<MixinInfo> getMixins() {
        return Collections.unmodifiableSet((Set<? extends MixinInfo>)this.mixins);
    }
    
    public boolean isMixin() {
        return this.isMixin;
    }
    
    public boolean isPublic() {
        return (this.access & 0x1) != 0x0;
    }
    
    public boolean isAbstract() {
        return (this.access & 0x400) != 0x0;
    }
    
    public boolean isSynthetic() {
        return (this.access & 0x1000) != 0x0;
    }
    
    public boolean isProbablyStatic() {
        return this.isProbablyStatic;
    }
    
    public boolean isInner() {
        return this.outerName != null;
    }
    
    public boolean isInterface() {
        return this.isInterface;
    }
    
    public Set<String> getInterfaces() {
        return Collections.unmodifiableSet((Set<? extends String>)this.interfaces);
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    public MethodMapper getMethodMapper() {
        return this.methodMapper;
    }
    
    public int getAccess() {
        return this.access;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getClassName() {
        return this.name.replace('/', '.');
    }
    
    public String getSuperName() {
        return this.superName;
    }
    
    public ClassInfo getSuperClass() {
        if (this.superClass == null && this.superName != null) {
            this.superClass = forName(this.superName);
        }
        return this.superClass;
    }
    
    public String getOuterName() {
        return this.outerName;
    }
    
    public ClassInfo getOuterClass() {
        if (this.outerClass == null && this.outerName != null) {
            this.outerClass = forName(this.outerName);
        }
        return this.outerClass;
    }
    
    public ClassSignature getSignature() {
        return this.signature.wake();
    }
    
    List<ClassInfo> getTargets() {
        if (this.mixin != null) {
            final List<ClassInfo> targets = new ArrayList<ClassInfo>();
            targets.add(this);
            targets.addAll(this.mixin.getTargets());
            return targets;
        }
        return (List<ClassInfo>)ImmutableList.of((Object)this);
    }
    
    public Set<Method> getMethods() {
        return Collections.unmodifiableSet((Set<? extends Method>)this.methods);
    }
    
    public Set<Method> getInterfaceMethods(final boolean includeMixins) {
        final Set<Method> methods = new HashSet<Method>();
        ClassInfo supClass = this.addMethodsRecursive(methods, includeMixins);
        if (!this.isInterface) {
            while (supClass != null && supClass != ClassInfo.OBJECT) {
                supClass = supClass.addMethodsRecursive(methods, includeMixins);
            }
        }
        final Iterator<Method> it = methods.iterator();
        while (it.hasNext()) {
            if (!it.next().isAbstract()) {
                it.remove();
            }
        }
        return Collections.unmodifiableSet((Set<? extends Method>)methods);
    }
    
    private ClassInfo addMethodsRecursive(final Set<Method> methods, final boolean includeMixins) {
        if (this.isInterface) {
            for (final Method method : this.methods) {
                if (!method.isAbstract()) {
                    methods.remove(method);
                }
                methods.add(method);
            }
        }
        else if (!this.isMixin && includeMixins) {
            for (final MixinInfo mixin : this.mixins) {
                mixin.getClassInfo().addMethodsRecursive(methods, includeMixins);
            }
        }
        for (final String iface : this.interfaces) {
            forName(iface).addMethodsRecursive(methods, includeMixins);
        }
        return this.getSuperClass();
    }
    
    public boolean hasSuperClass(final String superClass) {
        return this.hasSuperClass(superClass, Traversal.NONE);
    }
    
    public boolean hasSuperClass(final String superClass, final Traversal traversal) {
        return "java/lang/Object".equals(superClass) || this.findSuperClass(superClass, traversal) != null;
    }
    
    public boolean hasSuperClass(final ClassInfo superClass) {
        return this.hasSuperClass(superClass, Traversal.NONE, false);
    }
    
    public boolean hasSuperClass(final ClassInfo superClass, final Traversal traversal) {
        return this.hasSuperClass(superClass, traversal, false);
    }
    
    public boolean hasSuperClass(final ClassInfo superClass, final Traversal traversal, final boolean includeInterfaces) {
        return ClassInfo.OBJECT == superClass || this.findSuperClass(superClass.name, traversal, includeInterfaces) != null;
    }
    
    public ClassInfo findSuperClass(final String superClass) {
        return this.findSuperClass(superClass, Traversal.NONE);
    }
    
    public ClassInfo findSuperClass(final String superClass, final Traversal traversal) {
        return this.findSuperClass(superClass, traversal, false, new HashSet<String>());
    }
    
    public ClassInfo findSuperClass(final String superClass, final Traversal traversal, final boolean includeInterfaces) {
        if (ClassInfo.OBJECT.name.equals(superClass)) {
            return null;
        }
        return this.findSuperClass(superClass, traversal, includeInterfaces, new HashSet<String>());
    }
    
    private ClassInfo findSuperClass(final String superClass, final Traversal traversal, final boolean includeInterfaces, final Set<String> traversed) {
        final ClassInfo superClassInfo = this.getSuperClass();
        if (superClassInfo != null) {
            for (final ClassInfo superTarget : superClassInfo.getTargets()) {
                if (superClass.equals(superTarget.getName())) {
                    return superClassInfo;
                }
                final ClassInfo found = superTarget.findSuperClass(superClass, traversal.next(), includeInterfaces, traversed);
                if (found != null) {
                    return found;
                }
            }
        }
        if (includeInterfaces) {
            final ClassInfo iface = this.findInterface(superClass);
            if (iface != null) {
                return iface;
            }
        }
        if (traversal.canTraverse()) {
            for (final MixinInfo mixin : this.mixins) {
                final String mixinClassName = mixin.getClassName();
                if (traversed.contains(mixinClassName)) {
                    continue;
                }
                traversed.add(mixinClassName);
                final ClassInfo mixinClass = mixin.getClassInfo();
                if (superClass.equals(mixinClass.getName())) {
                    return mixinClass;
                }
                final ClassInfo targetSuper = mixinClass.findSuperClass(superClass, Traversal.ALL, includeInterfaces, traversed);
                if (targetSuper != null) {
                    return targetSuper;
                }
            }
        }
        return null;
    }
    
    private ClassInfo findInterface(final String superClass) {
        for (final String ifaceName : this.getInterfaces()) {
            final ClassInfo iface = forName(ifaceName);
            if (superClass.equals(ifaceName)) {
                return iface;
            }
            final ClassInfo superIface = iface.findInterface(superClass);
            if (superIface != null) {
                return superIface;
            }
        }
        return null;
    }
    
    ClassInfo findCorrespondingType(final ClassInfo mixin) {
        if (mixin == null || !mixin.isMixin || this.isMixin) {
            return null;
        }
        ClassInfo correspondingType = this.correspondingTypes.get(mixin);
        if (correspondingType == null) {
            correspondingType = this.findSuperTypeForMixin(mixin);
            this.correspondingTypes.put(mixin, correspondingType);
        }
        return correspondingType;
    }
    
    private ClassInfo findSuperTypeForMixin(final ClassInfo mixin) {
        for (ClassInfo superClass = this; superClass != null && superClass != ClassInfo.OBJECT; superClass = superClass.getSuperClass()) {
            for (final MixinInfo minion : superClass.mixins) {
                if (minion.getClassInfo().equals(mixin)) {
                    return superClass;
                }
            }
        }
        return null;
    }
    
    public boolean hasMixinInHierarchy() {
        if (!this.isMixin) {
            return false;
        }
        for (ClassInfo supClass = this.getSuperClass(); supClass != null && supClass != ClassInfo.OBJECT; supClass = supClass.getSuperClass()) {
            if (supClass.isMixin) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasMixinTargetInHierarchy() {
        if (this.isMixin) {
            return false;
        }
        for (ClassInfo supClass = this.getSuperClass(); supClass != null && supClass != ClassInfo.OBJECT; supClass = supClass.getSuperClass()) {
            if (supClass.mixins.size() > 0) {
                return true;
            }
        }
        return false;
    }
    
    public Method findMethodInHierarchy(final MethodNode method, final SearchType searchType) {
        return this.findMethodInHierarchy(method.name, method.desc, searchType, Traversal.NONE);
    }
    
    public Method findMethodInHierarchy(final MethodNode method, final SearchType searchType, final int flags) {
        return this.findMethodInHierarchy(method.name, method.desc, searchType, Traversal.NONE, flags);
    }
    
    public Method findMethodInHierarchy(final MethodInsnNode method, final SearchType searchType) {
        return this.findMethodInHierarchy(method.name, method.desc, searchType, Traversal.NONE);
    }
    
    public Method findMethodInHierarchy(final MethodInsnNode method, final SearchType searchType, final int flags) {
        return this.findMethodInHierarchy(method.name, method.desc, searchType, Traversal.NONE, flags);
    }
    
    public Method findMethodInHierarchy(final String name, final String desc, final SearchType searchType) {
        return this.findMethodInHierarchy(name, desc, searchType, Traversal.NONE);
    }
    
    public Method findMethodInHierarchy(final String name, final String desc, final SearchType searchType, final Traversal traversal) {
        return this.findMethodInHierarchy(name, desc, searchType, traversal, 0);
    }
    
    public Method findMethodInHierarchy(final String name, final String desc, final SearchType searchType, final Traversal traversal, final int flags) {
        return this.findInHierarchy(name, desc, searchType, traversal, flags, Member.Type.METHOD);
    }
    
    public Field findFieldInHierarchy(final FieldNode field, final SearchType searchType) {
        return this.findFieldInHierarchy(field.name, field.desc, searchType, Traversal.NONE);
    }
    
    public Field findFieldInHierarchy(final FieldNode field, final SearchType searchType, final int flags) {
        return this.findFieldInHierarchy(field.name, field.desc, searchType, Traversal.NONE, flags);
    }
    
    public Field findFieldInHierarchy(final FieldInsnNode field, final SearchType searchType) {
        return this.findFieldInHierarchy(field.name, field.desc, searchType, Traversal.NONE);
    }
    
    public Field findFieldInHierarchy(final FieldInsnNode field, final SearchType searchType, final int flags) {
        return this.findFieldInHierarchy(field.name, field.desc, searchType, Traversal.NONE, flags);
    }
    
    public Field findFieldInHierarchy(final String name, final String desc, final SearchType searchType) {
        return this.findFieldInHierarchy(name, desc, searchType, Traversal.NONE);
    }
    
    public Field findFieldInHierarchy(final String name, final String desc, final SearchType searchType, final Traversal traversal) {
        return this.findFieldInHierarchy(name, desc, searchType, traversal, 0);
    }
    
    public Field findFieldInHierarchy(final String name, final String desc, final SearchType searchType, final Traversal traversal, final int flags) {
        return this.findInHierarchy(name, desc, searchType, traversal, flags, Member.Type.FIELD);
    }
    
    private <M extends Member> M findInHierarchy(final String name, final String desc, final SearchType searchType, final Traversal traversal, final int flags, final Member.Type type) {
        if (searchType == SearchType.ALL_CLASSES) {
            final M member = this.findMember(name, desc, flags, type);
            if (member != null) {
                return member;
            }
            if (traversal.canTraverse()) {
                for (final MixinInfo mixin : this.mixins) {
                    final M mixinMember = mixin.getClassInfo().findMember(name, desc, flags, type);
                    if (mixinMember != null) {
                        return this.cloneMember(mixinMember);
                    }
                }
            }
        }
        final ClassInfo superClassInfo = this.getSuperClass();
        if (superClassInfo != null) {
            for (final ClassInfo superTarget : superClassInfo.getTargets()) {
                final M member2 = (M)superTarget.findInHierarchy(name, desc, SearchType.ALL_CLASSES, traversal.next(), flags & 0xFFFFFFFD, type);
                if (member2 != null) {
                    return member2;
                }
            }
        }
        if (type == Member.Type.METHOD && (this.isInterface || MixinEnvironment.getCompatibilityLevel().supportsMethodsInInterfaces())) {
            for (final String implemented : this.interfaces) {
                final ClassInfo iface = forName(implemented);
                if (iface == null) {
                    ClassInfo.logger.debug("Failed to resolve declared interface {} on {}", new Object[] { implemented, this.name });
                }
                else {
                    final M member3 = (M)iface.findInHierarchy(name, desc, SearchType.ALL_CLASSES, traversal.next(), flags & 0xFFFFFFFD, type);
                    if (member3 != null) {
                        return (M)(this.isInterface ? member3 : new InterfaceMethod(member3));
                    }
                    continue;
                }
            }
        }
        return null;
    }
    
    private <M extends Member> M cloneMember(final M member) {
        if (member instanceof Method) {
            return (M)new Method(member);
        }
        return (M)new Field(member);
    }
    
    public Method findMethod(final MethodNode method) {
        return this.findMethod(method.name, method.desc, method.access);
    }
    
    public Method findMethod(final MethodNode method, final int flags) {
        return this.findMethod(method.name, method.desc, flags);
    }
    
    public Method findMethod(final MethodInsnNode method) {
        return this.findMethod(method.name, method.desc, 0);
    }
    
    public Method findMethod(final MethodInsnNode method, final int flags) {
        return this.findMethod(method.name, method.desc, flags);
    }
    
    public Method findMethod(final String name, final String desc, final int flags) {
        return this.findMember(name, desc, flags, Member.Type.METHOD);
    }
    
    public Field findField(final FieldNode field) {
        return this.findField(field.name, field.desc, field.access);
    }
    
    public Field findField(final FieldInsnNode field, final int flags) {
        return this.findField(field.name, field.desc, flags);
    }
    
    public Field findField(final String name, final String desc, final int flags) {
        return this.findMember(name, desc, flags, Member.Type.FIELD);
    }
    
    private <M extends Member> M findMember(final String name, final String desc, final int flags, final Member.Type memberType) {
        final Set<M> members = (Set<M>)((memberType == Member.Type.METHOD) ? this.methods : this.fields);
        for (final M member : members) {
            if (member.equals(name, desc) && member.matchesFlags(flags)) {
                return member;
            }
        }
        return null;
    }
    
    @Override
    public boolean equals(final Object other) {
        return other instanceof ClassInfo && ((ClassInfo)other).name.equals(this.name);
    }
    
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    
    static ClassInfo fromClassNode(final ClassNode classNode) {
        ClassInfo info = ClassInfo.cache.get(classNode.name);
        if (info == null) {
            info = new ClassInfo(classNode);
            ClassInfo.cache.put(classNode.name, info);
        }
        return info;
    }
    
    public static ClassInfo forName(String className) {
        className = className.replace('.', '/');
        ClassInfo info = ClassInfo.cache.get(className);
        if (info == null) {
            try {
                final ClassNode classNode = MixinService.getService().getBytecodeProvider().getClassNode(className);
                info = new ClassInfo(classNode);
            }
            catch (Exception ex) {
                ClassInfo.logger.catching(Level.TRACE, (Throwable)ex);
                ClassInfo.logger.warn("Error loading class: {} ({}: {})", new Object[] { className, ex.getClass().getName(), ex.getMessage() });
            }
            ClassInfo.cache.put(className, info);
            ClassInfo.logger.trace("Added class metadata for {} to metadata cache", new Object[] { className });
        }
        return info;
    }
    
    public static ClassInfo forType(final Type type) {
        if (type.getSort() == 9) {
            return forType(type.getElementType());
        }
        if (type.getSort() < 9) {
            return null;
        }
        return forName(type.getClassName().replace('.', '/'));
    }
    
    public static ClassInfo getCommonSuperClass(final String type1, final String type2) {
        if (type1 == null || type2 == null) {
            return ClassInfo.OBJECT;
        }
        return getCommonSuperClass(forName(type1), forName(type2));
    }
    
    public static ClassInfo getCommonSuperClass(final Type type1, final Type type2) {
        if (type1 == null || type2 == null || type1.getSort() != 10 || type2.getSort() != 10) {
            return ClassInfo.OBJECT;
        }
        return getCommonSuperClass(forType(type1), forType(type2));
    }
    
    private static ClassInfo getCommonSuperClass(final ClassInfo type1, final ClassInfo type2) {
        return getCommonSuperClass(type1, type2, false);
    }
    
    public static ClassInfo getCommonSuperClassOrInterface(final String type1, final String type2) {
        if (type1 == null || type2 == null) {
            return ClassInfo.OBJECT;
        }
        return getCommonSuperClassOrInterface(forName(type1), forName(type2));
    }
    
    public static ClassInfo getCommonSuperClassOrInterface(final Type type1, final Type type2) {
        if (type1 == null || type2 == null || type1.getSort() != 10 || type2.getSort() != 10) {
            return ClassInfo.OBJECT;
        }
        return getCommonSuperClassOrInterface(forType(type1), forType(type2));
    }
    
    public static ClassInfo getCommonSuperClassOrInterface(final ClassInfo type1, final ClassInfo type2) {
        return getCommonSuperClass(type1, type2, true);
    }
    
    private static ClassInfo getCommonSuperClass(ClassInfo type1, final ClassInfo type2, final boolean includeInterfaces) {
        if (type1.hasSuperClass(type2, Traversal.NONE, includeInterfaces)) {
            return type2;
        }
        if (type2.hasSuperClass(type1, Traversal.NONE, includeInterfaces)) {
            return type1;
        }
        if (type1.isInterface() || type2.isInterface()) {
            return ClassInfo.OBJECT;
        }
        do {
            type1 = type1.getSuperClass();
            if (type1 == null) {
                return ClassInfo.OBJECT;
            }
        } while (!type2.hasSuperClass(type1, Traversal.NONE, includeInterfaces));
        return type1;
    }
    
    static {
        logger = LogManager.getLogger("mixin");
        profiler = MixinEnvironment.getProfiler();
        cache = new HashMap<String, ClassInfo>();
        OBJECT = new ClassInfo();
        ClassInfo.cache.put("java/lang/Object", ClassInfo.OBJECT);
    }
    
    public enum SearchType
    {
        ALL_CLASSES, 
        SUPER_CLASSES_ONLY;
    }
    
    public enum Traversal
    {
        NONE((Traversal)null, false, SearchType.SUPER_CLASSES_ONLY), 
        ALL((Traversal)null, true, SearchType.ALL_CLASSES), 
        IMMEDIATE(Traversal.NONE, true, SearchType.SUPER_CLASSES_ONLY), 
        SUPER(Traversal.ALL, false, SearchType.SUPER_CLASSES_ONLY);
        
        private final Traversal next;
        private final boolean traverse;
        private final SearchType searchType;
        
        private Traversal(final Traversal next, final boolean traverse, final SearchType searchType) {
            this.next = ((next != null) ? next : this);
            this.traverse = traverse;
            this.searchType = searchType;
        }
        
        public Traversal next() {
            return this.next;
        }
        
        public boolean canTraverse() {
            return this.traverse;
        }
        
        public SearchType getSearchType() {
            return this.searchType;
        }
    }
    
    public static class FrameData
    {
        private static final String[] FRAMETYPES;
        public final int index;
        public final int type;
        public final int locals;
        
        FrameData(final int index, final int type, final int locals) {
            this.index = index;
            this.type = type;
            this.locals = locals;
        }
        
        FrameData(final int index, final FrameNode frameNode) {
            this.index = index;
            this.type = frameNode.type;
            this.locals = ((frameNode.local != null) ? frameNode.local.size() : 0);
        }
        
        @Override
        public String toString() {
            return String.format("FrameData[index=%d, type=%s, locals=%d]", this.index, FrameData.FRAMETYPES[this.type + 1], this.locals);
        }
        
        static {
            FRAMETYPES = new String[] { "NEW", "FULL", "APPEND", "CHOP", "SAME", "SAME1" };
        }
    }
    
    abstract static class Member
    {
        private final Type type;
        private final String memberName;
        private final String memberDesc;
        private final boolean isInjected;
        private final int modifiers;
        private String currentName;
        private String currentDesc;
        private boolean decoratedFinal;
        private boolean decoratedMutable;
        private boolean unique;
        
        protected Member(final Member member) {
            this(member.type, member.memberName, member.memberDesc, member.modifiers, member.isInjected);
            this.currentName = member.currentName;
            this.currentDesc = member.currentDesc;
            this.unique = member.unique;
        }
        
        protected Member(final Type type, final String name, final String desc, final int access) {
            this(type, name, desc, access, false);
        }
        
        protected Member(final Type type, final String name, final String desc, final int access, final boolean injected) {
            this.type = type;
            this.memberName = name;
            this.memberDesc = desc;
            this.isInjected = injected;
            this.currentName = name;
            this.currentDesc = desc;
            this.modifiers = access;
        }
        
        public String getOriginalName() {
            return this.memberName;
        }
        
        public String getName() {
            return this.currentName;
        }
        
        public String getOriginalDesc() {
            return this.memberDesc;
        }
        
        public String getDesc() {
            return this.currentDesc;
        }
        
        public boolean isInjected() {
            return this.isInjected;
        }
        
        public boolean isRenamed() {
            return !this.currentName.equals(this.memberName);
        }
        
        public boolean isRemapped() {
            return !this.currentDesc.equals(this.memberDesc);
        }
        
        public boolean isPrivate() {
            return (this.modifiers & 0x2) != 0x0;
        }
        
        public boolean isStatic() {
            return (this.modifiers & 0x8) != 0x0;
        }
        
        public boolean isAbstract() {
            return (this.modifiers & 0x400) != 0x0;
        }
        
        public boolean isFinal() {
            return (this.modifiers & 0x10) != 0x0;
        }
        
        public boolean isSynthetic() {
            return (this.modifiers & 0x1000) != 0x0;
        }
        
        public boolean isUnique() {
            return this.unique;
        }
        
        public void setUnique(final boolean unique) {
            this.unique = unique;
        }
        
        public boolean isDecoratedFinal() {
            return this.decoratedFinal;
        }
        
        public boolean isDecoratedMutable() {
            return this.decoratedMutable;
        }
        
        public void setDecoratedFinal(final boolean decoratedFinal, final boolean decoratedMutable) {
            this.decoratedFinal = decoratedFinal;
            this.decoratedMutable = decoratedMutable;
        }
        
        public boolean matchesFlags(final int flags) {
            return ((~this.modifiers | (flags & 0x2)) & 0x2) != 0x0 && ((~this.modifiers | (flags & 0x8)) & 0x8) != 0x0;
        }
        
        public abstract ClassInfo getOwner();
        
        public ClassInfo getImplementor() {
            return this.getOwner();
        }
        
        public int getAccess() {
            return this.modifiers;
        }
        
        public String renameTo(final String name) {
            return this.currentName = name;
        }
        
        public String remapTo(final String desc) {
            return this.currentDesc = desc;
        }
        
        public boolean equals(final String name, final String desc) {
            return (this.memberName.equals(name) || this.currentName.equals(name)) && (this.memberDesc.equals(desc) || this.currentDesc.equals(desc));
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof Member)) {
                return false;
            }
            final Member other = (Member)obj;
            return (other.memberName.equals(this.memberName) || other.currentName.equals(this.currentName)) && (other.memberDesc.equals(this.memberDesc) || other.currentDesc.equals(this.currentDesc));
        }
        
        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }
        
        @Override
        public String toString() {
            return String.format(this.getDisplayFormat(), this.memberName, this.memberDesc);
        }
        
        protected String getDisplayFormat() {
            return "%s%s";
        }
        
        enum Type
        {
            METHOD, 
            FIELD;
        }
    }
    
    public class Method extends Member
    {
        private final List<FrameData> frames;
        private boolean isAccessor;
        
        public Method(final Member member) {
            super(member);
            this.frames = ((member instanceof Method) ? ((Method)member).frames : null);
        }
        
        public Method(final ClassInfo this$0, final MethodNode method) {
            this(this$0, method, false);
            this.setUnique(Annotations.getVisible(method, Unique.class) != null);
            this.isAccessor = (Annotations.getSingleVisible(method, Accessor.class, Invoker.class) != null);
        }
        
        public Method(final MethodNode method, final boolean injected) {
            super(Type.METHOD, method.name, method.desc, method.access, injected);
            this.frames = this.gatherFrames(method);
            this.setUnique(Annotations.getVisible(method, Unique.class) != null);
            this.isAccessor = (Annotations.getSingleVisible(method, Accessor.class, Invoker.class) != null);
        }
        
        public Method(final String name, final String desc) {
            super(Type.METHOD, name, desc, 1, false);
            this.frames = null;
        }
        
        public Method(final String name, final String desc, final int access) {
            super(Type.METHOD, name, desc, access, false);
            this.frames = null;
        }
        
        public Method(final String name, final String desc, final int access, final boolean injected) {
            super(Type.METHOD, name, desc, access, injected);
            this.frames = null;
        }
        
        private List<FrameData> gatherFrames(final MethodNode method) {
            final List<FrameData> frames = new ArrayList<FrameData>();
            for (final AbstractInsnNode insn : method.instructions) {
                if (insn instanceof FrameNode) {
                    frames.add(new FrameData(method.instructions.indexOf(insn), (FrameNode)insn));
                }
            }
            return frames;
        }
        
        public List<FrameData> getFrames() {
            return this.frames;
        }
        
        @Override
        public ClassInfo getOwner() {
            return ClassInfo.this;
        }
        
        public boolean isAccessor() {
            return this.isAccessor;
        }
        
        @Override
        public boolean equals(final Object obj) {
            return obj instanceof Method && super.equals(obj);
        }
    }
    
    public class InterfaceMethod extends Method
    {
        private final ClassInfo owner;
        
        public InterfaceMethod(final Member member) {
            super(member);
            this.owner = member.getOwner();
        }
        
        @Override
        public ClassInfo getOwner() {
            return this.owner;
        }
        
        @Override
        public ClassInfo getImplementor() {
            return ClassInfo.this;
        }
    }
    
    class Field extends Member
    {
        public Field(final Member member) {
            super(member);
        }
        
        public Field(final ClassInfo this$0, final FieldNode field) {
            this(this$0, field, false);
        }
        
        public Field(final FieldNode field, final boolean injected) {
            super(Type.FIELD, field.name, field.desc, field.access, injected);
            this.setUnique(Annotations.getVisible(field, Unique.class) != null);
            if (Annotations.getVisible(field, Shadow.class) != null) {
                final boolean decoratedFinal = Annotations.getVisible(field, Final.class) != null;
                final boolean decoratedMutable = Annotations.getVisible(field, Mutable.class) != null;
                this.setDecoratedFinal(decoratedFinal, decoratedMutable);
            }
        }
        
        public Field(final String name, final String desc, final int access) {
            super(Type.FIELD, name, desc, access, false);
        }
        
        public Field(final String name, final String desc, final int access, final boolean injected) {
            super(Type.FIELD, name, desc, access, injected);
        }
        
        @Override
        public ClassInfo getOwner() {
            return ClassInfo.this;
        }
        
        @Override
        public boolean equals(final Object obj) {
            return obj instanceof Field && super.equals(obj);
        }
        
        @Override
        protected String getDisplayFormat() {
            return "%s:%s";
        }
    }
}

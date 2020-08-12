// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer;

import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.mixin.transformer.throwables.MixinTransformerError;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.lang.annotation.Annotation;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.lib.ClassWriter;
import org.spongepowered.asm.lib.MethodVisitor;
import org.spongepowered.asm.lib.FieldVisitor;
import org.spongepowered.asm.lib.ClassVisitor;
import org.spongepowered.asm.transformers.MixinClassWriter;
import org.spongepowered.asm.lib.ClassReader;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.spongepowered.asm.transformers.TreeTransformer;

class MixinPostProcessor extends TreeTransformer implements MixinConfig.IListener
{
    private final Set<String> syntheticInnerClasses;
    private final Map<String, MixinInfo> accessorMixins;
    private final Set<String> loadable;
    
    MixinPostProcessor() {
        this.syntheticInnerClasses = new HashSet<String>();
        this.accessorMixins = new HashMap<String, MixinInfo>();
        this.loadable = new HashSet<String>();
    }
    
    @Override
    public void onInit(final MixinInfo mixin) {
        for (final String innerClass : mixin.getSyntheticInnerClasses()) {
            this.registerSyntheticInner(innerClass.replace('/', '.'));
        }
    }
    
    @Override
    public void onPrepare(final MixinInfo mixin) {
        final String className = mixin.getClassName();
        if (mixin.isLoadable()) {
            this.registerLoadable(className);
        }
        if (mixin.isAccessor()) {
            this.registerAccessor(mixin);
        }
    }
    
    void registerSyntheticInner(final String className) {
        this.syntheticInnerClasses.add(className);
    }
    
    void registerLoadable(final String className) {
        this.loadable.add(className);
    }
    
    void registerAccessor(final MixinInfo mixin) {
        this.registerLoadable(mixin.getClassName());
        this.accessorMixins.put(mixin.getClassName(), mixin);
    }
    
    boolean canTransform(final String className) {
        return this.syntheticInnerClasses.contains(className) || this.loadable.contains(className);
    }
    
    @Override
    public String getName() {
        return this.getClass().getName();
    }
    
    @Override
    public boolean isDelegationExcluded() {
        return true;
    }
    
    @Override
    public byte[] transformClassBytes(final String name, final String transformedName, final byte[] bytes) {
        if (this.syntheticInnerClasses.contains(transformedName)) {
            return this.processSyntheticInner(bytes);
        }
        if (this.accessorMixins.containsKey(transformedName)) {
            final MixinInfo mixin = this.accessorMixins.get(transformedName);
            return this.processAccessor(bytes, mixin);
        }
        return bytes;
    }
    
    private byte[] processSyntheticInner(final byte[] bytes) {
        final ClassReader cr = new ClassReader(bytes);
        final ClassWriter cw = new MixinClassWriter(cr, 0);
        final ClassVisitor visibilityVisitor = new ClassVisitor(327680, cw) {
            @Override
            public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
                super.visit(version, access | 0x1, name, signature, superName, interfaces);
            }
            
            @Override
            public FieldVisitor visitField(int access, final String name, final String desc, final String signature, final Object value) {
                if ((access & 0x6) == 0x0) {
                    access |= 0x1;
                }
                return super.visitField(access, name, desc, signature, value);
            }
            
            @Override
            public MethodVisitor visitMethod(int access, final String name, final String desc, final String signature, final String[] exceptions) {
                if ((access & 0x6) == 0x0) {
                    access |= 0x1;
                }
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        };
        cr.accept(visibilityVisitor, 8);
        return cw.toByteArray();
    }
    
    private byte[] processAccessor(final byte[] bytes, final MixinInfo mixin) {
        if (!MixinEnvironment.getCompatibilityLevel().isAtLeast(MixinEnvironment.CompatibilityLevel.JAVA_8)) {
            return bytes;
        }
        boolean transformed = false;
        final MixinInfo.MixinClassNode classNode = mixin.getClassNode(0);
        final ClassInfo targetClass = mixin.getTargets().get(0);
        for (final MixinInfo.MixinMethodNode methodNode : classNode.mixinMethods) {
            if (!Bytecode.hasFlag(methodNode, 8)) {
                continue;
            }
            final AnnotationNode accessor = methodNode.getVisibleAnnotation(Accessor.class);
            final AnnotationNode invoker = methodNode.getVisibleAnnotation(Invoker.class);
            if (accessor == null && invoker == null) {
                continue;
            }
            final ClassInfo.Method method = getAccessorMethod(mixin, methodNode, targetClass);
            createProxy(methodNode, targetClass, method);
            transformed = true;
        }
        if (transformed) {
            return this.writeClass(classNode);
        }
        return bytes;
    }
    
    private static ClassInfo.Method getAccessorMethod(final MixinInfo mixin, final MethodNode methodNode, final ClassInfo targetClass) throws MixinTransformerError {
        final ClassInfo.Method method = mixin.getClassInfo().findMethod(methodNode, 10);
        if (!method.isRenamed()) {
            throw new MixinTransformerError("Unexpected state: " + mixin + " loaded before " + targetClass + " was conformed");
        }
        return method;
    }
    
    private static void createProxy(final MethodNode methodNode, final ClassInfo targetClass, final ClassInfo.Method method) {
        methodNode.instructions.clear();
        final Type[] args = Type.getArgumentTypes(methodNode.desc);
        final Type returnType = Type.getReturnType(methodNode.desc);
        Bytecode.loadArgs(args, methodNode.instructions, 0);
        methodNode.instructions.add(new MethodInsnNode(184, targetClass.getName(), method.getName(), methodNode.desc, false));
        methodNode.instructions.add(new InsnNode(returnType.getOpcode(172)));
        methodNode.maxStack = Bytecode.getFirstNonArgLocalIndex(args, false);
        methodNode.maxLocals = 0;
    }
}

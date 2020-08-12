// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.code;

import java.util.Collections;
import java.util.HashSet;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.lib.tree.LdcInsnNode;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.lib.tree.InsnList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;
import org.spongepowered.asm.mixin.MixinEnvironment;
import java.util.Set;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.mixin.injection.struct.Target;
import java.util.Iterator;
import java.util.ArrayList;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import java.util.List;
import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.apache.logging.log4j.Logger;

public abstract class Injector
{
    protected static final Logger logger;
    protected InjectionInfo info;
    protected final ClassNode classNode;
    protected final MethodNode methodNode;
    protected final Type[] methodArgs;
    protected final Type returnType;
    protected final boolean isStatic;
    
    public Injector(final InjectionInfo info) {
        this(info.getClassNode(), info.getMethod());
        this.info = info;
    }
    
    private Injector(final ClassNode classNode, final MethodNode methodNode) {
        this.classNode = classNode;
        this.methodNode = methodNode;
        this.methodArgs = Type.getArgumentTypes(this.methodNode.desc);
        this.returnType = Type.getReturnType(this.methodNode.desc);
        this.isStatic = Bytecode.methodIsStatic(this.methodNode);
    }
    
    @Override
    public String toString() {
        return String.format("%s::%s", this.classNode.name, this.methodNode.name);
    }
    
    public final List<InjectionNodes.InjectionNode> find(final InjectorTarget injectorTarget, final List<InjectionPoint> injectionPoints) {
        this.sanityCheck(injectorTarget.getTarget(), injectionPoints);
        final List<InjectionNodes.InjectionNode> myNodes = new ArrayList<InjectionNodes.InjectionNode>();
        for (final TargetNode node : this.findTargetNodes(injectorTarget, injectionPoints)) {
            this.addTargetNode(injectorTarget.getTarget(), myNodes, node.insn, node.nominators);
        }
        return myNodes;
    }
    
    protected void addTargetNode(final Target target, final List<InjectionNodes.InjectionNode> myNodes, final AbstractInsnNode node, final Set<InjectionPoint> nominators) {
        myNodes.add(target.addInjectionNode(node));
    }
    
    public final void inject(final Target target, final List<InjectionNodes.InjectionNode> nodes) {
        for (final InjectionNodes.InjectionNode node : nodes) {
            if (node.isRemoved()) {
                if (!this.info.getContext().getOption(MixinEnvironment.Option.DEBUG_VERBOSE)) {
                    continue;
                }
                Injector.logger.warn("Target node for {} was removed by a previous injector in {}", new Object[] { this.info, target });
            }
            else {
                this.inject(target, node);
            }
        }
        for (final InjectionNodes.InjectionNode node : nodes) {
            this.postInject(target, node);
        }
    }
    
    private Collection<TargetNode> findTargetNodes(final InjectorTarget injectorTarget, final List<InjectionPoint> injectionPoints) {
        final MethodNode method = injectorTarget.getMethod();
        final Map<Integer, TargetNode> targetNodes = new TreeMap<Integer, TargetNode>();
        final Collection<AbstractInsnNode> nodes = new ArrayList<AbstractInsnNode>(32);
        for (final InjectionPoint injectionPoint : injectionPoints) {
            nodes.clear();
            if (this.findTargetNodes(method, injectionPoint, injectorTarget.getSlice(injectionPoint), nodes)) {
                for (final AbstractInsnNode insn : nodes) {
                    final Integer key = method.instructions.indexOf(insn);
                    TargetNode targetNode = targetNodes.get(key);
                    if (targetNode == null) {
                        targetNode = new TargetNode(insn);
                        targetNodes.put(key, targetNode);
                    }
                    targetNode.nominators.add(injectionPoint);
                }
            }
        }
        return targetNodes.values();
    }
    
    protected boolean findTargetNodes(final MethodNode into, final InjectionPoint injectionPoint, final InsnList insns, final Collection<AbstractInsnNode> nodes) {
        return injectionPoint.find(into.desc, insns, nodes);
    }
    
    protected void sanityCheck(final Target target, final List<InjectionPoint> injectionPoints) {
        if (target.classNode != this.classNode) {
            throw new InvalidInjectionException(this.info, "Target class does not match injector class in " + this);
        }
    }
    
    protected abstract void inject(final Target p0, final InjectionNodes.InjectionNode p1);
    
    protected void postInject(final Target target, final InjectionNodes.InjectionNode node) {
    }
    
    protected AbstractInsnNode invokeHandler(final InsnList insns) {
        return this.invokeHandler(insns, this.methodNode);
    }
    
    protected AbstractInsnNode invokeHandler(final InsnList insns, final MethodNode handler) {
        final boolean isPrivate = (handler.access & 0x2) != 0x0;
        final int invokeOpcode = this.isStatic ? 184 : (isPrivate ? 183 : 182);
        final MethodInsnNode insn = new MethodInsnNode(invokeOpcode, this.classNode.name, handler.name, handler.desc, false);
        insns.add(insn);
        this.info.addCallbackInvocation(handler);
        return insn;
    }
    
    protected void throwException(final InsnList insns, final String exceptionType, final String message) {
        insns.add(new TypeInsnNode(187, exceptionType));
        insns.add(new InsnNode(89));
        insns.add(new LdcInsnNode(message));
        insns.add(new MethodInsnNode(183, exceptionType, "<init>", "(Ljava/lang/String;)V", false));
        insns.add(new InsnNode(191));
    }
    
    public static boolean canCoerce(final Type from, final Type to) {
        if (from.getSort() == 10 && to.getSort() == 10) {
            return canCoerce(ClassInfo.forType(from), ClassInfo.forType(to));
        }
        return canCoerce(from.getDescriptor(), to.getDescriptor());
    }
    
    public static boolean canCoerce(final String from, final String to) {
        return from.length() <= 1 && to.length() <= 1 && canCoerce(from.charAt(0), to.charAt(0));
    }
    
    public static boolean canCoerce(final char from, final char to) {
        return to == 'I' && "IBSCZ".indexOf(from) > -1;
    }
    
    private static boolean canCoerce(final ClassInfo from, final ClassInfo to) {
        return from != null && to != null && (to == from || to.hasSuperClass(from));
    }
    
    static {
        logger = LogManager.getLogger("mixin");
    }
    
    public static final class TargetNode
    {
        final AbstractInsnNode insn;
        final Set<InjectionPoint> nominators;
        
        TargetNode(final AbstractInsnNode insn) {
            this.nominators = new HashSet<InjectionPoint>();
            this.insn = insn;
        }
        
        public AbstractInsnNode getNode() {
            return this.insn;
        }
        
        public Set<InjectionPoint> getNominators() {
            return Collections.unmodifiableSet((Set<? extends InjectionPoint>)this.nominators);
        }
        
        @Override
        public boolean equals(final Object obj) {
            return obj != null && obj.getClass() == TargetNode.class && ((TargetNode)obj).insn == this.insn;
        }
        
        @Override
        public int hashCode() {
            return this.insn.hashCode();
        }
    }
}

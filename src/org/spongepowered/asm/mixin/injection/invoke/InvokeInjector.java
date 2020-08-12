// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.invoke;

import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import java.util.List;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.code.Injector;

public abstract class InvokeInjector extends Injector
{
    protected final String annotationType;
    
    public InvokeInjector(final InjectionInfo info, final String annotationType) {
        super(info);
        this.annotationType = annotationType;
    }
    
    @Override
    protected void sanityCheck(final Target target, final List<InjectionPoint> injectionPoints) {
        super.sanityCheck(target, injectionPoints);
        this.checkTarget(target);
    }
    
    protected void checkTarget(final Target target) {
        this.checkTargetModifiers(target, true);
    }
    
    protected final void checkTargetModifiers(final Target target, final boolean exactMatch) {
        if (exactMatch && target.isStatic != this.isStatic) {
            throw new InvalidInjectionException(this.info, "'static' modifier of handler method does not match target in " + this);
        }
        if (!exactMatch && !this.isStatic && target.isStatic) {
            throw new InvalidInjectionException(this.info, "non-static callback method " + this + " targets a static method which is not supported");
        }
    }
    
    protected void checkTargetForNode(final Target target, final InjectionNodes.InjectionNode node) {
        if (target.isCtor) {
            final MethodInsnNode superCall = target.findSuperInitNode();
            final int superCallIndex = target.indexOf(superCall);
            final int targetIndex = target.indexOf(node.getCurrentTarget());
            if (targetIndex <= superCallIndex) {
                if (!this.isStatic) {
                    throw new InvalidInjectionException(this.info, "Pre-super " + this.annotationType + " invocation must be static in " + this);
                }
                return;
            }
        }
        this.checkTargetModifiers(target, true);
    }
    
    @Override
    protected void inject(final Target target, final InjectionNodes.InjectionNode node) {
        if (!(node.getCurrentTarget() instanceof MethodInsnNode)) {
            throw new InvalidInjectionException(this.info, this.annotationType + " annotation on is targetting a non-method insn in " + target + " in " + this);
        }
        this.injectAtInvoke(target, node);
    }
    
    protected abstract void injectAtInvoke(final Target p0, final InjectionNodes.InjectionNode p1);
    
    protected AbstractInsnNode invokeHandlerWithArgs(final Type[] args, final InsnList insns, final int[] argMap) {
        return this.invokeHandlerWithArgs(args, insns, argMap, 0, args.length);
    }
    
    protected AbstractInsnNode invokeHandlerWithArgs(final Type[] args, final InsnList insns, final int[] argMap, final int startArg, final int endArg) {
        if (!this.isStatic) {
            insns.add(new VarInsnNode(25, 0));
        }
        this.pushArgs(args, insns, argMap, startArg, endArg);
        return this.invokeHandler(insns);
    }
    
    protected int[] storeArgs(final Target target, final Type[] args, final InsnList insns, final int start) {
        final int[] argMap = target.generateArgMap(args, start);
        this.storeArgs(args, insns, argMap, start, args.length);
        return argMap;
    }
    
    protected void storeArgs(final Type[] args, final InsnList insns, final int[] argMap, final int start, final int end) {
        for (int arg = end - 1; arg >= start; --arg) {
            insns.add(new VarInsnNode(args[arg].getOpcode(54), argMap[arg]));
        }
    }
    
    protected void pushArgs(final Type[] args, final InsnList insns, final int[] argMap, final int start, final int end) {
        for (int arg = start; arg < end; ++arg) {
            insns.add(new VarInsnNode(args[arg].getOpcode(21), argMap[arg]));
        }
    }
}

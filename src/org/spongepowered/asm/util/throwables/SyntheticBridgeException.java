// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.util.throwables;

import java.util.ListIterator;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.lib.Type;
import java.util.Iterator;
import org.spongepowered.asm.util.Bytecode;
import java.lang.annotation.Annotation;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.refmap.IMixinContext;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.mixin.throwables.MixinException;

public class SyntheticBridgeException extends MixinException
{
    private static final long serialVersionUID = 1L;
    private final Problem problem;
    private final String name;
    private final String desc;
    private final int index;
    private final AbstractInsnNode a;
    private final AbstractInsnNode b;
    
    public SyntheticBridgeException(final Problem problem, final String name, final String desc, final int index, final AbstractInsnNode a, final AbstractInsnNode b) {
        super(problem.getMessage(name, desc, index, a, b));
        this.problem = problem;
        this.name = name;
        this.desc = desc;
        this.index = index;
        this.a = a;
        this.b = b;
    }
    
    public void printAnalysis(final IMixinContext context, final MethodNode mda, final MethodNode mdb) {
        final PrettyPrinter printer = new PrettyPrinter();
        printer.addWrapped(100, this.getMessage(), new Object[0]).hr();
        printer.add().kv("Method", (Object)(this.name + this.desc)).kv("Problem Type", this.problem).add().hr();
        final String merged = Annotations.getValue(Annotations.getVisible(mda, MixinMerged.class), "mixin");
        final String owner = (merged != null) ? merged : context.getTargetClassRef().replace('/', '.');
        this.printMethod(printer.add("Existing method").add().kv("Owner", (Object)owner).add(), mda).hr();
        this.printMethod(printer.add("Incoming method").add().kv("Owner", (Object)context.getClassRef().replace('/', '.')).add(), mdb).hr();
        this.printProblem(printer, context, mda, mdb).print(System.err);
    }
    
    private PrettyPrinter printMethod(final PrettyPrinter printer, final MethodNode method) {
        int index = 0;
        final Iterator<AbstractInsnNode> iter = method.instructions.iterator();
        while (iter.hasNext()) {
            printer.kv((index == this.index) ? ">>>>" : "", (Object)Bytecode.describeNode(iter.next()));
            ++index;
        }
        return printer.add();
    }
    
    private PrettyPrinter printProblem(final PrettyPrinter printer, final IMixinContext context, final MethodNode mda, final MethodNode mdb) {
        final Type target = Type.getObjectType(context.getTargetClassRef());
        printer.add("Analysis").add();
        switch (this.problem) {
            case BAD_INSN: {
                printer.add("The bridge methods are not compatible because they contain incompatible opcodes");
                printer.add("at index " + this.index + ":").add();
                printer.kv("Existing opcode: %s", (Object)Bytecode.getOpcodeName(this.a));
                printer.kv("Incoming opcode: %s", (Object)Bytecode.getOpcodeName(this.b)).add();
                printer.add("This implies that the bridge methods are from different interfaces. This problem");
                printer.add("may not be resolvable without changing the base interfaces.").add();
                break;
            }
            case BAD_LOAD: {
                printer.add("The bridge methods are not compatible because they contain different variables at");
                printer.add("opcode index " + this.index + ".").add();
                final ListIterator<AbstractInsnNode> ia = mda.instructions.iterator();
                final ListIterator<AbstractInsnNode> ib = mdb.instructions.iterator();
                final Type[] argsa = Type.getArgumentTypes(mda.desc);
                final Type[] argsb = Type.getArgumentTypes(mdb.desc);
                int index = 0;
                while (ia.hasNext() && ib.hasNext()) {
                    final AbstractInsnNode na = ia.next();
                    final AbstractInsnNode nb = ib.next();
                    if (na instanceof VarInsnNode && nb instanceof VarInsnNode) {
                        final VarInsnNode va = (VarInsnNode)na;
                        final VarInsnNode vb = (VarInsnNode)nb;
                        final Type ta = (va.var > 0) ? argsa[va.var - 1] : target;
                        final Type tb = (vb.var > 0) ? argsb[vb.var - 1] : target;
                        printer.kv("Target " + index, "%8s %-2d %s", Bytecode.getOpcodeName(va), va.var, ta);
                        printer.kv("Incoming " + index, "%8s %-2d %s", Bytecode.getOpcodeName(vb), vb.var, tb);
                        if (ta.equals(tb)) {
                            printer.kv("", "Types match: %s", ta);
                        }
                        else if (ta.getSort() != tb.getSort()) {
                            printer.kv("", (Object)"Types are incompatible");
                        }
                        else if (ta.getSort() == 10) {
                            final ClassInfo superClass = ClassInfo.getCommonSuperClassOrInterface(ta, tb);
                            printer.kv("", "Common supertype: %s", superClass);
                        }
                        printer.add();
                    }
                    ++index;
                }
                printer.add("Since this probably means that the methods come from different interfaces, you");
                printer.add("may have a \"multiple inheritance\" problem, it may not be possible to implement");
                printer.add("both root interfaces");
                break;
            }
            case BAD_CAST: {
                printer.add("Incompatible CHECKCAST encountered at opcode " + this.index + ", this could indicate that the bridge");
                printer.add("is casting down for contravariant generic types. It may be possible to coalesce the");
                printer.add("bridges by adjusting the types in the target method.").add();
                final Type ta2 = Type.getObjectType(((TypeInsnNode)this.a).desc);
                final Type tb2 = Type.getObjectType(((TypeInsnNode)this.b).desc);
                printer.kv("Target type", ta2);
                printer.kv("Incoming type", tb2);
                printer.kv("Common supertype", ClassInfo.getCommonSuperClassOrInterface(ta2, tb2)).add();
                break;
            }
            case BAD_INVOKE_NAME: {
                printer.add("Incompatible invocation targets in synthetic bridge. This is extremely unusual");
                printer.add("and implies that a remapping transformer has incorrectly remapped a method. This");
                printer.add("is an unrecoverable error.");
                break;
            }
            case BAD_INVOKE_DESC: {
                final MethodInsnNode mdna = (MethodInsnNode)this.a;
                final MethodInsnNode mdnb = (MethodInsnNode)this.b;
                final Type[] arga = Type.getArgumentTypes(mdna.desc);
                final Type[] argb = Type.getArgumentTypes(mdnb.desc);
                if (arga.length != argb.length) {
                    final int argCount = Type.getArgumentTypes(mda.desc).length;
                    final String winner = (arga.length == argCount) ? "The TARGET" : ((argb.length == argCount) ? " The INCOMING" : "NEITHER");
                    printer.add("Mismatched invocation descriptors in synthetic bridge implies that a remapping");
                    printer.add("transformer has incorrectly coalesced a bridge method with a conflicting name.");
                    printer.add("Overlapping bridge methods should always have the same number of arguments, yet");
                    printer.add("the target method has %d arguments, the incoming method has %d. This is an", arga.length, argb.length);
                    printer.add("unrecoverable error. %s method has the expected arg count of %d", winner, argCount);
                    break;
                }
                final Type rta = Type.getReturnType(mdna.desc);
                final Type rtb = Type.getReturnType(mdnb.desc);
                printer.add("Incompatible invocation descriptors in synthetic bridge implies that generified");
                printer.add("types are incompatible over one or more generic superclasses or interfaces. It may");
                printer.add("be possible to adjust the generic types on implemented members to rectify this");
                printer.add("problem by coalescing the appropriate generic types.").add();
                this.printTypeComparison(printer, "return type", rta, rtb);
                for (int i = 0; i < arga.length; ++i) {
                    this.printTypeComparison(printer, "arg " + i, arga[i], argb[i]);
                }
                break;
            }
            case BAD_LENGTH: {
                printer.add("Mismatched bridge method length implies the bridge methods are incompatible");
                printer.add("and may originate from different superinterfaces. This is an unrecoverable");
                printer.add("error.").add();
                break;
            }
        }
        return printer;
    }
    
    private PrettyPrinter printTypeComparison(final PrettyPrinter printer, final String index, final Type tpa, final Type tpb) {
        printer.kv("Target " + index, "%s", tpa);
        printer.kv("Incoming " + index, "%s", tpb);
        if (tpa.equals(tpb)) {
            printer.kv("Analysis", "Types match: %s", tpa);
        }
        else if (tpa.getSort() != tpb.getSort()) {
            printer.kv("Analysis", (Object)"Types are incompatible");
        }
        else if (tpa.getSort() == 10) {
            final ClassInfo superClass = ClassInfo.getCommonSuperClassOrInterface(tpa, tpb);
            printer.kv("Analysis", "Common supertype: L%s;", superClass);
        }
        return printer.add();
    }
    
    public enum Problem
    {
        BAD_INSN("Conflicting opcodes %4$s and %5$s at offset %3$d in synthetic bridge method %1$s%2$s"), 
        BAD_LOAD("Conflicting variable access at offset %3$d in synthetic bridge method %1$s%2$s"), 
        BAD_CAST("Conflicting type cast at offset %3$d in synthetic bridge method %1$s%2$s"), 
        BAD_INVOKE_NAME("Conflicting synthetic bridge target method name in synthetic bridge method %1$s%2$s Existing:%6$s Incoming:%7$s"), 
        BAD_INVOKE_DESC("Conflicting synthetic bridge target method descriptor in synthetic bridge method %1$s%2$s Existing:%8$s Incoming:%9$s"), 
        BAD_LENGTH("Mismatched bridge method length for synthetic bridge method %1$s%2$s unexpected extra opcode at offset %3$d");
        
        private final String message;
        
        private Problem(final String message) {
            this.message = message;
        }
        
        String getMessage(final String name, final String desc, final int index, final AbstractInsnNode a, final AbstractInsnNode b) {
            return String.format(this.message, name, desc, index, Bytecode.getOpcodeName(a), Bytecode.getOpcodeName(a), getInsnName(a), getInsnName(b), getInsnDesc(a), getInsnDesc(b));
        }
        
        private static String getInsnName(final AbstractInsnNode node) {
            return (node instanceof MethodInsnNode) ? ((MethodInsnNode)node).name : "";
        }
        
        private static String getInsnDesc(final AbstractInsnNode node) {
            return (node instanceof MethodInsnNode) ? ((MethodInsnNode)node).desc : "";
        }
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.util;

import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Overwrite;
import java.util.ListIterator;
import org.spongepowered.asm.util.throwables.SyntheticBridgeException;
import java.util.ArrayList;
import java.util.List;
import com.google.common.primitives.Ints;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import java.lang.annotation.Annotation;
import com.google.common.base.Joiner;
import java.util.HashMap;
import java.util.Map;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.FieldNode;
import java.lang.reflect.Field;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.lib.tree.FrameNode;
import org.spongepowered.asm.lib.tree.IntInsnNode;
import org.spongepowered.asm.lib.tree.LdcInsnNode;
import org.spongepowered.asm.lib.tree.LineNumberNode;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.lib.tree.JumpInsnNode;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.lib.util.CheckClassAdapter;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.ClassWriter;
import org.spongepowered.asm.lib.MethodVisitor;
import org.spongepowered.asm.lib.ClassVisitor;
import org.spongepowered.asm.lib.util.TraceClassVisitor;
import java.io.PrintWriter;
import java.io.OutputStream;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import java.util.Iterator;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.apache.logging.log4j.Logger;
import java.util.regex.Pattern;

public final class Bytecode
{
    public static final int[] CONSTANTS_INT;
    public static final int[] CONSTANTS_FLOAT;
    public static final int[] CONSTANTS_DOUBLE;
    public static final int[] CONSTANTS_LONG;
    public static final int[] CONSTANTS_ALL;
    private static final Object[] CONSTANTS_VALUES;
    private static final String[] CONSTANTS_TYPES;
    private static final String[] BOXING_TYPES;
    private static final String[] UNBOXING_METHODS;
    private static final Class<?>[] MERGEABLE_MIXIN_ANNOTATIONS;
    private static Pattern mergeableAnnotationPattern;
    private static final Logger logger;
    
    private Bytecode() {
    }
    
    public static MethodNode findMethod(final ClassNode classNode, final String name, final String desc) {
        for (final MethodNode method : classNode.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                return method;
            }
        }
        return null;
    }
    
    public static AbstractInsnNode findInsn(final MethodNode method, final int opcode) {
        for (final AbstractInsnNode insn : method.instructions) {
            if (insn.getOpcode() == opcode) {
                return insn;
            }
        }
        return null;
    }
    
    public static MethodInsnNode findSuperInit(final MethodNode method, final String superName) {
        if (!"<init>".equals(method.name)) {
            return null;
        }
        int news = 0;
        for (final AbstractInsnNode insn : method.instructions) {
            if (insn instanceof TypeInsnNode && insn.getOpcode() == 187) {
                ++news;
            }
            else {
                if (!(insn instanceof MethodInsnNode) || insn.getOpcode() != 183) {
                    continue;
                }
                final MethodInsnNode methodNode = (MethodInsnNode)insn;
                if (!"<init>".equals(methodNode.name)) {
                    continue;
                }
                if (news > 0) {
                    --news;
                }
                else {
                    if (methodNode.owner.equals(superName)) {
                        return methodNode;
                    }
                    continue;
                }
            }
        }
        return null;
    }
    
    public static void textify(final ClassNode classNode, final OutputStream out) {
        classNode.accept(new TraceClassVisitor(new PrintWriter(out)));
    }
    
    public static void textify(final MethodNode methodNode, final OutputStream out) {
        final TraceClassVisitor trace = new TraceClassVisitor(new PrintWriter(out));
        final MethodVisitor mv = trace.visitMethod(methodNode.access, methodNode.name, methodNode.desc, methodNode.signature, methodNode.exceptions.toArray(new String[0]));
        methodNode.accept(mv);
        trace.visitEnd();
    }
    
    public static void dumpClass(final ClassNode classNode) {
        final ClassWriter cw = new ClassWriter(3);
        classNode.accept(cw);
        dumpClass(cw.toByteArray());
    }
    
    public static void dumpClass(final byte[] bytes) {
        final ClassReader cr = new ClassReader(bytes);
        CheckClassAdapter.verify(cr, true, new PrintWriter(System.out));
    }
    
    public static void printMethodWithOpcodeIndices(final MethodNode method) {
        System.err.printf("%s%s\n", method.name, method.desc);
        int i = 0;
        final Iterator<AbstractInsnNode> iter = method.instructions.iterator();
        while (iter.hasNext()) {
            System.err.printf("[%4d] %s\n", i++, describeNode(iter.next()));
        }
    }
    
    public static void printMethod(final MethodNode method) {
        System.err.printf("%s%s\n", method.name, method.desc);
        final Iterator<AbstractInsnNode> iter = method.instructions.iterator();
        while (iter.hasNext()) {
            System.err.print("  ");
            printNode(iter.next());
        }
    }
    
    public static void printNode(final AbstractInsnNode node) {
        System.err.printf("%s\n", describeNode(node));
    }
    
    public static String describeNode(final AbstractInsnNode node) {
        if (node == null) {
            return String.format("   %-14s ", "null");
        }
        if (node instanceof LabelNode) {
            return String.format("[%s]", ((LabelNode)node).getLabel());
        }
        String out = String.format("   %-14s ", node.getClass().getSimpleName().replace("Node", ""));
        if (node instanceof JumpInsnNode) {
            out += String.format("[%s] [%s]", getOpcodeName(node), ((JumpInsnNode)node).label.getLabel());
        }
        else if (node instanceof VarInsnNode) {
            out += String.format("[%s] %d", getOpcodeName(node), ((VarInsnNode)node).var);
        }
        else if (node instanceof MethodInsnNode) {
            final MethodInsnNode mth = (MethodInsnNode)node;
            out += String.format("[%s] %s %s %s", getOpcodeName(node), mth.owner, mth.name, mth.desc);
        }
        else if (node instanceof FieldInsnNode) {
            final FieldInsnNode fld = (FieldInsnNode)node;
            out += String.format("[%s] %s %s %s", getOpcodeName(node), fld.owner, fld.name, fld.desc);
        }
        else if (node instanceof LineNumberNode) {
            final LineNumberNode ln = (LineNumberNode)node;
            out += String.format("LINE=[%d] LABEL=[%s]", ln.line, ln.start.getLabel());
        }
        else if (node instanceof LdcInsnNode) {
            out += ((LdcInsnNode)node).cst;
        }
        else if (node instanceof IntInsnNode) {
            out += ((IntInsnNode)node).operand;
        }
        else if (node instanceof FrameNode) {
            out += String.format("[%s] ", getOpcodeName(((FrameNode)node).type, "H_INVOKEINTERFACE", -1));
        }
        else {
            out += String.format("[%s] ", getOpcodeName(node));
        }
        return out;
    }
    
    public static String getOpcodeName(final AbstractInsnNode node) {
        return (node != null) ? getOpcodeName(node.getOpcode()) : "";
    }
    
    public static String getOpcodeName(final int opcode) {
        return getOpcodeName(opcode, "UNINITIALIZED_THIS", 1);
    }
    
    private static String getOpcodeName(final int opcode, final String start, final int min) {
        if (opcode >= min) {
            boolean found = false;
            try {
                for (final Field f : Opcodes.class.getDeclaredFields()) {
                    if (found || f.getName().equals(start)) {
                        found = true;
                        if (f.getType() == Integer.TYPE && f.getInt(null) == opcode) {
                            return f.getName();
                        }
                    }
                }
            }
            catch (Exception ex) {}
        }
        return (opcode >= 0) ? String.valueOf(opcode) : "UNKNOWN";
    }
    
    public static boolean methodHasLineNumbers(final MethodNode method) {
        final Iterator<AbstractInsnNode> iter = method.instructions.iterator();
        while (iter.hasNext()) {
            if (iter.next() instanceof LineNumberNode) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean methodIsStatic(final MethodNode method) {
        return (method.access & 0x8) == 0x8;
    }
    
    public static boolean fieldIsStatic(final FieldNode field) {
        return (field.access & 0x8) == 0x8;
    }
    
    public static int getFirstNonArgLocalIndex(final MethodNode method) {
        return getFirstNonArgLocalIndex(Type.getArgumentTypes(method.desc), (method.access & 0x8) == 0x0);
    }
    
    public static int getFirstNonArgLocalIndex(final Type[] args, final boolean includeThis) {
        return getArgsSize(args) + (includeThis ? 1 : 0);
    }
    
    public static int getArgsSize(final Type[] args) {
        int size = 0;
        for (final Type type : args) {
            size += type.getSize();
        }
        return size;
    }
    
    public static void loadArgs(final Type[] args, final InsnList insns, final int pos) {
        loadArgs(args, insns, pos, -1);
    }
    
    public static void loadArgs(final Type[] args, final InsnList insns, final int start, final int end) {
        loadArgs(args, insns, start, end, null);
    }
    
    public static void loadArgs(final Type[] args, final InsnList insns, final int start, final int end, final Type[] casts) {
        int pos = start;
        int index = 0;
        for (final Type type : args) {
            insns.add(new VarInsnNode(type.getOpcode(21), pos));
            if (casts != null && index < casts.length && casts[index] != null) {
                insns.add(new TypeInsnNode(192, casts[index].getInternalName()));
            }
            pos += type.getSize();
            if (end >= start && pos >= end) {
                return;
            }
            ++index;
        }
    }
    
    public static Map<LabelNode, LabelNode> cloneLabels(final InsnList source) {
        final Map<LabelNode, LabelNode> labels = new HashMap<LabelNode, LabelNode>();
        for (final AbstractInsnNode insn : source) {
            if (insn instanceof LabelNode) {
                labels.put((LabelNode)insn, new LabelNode(((LabelNode)insn).getLabel()));
            }
        }
        return labels;
    }
    
    public static String generateDescriptor(final Object returnType, final Object... args) {
        final StringBuilder sb = new StringBuilder().append('(');
        for (final Object arg : args) {
            sb.append(toDescriptor(arg));
        }
        return sb.append(')').append((returnType != null) ? toDescriptor(returnType) : "V").toString();
    }
    
    private static String toDescriptor(final Object arg) {
        if (arg instanceof String) {
            return (String)arg;
        }
        if (arg instanceof Type) {
            return arg.toString();
        }
        if (arg instanceof Class) {
            return Type.getDescriptor((Class<?>)arg);
        }
        return (arg == null) ? "" : arg.toString();
    }
    
    public static String getDescriptor(final Type[] args) {
        return "(" + Joiner.on("").join((Object[])args) + ")";
    }
    
    public static String getDescriptor(final Type[] args, final Type returnType) {
        return getDescriptor(args) + returnType.toString();
    }
    
    public static String changeDescriptorReturnType(final String desc, final String returnType) {
        if (desc == null) {
            return null;
        }
        if (returnType == null) {
            return desc;
        }
        return desc.substring(0, desc.lastIndexOf(41) + 1) + returnType;
    }
    
    public static String getSimpleName(final Class<? extends Annotation> annotationType) {
        return annotationType.getSimpleName();
    }
    
    public static String getSimpleName(final AnnotationNode annotation) {
        return getSimpleName(annotation.desc);
    }
    
    public static String getSimpleName(final String desc) {
        final int pos = Math.max(desc.lastIndexOf(47), 0);
        return desc.substring(pos + 1).replace(";", "");
    }
    
    public static boolean isConstant(final AbstractInsnNode insn) {
        return insn != null && Ints.contains(Bytecode.CONSTANTS_ALL, insn.getOpcode());
    }
    
    public static Object getConstant(final AbstractInsnNode insn) {
        if (insn == null) {
            return null;
        }
        if (insn instanceof LdcInsnNode) {
            return ((LdcInsnNode)insn).cst;
        }
        if (!(insn instanceof IntInsnNode)) {
            final int index = Ints.indexOf(Bytecode.CONSTANTS_ALL, insn.getOpcode());
            return (index < 0) ? null : Bytecode.CONSTANTS_VALUES[index];
        }
        final int value = ((IntInsnNode)insn).operand;
        if (insn.getOpcode() == 16 || insn.getOpcode() == 17) {
            return value;
        }
        throw new IllegalArgumentException("IntInsnNode with invalid opcode " + insn.getOpcode() + " in getConstant");
    }
    
    public static Type getConstantType(final AbstractInsnNode insn) {
        if (insn == null) {
            return null;
        }
        if (!(insn instanceof LdcInsnNode)) {
            final int index = Ints.indexOf(Bytecode.CONSTANTS_ALL, insn.getOpcode());
            return (index < 0) ? null : Type.getType(Bytecode.CONSTANTS_TYPES[index]);
        }
        final Object cst = ((LdcInsnNode)insn).cst;
        if (cst instanceof Integer) {
            return Type.getType("I");
        }
        if (cst instanceof Float) {
            return Type.getType("F");
        }
        if (cst instanceof Long) {
            return Type.getType("J");
        }
        if (cst instanceof Double) {
            return Type.getType("D");
        }
        if (cst instanceof String) {
            return Type.getType("Ljava/lang/String;");
        }
        if (cst instanceof Type) {
            return Type.getType("Ljava/lang/Class;");
        }
        throw new IllegalArgumentException("LdcInsnNode with invalid payload type " + cst.getClass() + " in getConstant");
    }
    
    public static boolean hasFlag(final ClassNode classNode, final int flag) {
        return (classNode.access & flag) == flag;
    }
    
    public static boolean hasFlag(final MethodNode method, final int flag) {
        return (method.access & flag) == flag;
    }
    
    public static boolean hasFlag(final FieldNode field, final int flag) {
        return (field.access & flag) == flag;
    }
    
    public static boolean compareFlags(final MethodNode m1, final MethodNode m2, final int flag) {
        return hasFlag(m1, flag) == hasFlag(m2, flag);
    }
    
    public static boolean compareFlags(final FieldNode f1, final FieldNode f2, final int flag) {
        return hasFlag(f1, flag) == hasFlag(f2, flag);
    }
    
    public static Visibility getVisibility(final MethodNode method) {
        return getVisibility(method.access & 0x7);
    }
    
    public static Visibility getVisibility(final FieldNode field) {
        return getVisibility(field.access & 0x7);
    }
    
    private static Visibility getVisibility(final int flags) {
        if ((flags & 0x4) != 0x0) {
            return Visibility.PROTECTED;
        }
        if ((flags & 0x2) != 0x0) {
            return Visibility.PRIVATE;
        }
        if ((flags & 0x1) != 0x0) {
            return Visibility.PUBLIC;
        }
        return Visibility.PACKAGE;
    }
    
    public static void setVisibility(final MethodNode method, final Visibility visibility) {
        method.access = setVisibility(method.access, visibility.access);
    }
    
    public static void setVisibility(final FieldNode field, final Visibility visibility) {
        field.access = setVisibility(field.access, visibility.access);
    }
    
    public static void setVisibility(final MethodNode method, final int access) {
        method.access = setVisibility(method.access, access);
    }
    
    public static void setVisibility(final FieldNode field, final int access) {
        field.access = setVisibility(field.access, access);
    }
    
    private static int setVisibility(final int oldAccess, final int newAccess) {
        return (oldAccess & 0xFFFFFFF8) | (newAccess & 0x7);
    }
    
    public static int getMaxLineNumber(final ClassNode classNode, final int min, final int pad) {
        int max = 0;
        for (final MethodNode method : classNode.methods) {
            for (final AbstractInsnNode insn : method.instructions) {
                if (insn instanceof LineNumberNode) {
                    max = Math.max(max, ((LineNumberNode)insn).line);
                }
            }
        }
        return Math.max(min, max + pad);
    }
    
    public static String getBoxingType(final Type type) {
        return (type == null) ? null : Bytecode.BOXING_TYPES[type.getSort()];
    }
    
    public static String getUnboxingMethod(final Type type) {
        return (type == null) ? null : Bytecode.UNBOXING_METHODS[type.getSort()];
    }
    
    public static void mergeAnnotations(final ClassNode from, final ClassNode to) {
        to.visibleAnnotations = mergeAnnotations(from.visibleAnnotations, to.visibleAnnotations, "class", from.name);
        to.invisibleAnnotations = mergeAnnotations(from.invisibleAnnotations, to.invisibleAnnotations, "class", from.name);
    }
    
    public static void mergeAnnotations(final MethodNode from, final MethodNode to) {
        to.visibleAnnotations = mergeAnnotations(from.visibleAnnotations, to.visibleAnnotations, "method", from.name);
        to.invisibleAnnotations = mergeAnnotations(from.invisibleAnnotations, to.invisibleAnnotations, "method", from.name);
    }
    
    public static void mergeAnnotations(final FieldNode from, final FieldNode to) {
        to.visibleAnnotations = mergeAnnotations(from.visibleAnnotations, to.visibleAnnotations, "field", from.name);
        to.invisibleAnnotations = mergeAnnotations(from.invisibleAnnotations, to.invisibleAnnotations, "field", from.name);
    }
    
    private static List<AnnotationNode> mergeAnnotations(final List<AnnotationNode> from, List<AnnotationNode> to, final String type, final String name) {
        try {
            if (from == null) {
                return to;
            }
            if (to == null) {
                to = new ArrayList<AnnotationNode>();
            }
            for (final AnnotationNode annotation : from) {
                if (!isMergeableAnnotation(annotation)) {
                    continue;
                }
                final Iterator<AnnotationNode> iter = to.iterator();
                while (iter.hasNext()) {
                    if (iter.next().desc.equals(annotation.desc)) {
                        iter.remove();
                        break;
                    }
                }
                to.add(annotation);
            }
        }
        catch (Exception ex) {
            Bytecode.logger.warn("Exception encountered whilst merging annotations for {} {}", new Object[] { type, name });
        }
        return to;
    }
    
    private static boolean isMergeableAnnotation(final AnnotationNode annotation) {
        return !annotation.desc.startsWith("L" + Constants.MIXIN_PACKAGE_REF) || Bytecode.mergeableAnnotationPattern.matcher(annotation.desc).matches();
    }
    
    private static Pattern getMergeableAnnotationPattern() {
        final StringBuilder sb = new StringBuilder("^L(");
        for (int i = 0; i < Bytecode.MERGEABLE_MIXIN_ANNOTATIONS.length; ++i) {
            if (i > 0) {
                sb.append('|');
            }
            sb.append(Bytecode.MERGEABLE_MIXIN_ANNOTATIONS[i].getName().replace('.', '/'));
        }
        return Pattern.compile(sb.append(");$").toString());
    }
    
    public static void compareBridgeMethods(final MethodNode a, final MethodNode b) {
        final ListIterator<AbstractInsnNode> ia = a.instructions.iterator();
        final ListIterator<AbstractInsnNode> ib = b.instructions.iterator();
        int index = 0;
        while (ia.hasNext() && ib.hasNext()) {
            final AbstractInsnNode na = ia.next();
            final AbstractInsnNode nb = ib.next();
            if (!(na instanceof LabelNode)) {
                if (na instanceof MethodInsnNode) {
                    final MethodInsnNode ma = (MethodInsnNode)na;
                    final MethodInsnNode mb = (MethodInsnNode)nb;
                    if (!ma.name.equals(mb.name)) {
                        throw new SyntheticBridgeException(SyntheticBridgeException.Problem.BAD_INVOKE_NAME, a.name, a.desc, index, na, nb);
                    }
                    if (!ma.desc.equals(mb.desc)) {
                        throw new SyntheticBridgeException(SyntheticBridgeException.Problem.BAD_INVOKE_DESC, a.name, a.desc, index, na, nb);
                    }
                }
                else {
                    if (na.getOpcode() != nb.getOpcode()) {
                        throw new SyntheticBridgeException(SyntheticBridgeException.Problem.BAD_INSN, a.name, a.desc, index, na, nb);
                    }
                    if (na instanceof VarInsnNode) {
                        final VarInsnNode va = (VarInsnNode)na;
                        final VarInsnNode vb = (VarInsnNode)nb;
                        if (va.var != vb.var) {
                            throw new SyntheticBridgeException(SyntheticBridgeException.Problem.BAD_LOAD, a.name, a.desc, index, na, nb);
                        }
                    }
                    else if (na instanceof TypeInsnNode) {
                        final TypeInsnNode ta = (TypeInsnNode)na;
                        final TypeInsnNode tb = (TypeInsnNode)nb;
                        if (ta.getOpcode() == 192 && !ta.desc.equals(tb.desc)) {
                            throw new SyntheticBridgeException(SyntheticBridgeException.Problem.BAD_CAST, a.name, a.desc, index, na, nb);
                        }
                    }
                }
            }
            ++index;
        }
        if (ia.hasNext() || ib.hasNext()) {
            throw new SyntheticBridgeException(SyntheticBridgeException.Problem.BAD_LENGTH, a.name, a.desc, index, null, null);
        }
    }
    
    static {
        CONSTANTS_INT = new int[] { 2, 3, 4, 5, 6, 7, 8 };
        CONSTANTS_FLOAT = new int[] { 11, 12, 13 };
        CONSTANTS_DOUBLE = new int[] { 14, 15 };
        CONSTANTS_LONG = new int[] { 9, 10 };
        CONSTANTS_ALL = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18 };
        CONSTANTS_VALUES = new Object[] { null, -1, 0, 1, 2, 3, 4, 5, 0L, 1L, 0.0f, 1.0f, 2.0f, 0.0, 1.0 };
        CONSTANTS_TYPES = new String[] { null, "I", "I", "I", "I", "I", "I", "I", "J", "J", "F", "F", "F", "D", "D", "I", "I" };
        BOXING_TYPES = new String[] { null, "java/lang/Boolean", "java/lang/Character", "java/lang/Byte", "java/lang/Short", "java/lang/Integer", "java/lang/Float", "java/lang/Long", "java/lang/Double", null, null, null };
        UNBOXING_METHODS = new String[] { null, "booleanValue", "charValue", "byteValue", "shortValue", "intValue", "floatValue", "longValue", "doubleValue", null, null, null };
        MERGEABLE_MIXIN_ANNOTATIONS = new Class[] { Overwrite.class, Intrinsic.class, Final.class, Debug.class };
        Bytecode.mergeableAnnotationPattern = getMergeableAnnotationPattern();
        logger = LogManager.getLogger("mixin");
    }
    
    public enum Visibility
    {
        PRIVATE(2), 
        PROTECTED(4), 
        PACKAGE(0), 
        PUBLIC(1);
        
        static final int MASK = 7;
        final int access;
        
        private Visibility(final int access) {
            this.access = access;
        }
    }
}

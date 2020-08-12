// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.tree;

import org.spongepowered.asm.lib.ClassVisitor;
import org.spongepowered.asm.lib.Label;
import org.spongepowered.asm.lib.Handle;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.TypePath;
import org.spongepowered.asm.lib.AnnotationVisitor;
import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import org.spongepowered.asm.lib.Attribute;
import java.util.List;
import org.spongepowered.asm.lib.MethodVisitor;

public class MethodNode extends MethodVisitor
{
    public int access;
    public String name;
    public String desc;
    public String signature;
    public List<String> exceptions;
    public List<ParameterNode> parameters;
    public List<AnnotationNode> visibleAnnotations;
    public List<AnnotationNode> invisibleAnnotations;
    public List<TypeAnnotationNode> visibleTypeAnnotations;
    public List<TypeAnnotationNode> invisibleTypeAnnotations;
    public List<Attribute> attrs;
    public Object annotationDefault;
    public List<AnnotationNode>[] visibleParameterAnnotations;
    public List<AnnotationNode>[] invisibleParameterAnnotations;
    public InsnList instructions;
    public List<TryCatchBlockNode> tryCatchBlocks;
    public int maxStack;
    public int maxLocals;
    public List<LocalVariableNode> localVariables;
    public List<LocalVariableAnnotationNode> visibleLocalVariableAnnotations;
    public List<LocalVariableAnnotationNode> invisibleLocalVariableAnnotations;
    private boolean visited;
    
    public MethodNode() {
        this(327680);
        if (this.getClass() != MethodNode.class) {
            throw new IllegalStateException();
        }
    }
    
    public MethodNode(final int api) {
        super(api);
        this.instructions = new InsnList();
    }
    
    public MethodNode(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        this(327680, access, name, desc, signature, exceptions);
        if (this.getClass() != MethodNode.class) {
            throw new IllegalStateException();
        }
    }
    
    public MethodNode(final int api, final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        super(api);
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.signature = signature;
        this.exceptions = new ArrayList<String>((exceptions == null) ? 0 : exceptions.length);
        final boolean isAbstract = (access & 0x400) != 0x0;
        if (!isAbstract) {
            this.localVariables = new ArrayList<LocalVariableNode>(5);
        }
        this.tryCatchBlocks = new ArrayList<TryCatchBlockNode>();
        if (exceptions != null) {
            this.exceptions.addAll(Arrays.asList(exceptions));
        }
        this.instructions = new InsnList();
    }
    
    @Override
    public void visitParameter(final String name, final int access) {
        if (this.parameters == null) {
            this.parameters = new ArrayList<ParameterNode>(5);
        }
        this.parameters.add(new ParameterNode(name, access));
    }
    
    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return new AnnotationNode(new ArrayList<Object>(0) {
            @Override
            public boolean add(final Object o) {
                MethodNode.this.annotationDefault = o;
                return super.add(o);
            }
        });
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        final AnnotationNode an = new AnnotationNode(desc);
        if (visible) {
            if (this.visibleAnnotations == null) {
                this.visibleAnnotations = new ArrayList<AnnotationNode>(1);
            }
            this.visibleAnnotations.add(an);
        }
        else {
            if (this.invisibleAnnotations == null) {
                this.invisibleAnnotations = new ArrayList<AnnotationNode>(1);
            }
            this.invisibleAnnotations.add(an);
        }
        return an;
    }
    
    @Override
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        final TypeAnnotationNode an = new TypeAnnotationNode(typeRef, typePath, desc);
        if (visible) {
            if (this.visibleTypeAnnotations == null) {
                this.visibleTypeAnnotations = new ArrayList<TypeAnnotationNode>(1);
            }
            this.visibleTypeAnnotations.add(an);
        }
        else {
            if (this.invisibleTypeAnnotations == null) {
                this.invisibleTypeAnnotations = new ArrayList<TypeAnnotationNode>(1);
            }
            this.invisibleTypeAnnotations.add(an);
        }
        return an;
    }
    
    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
        final AnnotationNode an = new AnnotationNode(desc);
        if (visible) {
            if (this.visibleParameterAnnotations == null) {
                final int params = Type.getArgumentTypes(this.desc).length;
                this.visibleParameterAnnotations = (List<AnnotationNode>[])new List[params];
            }
            if (this.visibleParameterAnnotations[parameter] == null) {
                this.visibleParameterAnnotations[parameter] = new ArrayList<AnnotationNode>(1);
            }
            this.visibleParameterAnnotations[parameter].add(an);
        }
        else {
            if (this.invisibleParameterAnnotations == null) {
                final int params = Type.getArgumentTypes(this.desc).length;
                this.invisibleParameterAnnotations = (List<AnnotationNode>[])new List[params];
            }
            if (this.invisibleParameterAnnotations[parameter] == null) {
                this.invisibleParameterAnnotations[parameter] = new ArrayList<AnnotationNode>(1);
            }
            this.invisibleParameterAnnotations[parameter].add(an);
        }
        return an;
    }
    
    @Override
    public void visitAttribute(final Attribute attr) {
        if (this.attrs == null) {
            this.attrs = new ArrayList<Attribute>(1);
        }
        this.attrs.add(attr);
    }
    
    @Override
    public void visitCode() {
    }
    
    @Override
    public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
        this.instructions.add(new FrameNode(type, nLocal, (Object[])((local == null) ? null : this.getLabelNodes(local)), nStack, (Object[])((stack == null) ? null : this.getLabelNodes(stack))));
    }
    
    @Override
    public void visitInsn(final int opcode) {
        this.instructions.add(new InsnNode(opcode));
    }
    
    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        this.instructions.add(new IntInsnNode(opcode, operand));
    }
    
    @Override
    public void visitVarInsn(final int opcode, final int var) {
        this.instructions.add(new VarInsnNode(opcode, var));
    }
    
    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        this.instructions.add(new TypeInsnNode(opcode, type));
    }
    
    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        this.instructions.add(new FieldInsnNode(opcode, owner, name, desc));
    }
    
    @Deprecated
    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
        if (this.api >= 327680) {
            super.visitMethodInsn(opcode, owner, name, desc);
            return;
        }
        this.instructions.add(new MethodInsnNode(opcode, owner, name, desc));
    }
    
    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf) {
        if (this.api < 327680) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }
        this.instructions.add(new MethodInsnNode(opcode, owner, name, desc, itf));
    }
    
    @Override
    public void visitInvokeDynamicInsn(final String name, final String desc, final Handle bsm, final Object... bsmArgs) {
        this.instructions.add(new InvokeDynamicInsnNode(name, desc, bsm, bsmArgs));
    }
    
    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        this.instructions.add(new JumpInsnNode(opcode, this.getLabelNode(label)));
    }
    
    @Override
    public void visitLabel(final Label label) {
        this.instructions.add(this.getLabelNode(label));
    }
    
    @Override
    public void visitLdcInsn(final Object cst) {
        this.instructions.add(new LdcInsnNode(cst));
    }
    
    @Override
    public void visitIincInsn(final int var, final int increment) {
        this.instructions.add(new IincInsnNode(var, increment));
    }
    
    @Override
    public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
        this.instructions.add(new TableSwitchInsnNode(min, max, this.getLabelNode(dflt), this.getLabelNodes(labels)));
    }
    
    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
        this.instructions.add(new LookupSwitchInsnNode(this.getLabelNode(dflt), keys, this.getLabelNodes(labels)));
    }
    
    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        this.instructions.add(new MultiANewArrayInsnNode(desc, dims));
    }
    
    @Override
    public AnnotationVisitor visitInsnAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        AbstractInsnNode insn;
        for (insn = this.instructions.getLast(); insn.getOpcode() == -1; insn = insn.getPrevious()) {}
        final TypeAnnotationNode an = new TypeAnnotationNode(typeRef, typePath, desc);
        if (visible) {
            if (insn.visibleTypeAnnotations == null) {
                insn.visibleTypeAnnotations = new ArrayList<TypeAnnotationNode>(1);
            }
            insn.visibleTypeAnnotations.add(an);
        }
        else {
            if (insn.invisibleTypeAnnotations == null) {
                insn.invisibleTypeAnnotations = new ArrayList<TypeAnnotationNode>(1);
            }
            insn.invisibleTypeAnnotations.add(an);
        }
        return an;
    }
    
    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        this.tryCatchBlocks.add(new TryCatchBlockNode(this.getLabelNode(start), this.getLabelNode(end), this.getLabelNode(handler), type));
    }
    
    @Override
    public AnnotationVisitor visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        final TryCatchBlockNode tcb = this.tryCatchBlocks.get((typeRef & 0xFFFF00) >> 8);
        final TypeAnnotationNode an = new TypeAnnotationNode(typeRef, typePath, desc);
        if (visible) {
            if (tcb.visibleTypeAnnotations == null) {
                tcb.visibleTypeAnnotations = new ArrayList<TypeAnnotationNode>(1);
            }
            tcb.visibleTypeAnnotations.add(an);
        }
        else {
            if (tcb.invisibleTypeAnnotations == null) {
                tcb.invisibleTypeAnnotations = new ArrayList<TypeAnnotationNode>(1);
            }
            tcb.invisibleTypeAnnotations.add(an);
        }
        return an;
    }
    
    @Override
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
        this.localVariables.add(new LocalVariableNode(name, desc, signature, this.getLabelNode(start), this.getLabelNode(end), index));
    }
    
    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(final int typeRef, final TypePath typePath, final Label[] start, final Label[] end, final int[] index, final String desc, final boolean visible) {
        final LocalVariableAnnotationNode an = new LocalVariableAnnotationNode(typeRef, typePath, this.getLabelNodes(start), this.getLabelNodes(end), index, desc);
        if (visible) {
            if (this.visibleLocalVariableAnnotations == null) {
                this.visibleLocalVariableAnnotations = new ArrayList<LocalVariableAnnotationNode>(1);
            }
            this.visibleLocalVariableAnnotations.add(an);
        }
        else {
            if (this.invisibleLocalVariableAnnotations == null) {
                this.invisibleLocalVariableAnnotations = new ArrayList<LocalVariableAnnotationNode>(1);
            }
            this.invisibleLocalVariableAnnotations.add(an);
        }
        return an;
    }
    
    @Override
    public void visitLineNumber(final int line, final Label start) {
        this.instructions.add(new LineNumberNode(line, this.getLabelNode(start)));
    }
    
    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        this.maxStack = maxStack;
        this.maxLocals = maxLocals;
    }
    
    @Override
    public void visitEnd() {
    }
    
    protected LabelNode getLabelNode(final Label l) {
        if (!(l.info instanceof LabelNode)) {
            l.info = new LabelNode();
        }
        return (LabelNode)l.info;
    }
    
    private LabelNode[] getLabelNodes(final Label[] l) {
        final LabelNode[] nodes = new LabelNode[l.length];
        for (int i = 0; i < l.length; ++i) {
            nodes[i] = this.getLabelNode(l[i]);
        }
        return nodes;
    }
    
    private Object[] getLabelNodes(final Object[] objs) {
        final Object[] nodes = new Object[objs.length];
        for (int i = 0; i < objs.length; ++i) {
            Object o = objs[i];
            if (o instanceof Label) {
                o = this.getLabelNode((Label)o);
            }
            nodes[i] = o;
        }
        return nodes;
    }
    
    public void check(final int api) {
        if (api == 262144) {
            if (this.visibleTypeAnnotations != null && this.visibleTypeAnnotations.size() > 0) {
                throw new RuntimeException();
            }
            if (this.invisibleTypeAnnotations != null && this.invisibleTypeAnnotations.size() > 0) {
                throw new RuntimeException();
            }
            for (int n = (this.tryCatchBlocks == null) ? 0 : this.tryCatchBlocks.size(), i = 0; i < n; ++i) {
                final TryCatchBlockNode tcb = this.tryCatchBlocks.get(i);
                if (tcb.visibleTypeAnnotations != null && tcb.visibleTypeAnnotations.size() > 0) {
                    throw new RuntimeException();
                }
                if (tcb.invisibleTypeAnnotations != null && tcb.invisibleTypeAnnotations.size() > 0) {
                    throw new RuntimeException();
                }
            }
            for (int i = 0; i < this.instructions.size(); ++i) {
                final AbstractInsnNode insn = this.instructions.get(i);
                if (insn.visibleTypeAnnotations != null && insn.visibleTypeAnnotations.size() > 0) {
                    throw new RuntimeException();
                }
                if (insn.invisibleTypeAnnotations != null && insn.invisibleTypeAnnotations.size() > 0) {
                    throw new RuntimeException();
                }
                if (insn instanceof MethodInsnNode) {
                    final boolean itf = ((MethodInsnNode)insn).itf;
                    if (itf != (insn.opcode == 185)) {
                        throw new RuntimeException();
                    }
                }
            }
            if (this.visibleLocalVariableAnnotations != null && this.visibleLocalVariableAnnotations.size() > 0) {
                throw new RuntimeException();
            }
            if (this.invisibleLocalVariableAnnotations != null && this.invisibleLocalVariableAnnotations.size() > 0) {
                throw new RuntimeException();
            }
        }
    }
    
    public void accept(final ClassVisitor cv) {
        final String[] exceptions = new String[this.exceptions.size()];
        this.exceptions.toArray(exceptions);
        final MethodVisitor mv = cv.visitMethod(this.access, this.name, this.desc, this.signature, exceptions);
        if (mv != null) {
            this.accept(mv);
        }
    }
    
    public void accept(final MethodVisitor mv) {
        for (int n = (this.parameters == null) ? 0 : this.parameters.size(), i = 0; i < n; ++i) {
            final ParameterNode parameter = this.parameters.get(i);
            mv.visitParameter(parameter.name, parameter.access);
        }
        if (this.annotationDefault != null) {
            final AnnotationVisitor av = mv.visitAnnotationDefault();
            AnnotationNode.accept(av, null, this.annotationDefault);
            if (av != null) {
                av.visitEnd();
            }
        }
        for (int n = (this.visibleAnnotations == null) ? 0 : this.visibleAnnotations.size(), i = 0; i < n; ++i) {
            final AnnotationNode an = this.visibleAnnotations.get(i);
            an.accept(mv.visitAnnotation(an.desc, true));
        }
        for (int n = (this.invisibleAnnotations == null) ? 0 : this.invisibleAnnotations.size(), i = 0; i < n; ++i) {
            final AnnotationNode an = this.invisibleAnnotations.get(i);
            an.accept(mv.visitAnnotation(an.desc, false));
        }
        for (int n = (this.visibleTypeAnnotations == null) ? 0 : this.visibleTypeAnnotations.size(), i = 0; i < n; ++i) {
            final TypeAnnotationNode an2 = this.visibleTypeAnnotations.get(i);
            an2.accept(mv.visitTypeAnnotation(an2.typeRef, an2.typePath, an2.desc, true));
        }
        for (int n = (this.invisibleTypeAnnotations == null) ? 0 : this.invisibleTypeAnnotations.size(), i = 0; i < n; ++i) {
            final TypeAnnotationNode an2 = this.invisibleTypeAnnotations.get(i);
            an2.accept(mv.visitTypeAnnotation(an2.typeRef, an2.typePath, an2.desc, false));
        }
        for (int n = (this.visibleParameterAnnotations == null) ? 0 : this.visibleParameterAnnotations.length, i = 0; i < n; ++i) {
            final List<?> l = this.visibleParameterAnnotations[i];
            if (l != null) {
                for (int j = 0; j < l.size(); ++j) {
                    final AnnotationNode an3 = (AnnotationNode)l.get(j);
                    an3.accept(mv.visitParameterAnnotation(i, an3.desc, true));
                }
            }
        }
        for (int n = (this.invisibleParameterAnnotations == null) ? 0 : this.invisibleParameterAnnotations.length, i = 0; i < n; ++i) {
            final List<?> l = this.invisibleParameterAnnotations[i];
            if (l != null) {
                for (int j = 0; j < l.size(); ++j) {
                    final AnnotationNode an3 = (AnnotationNode)l.get(j);
                    an3.accept(mv.visitParameterAnnotation(i, an3.desc, false));
                }
            }
        }
        if (this.visited) {
            this.instructions.resetLabels();
        }
        for (int n = (this.attrs == null) ? 0 : this.attrs.size(), i = 0; i < n; ++i) {
            mv.visitAttribute(this.attrs.get(i));
        }
        if (this.instructions.size() > 0) {
            mv.visitCode();
            for (int n = (this.tryCatchBlocks == null) ? 0 : this.tryCatchBlocks.size(), i = 0; i < n; ++i) {
                this.tryCatchBlocks.get(i).updateIndex(i);
                this.tryCatchBlocks.get(i).accept(mv);
            }
            this.instructions.accept(mv);
            for (int n = (this.localVariables == null) ? 0 : this.localVariables.size(), i = 0; i < n; ++i) {
                this.localVariables.get(i).accept(mv);
            }
            for (int n = (this.visibleLocalVariableAnnotations == null) ? 0 : this.visibleLocalVariableAnnotations.size(), i = 0; i < n; ++i) {
                this.visibleLocalVariableAnnotations.get(i).accept(mv, true);
            }
            for (int n = (this.invisibleLocalVariableAnnotations == null) ? 0 : this.invisibleLocalVariableAnnotations.size(), i = 0; i < n; ++i) {
                this.invisibleLocalVariableAnnotations.get(i).accept(mv, false);
            }
            mv.visitMaxs(this.maxStack, this.maxLocals);
            this.visited = true;
        }
        mv.visitEnd();
    }
}

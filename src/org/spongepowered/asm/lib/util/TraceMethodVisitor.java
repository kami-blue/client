// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.util;

import org.spongepowered.asm.lib.Label;
import org.spongepowered.asm.lib.Handle;
import org.spongepowered.asm.lib.Attribute;
import org.spongepowered.asm.lib.TypePath;
import org.spongepowered.asm.lib.AnnotationVisitor;
import org.spongepowered.asm.lib.MethodVisitor;

public final class TraceMethodVisitor extends MethodVisitor
{
    public final Printer p;
    
    public TraceMethodVisitor(final Printer p) {
        this(null, p);
    }
    
    public TraceMethodVisitor(final MethodVisitor mv, final Printer p) {
        super(327680, mv);
        this.p = p;
    }
    
    @Override
    public void visitParameter(final String name, final int access) {
        this.p.visitParameter(name, access);
        super.visitParameter(name, access);
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        final Printer p = this.p.visitMethodAnnotation(desc, visible);
        final AnnotationVisitor av = (this.mv == null) ? null : this.mv.visitAnnotation(desc, visible);
        return new TraceAnnotationVisitor(av, p);
    }
    
    @Override
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        final Printer p = this.p.visitMethodTypeAnnotation(typeRef, typePath, desc, visible);
        final AnnotationVisitor av = (this.mv == null) ? null : this.mv.visitTypeAnnotation(typeRef, typePath, desc, visible);
        return new TraceAnnotationVisitor(av, p);
    }
    
    @Override
    public void visitAttribute(final Attribute attr) {
        this.p.visitMethodAttribute(attr);
        super.visitAttribute(attr);
    }
    
    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        final Printer p = this.p.visitAnnotationDefault();
        final AnnotationVisitor av = (this.mv == null) ? null : this.mv.visitAnnotationDefault();
        return new TraceAnnotationVisitor(av, p);
    }
    
    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
        final Printer p = this.p.visitParameterAnnotation(parameter, desc, visible);
        final AnnotationVisitor av = (this.mv == null) ? null : this.mv.visitParameterAnnotation(parameter, desc, visible);
        return new TraceAnnotationVisitor(av, p);
    }
    
    @Override
    public void visitCode() {
        this.p.visitCode();
        super.visitCode();
    }
    
    @Override
    public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
        this.p.visitFrame(type, nLocal, local, nStack, stack);
        super.visitFrame(type, nLocal, local, nStack, stack);
    }
    
    @Override
    public void visitInsn(final int opcode) {
        this.p.visitInsn(opcode);
        super.visitInsn(opcode);
    }
    
    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        this.p.visitIntInsn(opcode, operand);
        super.visitIntInsn(opcode, operand);
    }
    
    @Override
    public void visitVarInsn(final int opcode, final int var) {
        this.p.visitVarInsn(opcode, var);
        super.visitVarInsn(opcode, var);
    }
    
    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        this.p.visitTypeInsn(opcode, type);
        super.visitTypeInsn(opcode, type);
    }
    
    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        this.p.visitFieldInsn(opcode, owner, name, desc);
        super.visitFieldInsn(opcode, owner, name, desc);
    }
    
    @Deprecated
    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
        if (this.api >= 327680) {
            super.visitMethodInsn(opcode, owner, name, desc);
            return;
        }
        this.p.visitMethodInsn(opcode, owner, name, desc);
        if (this.mv != null) {
            this.mv.visitMethodInsn(opcode, owner, name, desc);
        }
    }
    
    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf) {
        if (this.api < 327680) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }
        this.p.visitMethodInsn(opcode, owner, name, desc, itf);
        if (this.mv != null) {
            this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
    
    @Override
    public void visitInvokeDynamicInsn(final String name, final String desc, final Handle bsm, final Object... bsmArgs) {
        this.p.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }
    
    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        this.p.visitJumpInsn(opcode, label);
        super.visitJumpInsn(opcode, label);
    }
    
    @Override
    public void visitLabel(final Label label) {
        this.p.visitLabel(label);
        super.visitLabel(label);
    }
    
    @Override
    public void visitLdcInsn(final Object cst) {
        this.p.visitLdcInsn(cst);
        super.visitLdcInsn(cst);
    }
    
    @Override
    public void visitIincInsn(final int var, final int increment) {
        this.p.visitIincInsn(var, increment);
        super.visitIincInsn(var, increment);
    }
    
    @Override
    public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
        this.p.visitTableSwitchInsn(min, max, dflt, labels);
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }
    
    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
        this.p.visitLookupSwitchInsn(dflt, keys, labels);
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }
    
    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        this.p.visitMultiANewArrayInsn(desc, dims);
        super.visitMultiANewArrayInsn(desc, dims);
    }
    
    @Override
    public AnnotationVisitor visitInsnAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        final Printer p = this.p.visitInsnAnnotation(typeRef, typePath, desc, visible);
        final AnnotationVisitor av = (this.mv == null) ? null : this.mv.visitInsnAnnotation(typeRef, typePath, desc, visible);
        return new TraceAnnotationVisitor(av, p);
    }
    
    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        this.p.visitTryCatchBlock(start, end, handler, type);
        super.visitTryCatchBlock(start, end, handler, type);
    }
    
    @Override
    public AnnotationVisitor visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        final Printer p = this.p.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
        final AnnotationVisitor av = (this.mv == null) ? null : this.mv.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
        return new TraceAnnotationVisitor(av, p);
    }
    
    @Override
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
        this.p.visitLocalVariable(name, desc, signature, start, end, index);
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }
    
    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(final int typeRef, final TypePath typePath, final Label[] start, final Label[] end, final int[] index, final String desc, final boolean visible) {
        final Printer p = this.p.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
        final AnnotationVisitor av = (this.mv == null) ? null : this.mv.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
        return new TraceAnnotationVisitor(av, p);
    }
    
    @Override
    public void visitLineNumber(final int line, final Label start) {
        this.p.visitLineNumber(line, start);
        super.visitLineNumber(line, start);
    }
    
    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        this.p.visitMaxs(maxStack, maxLocals);
        super.visitMaxs(maxStack, maxLocals);
    }
    
    @Override
    public void visitEnd() {
        this.p.visitMethodEnd();
        super.visitEnd();
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib;

public abstract class MethodVisitor
{
    protected final int api;
    protected MethodVisitor mv;
    
    public MethodVisitor(final int api) {
        this(api, null);
    }
    
    public MethodVisitor(final int api, final MethodVisitor mv) {
        if (api != 262144 && api != 327680) {
            throw new IllegalArgumentException();
        }
        this.api = api;
        this.mv = mv;
    }
    
    public void visitParameter(final String name, final int access) {
        if (this.api < 327680) {
            throw new RuntimeException();
        }
        if (this.mv != null) {
            this.mv.visitParameter(name, access);
        }
    }
    
    public AnnotationVisitor visitAnnotationDefault() {
        if (this.mv != null) {
            return this.mv.visitAnnotationDefault();
        }
        return null;
    }
    
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        if (this.mv != null) {
            return this.mv.visitAnnotation(desc, visible);
        }
        return null;
    }
    
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        if (this.api < 327680) {
            throw new RuntimeException();
        }
        if (this.mv != null) {
            return this.mv.visitTypeAnnotation(typeRef, typePath, desc, visible);
        }
        return null;
    }
    
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
        if (this.mv != null) {
            return this.mv.visitParameterAnnotation(parameter, desc, visible);
        }
        return null;
    }
    
    public void visitAttribute(final Attribute attr) {
        if (this.mv != null) {
            this.mv.visitAttribute(attr);
        }
    }
    
    public void visitCode() {
        if (this.mv != null) {
            this.mv.visitCode();
        }
    }
    
    public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
        if (this.mv != null) {
            this.mv.visitFrame(type, nLocal, local, nStack, stack);
        }
    }
    
    public void visitInsn(final int opcode) {
        if (this.mv != null) {
            this.mv.visitInsn(opcode);
        }
    }
    
    public void visitIntInsn(final int opcode, final int operand) {
        if (this.mv != null) {
            this.mv.visitIntInsn(opcode, operand);
        }
    }
    
    public void visitVarInsn(final int opcode, final int var) {
        if (this.mv != null) {
            this.mv.visitVarInsn(opcode, var);
        }
    }
    
    public void visitTypeInsn(final int opcode, final String type) {
        if (this.mv != null) {
            this.mv.visitTypeInsn(opcode, type);
        }
    }
    
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        if (this.mv != null) {
            this.mv.visitFieldInsn(opcode, owner, name, desc);
        }
    }
    
    @Deprecated
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
        if (this.api >= 327680) {
            final boolean itf = opcode == 185;
            this.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }
        if (this.mv != null) {
            this.mv.visitMethodInsn(opcode, owner, name, desc);
        }
    }
    
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf) {
        if (this.api >= 327680) {
            if (this.mv != null) {
                this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
            }
            return;
        }
        if (itf != (opcode == 185)) {
            throw new IllegalArgumentException("INVOKESPECIAL/STATIC on interfaces require ASM 5");
        }
        this.visitMethodInsn(opcode, owner, name, desc);
    }
    
    public void visitInvokeDynamicInsn(final String name, final String desc, final Handle bsm, final Object... bsmArgs) {
        if (this.mv != null) {
            this.mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        }
    }
    
    public void visitJumpInsn(final int opcode, final Label label) {
        if (this.mv != null) {
            this.mv.visitJumpInsn(opcode, label);
        }
    }
    
    public void visitLabel(final Label label) {
        if (this.mv != null) {
            this.mv.visitLabel(label);
        }
    }
    
    public void visitLdcInsn(final Object cst) {
        if (this.mv != null) {
            this.mv.visitLdcInsn(cst);
        }
    }
    
    public void visitIincInsn(final int var, final int increment) {
        if (this.mv != null) {
            this.mv.visitIincInsn(var, increment);
        }
    }
    
    public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
        if (this.mv != null) {
            this.mv.visitTableSwitchInsn(min, max, dflt, labels);
        }
    }
    
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
        if (this.mv != null) {
            this.mv.visitLookupSwitchInsn(dflt, keys, labels);
        }
    }
    
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        if (this.mv != null) {
            this.mv.visitMultiANewArrayInsn(desc, dims);
        }
    }
    
    public AnnotationVisitor visitInsnAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        if (this.api < 327680) {
            throw new RuntimeException();
        }
        if (this.mv != null) {
            return this.mv.visitInsnAnnotation(typeRef, typePath, desc, visible);
        }
        return null;
    }
    
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        if (this.mv != null) {
            this.mv.visitTryCatchBlock(start, end, handler, type);
        }
    }
    
    public AnnotationVisitor visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        if (this.api < 327680) {
            throw new RuntimeException();
        }
        if (this.mv != null) {
            return this.mv.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
        }
        return null;
    }
    
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
        if (this.mv != null) {
            this.mv.visitLocalVariable(name, desc, signature, start, end, index);
        }
    }
    
    public AnnotationVisitor visitLocalVariableAnnotation(final int typeRef, final TypePath typePath, final Label[] start, final Label[] end, final int[] index, final String desc, final boolean visible) {
        if (this.api < 327680) {
            throw new RuntimeException();
        }
        if (this.mv != null) {
            return this.mv.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
        }
        return null;
    }
    
    public void visitLineNumber(final int line, final Label start) {
        if (this.mv != null) {
            this.mv.visitLineNumber(line, start);
        }
    }
    
    public void visitMaxs(final int maxStack, final int maxLocals) {
        if (this.mv != null) {
            this.mv.visitMaxs(maxStack, maxLocals);
        }
    }
    
    public void visitEnd() {
        if (this.mv != null) {
            this.mv.visitEnd();
        }
    }
}

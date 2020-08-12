// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib;

public abstract class ClassVisitor
{
    protected final int api;
    protected ClassVisitor cv;
    
    public ClassVisitor(final int api) {
        this(api, null);
    }
    
    public ClassVisitor(final int api, final ClassVisitor cv) {
        if (api != 262144 && api != 327680) {
            throw new IllegalArgumentException();
        }
        this.api = api;
        this.cv = cv;
    }
    
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        if (this.cv != null) {
            this.cv.visit(version, access, name, signature, superName, interfaces);
        }
    }
    
    public void visitSource(final String source, final String debug) {
        if (this.cv != null) {
            this.cv.visitSource(source, debug);
        }
    }
    
    public void visitOuterClass(final String owner, final String name, final String desc) {
        if (this.cv != null) {
            this.cv.visitOuterClass(owner, name, desc);
        }
    }
    
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        if (this.cv != null) {
            return this.cv.visitAnnotation(desc, visible);
        }
        return null;
    }
    
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        if (this.api < 327680) {
            throw new RuntimeException();
        }
        if (this.cv != null) {
            return this.cv.visitTypeAnnotation(typeRef, typePath, desc, visible);
        }
        return null;
    }
    
    public void visitAttribute(final Attribute attr) {
        if (this.cv != null) {
            this.cv.visitAttribute(attr);
        }
    }
    
    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
        if (this.cv != null) {
            this.cv.visitInnerClass(name, outerName, innerName, access);
        }
    }
    
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        if (this.cv != null) {
            return this.cv.visitField(access, name, desc, signature, value);
        }
        return null;
    }
    
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        if (this.cv != null) {
            return this.cv.visitMethod(access, name, desc, signature, exceptions);
        }
        return null;
    }
    
    public void visitEnd() {
        if (this.cv != null) {
            this.cv.visitEnd();
        }
    }
}

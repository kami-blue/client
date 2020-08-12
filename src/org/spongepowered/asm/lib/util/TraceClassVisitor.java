// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.util;

import org.spongepowered.asm.lib.MethodVisitor;
import org.spongepowered.asm.lib.FieldVisitor;
import org.spongepowered.asm.lib.Attribute;
import org.spongepowered.asm.lib.TypePath;
import org.spongepowered.asm.lib.AnnotationVisitor;
import java.io.PrintWriter;
import org.spongepowered.asm.lib.ClassVisitor;

public final class TraceClassVisitor extends ClassVisitor
{
    private final PrintWriter pw;
    public final Printer p;
    
    public TraceClassVisitor(final PrintWriter pw) {
        this(null, pw);
    }
    
    public TraceClassVisitor(final ClassVisitor cv, final PrintWriter pw) {
        this(cv, new Textifier(), pw);
    }
    
    public TraceClassVisitor(final ClassVisitor cv, final Printer p, final PrintWriter pw) {
        super(327680, cv);
        this.pw = pw;
        this.p = p;
    }
    
    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        this.p.visit(version, access, name, signature, superName, interfaces);
        super.visit(version, access, name, signature, superName, interfaces);
    }
    
    @Override
    public void visitSource(final String file, final String debug) {
        this.p.visitSource(file, debug);
        super.visitSource(file, debug);
    }
    
    @Override
    public void visitOuterClass(final String owner, final String name, final String desc) {
        this.p.visitOuterClass(owner, name, desc);
        super.visitOuterClass(owner, name, desc);
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        final Printer p = this.p.visitClassAnnotation(desc, visible);
        final AnnotationVisitor av = (this.cv == null) ? null : this.cv.visitAnnotation(desc, visible);
        return new TraceAnnotationVisitor(av, p);
    }
    
    @Override
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        final Printer p = this.p.visitClassTypeAnnotation(typeRef, typePath, desc, visible);
        final AnnotationVisitor av = (this.cv == null) ? null : this.cv.visitTypeAnnotation(typeRef, typePath, desc, visible);
        return new TraceAnnotationVisitor(av, p);
    }
    
    @Override
    public void visitAttribute(final Attribute attr) {
        this.p.visitClassAttribute(attr);
        super.visitAttribute(attr);
    }
    
    @Override
    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
        this.p.visitInnerClass(name, outerName, innerName, access);
        super.visitInnerClass(name, outerName, innerName, access);
    }
    
    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        final Printer p = this.p.visitField(access, name, desc, signature, value);
        final FieldVisitor fv = (this.cv == null) ? null : this.cv.visitField(access, name, desc, signature, value);
        return new TraceFieldVisitor(fv, p);
    }
    
    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final Printer p = this.p.visitMethod(access, name, desc, signature, exceptions);
        final MethodVisitor mv = (this.cv == null) ? null : this.cv.visitMethod(access, name, desc, signature, exceptions);
        return new TraceMethodVisitor(mv, p);
    }
    
    @Override
    public void visitEnd() {
        this.p.visitClassEnd();
        if (this.pw != null) {
            this.p.print(this.pw);
            this.pw.flush();
        }
        super.visitEnd();
    }
}

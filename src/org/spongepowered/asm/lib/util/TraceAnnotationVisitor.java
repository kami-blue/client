// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.util;

import org.spongepowered.asm.lib.AnnotationVisitor;

public final class TraceAnnotationVisitor extends AnnotationVisitor
{
    private final Printer p;
    
    public TraceAnnotationVisitor(final Printer p) {
        this(null, p);
    }
    
    public TraceAnnotationVisitor(final AnnotationVisitor av, final Printer p) {
        super(327680, av);
        this.p = p;
    }
    
    @Override
    public void visit(final String name, final Object value) {
        this.p.visit(name, value);
        super.visit(name, value);
    }
    
    @Override
    public void visitEnum(final String name, final String desc, final String value) {
        this.p.visitEnum(name, desc, value);
        super.visitEnum(name, desc, value);
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        final Printer p = this.p.visitAnnotation(name, desc);
        final AnnotationVisitor av = (this.av == null) ? null : this.av.visitAnnotation(name, desc);
        return new TraceAnnotationVisitor(av, p);
    }
    
    @Override
    public AnnotationVisitor visitArray(final String name) {
        final Printer p = this.p.visitArray(name);
        final AnnotationVisitor av = (this.av == null) ? null : this.av.visitArray(name);
        return new TraceAnnotationVisitor(av, p);
    }
    
    @Override
    public void visitEnd() {
        this.p.visitAnnotationEnd();
        super.visitEnd();
    }
}

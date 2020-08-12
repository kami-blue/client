// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib;

public abstract class AnnotationVisitor
{
    protected final int api;
    protected AnnotationVisitor av;
    
    public AnnotationVisitor(final int api) {
        this(api, null);
    }
    
    public AnnotationVisitor(final int api, final AnnotationVisitor av) {
        if (api != 262144 && api != 327680) {
            throw new IllegalArgumentException();
        }
        this.api = api;
        this.av = av;
    }
    
    public void visit(final String name, final Object value) {
        if (this.av != null) {
            this.av.visit(name, value);
        }
    }
    
    public void visitEnum(final String name, final String desc, final String value) {
        if (this.av != null) {
            this.av.visitEnum(name, desc, value);
        }
    }
    
    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        if (this.av != null) {
            return this.av.visitAnnotation(name, desc);
        }
        return null;
    }
    
    public AnnotationVisitor visitArray(final String name) {
        if (this.av != null) {
            return this.av.visitArray(name);
        }
        return null;
    }
    
    public void visitEnd() {
        if (this.av != null) {
            this.av.visitEnd();
        }
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.util;

import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.AnnotationVisitor;

public class CheckAnnotationAdapter extends AnnotationVisitor
{
    private final boolean named;
    private boolean end;
    
    public CheckAnnotationAdapter(final AnnotationVisitor av) {
        this(av, true);
    }
    
    CheckAnnotationAdapter(final AnnotationVisitor av, final boolean named) {
        super(327680, av);
        this.named = named;
    }
    
    @Override
    public void visit(final String name, final Object value) {
        this.checkEnd();
        this.checkName(name);
        if (!(value instanceof Byte) && !(value instanceof Boolean) && !(value instanceof Character) && !(value instanceof Short) && !(value instanceof Integer) && !(value instanceof Long) && !(value instanceof Float) && !(value instanceof Double) && !(value instanceof String) && !(value instanceof Type) && !(value instanceof byte[]) && !(value instanceof boolean[]) && !(value instanceof char[]) && !(value instanceof short[]) && !(value instanceof int[]) && !(value instanceof long[]) && !(value instanceof float[]) && !(value instanceof double[])) {
            throw new IllegalArgumentException("Invalid annotation value");
        }
        if (value instanceof Type) {
            final int sort = ((Type)value).getSort();
            if (sort == 11) {
                throw new IllegalArgumentException("Invalid annotation value");
            }
        }
        if (this.av != null) {
            this.av.visit(name, value);
        }
    }
    
    @Override
    public void visitEnum(final String name, final String desc, final String value) {
        this.checkEnd();
        this.checkName(name);
        CheckMethodAdapter.checkDesc(desc, false);
        if (value == null) {
            throw new IllegalArgumentException("Invalid enum value");
        }
        if (this.av != null) {
            this.av.visitEnum(name, desc, value);
        }
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        this.checkEnd();
        this.checkName(name);
        CheckMethodAdapter.checkDesc(desc, false);
        return new CheckAnnotationAdapter((this.av == null) ? null : this.av.visitAnnotation(name, desc));
    }
    
    @Override
    public AnnotationVisitor visitArray(final String name) {
        this.checkEnd();
        this.checkName(name);
        return new CheckAnnotationAdapter((this.av == null) ? null : this.av.visitArray(name), false);
    }
    
    @Override
    public void visitEnd() {
        this.checkEnd();
        this.end = true;
        if (this.av != null) {
            this.av.visitEnd();
        }
    }
    
    private void checkEnd() {
        if (this.end) {
            throw new IllegalStateException("Cannot call a visit method after visitEnd has been called");
        }
    }
    
    private void checkName(final String name) {
        if (this.named && name == null) {
            throw new IllegalArgumentException("Annotation value name must not be null");
        }
    }
}

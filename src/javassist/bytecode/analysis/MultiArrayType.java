// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.analysis;

import javassist.NotFoundException;
import javassist.ClassPool;
import javassist.CtClass;

public class MultiArrayType extends Type
{
    private MultiType component;
    private int dims;
    
    public MultiArrayType(final MultiType component, final int dims) {
        super(null);
        this.component = component;
        this.dims = dims;
    }
    
    @Override
    public CtClass getCtClass() {
        final CtClass clazz = this.component.getCtClass();
        if (clazz == null) {
            return null;
        }
        ClassPool pool = clazz.getClassPool();
        if (pool == null) {
            pool = ClassPool.getDefault();
        }
        final String name = this.arrayName(clazz.getName(), this.dims);
        try {
            return pool.get(name);
        }
        catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    boolean popChanged() {
        return this.component.popChanged();
    }
    
    @Override
    public int getDimensions() {
        return this.dims;
    }
    
    @Override
    public Type getComponent() {
        return (this.dims == 1) ? this.component : new MultiArrayType(this.component, this.dims - 1);
    }
    
    @Override
    public int getSize() {
        return 1;
    }
    
    @Override
    public boolean isArray() {
        return true;
    }
    
    @Override
    public boolean isAssignableFrom(final Type type) {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    @Override
    public boolean isReference() {
        return true;
    }
    
    public boolean isAssignableTo(final Type type) {
        if (Type.eq(type.getCtClass(), Type.OBJECT.getCtClass())) {
            return true;
        }
        if (Type.eq(type.getCtClass(), Type.CLONEABLE.getCtClass())) {
            return true;
        }
        if (Type.eq(type.getCtClass(), Type.SERIALIZABLE.getCtClass())) {
            return true;
        }
        if (!type.isArray()) {
            return false;
        }
        final Type typeRoot = this.getRootComponent(type);
        final int typeDims = type.getDimensions();
        if (typeDims > this.dims) {
            return false;
        }
        if (typeDims < this.dims) {
            return Type.eq(typeRoot.getCtClass(), Type.OBJECT.getCtClass()) || Type.eq(typeRoot.getCtClass(), Type.CLONEABLE.getCtClass()) || Type.eq(typeRoot.getCtClass(), Type.SERIALIZABLE.getCtClass());
        }
        return this.component.isAssignableTo(typeRoot);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof MultiArrayType)) {
            return false;
        }
        final MultiArrayType multi = (MultiArrayType)o;
        return this.component.equals(multi.component) && this.dims == multi.dims;
    }
    
    @Override
    public String toString() {
        return this.arrayName(this.component.toString(), this.dims);
    }
}

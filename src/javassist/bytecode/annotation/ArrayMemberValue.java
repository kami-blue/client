// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.annotation;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import javassist.ClassPool;
import javassist.bytecode.ConstPool;

public class ArrayMemberValue extends MemberValue
{
    MemberValue type;
    MemberValue[] values;
    
    public ArrayMemberValue(final ConstPool cp) {
        super('[', cp);
        this.type = null;
        this.values = null;
    }
    
    public ArrayMemberValue(final MemberValue t, final ConstPool cp) {
        super('[', cp);
        this.type = t;
        this.values = null;
    }
    
    @Override
    Object getValue(final ClassLoader cl, final ClassPool cp, final Method method) throws ClassNotFoundException {
        if (this.values == null) {
            throw new ClassNotFoundException("no array elements found: " + method.getName());
        }
        final int size = this.values.length;
        Class clazz;
        if (this.type == null) {
            clazz = method.getReturnType().getComponentType();
            if (clazz == null || size > 0) {
                throw new ClassNotFoundException("broken array type: " + method.getName());
            }
        }
        else {
            clazz = this.type.getType(cl);
        }
        final Object a = Array.newInstance(clazz, size);
        for (int i = 0; i < size; ++i) {
            Array.set(a, i, this.values[i].getValue(cl, cp, method));
        }
        return a;
    }
    
    @Override
    Class getType(final ClassLoader cl) throws ClassNotFoundException {
        if (this.type == null) {
            throw new ClassNotFoundException("no array type specified");
        }
        final Object a = Array.newInstance(this.type.getType(cl), 0);
        return a.getClass();
    }
    
    public MemberValue getType() {
        return this.type;
    }
    
    public MemberValue[] getValue() {
        return this.values;
    }
    
    public void setValue(final MemberValue[] elements) {
        this.values = elements;
        if (elements != null && elements.length > 0) {
            this.type = elements[0];
        }
    }
    
    @Override
    public String toString() {
        final StringBuffer buf = new StringBuffer("{");
        if (this.values != null) {
            for (int i = 0; i < this.values.length; ++i) {
                buf.append(this.values[i].toString());
                if (i + 1 < this.values.length) {
                    buf.append(", ");
                }
            }
        }
        buf.append("}");
        return buf.toString();
    }
    
    @Override
    public void write(final AnnotationsWriter writer) throws IOException {
        final int num = (this.values == null) ? 0 : this.values.length;
        writer.arrayValue(num);
        for (int i = 0; i < num; ++i) {
            this.values[i].write(writer);
        }
    }
    
    @Override
    public void accept(final MemberValueVisitor visitor) {
        visitor.visitArrayMemberValue(this);
    }
}

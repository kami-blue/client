// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.tree.analysis;

import java.util.List;
import org.spongepowered.asm.lib.Type;

public class SimpleVerifier extends BasicVerifier
{
    private final Type currentClass;
    private final Type currentSuperClass;
    private final List<Type> currentClassInterfaces;
    private final boolean isInterface;
    private ClassLoader loader;
    
    public SimpleVerifier() {
        this(null, null, false);
    }
    
    public SimpleVerifier(final Type currentClass, final Type currentSuperClass, final boolean isInterface) {
        this(currentClass, currentSuperClass, null, isInterface);
    }
    
    public SimpleVerifier(final Type currentClass, final Type currentSuperClass, final List<Type> currentClassInterfaces, final boolean isInterface) {
        this(327680, currentClass, currentSuperClass, currentClassInterfaces, isInterface);
    }
    
    protected SimpleVerifier(final int api, final Type currentClass, final Type currentSuperClass, final List<Type> currentClassInterfaces, final boolean isInterface) {
        super(api);
        this.loader = this.getClass().getClassLoader();
        this.currentClass = currentClass;
        this.currentSuperClass = currentSuperClass;
        this.currentClassInterfaces = currentClassInterfaces;
        this.isInterface = isInterface;
    }
    
    public void setClassLoader(final ClassLoader loader) {
        this.loader = loader;
    }
    
    @Override
    public BasicValue newValue(final Type type) {
        if (type == null) {
            return BasicValue.UNINITIALIZED_VALUE;
        }
        final boolean isArray = type.getSort() == 9;
        if (isArray) {
            switch (type.getElementType().getSort()) {
                case 1:
                case 2:
                case 3:
                case 4: {
                    return new BasicValue(type);
                }
            }
        }
        BasicValue v = super.newValue(type);
        if (BasicValue.REFERENCE_VALUE.equals(v)) {
            if (isArray) {
                v = this.newValue(type.getElementType());
                String desc = v.getType().getDescriptor();
                for (int i = 0; i < type.getDimensions(); ++i) {
                    desc = '[' + desc;
                }
                v = new BasicValue(Type.getType(desc));
            }
            else {
                v = new BasicValue(type);
            }
        }
        return v;
    }
    
    @Override
    protected boolean isArrayValue(final BasicValue value) {
        final Type t = value.getType();
        return t != null && ("Lnull;".equals(t.getDescriptor()) || t.getSort() == 9);
    }
    
    @Override
    protected BasicValue getElementValue(final BasicValue objectArrayValue) throws AnalyzerException {
        final Type arrayType = objectArrayValue.getType();
        if (arrayType != null) {
            if (arrayType.getSort() == 9) {
                return this.newValue(Type.getType(arrayType.getDescriptor().substring(1)));
            }
            if ("Lnull;".equals(arrayType.getDescriptor())) {
                return objectArrayValue;
            }
        }
        throw new Error("Internal error");
    }
    
    @Override
    protected boolean isSubTypeOf(final BasicValue value, final BasicValue expected) {
        final Type expectedType = expected.getType();
        final Type type = value.getType();
        switch (expectedType.getSort()) {
            case 5:
            case 6:
            case 7:
            case 8: {
                return type.equals(expectedType);
            }
            case 9:
            case 10: {
                return "Lnull;".equals(type.getDescriptor()) || ((type.getSort() == 10 || type.getSort() == 9) && this.isAssignableFrom(expectedType, type));
            }
            default: {
                throw new Error("Internal error");
            }
        }
    }
    
    @Override
    public BasicValue merge(final BasicValue v, final BasicValue w) {
        if (v.equals(w)) {
            return v;
        }
        Type t = v.getType();
        final Type u = w.getType();
        if (t == null || (t.getSort() != 10 && t.getSort() != 9) || u == null || (u.getSort() != 10 && u.getSort() != 9)) {
            return BasicValue.UNINITIALIZED_VALUE;
        }
        if ("Lnull;".equals(t.getDescriptor())) {
            return w;
        }
        if ("Lnull;".equals(u.getDescriptor())) {
            return v;
        }
        if (this.isAssignableFrom(t, u)) {
            return v;
        }
        if (this.isAssignableFrom(u, t)) {
            return w;
        }
        while (t != null && !this.isInterface(t)) {
            t = this.getSuperClass(t);
            if (this.isAssignableFrom(t, u)) {
                return this.newValue(t);
            }
        }
        return BasicValue.REFERENCE_VALUE;
    }
    
    protected boolean isInterface(final Type t) {
        if (this.currentClass != null && t.equals(this.currentClass)) {
            return this.isInterface;
        }
        return this.getClass(t).isInterface();
    }
    
    protected Type getSuperClass(final Type t) {
        if (this.currentClass != null && t.equals(this.currentClass)) {
            return this.currentSuperClass;
        }
        final Class<?> c = this.getClass(t).getSuperclass();
        return (c == null) ? null : Type.getType(c);
    }
    
    protected boolean isAssignableFrom(final Type t, final Type u) {
        if (t.equals(u)) {
            return true;
        }
        if (this.currentClass != null && t.equals(this.currentClass)) {
            if (this.getSuperClass(u) == null) {
                return false;
            }
            if (this.isInterface) {
                return u.getSort() == 10 || u.getSort() == 9;
            }
            return this.isAssignableFrom(t, this.getSuperClass(u));
        }
        else {
            if (this.currentClass == null || !u.equals(this.currentClass)) {
                Class<?> tc = this.getClass(t);
                if (tc.isInterface()) {
                    tc = Object.class;
                }
                return tc.isAssignableFrom(this.getClass(u));
            }
            if (this.isAssignableFrom(t, this.currentSuperClass)) {
                return true;
            }
            if (this.currentClassInterfaces != null) {
                for (int i = 0; i < this.currentClassInterfaces.size(); ++i) {
                    final Type v = this.currentClassInterfaces.get(i);
                    if (this.isAssignableFrom(t, v)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    protected Class<?> getClass(final Type t) {
        try {
            if (t.getSort() == 9) {
                return Class.forName(t.getDescriptor().replace('/', '.'), false, this.loader);
            }
            return Class.forName(t.getClassName(), false, this.loader);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e.toString());
        }
    }
}

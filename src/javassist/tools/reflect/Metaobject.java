// 
// Decompiled by Procyon v0.5.36
// 

package javassist.tools.reflect;

import java.lang.reflect.InvocationTargetException;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.io.Serializable;

public class Metaobject implements Serializable
{
    protected ClassMetaobject classmetaobject;
    protected Metalevel baseobject;
    protected Method[] methods;
    
    public Metaobject(final Object self, final Object[] args) {
        this.baseobject = (Metalevel)self;
        this.classmetaobject = this.baseobject._getClass();
        this.methods = this.classmetaobject.getReflectiveMethods();
    }
    
    protected Metaobject() {
        this.baseobject = null;
        this.classmetaobject = null;
        this.methods = null;
    }
    
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeObject(this.baseobject);
    }
    
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.baseobject = (Metalevel)in.readObject();
        this.classmetaobject = this.baseobject._getClass();
        this.methods = this.classmetaobject.getReflectiveMethods();
    }
    
    public final ClassMetaobject getClassMetaobject() {
        return this.classmetaobject;
    }
    
    public final Object getObject() {
        return this.baseobject;
    }
    
    public final void setObject(final Object self) {
        this.baseobject = (Metalevel)self;
        this.classmetaobject = this.baseobject._getClass();
        this.methods = this.classmetaobject.getReflectiveMethods();
        this.baseobject._setMetaobject(this);
    }
    
    public final String getMethodName(final int identifier) {
        final String mname = this.methods[identifier].getName();
        int j = 3;
        char c;
        do {
            c = mname.charAt(j++);
        } while (c >= '0' && '9' >= c);
        return mname.substring(j);
    }
    
    public final Class[] getParameterTypes(final int identifier) {
        return this.methods[identifier].getParameterTypes();
    }
    
    public final Class getReturnType(final int identifier) {
        return this.methods[identifier].getReturnType();
    }
    
    public Object trapFieldRead(final String name) {
        final Class jc = this.getClassMetaobject().getJavaClass();
        try {
            return jc.getField(name).get(this.getObject());
        }
        catch (NoSuchFieldException e) {
            throw new RuntimeException(e.toString());
        }
        catch (IllegalAccessException e2) {
            throw new RuntimeException(e2.toString());
        }
    }
    
    public void trapFieldWrite(final String name, final Object value) {
        final Class jc = this.getClassMetaobject().getJavaClass();
        try {
            jc.getField(name).set(this.getObject(), value);
        }
        catch (NoSuchFieldException e) {
            throw new RuntimeException(e.toString());
        }
        catch (IllegalAccessException e2) {
            throw new RuntimeException(e2.toString());
        }
    }
    
    public Object trapMethodcall(final int identifier, final Object[] args) throws Throwable {
        try {
            return this.methods[identifier].invoke(this.getObject(), args);
        }
        catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
        catch (IllegalAccessException e2) {
            throw new CannotInvokeException(e2);
        }
    }
}

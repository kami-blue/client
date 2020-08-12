// 
// Decompiled by Procyon v0.5.36
// 

package javassist.tools.reflect;

import java.util.Arrays;
import java.lang.reflect.InvocationTargetException;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.io.Serializable;

public class ClassMetaobject implements Serializable
{
    static final String methodPrefix = "_m_";
    static final int methodPrefixLen = 3;
    private Class javaClass;
    private Constructor[] constructors;
    private Method[] methods;
    public static boolean useContextClassLoader;
    
    public ClassMetaobject(final String[] params) {
        try {
            this.javaClass = this.getClassObject(params[0]);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("not found: " + params[0] + ", useContextClassLoader: " + Boolean.toString(ClassMetaobject.useContextClassLoader), e);
        }
        this.constructors = this.javaClass.getConstructors();
        this.methods = null;
    }
    
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeUTF(this.javaClass.getName());
    }
    
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.javaClass = this.getClassObject(in.readUTF());
        this.constructors = this.javaClass.getConstructors();
        this.methods = null;
    }
    
    private Class getClassObject(final String name) throws ClassNotFoundException {
        if (ClassMetaobject.useContextClassLoader) {
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        }
        return Class.forName(name);
    }
    
    public final Class getJavaClass() {
        return this.javaClass;
    }
    
    public final String getName() {
        return this.javaClass.getName();
    }
    
    public final boolean isInstance(final Object obj) {
        return this.javaClass.isInstance(obj);
    }
    
    public final Object newInstance(final Object[] args) throws CannotCreateException {
        for (int n = this.constructors.length, i = 0; i < n; ++i) {
            try {
                return this.constructors[i].newInstance(args);
            }
            catch (IllegalArgumentException ex) {}
            catch (InstantiationException e) {
                throw new CannotCreateException(e);
            }
            catch (IllegalAccessException e2) {
                throw new CannotCreateException(e2);
            }
            catch (InvocationTargetException e3) {
                throw new CannotCreateException(e3);
            }
        }
        throw new CannotCreateException("no constructor matches");
    }
    
    public Object trapFieldRead(final String name) {
        final Class jc = this.getJavaClass();
        try {
            return jc.getField(name).get(null);
        }
        catch (NoSuchFieldException e) {
            throw new RuntimeException(e.toString());
        }
        catch (IllegalAccessException e2) {
            throw new RuntimeException(e2.toString());
        }
    }
    
    public void trapFieldWrite(final String name, final Object value) {
        final Class jc = this.getJavaClass();
        try {
            jc.getField(name).set(null, value);
        }
        catch (NoSuchFieldException e) {
            throw new RuntimeException(e.toString());
        }
        catch (IllegalAccessException e2) {
            throw new RuntimeException(e2.toString());
        }
    }
    
    public static Object invoke(final Object target, final int identifier, final Object[] args) throws Throwable {
        final Method[] allmethods = target.getClass().getMethods();
        final int n = allmethods.length;
        final String head = "_m_" + identifier;
        for (int i = 0; i < n; ++i) {
            if (allmethods[i].getName().startsWith(head)) {
                try {
                    return allmethods[i].invoke(target, args);
                }
                catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
                catch (IllegalAccessException e2) {
                    throw new CannotInvokeException(e2);
                }
            }
        }
        throw new CannotInvokeException("cannot find a method");
    }
    
    public Object trapMethodcall(final int identifier, final Object[] args) throws Throwable {
        try {
            final Method[] m = this.getReflectiveMethods();
            return m[identifier].invoke(null, args);
        }
        catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
        catch (IllegalAccessException e2) {
            throw new CannotInvokeException(e2);
        }
    }
    
    public final Method[] getReflectiveMethods() {
        if (this.methods != null) {
            return this.methods;
        }
        final Class baseclass = this.getJavaClass();
        final Method[] allmethods = baseclass.getDeclaredMethods();
        final int n = allmethods.length;
        final int[] index = new int[n];
        int max = 0;
        for (int i = 0; i < n; ++i) {
            final Method m = allmethods[i];
            final String mname = m.getName();
            if (mname.startsWith("_m_")) {
                int k = 0;
                int j = 3;
                while (true) {
                    final char c = mname.charAt(j);
                    if ('0' > c || c > '9') {
                        break;
                    }
                    k = k * 10 + c - 48;
                    ++j;
                }
                index[i] = ++k;
                if (k > max) {
                    max = k;
                }
            }
        }
        this.methods = new Method[max];
        for (int i = 0; i < n; ++i) {
            if (index[i] > 0) {
                this.methods[index[i] - 1] = allmethods[i];
            }
        }
        return this.methods;
    }
    
    public final Method getMethod(final int identifier) {
        return this.getReflectiveMethods()[identifier];
    }
    
    public final String getMethodName(final int identifier) {
        final String mname = this.getReflectiveMethods()[identifier].getName();
        int j = 3;
        char c;
        do {
            c = mname.charAt(j++);
        } while (c >= '0' && '9' >= c);
        return mname.substring(j);
    }
    
    public final Class[] getParameterTypes(final int identifier) {
        return this.getReflectiveMethods()[identifier].getParameterTypes();
    }
    
    public final Class getReturnType(final int identifier) {
        return this.getReflectiveMethods()[identifier].getReturnType();
    }
    
    public final int getMethodIndex(final String originalName, final Class[] argTypes) throws NoSuchMethodException {
        final Method[] mthds = this.getReflectiveMethods();
        for (int i = 0; i < mthds.length; ++i) {
            if (mthds[i] != null) {
                if (this.getMethodName(i).equals(originalName) && Arrays.equals(argTypes, mthds[i].getParameterTypes())) {
                    return i;
                }
            }
        }
        throw new NoSuchMethodException("Method " + originalName + " not found");
    }
    
    static {
        ClassMetaobject.useContextClassLoader = false;
    }
}

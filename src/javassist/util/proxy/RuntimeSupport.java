// 
// Decompiled by Procyon v0.5.36
// 

package javassist.util.proxy;

import java.io.Serializable;
import java.io.InvalidClassException;
import java.lang.reflect.Method;

public class RuntimeSupport
{
    public static MethodHandler default_interceptor;
    
    public static void find2Methods(final Class clazz, final String superMethod, final String thisMethod, final int index, final String desc, final Method[] methods) {
        methods[index + 1] = ((thisMethod == null) ? null : findMethod(clazz, thisMethod, desc));
        methods[index] = findSuperClassMethod(clazz, superMethod, desc);
    }
    
    @Deprecated
    public static void find2Methods(final Object self, final String superMethod, final String thisMethod, final int index, final String desc, final Method[] methods) {
        methods[index + 1] = ((thisMethod == null) ? null : findMethod(self, thisMethod, desc));
        methods[index] = findSuperMethod(self, superMethod, desc);
    }
    
    @Deprecated
    public static Method findMethod(final Object self, final String name, final String desc) {
        final Method m = findMethod2(self.getClass(), name, desc);
        if (m == null) {
            error(self.getClass(), name, desc);
        }
        return m;
    }
    
    public static Method findMethod(final Class clazz, final String name, final String desc) {
        final Method m = findMethod2(clazz, name, desc);
        if (m == null) {
            error(clazz, name, desc);
        }
        return m;
    }
    
    public static Method findSuperMethod(final Object self, final String name, final String desc) {
        final Class clazz = self.getClass();
        return findSuperClassMethod(clazz, name, desc);
    }
    
    public static Method findSuperClassMethod(final Class clazz, final String name, final String desc) {
        Method m = findSuperMethod2(clazz.getSuperclass(), name, desc);
        if (m == null) {
            m = searchInterfaces(clazz, name, desc);
        }
        if (m == null) {
            error(clazz, name, desc);
        }
        return m;
    }
    
    private static void error(final Class clazz, final String name, final String desc) {
        throw new RuntimeException("not found " + name + ":" + desc + " in " + clazz.getName());
    }
    
    private static Method findSuperMethod2(final Class clazz, final String name, final String desc) {
        Method m = findMethod2(clazz, name, desc);
        if (m != null) {
            return m;
        }
        final Class superClass = clazz.getSuperclass();
        if (superClass != null) {
            m = findSuperMethod2(superClass, name, desc);
            if (m != null) {
                return m;
            }
        }
        return searchInterfaces(clazz, name, desc);
    }
    
    private static Method searchInterfaces(final Class clazz, final String name, final String desc) {
        Method m = null;
        final Class[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length; ++i) {
            m = findSuperMethod2(interfaces[i], name, desc);
            if (m != null) {
                return m;
            }
        }
        return m;
    }
    
    private static Method findMethod2(final Class clazz, final String name, final String desc) {
        final Method[] methods = SecurityActions.getDeclaredMethods(clazz);
        for (int n = methods.length, i = 0; i < n; ++i) {
            if (methods[i].getName().equals(name) && makeDescriptor(methods[i]).equals(desc)) {
                return methods[i];
            }
        }
        return null;
    }
    
    public static String makeDescriptor(final Method m) {
        final Class[] params = m.getParameterTypes();
        return makeDescriptor(params, m.getReturnType());
    }
    
    public static String makeDescriptor(final Class[] params, final Class retType) {
        final StringBuffer sbuf = new StringBuffer();
        sbuf.append('(');
        for (int i = 0; i < params.length; ++i) {
            makeDesc(sbuf, params[i]);
        }
        sbuf.append(')');
        if (retType != null) {
            makeDesc(sbuf, retType);
        }
        return sbuf.toString();
    }
    
    public static String makeDescriptor(final String params, final Class retType) {
        final StringBuffer sbuf = new StringBuffer(params);
        makeDesc(sbuf, retType);
        return sbuf.toString();
    }
    
    private static void makeDesc(final StringBuffer sbuf, final Class type) {
        if (type.isArray()) {
            sbuf.append('[');
            makeDesc(sbuf, type.getComponentType());
        }
        else if (type.isPrimitive()) {
            if (type == Void.TYPE) {
                sbuf.append('V');
            }
            else if (type == Integer.TYPE) {
                sbuf.append('I');
            }
            else if (type == Byte.TYPE) {
                sbuf.append('B');
            }
            else if (type == Long.TYPE) {
                sbuf.append('J');
            }
            else if (type == Double.TYPE) {
                sbuf.append('D');
            }
            else if (type == Float.TYPE) {
                sbuf.append('F');
            }
            else if (type == Character.TYPE) {
                sbuf.append('C');
            }
            else if (type == Short.TYPE) {
                sbuf.append('S');
            }
            else {
                if (type != Boolean.TYPE) {
                    throw new RuntimeException("bad type: " + type.getName());
                }
                sbuf.append('Z');
            }
        }
        else {
            sbuf.append('L').append(type.getName().replace('.', '/')).append(';');
        }
    }
    
    public static SerializedProxy makeSerializedProxy(final Object proxy) throws InvalidClassException {
        final Class clazz = proxy.getClass();
        MethodHandler methodHandler = null;
        if (proxy instanceof ProxyObject) {
            methodHandler = ((ProxyObject)proxy).getHandler();
        }
        else if (proxy instanceof Proxy) {
            methodHandler = ProxyFactory.getHandler((Proxy)proxy);
        }
        return new SerializedProxy(clazz, ProxyFactory.getFilterSignature(clazz), methodHandler);
    }
    
    static {
        RuntimeSupport.default_interceptor = new DefaultMethodHandler();
    }
    
    static class DefaultMethodHandler implements MethodHandler, Serializable
    {
        @Override
        public Object invoke(final Object self, final Method m, final Method proceed, final Object[] args) throws Exception {
            return proceed.invoke(self, args);
        }
    }
}

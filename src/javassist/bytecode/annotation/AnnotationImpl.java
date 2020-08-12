// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.annotation;

import javassist.bytecode.MethodInfo;
import javassist.bytecode.ClassFile;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationDefaultAttribute;
import java.lang.reflect.Proxy;
import javassist.ClassPool;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;

public class AnnotationImpl implements InvocationHandler
{
    private static final String JDK_ANNOTATION_CLASS_NAME = "java.lang.annotation.Annotation";
    private static Method JDK_ANNOTATION_TYPE_METHOD;
    private Annotation annotation;
    private ClassPool pool;
    private ClassLoader classLoader;
    private transient Class annotationType;
    private transient int cachedHashCode;
    
    public static Object make(final ClassLoader cl, final Class clazz, final ClassPool cp, final Annotation anon) {
        final AnnotationImpl handler = new AnnotationImpl(anon, cp, cl);
        return Proxy.newProxyInstance(cl, new Class[] { clazz }, handler);
    }
    
    private AnnotationImpl(final Annotation a, final ClassPool cp, final ClassLoader loader) {
        this.cachedHashCode = Integer.MIN_VALUE;
        this.annotation = a;
        this.pool = cp;
        this.classLoader = loader;
    }
    
    public String getTypeName() {
        return this.annotation.getTypeName();
    }
    
    private Class getAnnotationType() {
        if (this.annotationType == null) {
            final String typeName = this.annotation.getTypeName();
            try {
                this.annotationType = this.classLoader.loadClass(typeName);
            }
            catch (ClassNotFoundException e) {
                final NoClassDefFoundError error = new NoClassDefFoundError("Error loading annotation class: " + typeName);
                error.setStackTrace(e.getStackTrace());
                throw error;
            }
        }
        return this.annotationType;
    }
    
    public Annotation getAnnotation() {
        return this.annotation;
    }
    
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String name = method.getName();
        if (Object.class == method.getDeclaringClass()) {
            if ("equals".equals(name)) {
                final Object obj = args[0];
                return new Boolean(this.checkEquals(obj));
            }
            if ("toString".equals(name)) {
                return this.annotation.toString();
            }
            if ("hashCode".equals(name)) {
                return new Integer(this.hashCode());
            }
        }
        else if ("annotationType".equals(name) && method.getParameterTypes().length == 0) {
            return this.getAnnotationType();
        }
        final MemberValue mv = this.annotation.getMemberValue(name);
        if (mv == null) {
            return this.getDefault(name, method);
        }
        return mv.getValue(this.classLoader, this.pool, method);
    }
    
    private Object getDefault(final String name, final Method method) throws ClassNotFoundException, RuntimeException {
        final String classname = this.annotation.getTypeName();
        if (this.pool != null) {
            try {
                final CtClass cc = this.pool.get(classname);
                final ClassFile cf = cc.getClassFile2();
                final MethodInfo minfo = cf.getMethod(name);
                if (minfo != null) {
                    final AnnotationDefaultAttribute ainfo = (AnnotationDefaultAttribute)minfo.getAttribute("AnnotationDefault");
                    if (ainfo != null) {
                        final MemberValue mv = ainfo.getDefaultValue();
                        return mv.getValue(this.classLoader, this.pool, method);
                    }
                }
            }
            catch (NotFoundException e) {
                throw new RuntimeException("cannot find a class file: " + classname);
            }
        }
        throw new RuntimeException("no default value: " + classname + "." + name + "()");
    }
    
    @Override
    public int hashCode() {
        if (this.cachedHashCode == Integer.MIN_VALUE) {
            int hashCode = 0;
            this.getAnnotationType();
            final Method[] methods = this.annotationType.getDeclaredMethods();
            for (int i = 0; i < methods.length; ++i) {
                final String name = methods[i].getName();
                int valueHashCode = 0;
                final MemberValue mv = this.annotation.getMemberValue(name);
                Object value = null;
                try {
                    if (mv != null) {
                        value = mv.getValue(this.classLoader, this.pool, methods[i]);
                    }
                    if (value == null) {
                        value = this.getDefault(name, methods[i]);
                    }
                }
                catch (RuntimeException e) {
                    throw e;
                }
                catch (Exception e2) {
                    throw new RuntimeException("Error retrieving value " + name + " for annotation " + this.annotation.getTypeName(), e2);
                }
                if (value != null) {
                    if (value.getClass().isArray()) {
                        valueHashCode = arrayHashCode(value);
                    }
                    else {
                        valueHashCode = value.hashCode();
                    }
                }
                hashCode += (127 * name.hashCode() ^ valueHashCode);
            }
            this.cachedHashCode = hashCode;
        }
        return this.cachedHashCode;
    }
    
    private boolean checkEquals(final Object obj) throws Exception {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Proxy) {
            final InvocationHandler ih = Proxy.getInvocationHandler(obj);
            if (ih instanceof AnnotationImpl) {
                final AnnotationImpl other = (AnnotationImpl)ih;
                return this.annotation.equals(other.annotation);
            }
        }
        final Class otherAnnotationType = (Class)AnnotationImpl.JDK_ANNOTATION_TYPE_METHOD.invoke(obj, (Object[])null);
        if (!this.getAnnotationType().equals(otherAnnotationType)) {
            return false;
        }
        final Method[] methods = this.annotationType.getDeclaredMethods();
        for (int i = 0; i < methods.length; ++i) {
            final String name = methods[i].getName();
            final MemberValue mv = this.annotation.getMemberValue(name);
            Object value = null;
            Object otherValue = null;
            try {
                if (mv != null) {
                    value = mv.getValue(this.classLoader, this.pool, methods[i]);
                }
                if (value == null) {
                    value = this.getDefault(name, methods[i]);
                }
                otherValue = methods[i].invoke(obj, (Object[])null);
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e2) {
                throw new RuntimeException("Error retrieving value " + name + " for annotation " + this.annotation.getTypeName(), e2);
            }
            if (value == null && otherValue != null) {
                return false;
            }
            if (value != null && !value.equals(otherValue)) {
                return false;
            }
        }
        return true;
    }
    
    private static int arrayHashCode(final Object object) {
        if (object == null) {
            return 0;
        }
        int result = 1;
        final Object[] array = (Object[])object;
        for (int i = 0; i < array.length; ++i) {
            int elementHashCode = 0;
            if (array[i] != null) {
                elementHashCode = array[i].hashCode();
            }
            result = 31 * result + elementHashCode;
        }
        return result;
    }
    
    static {
        AnnotationImpl.JDK_ANNOTATION_TYPE_METHOD = null;
        try {
            final Class clazz = Class.forName("java.lang.annotation.Annotation");
            AnnotationImpl.JDK_ANNOTATION_TYPE_METHOD = clazz.getMethod("annotationType", (Class[])null);
        }
        catch (Exception ex) {}
    }
}

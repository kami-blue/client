// 
// Decompiled by Procyon v0.5.36
// 

package javassist.util.proxy;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import javassist.CannotCompileException;
import java.security.ProtectionDomain;
import javassist.bytecode.ClassFile;
import java.lang.reflect.Method;

public class FactoryHelper
{
    private static Method defineClass1;
    private static Method defineClass2;
    public static final Class[] primitiveTypes;
    public static final String[] wrapperTypes;
    public static final String[] wrapperDesc;
    public static final String[] unwarpMethods;
    public static final String[] unwrapDesc;
    public static final int[] dataSize;
    
    public static final int typeIndex(final Class type) {
        final Class[] list = FactoryHelper.primitiveTypes;
        for (int n = list.length, i = 0; i < n; ++i) {
            if (list[i] == type) {
                return i;
            }
        }
        throw new RuntimeException("bad type:" + type.getName());
    }
    
    public static Class toClass(final ClassFile cf, final ClassLoader loader) throws CannotCompileException {
        return toClass(cf, loader, null);
    }
    
    public static Class toClass(final ClassFile cf, final ClassLoader loader, final ProtectionDomain domain) throws CannotCompileException {
        try {
            final byte[] b = toBytecode(cf);
            Method method;
            Object[] args;
            if (domain == null) {
                method = FactoryHelper.defineClass1;
                args = new Object[] { cf.getName(), b, new Integer(0), new Integer(b.length) };
            }
            else {
                method = FactoryHelper.defineClass2;
                args = new Object[] { cf.getName(), b, new Integer(0), new Integer(b.length), domain };
            }
            return toClass2(method, loader, args);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (InvocationTargetException e2) {
            throw new CannotCompileException(e2.getTargetException());
        }
        catch (Exception e3) {
            throw new CannotCompileException(e3);
        }
    }
    
    private static synchronized Class toClass2(final Method method, final ClassLoader loader, final Object[] args) throws Exception {
        SecurityActions.setAccessible(method, true);
        final Class clazz = (Class)method.invoke(loader, args);
        SecurityActions.setAccessible(method, false);
        return clazz;
    }
    
    private static byte[] toBytecode(final ClassFile cf) throws IOException {
        final ByteArrayOutputStream barray = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(barray);
        try {
            cf.write(out);
        }
        finally {
            out.close();
        }
        return barray.toByteArray();
    }
    
    public static void writeFile(final ClassFile cf, final String directoryName) throws CannotCompileException {
        try {
            writeFile0(cf, directoryName);
        }
        catch (IOException e) {
            throw new CannotCompileException(e);
        }
    }
    
    private static void writeFile0(final ClassFile cf, final String directoryName) throws CannotCompileException, IOException {
        final String classname = cf.getName();
        final String filename = directoryName + File.separatorChar + classname.replace('.', File.separatorChar) + ".class";
        final int pos = filename.lastIndexOf(File.separatorChar);
        if (pos > 0) {
            final String dir = filename.substring(0, pos);
            if (!dir.equals(".")) {
                new File(dir).mkdirs();
            }
        }
        final DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
        try {
            cf.write(out);
        }
        catch (IOException e) {
            throw e;
        }
        finally {
            out.close();
        }
    }
    
    static {
        try {
            final Class cl = Class.forName("java.lang.ClassLoader");
            FactoryHelper.defineClass1 = SecurityActions.getDeclaredMethod(cl, "defineClass", new Class[] { String.class, byte[].class, Integer.TYPE, Integer.TYPE });
            FactoryHelper.defineClass2 = SecurityActions.getDeclaredMethod(cl, "defineClass", new Class[] { String.class, byte[].class, Integer.TYPE, Integer.TYPE, ProtectionDomain.class });
        }
        catch (Exception e) {
            throw new RuntimeException("cannot initialize");
        }
        primitiveTypes = new Class[] { Boolean.TYPE, Byte.TYPE, Character.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE, Void.TYPE };
        wrapperTypes = new String[] { "java.lang.Boolean", "java.lang.Byte", "java.lang.Character", "java.lang.Short", "java.lang.Integer", "java.lang.Long", "java.lang.Float", "java.lang.Double", "java.lang.Void" };
        wrapperDesc = new String[] { "(Z)V", "(B)V", "(C)V", "(S)V", "(I)V", "(J)V", "(F)V", "(D)V" };
        unwarpMethods = new String[] { "booleanValue", "byteValue", "charValue", "shortValue", "intValue", "longValue", "floatValue", "doubleValue" };
        unwrapDesc = new String[] { "()Z", "()B", "()C", "()S", "()I", "()J", "()F", "()D" };
        dataSize = new int[] { 1, 1, 1, 1, 1, 2, 1, 2 };
    }
}

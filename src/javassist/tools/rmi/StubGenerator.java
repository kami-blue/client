// 
// Decompiled by Procyon v0.5.36
// 

package javassist.tools.rmi;

import java.lang.reflect.Method;
import javassist.CtConstructor;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.CtField;
import javassist.Modifier;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.CtClass;
import javassist.CtMethod;
import java.util.Hashtable;
import javassist.ClassPool;
import javassist.Translator;

public class StubGenerator implements Translator
{
    private static final String fieldImporter = "importer";
    private static final String fieldObjectId = "objectId";
    private static final String accessorObjectId = "_getObjectId";
    private static final String sampleClass = "javassist.tools.rmi.Sample";
    private ClassPool classPool;
    private Hashtable proxyClasses;
    private CtMethod forwardMethod;
    private CtMethod forwardStaticMethod;
    private CtClass[] proxyConstructorParamTypes;
    private CtClass[] interfacesForProxy;
    private CtClass[] exceptionForProxy;
    
    public StubGenerator() {
        this.proxyClasses = new Hashtable();
    }
    
    @Override
    public void start(final ClassPool pool) throws NotFoundException {
        this.classPool = pool;
        final CtClass c = pool.get("javassist.tools.rmi.Sample");
        this.forwardMethod = c.getDeclaredMethod("forward");
        this.forwardStaticMethod = c.getDeclaredMethod("forwardStatic");
        this.proxyConstructorParamTypes = pool.get(new String[] { "javassist.tools.rmi.ObjectImporter", "int" });
        this.interfacesForProxy = pool.get(new String[] { "java.io.Serializable", "javassist.tools.rmi.Proxy" });
        this.exceptionForProxy = new CtClass[] { pool.get("javassist.tools.rmi.RemoteException") };
    }
    
    @Override
    public void onLoad(final ClassPool pool, final String classname) {
    }
    
    public boolean isProxyClass(final String name) {
        return this.proxyClasses.get(name) != null;
    }
    
    public synchronized boolean makeProxyClass(final Class clazz) throws CannotCompileException, NotFoundException {
        final String classname = clazz.getName();
        if (this.proxyClasses.get(classname) != null) {
            return false;
        }
        final CtClass ctclazz = this.produceProxyClass(this.classPool.get(classname), clazz);
        this.proxyClasses.put(classname, ctclazz);
        this.modifySuperclass(ctclazz);
        return true;
    }
    
    private CtClass produceProxyClass(final CtClass orgclass, final Class orgRtClass) throws CannotCompileException, NotFoundException {
        final int modify = orgclass.getModifiers();
        if (Modifier.isAbstract(modify) || Modifier.isNative(modify) || !Modifier.isPublic(modify)) {
            throw new CannotCompileException(orgclass.getName() + " must be public, non-native, and non-abstract.");
        }
        final CtClass proxy = this.classPool.makeClass(orgclass.getName(), orgclass.getSuperclass());
        proxy.setInterfaces(this.interfacesForProxy);
        CtField f = new CtField(this.classPool.get("javassist.tools.rmi.ObjectImporter"), "importer", proxy);
        f.setModifiers(2);
        proxy.addField(f, CtField.Initializer.byParameter(0));
        f = new CtField(CtClass.intType, "objectId", proxy);
        f.setModifiers(2);
        proxy.addField(f, CtField.Initializer.byParameter(1));
        proxy.addMethod(CtNewMethod.getter("_getObjectId", f));
        proxy.addConstructor(CtNewConstructor.defaultConstructor(proxy));
        final CtConstructor cons = CtNewConstructor.skeleton(this.proxyConstructorParamTypes, null, proxy);
        proxy.addConstructor(cons);
        try {
            this.addMethods(proxy, orgRtClass.getMethods());
            return proxy;
        }
        catch (SecurityException e) {
            throw new CannotCompileException(e);
        }
    }
    
    private CtClass toCtClass(Class rtclass) throws NotFoundException {
        String name;
        if (!rtclass.isArray()) {
            name = rtclass.getName();
        }
        else {
            final StringBuffer sbuf = new StringBuffer();
            do {
                sbuf.append("[]");
                rtclass = rtclass.getComponentType();
            } while (rtclass.isArray());
            sbuf.insert(0, rtclass.getName());
            name = sbuf.toString();
        }
        return this.classPool.get(name);
    }
    
    private CtClass[] toCtClass(final Class[] rtclasses) throws NotFoundException {
        final int n = rtclasses.length;
        final CtClass[] ctclasses = new CtClass[n];
        for (int i = 0; i < n; ++i) {
            ctclasses[i] = this.toCtClass(rtclasses[i]);
        }
        return ctclasses;
    }
    
    private void addMethods(final CtClass proxy, final Method[] ms) throws CannotCompileException, NotFoundException {
        for (int i = 0; i < ms.length; ++i) {
            final Method m = ms[i];
            final int mod = m.getModifiers();
            if (m.getDeclaringClass() != Object.class && !Modifier.isFinal(mod)) {
                if (Modifier.isPublic(mod)) {
                    CtMethod body;
                    if (Modifier.isStatic(mod)) {
                        body = this.forwardStaticMethod;
                    }
                    else {
                        body = this.forwardMethod;
                    }
                    final CtMethod wmethod = CtNewMethod.wrapped(this.toCtClass(m.getReturnType()), m.getName(), this.toCtClass((Class[])m.getParameterTypes()), this.exceptionForProxy, body, CtMethod.ConstParameter.integer(i), proxy);
                    wmethod.setModifiers(mod);
                    proxy.addMethod(wmethod);
                }
                else if (!Modifier.isProtected(mod) && !Modifier.isPrivate(mod)) {
                    throw new CannotCompileException("the methods must be public, protected, or private.");
                }
            }
        }
    }
    
    private void modifySuperclass(CtClass orgclass) throws CannotCompileException, NotFoundException {
        while (true) {
            final CtClass superclazz = orgclass.getSuperclass();
            if (superclazz == null) {
                break;
            }
            try {
                superclazz.getDeclaredConstructor(null);
            }
            catch (NotFoundException ex) {
                superclazz.addConstructor(CtNewConstructor.defaultConstructor(superclazz));
                orgclass = superclazz;
                continue;
            }
            break;
        }
    }
}

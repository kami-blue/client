// 
// Decompiled by Procyon v0.5.36
// 

package javassist.util.proxy;

import javassist.bytecode.ExceptionsAttribute;
import javassist.bytecode.Descriptor;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.StackMapTable;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import javassist.bytecode.Bytecode;
import javassist.bytecode.MethodInfo;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Collections;
import java.util.Collection;
import java.lang.reflect.Modifier;
import javassist.bytecode.ConstPool;
import javassist.bytecode.DuplicateMemberException;
import java.util.ArrayList;
import javassist.bytecode.FieldInfo;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.ProtectionDomain;
import java.lang.reflect.Field;
import java.lang.reflect.AccessibleObject;
import javassist.bytecode.ClassFile;
import javassist.CannotCompileException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Comparator;
import java.util.WeakHashMap;
import java.util.List;

public class ProxyFactory
{
    private Class superClass;
    private Class[] interfaces;
    private MethodFilter methodFilter;
    private MethodHandler handler;
    private List signatureMethods;
    private boolean hasGetHandler;
    private byte[] signature;
    private String classname;
    private String basename;
    private String superName;
    private Class thisClass;
    private boolean factoryUseCache;
    private boolean factoryWriteReplace;
    public String writeDirectory;
    private static final Class OBJECT_TYPE;
    private static final String HOLDER = "_methods_";
    private static final String HOLDER_TYPE = "[Ljava/lang/reflect/Method;";
    private static final String FILTER_SIGNATURE_FIELD = "_filter_signature";
    private static final String FILTER_SIGNATURE_TYPE = "[B";
    private static final String HANDLER = "handler";
    private static final String NULL_INTERCEPTOR_HOLDER = "javassist.util.proxy.RuntimeSupport";
    private static final String DEFAULT_INTERCEPTOR = "default_interceptor";
    private static final String HANDLER_TYPE;
    private static final String HANDLER_SETTER = "setHandler";
    private static final String HANDLER_SETTER_TYPE;
    private static final String HANDLER_GETTER = "getHandler";
    private static final String HANDLER_GETTER_TYPE;
    private static final String SERIAL_VERSION_UID_FIELD = "serialVersionUID";
    private static final String SERIAL_VERSION_UID_TYPE = "J";
    private static final long SERIAL_VERSION_UID_VALUE = -1L;
    public static volatile boolean useCache;
    public static volatile boolean useWriteReplace;
    private static WeakHashMap proxyCache;
    private static char[] hexDigits;
    public static ClassLoaderProvider classLoaderProvider;
    public static UniqueName nameGenerator;
    private static Comparator sorter;
    private static final String HANDLER_GETTER_KEY = "getHandler:()";
    
    public boolean isUseCache() {
        return this.factoryUseCache;
    }
    
    public void setUseCache(final boolean useCache) {
        if (this.handler != null && useCache) {
            throw new RuntimeException("caching cannot be enabled if the factory default interceptor has been set");
        }
        this.factoryUseCache = useCache;
    }
    
    public boolean isUseWriteReplace() {
        return this.factoryWriteReplace;
    }
    
    public void setUseWriteReplace(final boolean useWriteReplace) {
        this.factoryWriteReplace = useWriteReplace;
    }
    
    public static boolean isProxyClass(final Class cl) {
        return Proxy.class.isAssignableFrom(cl);
    }
    
    public ProxyFactory() {
        this.superClass = null;
        this.interfaces = null;
        this.methodFilter = null;
        this.handler = null;
        this.signature = null;
        this.signatureMethods = null;
        this.hasGetHandler = false;
        this.thisClass = null;
        this.writeDirectory = null;
        this.factoryUseCache = ProxyFactory.useCache;
        this.factoryWriteReplace = ProxyFactory.useWriteReplace;
    }
    
    public void setSuperclass(final Class clazz) {
        this.superClass = clazz;
        this.signature = null;
    }
    
    public Class getSuperclass() {
        return this.superClass;
    }
    
    public void setInterfaces(final Class[] ifs) {
        this.interfaces = ifs;
        this.signature = null;
    }
    
    public Class[] getInterfaces() {
        return this.interfaces;
    }
    
    public void setFilter(final MethodFilter mf) {
        this.methodFilter = mf;
        this.signature = null;
    }
    
    public Class createClass() {
        if (this.signature == null) {
            this.computeSignature(this.methodFilter);
        }
        return this.createClass1();
    }
    
    public Class createClass(final MethodFilter filter) {
        this.computeSignature(filter);
        return this.createClass1();
    }
    
    Class createClass(final byte[] signature) {
        this.installSignature(signature);
        return this.createClass1();
    }
    
    private Class createClass1() {
        Class result = this.thisClass;
        if (result == null) {
            final ClassLoader cl = this.getClassLoader();
            synchronized (ProxyFactory.proxyCache) {
                if (this.factoryUseCache) {
                    this.createClass2(cl);
                }
                else {
                    this.createClass3(cl);
                }
                result = this.thisClass;
                this.thisClass = null;
            }
        }
        return result;
    }
    
    public String getKey(final Class superClass, final Class[] interfaces, final byte[] signature, final boolean useWriteReplace) {
        final StringBuffer sbuf = new StringBuffer();
        if (superClass != null) {
            sbuf.append(superClass.getName());
        }
        sbuf.append(":");
        for (int i = 0; i < interfaces.length; ++i) {
            sbuf.append(interfaces[i].getName());
            sbuf.append(":");
        }
        for (int i = 0; i < signature.length; ++i) {
            final byte b = signature[i];
            final int lo = b & 0xF;
            final int hi = b >> 4 & 0xF;
            sbuf.append(ProxyFactory.hexDigits[lo]);
            sbuf.append(ProxyFactory.hexDigits[hi]);
        }
        if (useWriteReplace) {
            sbuf.append(":w");
        }
        return sbuf.toString();
    }
    
    private void createClass2(final ClassLoader cl) {
        final String key = this.getKey(this.superClass, this.interfaces, this.signature, this.factoryWriteReplace);
        HashMap cacheForTheLoader = ProxyFactory.proxyCache.get(cl);
        if (cacheForTheLoader == null) {
            cacheForTheLoader = new HashMap();
            ProxyFactory.proxyCache.put(cl, cacheForTheLoader);
        }
        ProxyDetails details = cacheForTheLoader.get(key);
        if (details != null) {
            final WeakReference reference = details.proxyClass;
            this.thisClass = (Class)reference.get();
            if (this.thisClass != null) {
                return;
            }
        }
        this.createClass3(cl);
        details = new ProxyDetails(this.signature, this.thisClass, this.factoryWriteReplace);
        cacheForTheLoader.put(key, details);
    }
    
    private void createClass3(final ClassLoader cl) {
        this.allocateClassName();
        try {
            final ClassFile cf = this.make();
            if (this.writeDirectory != null) {
                FactoryHelper.writeFile(cf, this.writeDirectory);
            }
            this.thisClass = FactoryHelper.toClass(cf, cl, this.getDomain());
            this.setField("_filter_signature", this.signature);
            if (!this.factoryUseCache) {
                this.setField("default_interceptor", this.handler);
            }
        }
        catch (CannotCompileException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    private void setField(final String fieldName, final Object value) {
        if (this.thisClass != null && value != null) {
            try {
                final Field f = this.thisClass.getField(fieldName);
                SecurityActions.setAccessible(f, true);
                f.set(null, value);
                SecurityActions.setAccessible(f, false);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    static byte[] getFilterSignature(final Class clazz) {
        return (byte[])getField(clazz, "_filter_signature");
    }
    
    private static Object getField(final Class clazz, final String fieldName) {
        try {
            final Field f = clazz.getField(fieldName);
            f.setAccessible(true);
            final Object value = f.get(null);
            f.setAccessible(false);
            return value;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static MethodHandler getHandler(final Proxy p) {
        try {
            final Field f = p.getClass().getDeclaredField("handler");
            f.setAccessible(true);
            final Object value = f.get(p);
            f.setAccessible(false);
            return (MethodHandler)value;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    protected ClassLoader getClassLoader() {
        return ProxyFactory.classLoaderProvider.get(this);
    }
    
    protected ClassLoader getClassLoader0() {
        ClassLoader loader = null;
        if (this.superClass != null && !this.superClass.getName().equals("java.lang.Object")) {
            loader = this.superClass.getClassLoader();
        }
        else if (this.interfaces != null && this.interfaces.length > 0) {
            loader = this.interfaces[0].getClassLoader();
        }
        if (loader == null) {
            loader = this.getClass().getClassLoader();
            if (loader == null) {
                loader = Thread.currentThread().getContextClassLoader();
                if (loader == null) {
                    loader = ClassLoader.getSystemClassLoader();
                }
            }
        }
        return loader;
    }
    
    protected ProtectionDomain getDomain() {
        Class clazz;
        if (this.superClass != null && !this.superClass.getName().equals("java.lang.Object")) {
            clazz = this.superClass;
        }
        else if (this.interfaces != null && this.interfaces.length > 0) {
            clazz = this.interfaces[0];
        }
        else {
            clazz = this.getClass();
        }
        return clazz.getProtectionDomain();
    }
    
    public Object create(final Class[] paramTypes, final Object[] args, final MethodHandler mh) throws NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final Object obj = this.create(paramTypes, args);
        ((Proxy)obj).setHandler(mh);
        return obj;
    }
    
    public Object create(final Class[] paramTypes, final Object[] args) throws NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final Class c = this.createClass();
        final Constructor cons = c.getConstructor((Class[])paramTypes);
        return cons.newInstance(args);
    }
    
    @Deprecated
    public void setHandler(final MethodHandler mi) {
        if (this.factoryUseCache && mi != null) {
            this.factoryUseCache = false;
            this.thisClass = null;
        }
        this.setField("default_interceptor", this.handler = mi);
    }
    
    private static String makeProxyName(final String classname) {
        synchronized (ProxyFactory.nameGenerator) {
            return ProxyFactory.nameGenerator.get(classname);
        }
    }
    
    private ClassFile make() throws CannotCompileException {
        final ClassFile cf = new ClassFile(false, this.classname, this.superName);
        cf.setAccessFlags(1);
        setInterfaces(cf, this.interfaces, (Class)(this.hasGetHandler ? Proxy.class : ProxyObject.class));
        final ConstPool pool = cf.getConstPool();
        if (!this.factoryUseCache) {
            final FieldInfo finfo = new FieldInfo(pool, "default_interceptor", ProxyFactory.HANDLER_TYPE);
            finfo.setAccessFlags(9);
            cf.addField(finfo);
        }
        final FieldInfo finfo2 = new FieldInfo(pool, "handler", ProxyFactory.HANDLER_TYPE);
        finfo2.setAccessFlags(2);
        cf.addField(finfo2);
        final FieldInfo finfo3 = new FieldInfo(pool, "_filter_signature", "[B");
        finfo3.setAccessFlags(9);
        cf.addField(finfo3);
        final FieldInfo finfo4 = new FieldInfo(pool, "serialVersionUID", "J");
        finfo4.setAccessFlags(25);
        cf.addField(finfo4);
        this.makeConstructors(this.classname, cf, pool, this.classname);
        final ArrayList forwarders = new ArrayList();
        final int s = this.overrideMethods(cf, pool, this.classname, forwarders);
        addClassInitializer(cf, pool, this.classname, s, forwarders);
        addSetter(this.classname, cf, pool);
        if (!this.hasGetHandler) {
            addGetter(this.classname, cf, pool);
        }
        if (this.factoryWriteReplace) {
            try {
                cf.addMethod(makeWriteReplace(pool));
            }
            catch (DuplicateMemberException ex) {}
        }
        this.thisClass = null;
        return cf;
    }
    
    private void checkClassAndSuperName() {
        if (this.interfaces == null) {
            this.interfaces = new Class[0];
        }
        if (this.superClass == null) {
            this.superClass = ProxyFactory.OBJECT_TYPE;
            this.superName = this.superClass.getName();
            this.basename = ((this.interfaces.length == 0) ? this.superName : this.interfaces[0].getName());
        }
        else {
            this.superName = this.superClass.getName();
            this.basename = this.superName;
        }
        if (Modifier.isFinal(this.superClass.getModifiers())) {
            throw new RuntimeException(this.superName + " is final");
        }
        if (this.basename.startsWith("java.")) {
            this.basename = "org.javassist.tmp." + this.basename;
        }
    }
    
    private void allocateClassName() {
        this.classname = makeProxyName(this.basename);
    }
    
    private void makeSortedMethodList() {
        this.checkClassAndSuperName();
        this.hasGetHandler = false;
        final HashMap allMethods = this.getMethods(this.superClass, this.interfaces);
        Collections.sort((List<Object>)(this.signatureMethods = new ArrayList(allMethods.entrySet())), ProxyFactory.sorter);
    }
    
    private void computeSignature(final MethodFilter filter) {
        this.makeSortedMethodList();
        final int l = this.signatureMethods.size();
        final int maxBytes = l + 7 >> 3;
        this.signature = new byte[maxBytes];
        for (int idx = 0; idx < l; ++idx) {
            final Map.Entry e = this.signatureMethods.get(idx);
            final Method m = e.getValue();
            final int mod = m.getModifiers();
            if (!Modifier.isFinal(mod) && !Modifier.isStatic(mod) && isVisible(mod, this.basename, m) && (filter == null || filter.isHandled(m))) {
                this.setBit(this.signature, idx);
            }
        }
    }
    
    private void installSignature(final byte[] signature) {
        this.makeSortedMethodList();
        final int l = this.signatureMethods.size();
        final int maxBytes = l + 7 >> 3;
        if (signature.length != maxBytes) {
            throw new RuntimeException("invalid filter signature length for deserialized proxy class");
        }
        this.signature = signature;
    }
    
    private boolean testBit(final byte[] signature, final int idx) {
        final int byteIdx = idx >> 3;
        if (byteIdx > signature.length) {
            return false;
        }
        final int bitIdx = idx & 0x7;
        final int mask = 1 << bitIdx;
        final int sigByte = signature[byteIdx];
        return (sigByte & mask) != 0x0;
    }
    
    private void setBit(final byte[] signature, final int idx) {
        final int byteIdx = idx >> 3;
        if (byteIdx < signature.length) {
            final int bitIdx = idx & 0x7;
            final int mask = 1 << bitIdx;
            final int sigByte = signature[byteIdx];
            signature[byteIdx] = (byte)(sigByte | mask);
        }
    }
    
    private static void setInterfaces(final ClassFile cf, final Class[] interfaces, final Class proxyClass) {
        final String setterIntf = proxyClass.getName();
        String[] list;
        if (interfaces == null || interfaces.length == 0) {
            list = new String[] { setterIntf };
        }
        else {
            list = new String[interfaces.length + 1];
            for (int i = 0; i < interfaces.length; ++i) {
                list[i] = interfaces[i].getName();
            }
            list[interfaces.length] = setterIntf;
        }
        cf.setInterfaces(list);
    }
    
    private static void addClassInitializer(final ClassFile cf, final ConstPool cp, final String classname, final int size, final ArrayList forwarders) throws CannotCompileException {
        final FieldInfo finfo = new FieldInfo(cp, "_methods_", "[Ljava/lang/reflect/Method;");
        finfo.setAccessFlags(10);
        cf.addField(finfo);
        final MethodInfo minfo = new MethodInfo(cp, "<clinit>", "()V");
        minfo.setAccessFlags(8);
        setThrows(minfo, cp, new Class[] { ClassNotFoundException.class });
        final Bytecode code = new Bytecode(cp, 0, 2);
        code.addIconst(size * 2);
        code.addAnewarray("java.lang.reflect.Method");
        final int varArray = 0;
        code.addAstore(0);
        code.addLdc(classname);
        code.addInvokestatic("java.lang.Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
        final int varClass = 1;
        code.addAstore(1);
        for (final Find2MethodsArgs args : forwarders) {
            callFind2Methods(code, args.methodName, args.delegatorName, args.origIndex, args.descriptor, 1, 0);
        }
        code.addAload(0);
        code.addPutstatic(classname, "_methods_", "[Ljava/lang/reflect/Method;");
        code.addLconst(-1L);
        code.addPutstatic(classname, "serialVersionUID", "J");
        code.addOpcode(177);
        minfo.setCodeAttribute(code.toCodeAttribute());
        cf.addMethod(minfo);
    }
    
    private static void callFind2Methods(final Bytecode code, final String superMethod, final String thisMethod, final int index, final String desc, final int classVar, final int arrayVar) {
        final String findClass = RuntimeSupport.class.getName();
        final String findDesc = "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;[Ljava/lang/reflect/Method;)V";
        code.addAload(classVar);
        code.addLdc(superMethod);
        if (thisMethod == null) {
            code.addOpcode(1);
        }
        else {
            code.addLdc(thisMethod);
        }
        code.addIconst(index);
        code.addLdc(desc);
        code.addAload(arrayVar);
        code.addInvokestatic(findClass, "find2Methods", findDesc);
    }
    
    private static void addSetter(final String classname, final ClassFile cf, final ConstPool cp) throws CannotCompileException {
        final MethodInfo minfo = new MethodInfo(cp, "setHandler", ProxyFactory.HANDLER_SETTER_TYPE);
        minfo.setAccessFlags(1);
        final Bytecode code = new Bytecode(cp, 2, 2);
        code.addAload(0);
        code.addAload(1);
        code.addPutfield(classname, "handler", ProxyFactory.HANDLER_TYPE);
        code.addOpcode(177);
        minfo.setCodeAttribute(code.toCodeAttribute());
        cf.addMethod(minfo);
    }
    
    private static void addGetter(final String classname, final ClassFile cf, final ConstPool cp) throws CannotCompileException {
        final MethodInfo minfo = new MethodInfo(cp, "getHandler", ProxyFactory.HANDLER_GETTER_TYPE);
        minfo.setAccessFlags(1);
        final Bytecode code = new Bytecode(cp, 1, 1);
        code.addAload(0);
        code.addGetfield(classname, "handler", ProxyFactory.HANDLER_TYPE);
        code.addOpcode(176);
        minfo.setCodeAttribute(code.toCodeAttribute());
        cf.addMethod(minfo);
    }
    
    private int overrideMethods(final ClassFile cf, final ConstPool cp, final String className, final ArrayList forwarders) throws CannotCompileException {
        final String prefix = makeUniqueName("_d", this.signatureMethods);
        final Iterator it = this.signatureMethods.iterator();
        int index = 0;
        while (it.hasNext()) {
            final Map.Entry e = it.next();
            final String key = e.getKey();
            final Method meth = e.getValue();
            if ((ClassFile.MAJOR_VERSION < 49 || !isBridge(meth)) && this.testBit(this.signature, index)) {
                this.override(className, meth, prefix, index, keyToDesc(key, meth), cf, cp, forwarders);
            }
            ++index;
        }
        return index;
    }
    
    private static boolean isBridge(final Method m) {
        return m.isBridge();
    }
    
    private void override(final String thisClassname, final Method meth, final String prefix, final int index, final String desc, final ClassFile cf, final ConstPool cp, final ArrayList forwarders) throws CannotCompileException {
        final Class declClass = meth.getDeclaringClass();
        String delegatorName = prefix + index + meth.getName();
        if (Modifier.isAbstract(meth.getModifiers())) {
            delegatorName = null;
        }
        else {
            final MethodInfo delegator = this.makeDelegator(meth, desc, cp, declClass, delegatorName);
            delegator.setAccessFlags(delegator.getAccessFlags() & 0xFFFFFFBF);
            cf.addMethod(delegator);
        }
        final MethodInfo forwarder = makeForwarder(thisClassname, meth, desc, cp, declClass, delegatorName, index, forwarders);
        cf.addMethod(forwarder);
    }
    
    private void makeConstructors(final String thisClassName, final ClassFile cf, final ConstPool cp, final String classname) throws CannotCompileException {
        final Constructor[] cons = SecurityActions.getDeclaredConstructors(this.superClass);
        final boolean doHandlerInit = !this.factoryUseCache;
        for (int i = 0; i < cons.length; ++i) {
            final Constructor c = cons[i];
            final int mod = c.getModifiers();
            if (!Modifier.isFinal(mod) && !Modifier.isPrivate(mod) && isVisible(mod, this.basename, c)) {
                final MethodInfo m = makeConstructor(thisClassName, c, cp, this.superClass, doHandlerInit);
                cf.addMethod(m);
            }
        }
    }
    
    private static String makeUniqueName(final String name, final List sortedMethods) {
        if (makeUniqueName0(name, sortedMethods.iterator())) {
            return name;
        }
        for (int i = 100; i < 999; ++i) {
            final String s = name + i;
            if (makeUniqueName0(s, sortedMethods.iterator())) {
                return s;
            }
        }
        throw new RuntimeException("cannot make a unique method name");
    }
    
    private static boolean makeUniqueName0(final String name, final Iterator it) {
        while (it.hasNext()) {
            final Map.Entry e = it.next();
            final String key = e.getKey();
            if (key.startsWith(name)) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean isVisible(final int mod, final String from, final Member meth) {
        if ((mod & 0x2) != 0x0) {
            return false;
        }
        if ((mod & 0x5) != 0x0) {
            return true;
        }
        final String p = getPackageName(from);
        final String q = getPackageName(meth.getDeclaringClass().getName());
        if (p == null) {
            return q == null;
        }
        return p.equals(q);
    }
    
    private static String getPackageName(final String name) {
        final int i = name.lastIndexOf(46);
        if (i < 0) {
            return null;
        }
        return name.substring(0, i);
    }
    
    private HashMap getMethods(final Class superClass, final Class[] interfaceTypes) {
        final HashMap hash = new HashMap();
        final HashSet set = new HashSet();
        for (int i = 0; i < interfaceTypes.length; ++i) {
            this.getMethods(hash, interfaceTypes[i], set);
        }
        this.getMethods(hash, superClass, set);
        return hash;
    }
    
    private void getMethods(final HashMap hash, final Class clazz, final Set visitedClasses) {
        if (!visitedClasses.add(clazz)) {
            return;
        }
        final Class[] ifs = clazz.getInterfaces();
        for (int i = 0; i < ifs.length; ++i) {
            this.getMethods(hash, ifs[i], visitedClasses);
        }
        final Class parent = clazz.getSuperclass();
        if (parent != null) {
            this.getMethods(hash, parent, visitedClasses);
        }
        final Method[] methods = SecurityActions.getDeclaredMethods(clazz);
        for (int j = 0; j < methods.length; ++j) {
            if (!Modifier.isPrivate(methods[j].getModifiers())) {
                final Method m = methods[j];
                final String key = m.getName() + ':' + RuntimeSupport.makeDescriptor(m);
                if (key.startsWith("getHandler:()")) {
                    this.hasGetHandler = true;
                }
                final Method oldMethod = hash.put(key, m);
                if (null != oldMethod && isBridge(m) && !Modifier.isPublic(oldMethod.getDeclaringClass().getModifiers()) && !Modifier.isAbstract(oldMethod.getModifiers()) && !isOverloaded(j, methods)) {
                    hash.put(key, oldMethod);
                }
                if (null != oldMethod && Modifier.isPublic(oldMethod.getModifiers()) && !Modifier.isPublic(m.getModifiers())) {
                    hash.put(key, oldMethod);
                }
            }
        }
    }
    
    private static boolean isOverloaded(final int index, final Method[] methods) {
        final String name = methods[index].getName();
        for (int i = 0; i < methods.length; ++i) {
            if (i != index && name.equals(methods[i].getName())) {
                return true;
            }
        }
        return false;
    }
    
    private static String keyToDesc(final String key, final Method m) {
        return key.substring(key.indexOf(58) + 1);
    }
    
    private static MethodInfo makeConstructor(final String thisClassName, final Constructor cons, final ConstPool cp, final Class superClass, final boolean doHandlerInit) {
        final String desc = RuntimeSupport.makeDescriptor(cons.getParameterTypes(), Void.TYPE);
        final MethodInfo minfo = new MethodInfo(cp, "<init>", desc);
        minfo.setAccessFlags(1);
        setThrows(minfo, cp, cons.getExceptionTypes());
        final Bytecode code = new Bytecode(cp, 0, 0);
        if (doHandlerInit) {
            code.addAload(0);
            code.addGetstatic(thisClassName, "default_interceptor", ProxyFactory.HANDLER_TYPE);
            code.addPutfield(thisClassName, "handler", ProxyFactory.HANDLER_TYPE);
            code.addGetstatic(thisClassName, "default_interceptor", ProxyFactory.HANDLER_TYPE);
            code.addOpcode(199);
            code.addIndex(10);
        }
        code.addAload(0);
        code.addGetstatic("javassist.util.proxy.RuntimeSupport", "default_interceptor", ProxyFactory.HANDLER_TYPE);
        code.addPutfield(thisClassName, "handler", ProxyFactory.HANDLER_TYPE);
        final int pc = code.currentPc();
        code.addAload(0);
        final int s = addLoadParameters(code, cons.getParameterTypes(), 1);
        code.addInvokespecial(superClass.getName(), "<init>", desc);
        code.addOpcode(177);
        code.setMaxLocals(s + 1);
        final CodeAttribute ca = code.toCodeAttribute();
        minfo.setCodeAttribute(ca);
        final StackMapTable.Writer writer = new StackMapTable.Writer(32);
        writer.sameFrame(pc);
        ca.setAttribute(writer.toStackMapTable(cp));
        return minfo;
    }
    
    private MethodInfo makeDelegator(final Method meth, final String desc, final ConstPool cp, final Class declClass, final String delegatorName) {
        final MethodInfo delegator = new MethodInfo(cp, delegatorName, desc);
        delegator.setAccessFlags(0x11 | (meth.getModifiers() & 0xFFFFFAD9));
        setThrows(delegator, cp, meth);
        final Bytecode code = new Bytecode(cp, 0, 0);
        code.addAload(0);
        int s = addLoadParameters(code, meth.getParameterTypes(), 1);
        final Class targetClass = this.invokespecialTarget(declClass);
        code.addInvokespecial(targetClass.isInterface(), cp.addClassInfo(targetClass.getName()), meth.getName(), desc);
        addReturn(code, meth.getReturnType());
        code.setMaxLocals(++s);
        delegator.setCodeAttribute(code.toCodeAttribute());
        return delegator;
    }
    
    private Class invokespecialTarget(final Class declClass) {
        if (declClass.isInterface()) {
            for (final Class i : this.interfaces) {
                if (declClass.isAssignableFrom(i)) {
                    return i;
                }
            }
        }
        return this.superClass;
    }
    
    private static MethodInfo makeForwarder(final String thisClassName, final Method meth, final String desc, final ConstPool cp, final Class declClass, final String delegatorName, final int index, final ArrayList forwarders) {
        final MethodInfo forwarder = new MethodInfo(cp, meth.getName(), desc);
        forwarder.setAccessFlags(0x10 | (meth.getModifiers() & 0xFFFFFADF));
        setThrows(forwarder, cp, meth);
        final int args = Descriptor.paramSize(desc);
        final Bytecode code = new Bytecode(cp, 0, args + 2);
        final int origIndex = index * 2;
        final int delIndex = index * 2 + 1;
        final int arrayVar = args + 1;
        code.addGetstatic(thisClassName, "_methods_", "[Ljava/lang/reflect/Method;");
        code.addAstore(arrayVar);
        forwarders.add(new Find2MethodsArgs(meth.getName(), delegatorName, desc, origIndex));
        code.addAload(0);
        code.addGetfield(thisClassName, "handler", ProxyFactory.HANDLER_TYPE);
        code.addAload(0);
        code.addAload(arrayVar);
        code.addIconst(origIndex);
        code.addOpcode(50);
        code.addAload(arrayVar);
        code.addIconst(delIndex);
        code.addOpcode(50);
        makeParameterList(code, meth.getParameterTypes());
        code.addInvokeinterface(MethodHandler.class.getName(), "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", 5);
        final Class retType = meth.getReturnType();
        addUnwrapper(code, retType);
        addReturn(code, retType);
        final CodeAttribute ca = code.toCodeAttribute();
        forwarder.setCodeAttribute(ca);
        return forwarder;
    }
    
    private static void setThrows(final MethodInfo minfo, final ConstPool cp, final Method orig) {
        final Class[] exceptions = orig.getExceptionTypes();
        setThrows(minfo, cp, exceptions);
    }
    
    private static void setThrows(final MethodInfo minfo, final ConstPool cp, final Class[] exceptions) {
        if (exceptions.length == 0) {
            return;
        }
        final String[] list = new String[exceptions.length];
        for (int i = 0; i < exceptions.length; ++i) {
            list[i] = exceptions[i].getName();
        }
        final ExceptionsAttribute ea = new ExceptionsAttribute(cp);
        ea.setExceptions(list);
        minfo.setExceptionsAttribute(ea);
    }
    
    private static int addLoadParameters(final Bytecode code, final Class[] params, final int offset) {
        int stacksize = 0;
        for (int n = params.length, i = 0; i < n; ++i) {
            stacksize += addLoad(code, stacksize + offset, params[i]);
        }
        return stacksize;
    }
    
    private static int addLoad(final Bytecode code, final int n, final Class type) {
        if (type.isPrimitive()) {
            if (type == Long.TYPE) {
                code.addLload(n);
                return 2;
            }
            if (type == Float.TYPE) {
                code.addFload(n);
            }
            else {
                if (type == Double.TYPE) {
                    code.addDload(n);
                    return 2;
                }
                code.addIload(n);
            }
        }
        else {
            code.addAload(n);
        }
        return 1;
    }
    
    private static int addReturn(final Bytecode code, final Class type) {
        if (type.isPrimitive()) {
            if (type == Long.TYPE) {
                code.addOpcode(173);
                return 2;
            }
            if (type == Float.TYPE) {
                code.addOpcode(174);
            }
            else {
                if (type == Double.TYPE) {
                    code.addOpcode(175);
                    return 2;
                }
                if (type == Void.TYPE) {
                    code.addOpcode(177);
                    return 0;
                }
                code.addOpcode(172);
            }
        }
        else {
            code.addOpcode(176);
        }
        return 1;
    }
    
    private static void makeParameterList(final Bytecode code, final Class[] params) {
        int regno = 1;
        final int n = params.length;
        code.addIconst(n);
        code.addAnewarray("java/lang/Object");
        for (int i = 0; i < n; ++i) {
            code.addOpcode(89);
            code.addIconst(i);
            final Class type = params[i];
            if (type.isPrimitive()) {
                regno = makeWrapper(code, type, regno);
            }
            else {
                code.addAload(regno);
                ++regno;
            }
            code.addOpcode(83);
        }
    }
    
    private static int makeWrapper(final Bytecode code, final Class type, final int regno) {
        final int index = FactoryHelper.typeIndex(type);
        final String wrapper = FactoryHelper.wrapperTypes[index];
        code.addNew(wrapper);
        code.addOpcode(89);
        addLoad(code, regno, type);
        code.addInvokespecial(wrapper, "<init>", FactoryHelper.wrapperDesc[index]);
        return regno + FactoryHelper.dataSize[index];
    }
    
    private static void addUnwrapper(final Bytecode code, final Class type) {
        if (type.isPrimitive()) {
            if (type == Void.TYPE) {
                code.addOpcode(87);
            }
            else {
                final int index = FactoryHelper.typeIndex(type);
                final String wrapper = FactoryHelper.wrapperTypes[index];
                code.addCheckcast(wrapper);
                code.addInvokevirtual(wrapper, FactoryHelper.unwarpMethods[index], FactoryHelper.unwrapDesc[index]);
            }
        }
        else {
            code.addCheckcast(type.getName());
        }
    }
    
    private static MethodInfo makeWriteReplace(final ConstPool cp) {
        final MethodInfo minfo = new MethodInfo(cp, "writeReplace", "()Ljava/lang/Object;");
        final String[] list = { "java.io.ObjectStreamException" };
        final ExceptionsAttribute ea = new ExceptionsAttribute(cp);
        ea.setExceptions(list);
        minfo.setExceptionsAttribute(ea);
        final Bytecode code = new Bytecode(cp, 0, 1);
        code.addAload(0);
        code.addInvokestatic("javassist.util.proxy.RuntimeSupport", "makeSerializedProxy", "(Ljava/lang/Object;)Ljavassist/util/proxy/SerializedProxy;");
        code.addOpcode(176);
        minfo.setCodeAttribute(code.toCodeAttribute());
        return minfo;
    }
    
    static {
        OBJECT_TYPE = Object.class;
        HANDLER_TYPE = 'L' + MethodHandler.class.getName().replace('.', '/') + ';';
        HANDLER_SETTER_TYPE = "(" + ProxyFactory.HANDLER_TYPE + ")V";
        HANDLER_GETTER_TYPE = "()" + ProxyFactory.HANDLER_TYPE;
        ProxyFactory.useCache = true;
        ProxyFactory.useWriteReplace = true;
        ProxyFactory.proxyCache = new WeakHashMap();
        ProxyFactory.hexDigits = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        ProxyFactory.classLoaderProvider = new ClassLoaderProvider() {
            @Override
            public ClassLoader get(final ProxyFactory pf) {
                return pf.getClassLoader0();
            }
        };
        ProxyFactory.nameGenerator = new UniqueName() {
            private final String sep = "_$$_jvst" + Integer.toHexString(this.hashCode() & 0xFFF) + "_";
            private int counter = 0;
            
            @Override
            public String get(final String classname) {
                return classname + this.sep + Integer.toHexString(this.counter++);
            }
        };
        ProxyFactory.sorter = new Comparator() {
            @Override
            public int compare(final Object o1, final Object o2) {
                final Map.Entry e1 = (Map.Entry)o1;
                final Map.Entry e2 = (Map.Entry)o2;
                final String key1 = e1.getKey();
                final String key2 = e2.getKey();
                return key1.compareTo(key2);
            }
        };
    }
    
    static class ProxyDetails
    {
        byte[] signature;
        WeakReference proxyClass;
        boolean isUseWriteReplace;
        
        ProxyDetails(final byte[] signature, final Class proxyClass, final boolean isUseWriteReplace) {
            this.signature = signature;
            this.proxyClass = new WeakReference((T)proxyClass);
            this.isUseWriteReplace = isUseWriteReplace;
        }
    }
    
    static class Find2MethodsArgs
    {
        String methodName;
        String delegatorName;
        String descriptor;
        int origIndex;
        
        Find2MethodsArgs(final String mname, final String dname, final String desc, final int index) {
            this.methodName = mname;
            this.delegatorName = dname;
            this.descriptor = desc;
            this.origIndex = index;
        }
    }
    
    public interface UniqueName
    {
        String get(final String p0);
    }
    
    public interface ClassLoaderProvider
    {
        ClassLoader get(final ProxyFactory p0);
    }
}

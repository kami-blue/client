// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import java.util.Set;
import javassist.compiler.CompileError;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.BadBytecode;
import javassist.compiler.Javac;
import javassist.expr.ExprEditor;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ConstantAttribute;
import java.util.HashMap;
import java.util.List;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.EnclosingMethodAttribute;
import javassist.bytecode.annotation.AnnotationImpl;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.AnnotationsAttribute;
import java.util.ArrayList;
import javassist.bytecode.InnerClassesAttribute;
import javassist.bytecode.AccessFlag;
import java.util.Map;
import javassist.bytecode.Descriptor;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.SignatureAttribute;
import java.net.URL;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import javassist.compiler.AccessorMaker;
import java.lang.ref.WeakReference;
import javassist.bytecode.ClassFile;

class CtClassType extends CtClass
{
    ClassPool classPool;
    boolean wasChanged;
    private boolean wasFrozen;
    boolean wasPruned;
    boolean gcConstPool;
    ClassFile classfile;
    byte[] rawClassfile;
    private WeakReference memberCache;
    private AccessorMaker accessors;
    private FieldInitLink fieldInitializers;
    private Hashtable hiddenMethods;
    private int uniqueNumberSeed;
    private boolean doPruning;
    private int getCount;
    private static final int GET_THRESHOLD = 2;
    
    CtClassType(final String name, final ClassPool cp) {
        super(name);
        this.doPruning = ClassPool.doPruning;
        this.classPool = cp;
        final boolean b = false;
        this.gcConstPool = b;
        this.wasPruned = b;
        this.wasFrozen = b;
        this.wasChanged = b;
        this.classfile = null;
        this.rawClassfile = null;
        this.memberCache = null;
        this.accessors = null;
        this.fieldInitializers = null;
        this.hiddenMethods = null;
        this.uniqueNumberSeed = 0;
        this.getCount = 0;
    }
    
    CtClassType(final InputStream ins, final ClassPool cp) throws IOException {
        this((String)null, cp);
        this.classfile = new ClassFile(new DataInputStream(ins));
        this.qualifiedName = this.classfile.getName();
    }
    
    CtClassType(final ClassFile cf, final ClassPool cp) {
        this((String)null, cp);
        this.classfile = cf;
        this.qualifiedName = this.classfile.getName();
    }
    
    @Override
    protected void extendToString(final StringBuffer buffer) {
        if (this.wasChanged) {
            buffer.append("changed ");
        }
        if (this.wasFrozen) {
            buffer.append("frozen ");
        }
        if (this.wasPruned) {
            buffer.append("pruned ");
        }
        buffer.append(Modifier.toString(this.getModifiers()));
        buffer.append(" class ");
        buffer.append(this.getName());
        try {
            final CtClass ext = this.getSuperclass();
            if (ext != null) {
                final String name = ext.getName();
                if (!name.equals("java.lang.Object")) {
                    buffer.append(" extends " + ext.getName());
                }
            }
        }
        catch (NotFoundException e) {
            buffer.append(" extends ??");
        }
        try {
            final CtClass[] intf = this.getInterfaces();
            if (intf.length > 0) {
                buffer.append(" implements ");
            }
            for (int i = 0; i < intf.length; ++i) {
                buffer.append(intf[i].getName());
                buffer.append(", ");
            }
        }
        catch (NotFoundException e) {
            buffer.append(" extends ??");
        }
        final CtMember.Cache memCache = this.getMembers();
        this.exToString(buffer, " fields=", memCache.fieldHead(), memCache.lastField());
        this.exToString(buffer, " constructors=", memCache.consHead(), memCache.lastCons());
        this.exToString(buffer, " methods=", memCache.methodHead(), memCache.lastMethod());
    }
    
    private void exToString(final StringBuffer buffer, final String msg, CtMember head, final CtMember tail) {
        buffer.append(msg);
        while (head != tail) {
            head = head.next();
            buffer.append(head);
            buffer.append(", ");
        }
    }
    
    @Override
    public AccessorMaker getAccessorMaker() {
        if (this.accessors == null) {
            this.accessors = new AccessorMaker(this);
        }
        return this.accessors;
    }
    
    @Override
    public ClassFile getClassFile2() {
        return this.getClassFile3(true);
    }
    
    public ClassFile getClassFile3(final boolean doCompress) {
        final ClassFile cfile = this.classfile;
        if (cfile != null) {
            return cfile;
        }
        if (doCompress) {
            this.classPool.compress();
        }
        if (this.rawClassfile != null) {
            try {
                final ClassFile cf = new ClassFile(new DataInputStream(new ByteArrayInputStream(this.rawClassfile)));
                this.rawClassfile = null;
                this.getCount = 2;
                return this.setClassFile(cf);
            }
            catch (IOException e) {
                throw new RuntimeException(e.toString(), e);
            }
        }
        InputStream fin = null;
        try {
            fin = this.classPool.openClassfile(this.getName());
            if (fin == null) {
                throw new NotFoundException(this.getName());
            }
            fin = new BufferedInputStream(fin);
            final ClassFile cf2 = new ClassFile(new DataInputStream(fin));
            if (!cf2.getName().equals(this.qualifiedName)) {
                throw new RuntimeException("cannot find " + this.qualifiedName + ": " + cf2.getName() + " found in " + this.qualifiedName.replace('.', '/') + ".class");
            }
            return this.setClassFile(cf2);
        }
        catch (NotFoundException e2) {
            throw new RuntimeException(e2.toString(), e2);
        }
        catch (IOException e3) {
            throw new RuntimeException(e3.toString(), e3);
        }
        finally {
            if (fin != null) {
                try {
                    fin.close();
                }
                catch (IOException ex) {}
            }
        }
    }
    
    @Override
    final void incGetCounter() {
        ++this.getCount;
    }
    
    @Override
    void compress() {
        if (this.getCount < 2) {
            if (!this.isModified() && ClassPool.releaseUnmodifiedClassFile) {
                this.removeClassFile();
            }
            else if (this.isFrozen() && !this.wasPruned) {
                this.saveClassFile();
            }
        }
        this.getCount = 0;
    }
    
    private synchronized void saveClassFile() {
        if (this.classfile == null || this.hasMemberCache() != null) {
            return;
        }
        final ByteArrayOutputStream barray = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(barray);
        try {
            this.classfile.write(out);
            barray.close();
            this.rawClassfile = barray.toByteArray();
            this.classfile = null;
        }
        catch (IOException ex) {}
    }
    
    private synchronized void removeClassFile() {
        if (this.classfile != null && !this.isModified() && this.hasMemberCache() == null) {
            this.classfile = null;
        }
    }
    
    private synchronized ClassFile setClassFile(final ClassFile cf) {
        if (this.classfile == null) {
            this.classfile = cf;
        }
        return this.classfile;
    }
    
    @Override
    public ClassPool getClassPool() {
        return this.classPool;
    }
    
    void setClassPool(final ClassPool cp) {
        this.classPool = cp;
    }
    
    @Override
    public URL getURL() throws NotFoundException {
        final URL url = this.classPool.find(this.getName());
        if (url == null) {
            throw new NotFoundException(this.getName());
        }
        return url;
    }
    
    @Override
    public boolean isModified() {
        return this.wasChanged;
    }
    
    @Override
    public boolean isFrozen() {
        return this.wasFrozen;
    }
    
    @Override
    public void freeze() {
        this.wasFrozen = true;
    }
    
    @Override
    void checkModify() throws RuntimeException {
        if (this.isFrozen()) {
            String msg = this.getName() + " class is frozen";
            if (this.wasPruned) {
                msg += " and pruned";
            }
            throw new RuntimeException(msg);
        }
        this.wasChanged = true;
    }
    
    @Override
    public void defrost() {
        this.checkPruned("defrost");
        this.wasFrozen = false;
    }
    
    @Override
    public boolean subtypeOf(final CtClass clazz) throws NotFoundException {
        final String cname = clazz.getName();
        if (this == clazz || this.getName().equals(cname)) {
            return true;
        }
        final ClassFile file = this.getClassFile2();
        final String supername = file.getSuperclass();
        if (supername != null && supername.equals(cname)) {
            return true;
        }
        final String[] ifs = file.getInterfaces();
        final int num = ifs.length;
        for (int i = 0; i < num; ++i) {
            if (ifs[i].equals(cname)) {
                return true;
            }
        }
        if (supername != null && this.classPool.get(supername).subtypeOf(clazz)) {
            return true;
        }
        for (int i = 0; i < num; ++i) {
            if (this.classPool.get(ifs[i]).subtypeOf(clazz)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void setName(final String name) throws RuntimeException {
        final String oldname = this.getName();
        if (name.equals(oldname)) {
            return;
        }
        this.classPool.checkNotFrozen(name);
        final ClassFile cf = this.getClassFile2();
        super.setName(name);
        cf.setName(name);
        this.nameReplaced();
        this.classPool.classNameChanged(oldname, this);
    }
    
    @Override
    public String getGenericSignature() {
        final SignatureAttribute sa = (SignatureAttribute)this.getClassFile2().getAttribute("Signature");
        return (sa == null) ? null : sa.getSignature();
    }
    
    @Override
    public void setGenericSignature(final String sig) {
        final ClassFile cf = this.getClassFile();
        final SignatureAttribute sa = new SignatureAttribute(cf.getConstPool(), sig);
        cf.addAttribute(sa);
    }
    
    @Override
    public void replaceClassName(final ClassMap classnames) throws RuntimeException {
        final String oldClassName = this.getName();
        String newClassName = (String)classnames.get(Descriptor.toJvmName(oldClassName));
        if (newClassName != null) {
            newClassName = Descriptor.toJavaName(newClassName);
            this.classPool.checkNotFrozen(newClassName);
        }
        super.replaceClassName(classnames);
        final ClassFile cf = this.getClassFile2();
        cf.renameClass(classnames);
        this.nameReplaced();
        if (newClassName != null) {
            super.setName(newClassName);
            this.classPool.classNameChanged(oldClassName, this);
        }
    }
    
    @Override
    public void replaceClassName(final String oldname, final String newname) throws RuntimeException {
        final String thisname = this.getName();
        if (thisname.equals(oldname)) {
            this.setName(newname);
        }
        else {
            super.replaceClassName(oldname, newname);
            this.getClassFile2().renameClass(oldname, newname);
            this.nameReplaced();
        }
    }
    
    @Override
    public boolean isInterface() {
        return Modifier.isInterface(this.getModifiers());
    }
    
    @Override
    public boolean isAnnotation() {
        return Modifier.isAnnotation(this.getModifiers());
    }
    
    @Override
    public boolean isEnum() {
        return Modifier.isEnum(this.getModifiers());
    }
    
    @Override
    public int getModifiers() {
        final ClassFile cf = this.getClassFile2();
        int acc = cf.getAccessFlags();
        acc = AccessFlag.clear(acc, 32);
        final int inner = cf.getInnerAccessFlags();
        if (inner != -1 && (inner & 0x8) != 0x0) {
            acc |= 0x8;
        }
        return AccessFlag.toModifier(acc);
    }
    
    @Override
    public CtClass[] getNestedClasses() throws NotFoundException {
        final ClassFile cf = this.getClassFile2();
        final InnerClassesAttribute ica = (InnerClassesAttribute)cf.getAttribute("InnerClasses");
        if (ica == null) {
            return new CtClass[0];
        }
        final String thisName = cf.getName() + "$";
        final int n = ica.tableLength();
        final ArrayList list = new ArrayList(n);
        for (int i = 0; i < n; ++i) {
            final String name = ica.innerClass(i);
            if (name != null && name.startsWith(thisName) && name.lastIndexOf(36) < thisName.length()) {
                list.add(this.classPool.get(name));
            }
        }
        return list.toArray(new CtClass[list.size()]);
    }
    
    @Override
    public void setModifiers(int mod) {
        final ClassFile cf = this.getClassFile2();
        if (Modifier.isStatic(mod)) {
            final int flags = cf.getInnerAccessFlags();
            if (flags == -1 || (flags & 0x8) == 0x0) {
                throw new RuntimeException("cannot change " + this.getName() + " into a static class");
            }
            mod &= 0xFFFFFFF7;
        }
        this.checkModify();
        cf.setAccessFlags(AccessFlag.of(mod));
    }
    
    @Override
    public boolean hasAnnotation(final String annotationName) {
        final ClassFile cf = this.getClassFile2();
        final AnnotationsAttribute ainfo = (AnnotationsAttribute)cf.getAttribute("RuntimeInvisibleAnnotations");
        final AnnotationsAttribute ainfo2 = (AnnotationsAttribute)cf.getAttribute("RuntimeVisibleAnnotations");
        return hasAnnotationType(annotationName, this.getClassPool(), ainfo, ainfo2);
    }
    
    @Deprecated
    static boolean hasAnnotationType(final Class clz, final ClassPool cp, final AnnotationsAttribute a1, final AnnotationsAttribute a2) {
        return hasAnnotationType(clz.getName(), cp, a1, a2);
    }
    
    static boolean hasAnnotationType(final String annotationTypeName, final ClassPool cp, final AnnotationsAttribute a1, final AnnotationsAttribute a2) {
        Annotation[] anno1;
        if (a1 == null) {
            anno1 = null;
        }
        else {
            anno1 = a1.getAnnotations();
        }
        Annotation[] anno2;
        if (a2 == null) {
            anno2 = null;
        }
        else {
            anno2 = a2.getAnnotations();
        }
        if (anno1 != null) {
            for (int i = 0; i < anno1.length; ++i) {
                if (anno1[i].getTypeName().equals(annotationTypeName)) {
                    return true;
                }
            }
        }
        if (anno2 != null) {
            for (int i = 0; i < anno2.length; ++i) {
                if (anno2[i].getTypeName().equals(annotationTypeName)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public Object getAnnotation(final Class clz) throws ClassNotFoundException {
        final ClassFile cf = this.getClassFile2();
        final AnnotationsAttribute ainfo = (AnnotationsAttribute)cf.getAttribute("RuntimeInvisibleAnnotations");
        final AnnotationsAttribute ainfo2 = (AnnotationsAttribute)cf.getAttribute("RuntimeVisibleAnnotations");
        return getAnnotationType(clz, this.getClassPool(), ainfo, ainfo2);
    }
    
    static Object getAnnotationType(final Class clz, final ClassPool cp, final AnnotationsAttribute a1, final AnnotationsAttribute a2) throws ClassNotFoundException {
        Annotation[] anno1;
        if (a1 == null) {
            anno1 = null;
        }
        else {
            anno1 = a1.getAnnotations();
        }
        Annotation[] anno2;
        if (a2 == null) {
            anno2 = null;
        }
        else {
            anno2 = a2.getAnnotations();
        }
        final String typeName = clz.getName();
        if (anno1 != null) {
            for (int i = 0; i < anno1.length; ++i) {
                if (anno1[i].getTypeName().equals(typeName)) {
                    return toAnnoType(anno1[i], cp);
                }
            }
        }
        if (anno2 != null) {
            for (int i = 0; i < anno2.length; ++i) {
                if (anno2[i].getTypeName().equals(typeName)) {
                    return toAnnoType(anno2[i], cp);
                }
            }
        }
        return null;
    }
    
    @Override
    public Object[] getAnnotations() throws ClassNotFoundException {
        return this.getAnnotations(false);
    }
    
    @Override
    public Object[] getAvailableAnnotations() {
        try {
            return this.getAnnotations(true);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Unexpected exception ", e);
        }
    }
    
    private Object[] getAnnotations(final boolean ignoreNotFound) throws ClassNotFoundException {
        final ClassFile cf = this.getClassFile2();
        final AnnotationsAttribute ainfo = (AnnotationsAttribute)cf.getAttribute("RuntimeInvisibleAnnotations");
        final AnnotationsAttribute ainfo2 = (AnnotationsAttribute)cf.getAttribute("RuntimeVisibleAnnotations");
        return toAnnotationType(ignoreNotFound, this.getClassPool(), ainfo, ainfo2);
    }
    
    static Object[] toAnnotationType(final boolean ignoreNotFound, final ClassPool cp, final AnnotationsAttribute a1, final AnnotationsAttribute a2) throws ClassNotFoundException {
        Annotation[] anno1;
        int size1;
        if (a1 == null) {
            anno1 = null;
            size1 = 0;
        }
        else {
            anno1 = a1.getAnnotations();
            size1 = anno1.length;
        }
        Annotation[] anno2;
        int size2;
        if (a2 == null) {
            anno2 = null;
            size2 = 0;
        }
        else {
            anno2 = a2.getAnnotations();
            size2 = anno2.length;
        }
        if (!ignoreNotFound) {
            final Object[] result = new Object[size1 + size2];
            for (int i = 0; i < size1; ++i) {
                result[i] = toAnnoType(anno1[i], cp);
            }
            for (int j = 0; j < size2; ++j) {
                result[j + size1] = toAnnoType(anno2[j], cp);
            }
            return result;
        }
        final ArrayList annotations = new ArrayList();
        for (int i = 0; i < size1; ++i) {
            try {
                annotations.add(toAnnoType(anno1[i], cp));
            }
            catch (ClassNotFoundException ex) {}
        }
        for (int j = 0; j < size2; ++j) {
            try {
                annotations.add(toAnnoType(anno2[j], cp));
            }
            catch (ClassNotFoundException ex2) {}
        }
        return annotations.toArray();
    }
    
    static Object[][] toAnnotationType(final boolean ignoreNotFound, final ClassPool cp, final ParameterAnnotationsAttribute a1, final ParameterAnnotationsAttribute a2, final MethodInfo minfo) throws ClassNotFoundException {
        int numParameters = 0;
        if (a1 != null) {
            numParameters = a1.numParameters();
        }
        else if (a2 != null) {
            numParameters = a2.numParameters();
        }
        else {
            numParameters = Descriptor.numOfParameters(minfo.getDescriptor());
        }
        final Object[][] result = new Object[numParameters][];
        for (int i = 0; i < numParameters; ++i) {
            Annotation[] anno1;
            int size1;
            if (a1 == null) {
                anno1 = null;
                size1 = 0;
            }
            else {
                anno1 = a1.getAnnotations()[i];
                size1 = anno1.length;
            }
            Annotation[] anno2;
            int size2;
            if (a2 == null) {
                anno2 = null;
                size2 = 0;
            }
            else {
                anno2 = a2.getAnnotations()[i];
                size2 = anno2.length;
            }
            if (!ignoreNotFound) {
                result[i] = new Object[size1 + size2];
                for (int j = 0; j < size1; ++j) {
                    result[i][j] = toAnnoType(anno1[j], cp);
                }
                for (int j = 0; j < size2; ++j) {
                    result[i][j + size1] = toAnnoType(anno2[j], cp);
                }
            }
            else {
                final ArrayList annotations = new ArrayList();
                for (int k = 0; k < size1; ++k) {
                    try {
                        annotations.add(toAnnoType(anno1[k], cp));
                    }
                    catch (ClassNotFoundException ex) {}
                }
                for (int k = 0; k < size2; ++k) {
                    try {
                        annotations.add(toAnnoType(anno2[k], cp));
                    }
                    catch (ClassNotFoundException ex2) {}
                }
                result[i] = annotations.toArray();
            }
        }
        return result;
    }
    
    private static Object toAnnoType(final Annotation anno, final ClassPool cp) throws ClassNotFoundException {
        try {
            final ClassLoader cl = cp.getClassLoader();
            return anno.toAnnotationType(cl, cp);
        }
        catch (ClassNotFoundException e) {
            final ClassLoader cl2 = cp.getClass().getClassLoader();
            try {
                return anno.toAnnotationType(cl2, cp);
            }
            catch (ClassNotFoundException e2) {
                try {
                    final Class clazz = cp.get(anno.getTypeName()).toClass();
                    return AnnotationImpl.make(clazz.getClassLoader(), clazz, cp, anno);
                }
                catch (Throwable e3) {
                    throw new ClassNotFoundException(anno.getTypeName());
                }
            }
        }
    }
    
    @Override
    public boolean subclassOf(final CtClass superclass) {
        if (superclass == null) {
            return false;
        }
        final String superName = superclass.getName();
        CtClass curr = this;
        try {
            while (curr != null) {
                if (curr.getName().equals(superName)) {
                    return true;
                }
                curr = curr.getSuperclass();
            }
        }
        catch (Exception ex) {}
        return false;
    }
    
    @Override
    public CtClass getSuperclass() throws NotFoundException {
        final String supername = this.getClassFile2().getSuperclass();
        if (supername == null) {
            return null;
        }
        return this.classPool.get(supername);
    }
    
    @Override
    public void setSuperclass(final CtClass clazz) throws CannotCompileException {
        this.checkModify();
        if (this.isInterface()) {
            this.addInterface(clazz);
        }
        else {
            this.getClassFile2().setSuperclass(clazz.getName());
        }
    }
    
    @Override
    public CtClass[] getInterfaces() throws NotFoundException {
        final String[] ifs = this.getClassFile2().getInterfaces();
        final int num = ifs.length;
        final CtClass[] ifc = new CtClass[num];
        for (int i = 0; i < num; ++i) {
            ifc[i] = this.classPool.get(ifs[i]);
        }
        return ifc;
    }
    
    @Override
    public void setInterfaces(final CtClass[] list) {
        this.checkModify();
        String[] ifs;
        if (list == null) {
            ifs = new String[0];
        }
        else {
            final int num = list.length;
            ifs = new String[num];
            for (int i = 0; i < num; ++i) {
                ifs[i] = list[i].getName();
            }
        }
        this.getClassFile2().setInterfaces(ifs);
    }
    
    @Override
    public void addInterface(final CtClass anInterface) {
        this.checkModify();
        if (anInterface != null) {
            this.getClassFile2().addInterface(anInterface.getName());
        }
    }
    
    @Override
    public CtClass getDeclaringClass() throws NotFoundException {
        final ClassFile cf = this.getClassFile2();
        final InnerClassesAttribute ica = (InnerClassesAttribute)cf.getAttribute("InnerClasses");
        if (ica == null) {
            return null;
        }
        final String name = this.getName();
        for (int n = ica.tableLength(), i = 0; i < n; ++i) {
            if (name.equals(ica.innerClass(i))) {
                final String outName = ica.outerClass(i);
                if (outName != null) {
                    return this.classPool.get(outName);
                }
                final EnclosingMethodAttribute ema = (EnclosingMethodAttribute)cf.getAttribute("EnclosingMethod");
                if (ema != null) {
                    return this.classPool.get(ema.className());
                }
            }
        }
        return null;
    }
    
    @Override
    public CtBehavior getEnclosingBehavior() throws NotFoundException {
        final ClassFile cf = this.getClassFile2();
        final EnclosingMethodAttribute ema = (EnclosingMethodAttribute)cf.getAttribute("EnclosingMethod");
        if (ema == null) {
            return null;
        }
        final CtClass enc = this.classPool.get(ema.className());
        final String name = ema.methodName();
        if ("<init>".equals(name)) {
            return enc.getConstructor(ema.methodDescriptor());
        }
        if ("<clinit>".equals(name)) {
            return enc.getClassInitializer();
        }
        return enc.getMethod(name, ema.methodDescriptor());
    }
    
    @Override
    public CtClass makeNestedClass(final String name, final boolean isStatic) {
        if (!isStatic) {
            throw new RuntimeException("sorry, only nested static class is supported");
        }
        this.checkModify();
        final CtClass c = this.classPool.makeNestedClass(this.getName() + "$" + name);
        final ClassFile cf = this.getClassFile2();
        final ClassFile cf2 = c.getClassFile2();
        InnerClassesAttribute ica = (InnerClassesAttribute)cf.getAttribute("InnerClasses");
        if (ica == null) {
            ica = new InnerClassesAttribute(cf.getConstPool());
            cf.addAttribute(ica);
        }
        ica.append(c.getName(), this.getName(), name, (cf2.getAccessFlags() & 0xFFFFFFDF) | 0x8);
        cf2.addAttribute(ica.copy(cf2.getConstPool(), null));
        return c;
    }
    
    private void nameReplaced() {
        final CtMember.Cache cache = this.hasMemberCache();
        if (cache != null) {
            CtMember mth = cache.methodHead();
            final CtMember tail = cache.lastMethod();
            while (mth != tail) {
                mth = mth.next();
                mth.nameReplaced();
            }
        }
    }
    
    protected CtMember.Cache hasMemberCache() {
        final WeakReference cache = this.memberCache;
        if (cache != null) {
            return (CtMember.Cache)cache.get();
        }
        return null;
    }
    
    protected synchronized CtMember.Cache getMembers() {
        CtMember.Cache cache = null;
        if (this.memberCache == null || (cache = (CtMember.Cache)this.memberCache.get()) == null) {
            cache = new CtMember.Cache(this);
            this.makeFieldCache(cache);
            this.makeBehaviorCache(cache);
            this.memberCache = new WeakReference((T)cache);
        }
        return cache;
    }
    
    private void makeFieldCache(final CtMember.Cache cache) {
        final List list = this.getClassFile3(false).getFields();
        for (int n = list.size(), i = 0; i < n; ++i) {
            final FieldInfo finfo = list.get(i);
            final CtField newField = new CtField(finfo, this);
            cache.addField(newField);
        }
    }
    
    private void makeBehaviorCache(final CtMember.Cache cache) {
        final List list = this.getClassFile3(false).getMethods();
        for (int n = list.size(), i = 0; i < n; ++i) {
            final MethodInfo minfo = list.get(i);
            if (minfo.isMethod()) {
                final CtMethod newMethod = new CtMethod(minfo, this);
                cache.addMethod(newMethod);
            }
            else {
                final CtConstructor newCons = new CtConstructor(minfo, this);
                cache.addConstructor(newCons);
            }
        }
    }
    
    @Override
    public CtField[] getFields() {
        final ArrayList alist = new ArrayList();
        getFields(alist, this);
        return alist.toArray(new CtField[alist.size()]);
    }
    
    private static void getFields(final ArrayList alist, final CtClass cc) {
        if (cc == null) {
            return;
        }
        try {
            getFields(alist, cc.getSuperclass());
        }
        catch (NotFoundException ex) {}
        try {
            final CtClass[] ifs = cc.getInterfaces();
            for (int num = ifs.length, i = 0; i < num; ++i) {
                getFields(alist, ifs[i]);
            }
        }
        catch (NotFoundException ex2) {}
        final CtMember.Cache memCache = ((CtClassType)cc).getMembers();
        CtMember field = memCache.fieldHead();
        final CtMember tail = memCache.lastField();
        while (field != tail) {
            field = field.next();
            if (!Modifier.isPrivate(field.getModifiers())) {
                alist.add(field);
            }
        }
    }
    
    @Override
    public CtField getField(final String name, final String desc) throws NotFoundException {
        final CtField f = this.getField2(name, desc);
        return this.checkGetField(f, name, desc);
    }
    
    private CtField checkGetField(final CtField f, final String name, final String desc) throws NotFoundException {
        if (f == null) {
            String msg = "field: " + name;
            if (desc != null) {
                msg = msg + " type " + desc;
            }
            throw new NotFoundException(msg + " in " + this.getName());
        }
        return f;
    }
    
    @Override
    CtField getField2(final String name, final String desc) {
        final CtField df = this.getDeclaredField2(name, desc);
        if (df != null) {
            return df;
        }
        try {
            final CtClass[] ifs = this.getInterfaces();
            for (int num = ifs.length, i = 0; i < num; ++i) {
                final CtField f = ifs[i].getField2(name, desc);
                if (f != null) {
                    return f;
                }
            }
            final CtClass s = this.getSuperclass();
            if (s != null) {
                return s.getField2(name, desc);
            }
        }
        catch (NotFoundException ex) {}
        return null;
    }
    
    @Override
    public CtField[] getDeclaredFields() {
        final CtMember.Cache memCache = this.getMembers();
        CtMember field = memCache.fieldHead();
        final CtMember tail = memCache.lastField();
        final int num = CtMember.Cache.count(field, tail);
        final CtField[] cfs = new CtField[num];
        for (int i = 0; field != tail; field = field.next(), cfs[i++] = (CtField)field) {}
        return cfs;
    }
    
    @Override
    public CtField getDeclaredField(final String name) throws NotFoundException {
        return this.getDeclaredField(name, null);
    }
    
    @Override
    public CtField getDeclaredField(final String name, final String desc) throws NotFoundException {
        final CtField f = this.getDeclaredField2(name, desc);
        return this.checkGetField(f, name, desc);
    }
    
    private CtField getDeclaredField2(final String name, final String desc) {
        final CtMember.Cache memCache = this.getMembers();
        CtMember field = memCache.fieldHead();
        final CtMember tail = memCache.lastField();
        while (field != tail) {
            field = field.next();
            if (field.getName().equals(name) && (desc == null || desc.equals(field.getSignature()))) {
                return (CtField)field;
            }
        }
        return null;
    }
    
    @Override
    public CtBehavior[] getDeclaredBehaviors() {
        final CtMember.Cache memCache = this.getMembers();
        CtMember cons = memCache.consHead();
        final CtMember consTail = memCache.lastCons();
        final int cnum = CtMember.Cache.count(cons, consTail);
        CtMember mth = memCache.methodHead();
        final CtMember mthTail = memCache.lastMethod();
        final int mnum = CtMember.Cache.count(mth, mthTail);
        CtBehavior[] cb;
        int i;
        for (cb = new CtBehavior[cnum + mnum], i = 0; cons != consTail; cons = cons.next(), cb[i++] = (CtBehavior)cons) {}
        while (mth != mthTail) {
            mth = mth.next();
            cb[i++] = (CtBehavior)mth;
        }
        return cb;
    }
    
    @Override
    public CtConstructor[] getConstructors() {
        final CtMember.Cache memCache = this.getMembers();
        final CtMember cons = memCache.consHead();
        final CtMember consTail = memCache.lastCons();
        int n = 0;
        CtMember mem = cons;
        while (mem != consTail) {
            mem = mem.next();
            if (isPubCons((CtConstructor)mem)) {
                ++n;
            }
        }
        final CtConstructor[] result = new CtConstructor[n];
        int i = 0;
        mem = cons;
        while (mem != consTail) {
            mem = mem.next();
            final CtConstructor cc = (CtConstructor)mem;
            if (isPubCons(cc)) {
                result[i++] = cc;
            }
        }
        return result;
    }
    
    private static boolean isPubCons(final CtConstructor cons) {
        return !Modifier.isPrivate(cons.getModifiers()) && cons.isConstructor();
    }
    
    @Override
    public CtConstructor getConstructor(final String desc) throws NotFoundException {
        final CtMember.Cache memCache = this.getMembers();
        CtMember cons = memCache.consHead();
        final CtMember consTail = memCache.lastCons();
        while (cons != consTail) {
            cons = cons.next();
            final CtConstructor cc = (CtConstructor)cons;
            if (cc.getMethodInfo2().getDescriptor().equals(desc) && cc.isConstructor()) {
                return cc;
            }
        }
        return super.getConstructor(desc);
    }
    
    @Override
    public CtConstructor[] getDeclaredConstructors() {
        final CtMember.Cache memCache = this.getMembers();
        final CtMember cons = memCache.consHead();
        final CtMember consTail = memCache.lastCons();
        int n = 0;
        CtMember mem = cons;
        while (mem != consTail) {
            mem = mem.next();
            final CtConstructor cc = (CtConstructor)mem;
            if (cc.isConstructor()) {
                ++n;
            }
        }
        final CtConstructor[] result = new CtConstructor[n];
        int i = 0;
        mem = cons;
        while (mem != consTail) {
            mem = mem.next();
            final CtConstructor cc2 = (CtConstructor)mem;
            if (cc2.isConstructor()) {
                result[i++] = cc2;
            }
        }
        return result;
    }
    
    @Override
    public CtConstructor getClassInitializer() {
        final CtMember.Cache memCache = this.getMembers();
        CtMember cons = memCache.consHead();
        final CtMember consTail = memCache.lastCons();
        while (cons != consTail) {
            cons = cons.next();
            final CtConstructor cc = (CtConstructor)cons;
            if (cc.isClassInitializer()) {
                return cc;
            }
        }
        return null;
    }
    
    @Override
    public CtMethod[] getMethods() {
        final HashMap h = new HashMap();
        getMethods0(h, this);
        return (CtMethod[])h.values().toArray(new CtMethod[h.size()]);
    }
    
    private static void getMethods0(final HashMap h, final CtClass cc) {
        try {
            final CtClass[] ifs = cc.getInterfaces();
            for (int size = ifs.length, i = 0; i < size; ++i) {
                getMethods0(h, ifs[i]);
            }
        }
        catch (NotFoundException ex) {}
        try {
            final CtClass s = cc.getSuperclass();
            if (s != null) {
                getMethods0(h, s);
            }
        }
        catch (NotFoundException ex2) {}
        if (cc instanceof CtClassType) {
            final CtMember.Cache memCache = ((CtClassType)cc).getMembers();
            CtMember mth = memCache.methodHead();
            final CtMember mthTail = memCache.lastMethod();
            while (mth != mthTail) {
                mth = mth.next();
                if (!Modifier.isPrivate(mth.getModifiers())) {
                    h.put(((CtMethod)mth).getStringRep(), mth);
                }
            }
        }
    }
    
    @Override
    public CtMethod getMethod(final String name, final String desc) throws NotFoundException {
        final CtMethod m = getMethod0(this, name, desc);
        if (m != null) {
            return m;
        }
        throw new NotFoundException(name + "(..) is not found in " + this.getName());
    }
    
    private static CtMethod getMethod0(final CtClass cc, final String name, final String desc) {
        if (cc instanceof CtClassType) {
            final CtMember.Cache memCache = ((CtClassType)cc).getMembers();
            CtMember mth = memCache.methodHead();
            final CtMember mthTail = memCache.lastMethod();
            while (mth != mthTail) {
                mth = mth.next();
                if (mth.getName().equals(name) && ((CtMethod)mth).getMethodInfo2().getDescriptor().equals(desc)) {
                    return (CtMethod)mth;
                }
            }
        }
        try {
            final CtClass s = cc.getSuperclass();
            if (s != null) {
                final CtMethod m = getMethod0(s, name, desc);
                if (m != null) {
                    return m;
                }
            }
        }
        catch (NotFoundException ex) {}
        try {
            final CtClass[] ifs = cc.getInterfaces();
            for (int size = ifs.length, i = 0; i < size; ++i) {
                final CtMethod j = getMethod0(ifs[i], name, desc);
                if (j != null) {
                    return j;
                }
            }
        }
        catch (NotFoundException ex2) {}
        return null;
    }
    
    @Override
    public CtMethod[] getDeclaredMethods() {
        final CtMember.Cache memCache = this.getMembers();
        CtMember mth = memCache.methodHead();
        final CtMember mthTail = memCache.lastMethod();
        final int num = CtMember.Cache.count(mth, mthTail);
        final CtMethod[] cms = new CtMethod[num];
        for (int i = 0; mth != mthTail; mth = mth.next(), cms[i++] = (CtMethod)mth) {}
        return cms;
    }
    
    @Override
    public CtMethod[] getDeclaredMethods(final String name) throws NotFoundException {
        final CtMember.Cache memCache = this.getMembers();
        CtMember mth = memCache.methodHead();
        final CtMember mthTail = memCache.lastMethod();
        final ArrayList methods = new ArrayList();
        while (mth != mthTail) {
            mth = mth.next();
            if (mth.getName().equals(name)) {
                methods.add(mth);
            }
        }
        return methods.toArray(new CtMethod[methods.size()]);
    }
    
    @Override
    public CtMethod getDeclaredMethod(final String name) throws NotFoundException {
        final CtMember.Cache memCache = this.getMembers();
        CtMember mth = memCache.methodHead();
        final CtMember mthTail = memCache.lastMethod();
        while (mth != mthTail) {
            mth = mth.next();
            if (mth.getName().equals(name)) {
                return (CtMethod)mth;
            }
        }
        throw new NotFoundException(name + "(..) is not found in " + this.getName());
    }
    
    @Override
    public CtMethod getDeclaredMethod(final String name, final CtClass[] params) throws NotFoundException {
        final String desc = Descriptor.ofParameters(params);
        final CtMember.Cache memCache = this.getMembers();
        CtMember mth = memCache.methodHead();
        final CtMember mthTail = memCache.lastMethod();
        while (mth != mthTail) {
            mth = mth.next();
            if (mth.getName().equals(name) && ((CtMethod)mth).getMethodInfo2().getDescriptor().startsWith(desc)) {
                return (CtMethod)mth;
            }
        }
        throw new NotFoundException(name + "(..) is not found in " + this.getName());
    }
    
    @Override
    public void addField(final CtField f, final String init) throws CannotCompileException {
        this.addField(f, CtField.Initializer.byExpr(init));
    }
    
    @Override
    public void addField(final CtField f, CtField.Initializer init) throws CannotCompileException {
        this.checkModify();
        if (f.getDeclaringClass() != this) {
            throw new CannotCompileException("cannot add");
        }
        if (init == null) {
            init = f.getInit();
        }
        if (init != null) {
            init.check(f.getSignature());
            final int mod = f.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isFinal(mod)) {
                try {
                    final ConstPool cp = this.getClassFile2().getConstPool();
                    final int index = init.getConstantValue(cp, f.getType());
                    if (index != 0) {
                        f.getFieldInfo2().addAttribute(new ConstantAttribute(cp, index));
                        init = null;
                    }
                }
                catch (NotFoundException ex) {}
            }
        }
        this.getMembers().addField(f);
        this.getClassFile2().addField(f.getFieldInfo2());
        if (init != null) {
            final FieldInitLink fil = new FieldInitLink(f, init);
            FieldInitLink link = this.fieldInitializers;
            if (link == null) {
                this.fieldInitializers = fil;
            }
            else {
                while (link.next != null) {
                    link = link.next;
                }
                link.next = fil;
            }
        }
    }
    
    @Override
    public void removeField(final CtField f) throws NotFoundException {
        this.checkModify();
        final FieldInfo fi = f.getFieldInfo2();
        final ClassFile cf = this.getClassFile2();
        if (cf.getFields().remove(fi)) {
            this.getMembers().remove(f);
            this.gcConstPool = true;
            return;
        }
        throw new NotFoundException(f.toString());
    }
    
    @Override
    public CtConstructor makeClassInitializer() throws CannotCompileException {
        final CtConstructor clinit = this.getClassInitializer();
        if (clinit != null) {
            return clinit;
        }
        this.checkModify();
        final ClassFile cf = this.getClassFile2();
        final Bytecode code = new Bytecode(cf.getConstPool(), 0, 0);
        this.modifyClassConstructor(cf, code, 0, 0);
        return this.getClassInitializer();
    }
    
    @Override
    public void addConstructor(final CtConstructor c) throws CannotCompileException {
        this.checkModify();
        if (c.getDeclaringClass() != this) {
            throw new CannotCompileException("cannot add");
        }
        this.getMembers().addConstructor(c);
        this.getClassFile2().addMethod(c.getMethodInfo2());
    }
    
    @Override
    public void removeConstructor(final CtConstructor m) throws NotFoundException {
        this.checkModify();
        final MethodInfo mi = m.getMethodInfo2();
        final ClassFile cf = this.getClassFile2();
        if (cf.getMethods().remove(mi)) {
            this.getMembers().remove(m);
            this.gcConstPool = true;
            return;
        }
        throw new NotFoundException(m.toString());
    }
    
    @Override
    public void addMethod(final CtMethod m) throws CannotCompileException {
        this.checkModify();
        if (m.getDeclaringClass() != this) {
            throw new CannotCompileException("bad declaring class");
        }
        final int mod = m.getModifiers();
        if ((this.getModifiers() & 0x200) != 0x0) {
            if (Modifier.isProtected(mod) || Modifier.isPrivate(mod)) {
                throw new CannotCompileException("an interface method must be public: " + m.toString());
            }
            m.setModifiers(mod | 0x1);
        }
        this.getMembers().addMethod(m);
        this.getClassFile2().addMethod(m.getMethodInfo2());
        if ((mod & 0x400) != 0x0) {
            this.setModifiers(this.getModifiers() | 0x400);
        }
    }
    
    @Override
    public void removeMethod(final CtMethod m) throws NotFoundException {
        this.checkModify();
        final MethodInfo mi = m.getMethodInfo2();
        final ClassFile cf = this.getClassFile2();
        if (cf.getMethods().remove(mi)) {
            this.getMembers().remove(m);
            this.gcConstPool = true;
            return;
        }
        throw new NotFoundException(m.toString());
    }
    
    @Override
    public byte[] getAttribute(final String name) {
        final AttributeInfo ai = this.getClassFile2().getAttribute(name);
        if (ai == null) {
            return null;
        }
        return ai.get();
    }
    
    @Override
    public void setAttribute(final String name, final byte[] data) {
        this.checkModify();
        final ClassFile cf = this.getClassFile2();
        cf.addAttribute(new AttributeInfo(cf.getConstPool(), name, data));
    }
    
    @Override
    public void instrument(final CodeConverter converter) throws CannotCompileException {
        this.checkModify();
        final ClassFile cf = this.getClassFile2();
        final ConstPool cp = cf.getConstPool();
        final List list = cf.getMethods();
        for (int n = list.size(), i = 0; i < n; ++i) {
            final MethodInfo minfo = list.get(i);
            converter.doit(this, minfo, cp);
        }
    }
    
    @Override
    public void instrument(final ExprEditor editor) throws CannotCompileException {
        this.checkModify();
        final ClassFile cf = this.getClassFile2();
        final List list = cf.getMethods();
        for (int n = list.size(), i = 0; i < n; ++i) {
            final MethodInfo minfo = list.get(i);
            editor.doit(this, minfo);
        }
    }
    
    @Override
    public void prune() {
        if (this.wasPruned) {
            return;
        }
        final boolean b = true;
        this.wasFrozen = b;
        this.wasPruned = b;
        this.getClassFile2().prune();
    }
    
    @Override
    public void rebuildClassFile() {
        this.gcConstPool = true;
    }
    
    @Override
    public void toBytecode(final DataOutputStream out) throws CannotCompileException, IOException {
        try {
            if (this.isModified()) {
                this.checkPruned("toBytecode");
                final ClassFile cf = this.getClassFile2();
                if (this.gcConstPool) {
                    cf.compact();
                    this.gcConstPool = false;
                }
                this.modifyClassConstructor(cf);
                this.modifyConstructors(cf);
                if (CtClassType.debugDump != null) {
                    this.dumpClassFile(cf);
                }
                cf.write(out);
                out.flush();
                this.fieldInitializers = null;
                if (this.doPruning) {
                    cf.prune();
                    this.wasPruned = true;
                }
            }
            else {
                this.classPool.writeClassfile(this.getName(), out);
            }
            this.getCount = 0;
            this.wasFrozen = true;
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        catch (IOException e2) {
            throw new CannotCompileException(e2);
        }
    }
    
    private void dumpClassFile(final ClassFile cf) throws IOException {
        final DataOutputStream dump = this.makeFileOutput(CtClassType.debugDump);
        try {
            cf.write(dump);
        }
        finally {
            dump.close();
        }
    }
    
    private void checkPruned(final String method) {
        if (this.wasPruned) {
            throw new RuntimeException(method + "(): " + this.getName() + " was pruned.");
        }
    }
    
    @Override
    public boolean stopPruning(final boolean stop) {
        final boolean prev = !this.doPruning;
        this.doPruning = !stop;
        return prev;
    }
    
    private void modifyClassConstructor(final ClassFile cf) throws CannotCompileException, NotFoundException {
        if (this.fieldInitializers == null) {
            return;
        }
        final Bytecode code = new Bytecode(cf.getConstPool(), 0, 0);
        final Javac jv = new Javac(code, this);
        int stacksize = 0;
        boolean doInit = false;
        for (FieldInitLink fi = this.fieldInitializers; fi != null; fi = fi.next) {
            final CtField f = fi.field;
            if (Modifier.isStatic(f.getModifiers())) {
                doInit = true;
                final int s = fi.init.compileIfStatic(f.getType(), f.getName(), code, jv);
                if (stacksize < s) {
                    stacksize = s;
                }
            }
        }
        if (doInit) {
            this.modifyClassConstructor(cf, code, stacksize, 0);
        }
    }
    
    private void modifyClassConstructor(final ClassFile cf, final Bytecode code, final int stacksize, final int localsize) throws CannotCompileException {
        MethodInfo m = cf.getStaticInitializer();
        if (m == null) {
            code.add(177);
            code.setMaxStack(stacksize);
            code.setMaxLocals(localsize);
            m = new MethodInfo(cf.getConstPool(), "<clinit>", "()V");
            m.setAccessFlags(8);
            m.setCodeAttribute(code.toCodeAttribute());
            cf.addMethod(m);
            final CtMember.Cache cache = this.hasMemberCache();
            if (cache != null) {
                cache.addConstructor(new CtConstructor(m, this));
            }
        }
        else {
            final CodeAttribute codeAttr = m.getCodeAttribute();
            if (codeAttr == null) {
                throw new CannotCompileException("empty <clinit>");
            }
            try {
                final CodeIterator it = codeAttr.iterator();
                final int pos = it.insertEx(code.get());
                it.insert(code.getExceptionTable(), pos);
                final int maxstack = codeAttr.getMaxStack();
                if (maxstack < stacksize) {
                    codeAttr.setMaxStack(stacksize);
                }
                final int maxlocals = codeAttr.getMaxLocals();
                if (maxlocals < localsize) {
                    codeAttr.setMaxLocals(localsize);
                }
            }
            catch (BadBytecode e) {
                throw new CannotCompileException(e);
            }
        }
        try {
            m.rebuildStackMapIf6(this.classPool, cf);
        }
        catch (BadBytecode e2) {
            throw new CannotCompileException(e2);
        }
    }
    
    private void modifyConstructors(final ClassFile cf) throws CannotCompileException, NotFoundException {
        if (this.fieldInitializers == null) {
            return;
        }
        final ConstPool cp = cf.getConstPool();
        final List list = cf.getMethods();
        for (int n = list.size(), i = 0; i < n; ++i) {
            final MethodInfo minfo = list.get(i);
            if (minfo.isConstructor()) {
                final CodeAttribute codeAttr = minfo.getCodeAttribute();
                if (codeAttr != null) {
                    try {
                        final Bytecode init = new Bytecode(cp, 0, codeAttr.getMaxLocals());
                        final CtClass[] params = Descriptor.getParameterTypes(minfo.getDescriptor(), this.classPool);
                        final int stacksize = this.makeFieldInitializer(init, params);
                        insertAuxInitializer(codeAttr, init, stacksize);
                        minfo.rebuildStackMapIf6(this.classPool, cf);
                    }
                    catch (BadBytecode e) {
                        throw new CannotCompileException(e);
                    }
                }
            }
        }
    }
    
    private static void insertAuxInitializer(final CodeAttribute codeAttr, final Bytecode initializer, final int stacksize) throws BadBytecode {
        final CodeIterator it = codeAttr.iterator();
        int index = it.skipSuperConstructor();
        if (index < 0) {
            index = it.skipThisConstructor();
            if (index >= 0) {
                return;
            }
        }
        final int pos = it.insertEx(initializer.get());
        it.insert(initializer.getExceptionTable(), pos);
        final int maxstack = codeAttr.getMaxStack();
        if (maxstack < stacksize) {
            codeAttr.setMaxStack(stacksize);
        }
    }
    
    private int makeFieldInitializer(final Bytecode code, final CtClass[] parameters) throws CannotCompileException, NotFoundException {
        int stacksize = 0;
        final Javac jv = new Javac(code, this);
        try {
            jv.recordParams(parameters, false);
        }
        catch (CompileError e) {
            throw new CannotCompileException(e);
        }
        for (FieldInitLink fi = this.fieldInitializers; fi != null; fi = fi.next) {
            final CtField f = fi.field;
            if (!Modifier.isStatic(f.getModifiers())) {
                final int s = fi.init.compile(f.getType(), f.getName(), code, parameters, jv);
                if (stacksize < s) {
                    stacksize = s;
                }
            }
        }
        return stacksize;
    }
    
    Hashtable getHiddenMethods() {
        if (this.hiddenMethods == null) {
            this.hiddenMethods = new Hashtable();
        }
        return this.hiddenMethods;
    }
    
    int getUniqueNumber() {
        return this.uniqueNumberSeed++;
    }
    
    @Override
    public String makeUniqueName(final String prefix) {
        final HashMap table = new HashMap();
        this.makeMemberList(table);
        final Set keys = table.keySet();
        final String[] methods = new String[keys.size()];
        keys.toArray(methods);
        if (notFindInArray(prefix, methods)) {
            return prefix;
        }
        int i = 100;
        while (i <= 999) {
            final String name = prefix + i++;
            if (notFindInArray(name, methods)) {
                return name;
            }
        }
        throw new RuntimeException("too many unique name");
    }
    
    private static boolean notFindInArray(final String prefix, final String[] values) {
        for (int len = values.length, i = 0; i < len; ++i) {
            if (values[i].startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }
    
    private void makeMemberList(final HashMap table) {
        final int mod = this.getModifiers();
        Label_0076: {
            if (!Modifier.isAbstract(mod)) {
                if (!Modifier.isInterface(mod)) {
                    break Label_0076;
                }
            }
            try {
                for (final CtClass ic : this.getInterfaces()) {
                    if (ic != null && ic instanceof CtClassType) {
                        ((CtClassType)ic).makeMemberList(table);
                    }
                }
            }
            catch (NotFoundException ex) {}
            try {
                final CtClass s = this.getSuperclass();
                if (s != null && s instanceof CtClassType) {
                    ((CtClassType)s).makeMemberList(table);
                }
            }
            catch (NotFoundException ex2) {}
        }
        List list = this.getClassFile2().getMethods();
        for (int n = list.size(), i = 0; i < n; ++i) {
            final MethodInfo minfo = list.get(i);
            table.put(minfo.getName(), this);
        }
        list = this.getClassFile2().getFields();
        for (int n = list.size(), i = 0; i < n; ++i) {
            final FieldInfo finfo = list.get(i);
            table.put(finfo.getName(), this);
        }
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.security.ProtectionDomain;
import javassist.expr.ExprEditor;
import javassist.bytecode.Descriptor;
import java.util.Map;
import java.util.Collection;
import java.net.URL;
import javassist.compiler.AccessorMaker;
import javassist.bytecode.ClassFile;

public abstract class CtClass
{
    protected String qualifiedName;
    public static String debugDump;
    public static final String version = "3.21.0-GA";
    static final String javaLangObject = "java.lang.Object";
    public static CtClass booleanType;
    public static CtClass charType;
    public static CtClass byteType;
    public static CtClass shortType;
    public static CtClass intType;
    public static CtClass longType;
    public static CtClass floatType;
    public static CtClass doubleType;
    public static CtClass voidType;
    static CtClass[] primitiveTypes;
    
    public static void main(final String[] args) {
        System.out.println("Javassist version 3.21.0-GA");
        System.out.println("Copyright (C) 1999-2016 Shigeru Chiba. All Rights Reserved.");
    }
    
    protected CtClass(final String name) {
        this.qualifiedName = name;
    }
    
    @Override
    public String toString() {
        final StringBuffer buf = new StringBuffer(this.getClass().getName());
        buf.append("@");
        buf.append(Integer.toHexString(this.hashCode()));
        buf.append("[");
        this.extendToString(buf);
        buf.append("]");
        return buf.toString();
    }
    
    protected void extendToString(final StringBuffer buffer) {
        buffer.append(this.getName());
    }
    
    public ClassPool getClassPool() {
        return null;
    }
    
    public ClassFile getClassFile() {
        this.checkModify();
        return this.getClassFile2();
    }
    
    public ClassFile getClassFile2() {
        return null;
    }
    
    public AccessorMaker getAccessorMaker() {
        return null;
    }
    
    public URL getURL() throws NotFoundException {
        throw new NotFoundException(this.getName());
    }
    
    public boolean isModified() {
        return false;
    }
    
    public boolean isFrozen() {
        return true;
    }
    
    public void freeze() {
    }
    
    void checkModify() throws RuntimeException {
        if (this.isFrozen()) {
            throw new RuntimeException(this.getName() + " class is frozen");
        }
    }
    
    public void defrost() {
        throw new RuntimeException("cannot defrost " + this.getName());
    }
    
    public boolean isPrimitive() {
        return false;
    }
    
    public boolean isArray() {
        return false;
    }
    
    public CtClass getComponentType() throws NotFoundException {
        return null;
    }
    
    public boolean subtypeOf(final CtClass clazz) throws NotFoundException {
        return this == clazz || this.getName().equals(clazz.getName());
    }
    
    public String getName() {
        return this.qualifiedName;
    }
    
    public final String getSimpleName() {
        final String qname = this.qualifiedName;
        final int index = qname.lastIndexOf(46);
        if (index < 0) {
            return qname;
        }
        return qname.substring(index + 1);
    }
    
    public final String getPackageName() {
        final String qname = this.qualifiedName;
        final int index = qname.lastIndexOf(46);
        if (index < 0) {
            return null;
        }
        return qname.substring(0, index);
    }
    
    public void setName(final String name) {
        this.checkModify();
        if (name != null) {
            this.qualifiedName = name;
        }
    }
    
    public String getGenericSignature() {
        return null;
    }
    
    public void setGenericSignature(final String sig) {
        this.checkModify();
    }
    
    public void replaceClassName(final String oldName, final String newName) {
        this.checkModify();
    }
    
    public void replaceClassName(final ClassMap map) {
        this.checkModify();
    }
    
    public synchronized Collection getRefClasses() {
        final ClassFile cf = this.getClassFile2();
        if (cf != null) {
            final ClassMap cm = new ClassMap() {
                @Override
                public void put(final String oldname, final String newname) {
                    this.put0(oldname, newname);
                }
                
                @Override
                public Object get(final Object jvmClassName) {
                    final String n = ClassMap.toJavaName((String)jvmClassName);
                    this.put0(n, n);
                    return null;
                }
                
                @Override
                public void fix(final String name) {
                }
            };
            cf.getRefClasses(cm);
            return cm.values();
        }
        return null;
    }
    
    public boolean isInterface() {
        return false;
    }
    
    public boolean isAnnotation() {
        return false;
    }
    
    public boolean isEnum() {
        return false;
    }
    
    public int getModifiers() {
        return 0;
    }
    
    public boolean hasAnnotation(final Class annotationType) {
        return this.hasAnnotation(annotationType.getName());
    }
    
    public boolean hasAnnotation(final String annotationTypeName) {
        return false;
    }
    
    public Object getAnnotation(final Class clz) throws ClassNotFoundException {
        return null;
    }
    
    public Object[] getAnnotations() throws ClassNotFoundException {
        return new Object[0];
    }
    
    public Object[] getAvailableAnnotations() {
        return new Object[0];
    }
    
    public CtClass[] getDeclaredClasses() throws NotFoundException {
        return this.getNestedClasses();
    }
    
    public CtClass[] getNestedClasses() throws NotFoundException {
        return new CtClass[0];
    }
    
    public void setModifiers(final int mod) {
        this.checkModify();
    }
    
    public boolean subclassOf(final CtClass superclass) {
        return false;
    }
    
    public CtClass getSuperclass() throws NotFoundException {
        return null;
    }
    
    public void setSuperclass(final CtClass clazz) throws CannotCompileException {
        this.checkModify();
    }
    
    public CtClass[] getInterfaces() throws NotFoundException {
        return new CtClass[0];
    }
    
    public void setInterfaces(final CtClass[] list) {
        this.checkModify();
    }
    
    public void addInterface(final CtClass anInterface) {
        this.checkModify();
    }
    
    public CtClass getDeclaringClass() throws NotFoundException {
        return null;
    }
    
    @Deprecated
    public final CtMethod getEnclosingMethod() throws NotFoundException {
        final CtBehavior b = this.getEnclosingBehavior();
        if (b == null) {
            return null;
        }
        if (b instanceof CtMethod) {
            return (CtMethod)b;
        }
        throw new NotFoundException(b.getLongName() + " is enclosing " + this.getName());
    }
    
    public CtBehavior getEnclosingBehavior() throws NotFoundException {
        return null;
    }
    
    public CtClass makeNestedClass(final String name, final boolean isStatic) {
        throw new RuntimeException(this.getName() + " is not a class");
    }
    
    public CtField[] getFields() {
        return new CtField[0];
    }
    
    public CtField getField(final String name) throws NotFoundException {
        return this.getField(name, null);
    }
    
    public CtField getField(final String name, final String desc) throws NotFoundException {
        throw new NotFoundException(name);
    }
    
    CtField getField2(final String name, final String desc) {
        return null;
    }
    
    public CtField[] getDeclaredFields() {
        return new CtField[0];
    }
    
    public CtField getDeclaredField(final String name) throws NotFoundException {
        throw new NotFoundException(name);
    }
    
    public CtField getDeclaredField(final String name, final String desc) throws NotFoundException {
        throw new NotFoundException(name);
    }
    
    public CtBehavior[] getDeclaredBehaviors() {
        return new CtBehavior[0];
    }
    
    public CtConstructor[] getConstructors() {
        return new CtConstructor[0];
    }
    
    public CtConstructor getConstructor(final String desc) throws NotFoundException {
        throw new NotFoundException("no such constructor");
    }
    
    public CtConstructor[] getDeclaredConstructors() {
        return new CtConstructor[0];
    }
    
    public CtConstructor getDeclaredConstructor(final CtClass[] params) throws NotFoundException {
        final String desc = Descriptor.ofConstructor(params);
        return this.getConstructor(desc);
    }
    
    public CtConstructor getClassInitializer() {
        return null;
    }
    
    public CtMethod[] getMethods() {
        return new CtMethod[0];
    }
    
    public CtMethod getMethod(final String name, final String desc) throws NotFoundException {
        throw new NotFoundException(name);
    }
    
    public CtMethod[] getDeclaredMethods() {
        return new CtMethod[0];
    }
    
    public CtMethod getDeclaredMethod(final String name, final CtClass[] params) throws NotFoundException {
        throw new NotFoundException(name);
    }
    
    public CtMethod[] getDeclaredMethods(final String name) throws NotFoundException {
        throw new NotFoundException(name);
    }
    
    public CtMethod getDeclaredMethod(final String name) throws NotFoundException {
        throw new NotFoundException(name);
    }
    
    public CtConstructor makeClassInitializer() throws CannotCompileException {
        throw new CannotCompileException("not a class");
    }
    
    public void addConstructor(final CtConstructor c) throws CannotCompileException {
        this.checkModify();
    }
    
    public void removeConstructor(final CtConstructor c) throws NotFoundException {
        this.checkModify();
    }
    
    public void addMethod(final CtMethod m) throws CannotCompileException {
        this.checkModify();
    }
    
    public void removeMethod(final CtMethod m) throws NotFoundException {
        this.checkModify();
    }
    
    public void addField(final CtField f) throws CannotCompileException {
        this.addField(f, (CtField.Initializer)null);
    }
    
    public void addField(final CtField f, final String init) throws CannotCompileException {
        this.checkModify();
    }
    
    public void addField(final CtField f, final CtField.Initializer init) throws CannotCompileException {
        this.checkModify();
    }
    
    public void removeField(final CtField f) throws NotFoundException {
        this.checkModify();
    }
    
    public byte[] getAttribute(final String name) {
        return null;
    }
    
    public void setAttribute(final String name, final byte[] data) {
        this.checkModify();
    }
    
    public void instrument(final CodeConverter converter) throws CannotCompileException {
        this.checkModify();
    }
    
    public void instrument(final ExprEditor editor) throws CannotCompileException {
        this.checkModify();
    }
    
    public Class toClass() throws CannotCompileException {
        return this.getClassPool().toClass(this);
    }
    
    public Class toClass(ClassLoader loader, final ProtectionDomain domain) throws CannotCompileException {
        final ClassPool cp = this.getClassPool();
        if (loader == null) {
            loader = cp.getClassLoader();
        }
        return cp.toClass(this, loader, domain);
    }
    
    @Deprecated
    public final Class toClass(final ClassLoader loader) throws CannotCompileException {
        return this.getClassPool().toClass(this, loader);
    }
    
    public void detach() {
        final ClassPool cp = this.getClassPool();
        final CtClass obj = cp.removeCached(this.getName());
        if (obj != this) {
            cp.cacheCtClass(this.getName(), obj, false);
        }
    }
    
    public boolean stopPruning(final boolean stop) {
        return true;
    }
    
    public void prune() {
    }
    
    void incGetCounter() {
    }
    
    public void rebuildClassFile() {
    }
    
    public byte[] toBytecode() throws IOException, CannotCompileException {
        final ByteArrayOutputStream barray = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(barray);
        try {
            this.toBytecode(out);
        }
        finally {
            out.close();
        }
        return barray.toByteArray();
    }
    
    public void writeFile() throws NotFoundException, IOException, CannotCompileException {
        this.writeFile(".");
    }
    
    public void writeFile(final String directoryName) throws CannotCompileException, IOException {
        final DataOutputStream out = this.makeFileOutput(directoryName);
        try {
            this.toBytecode(out);
        }
        finally {
            out.close();
        }
    }
    
    protected DataOutputStream makeFileOutput(final String directoryName) {
        final String classname = this.getName();
        final String filename = directoryName + File.separatorChar + classname.replace('.', File.separatorChar) + ".class";
        final int pos = filename.lastIndexOf(File.separatorChar);
        if (pos > 0) {
            final String dir = filename.substring(0, pos);
            if (!dir.equals(".")) {
                new File(dir).mkdirs();
            }
        }
        return new DataOutputStream(new BufferedOutputStream(new DelayedFileOutputStream(filename)));
    }
    
    public void debugWriteFile() {
        this.debugWriteFile(".");
    }
    
    public void debugWriteFile(final String directoryName) {
        try {
            final boolean p = this.stopPruning(true);
            this.writeFile(directoryName);
            this.defrost();
            this.stopPruning(p);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void toBytecode(final DataOutputStream out) throws CannotCompileException, IOException {
        throw new CannotCompileException("not a class");
    }
    
    public String makeUniqueName(final String prefix) {
        throw new RuntimeException("not available in " + this.getName());
    }
    
    void compress() {
    }
    
    static {
        CtClass.debugDump = null;
        CtClass.primitiveTypes = new CtClass[9];
        CtClass.booleanType = new CtPrimitiveType("boolean", 'Z', "java.lang.Boolean", "booleanValue", "()Z", 172, 4, 1);
        CtClass.primitiveTypes[0] = CtClass.booleanType;
        CtClass.charType = new CtPrimitiveType("char", 'C', "java.lang.Character", "charValue", "()C", 172, 5, 1);
        CtClass.primitiveTypes[1] = CtClass.charType;
        CtClass.byteType = new CtPrimitiveType("byte", 'B', "java.lang.Byte", "byteValue", "()B", 172, 8, 1);
        CtClass.primitiveTypes[2] = CtClass.byteType;
        CtClass.shortType = new CtPrimitiveType("short", 'S', "java.lang.Short", "shortValue", "()S", 172, 9, 1);
        CtClass.primitiveTypes[3] = CtClass.shortType;
        CtClass.intType = new CtPrimitiveType("int", 'I', "java.lang.Integer", "intValue", "()I", 172, 10, 1);
        CtClass.primitiveTypes[4] = CtClass.intType;
        CtClass.longType = new CtPrimitiveType("long", 'J', "java.lang.Long", "longValue", "()J", 173, 11, 2);
        CtClass.primitiveTypes[5] = CtClass.longType;
        CtClass.floatType = new CtPrimitiveType("float", 'F', "java.lang.Float", "floatValue", "()F", 174, 6, 1);
        CtClass.primitiveTypes[6] = CtClass.floatType;
        CtClass.doubleType = new CtPrimitiveType("double", 'D', "java.lang.Double", "doubleValue", "()D", 175, 7, 2);
        CtClass.primitiveTypes[7] = CtClass.doubleType;
        CtClass.voidType = new CtPrimitiveType("void", 'V', "java.lang.Void", null, null, 177, 0, 0);
        CtClass.primitiveTypes[8] = CtClass.voidType;
    }
    
    static class DelayedFileOutputStream extends OutputStream
    {
        private FileOutputStream file;
        private String filename;
        
        DelayedFileOutputStream(final String name) {
            this.file = null;
            this.filename = name;
        }
        
        private void init() throws IOException {
            if (this.file == null) {
                this.file = new FileOutputStream(this.filename);
            }
        }
        
        @Override
        public void write(final int b) throws IOException {
            this.init();
            this.file.write(b);
        }
        
        @Override
        public void write(final byte[] b) throws IOException {
            this.init();
            this.file.write(b);
        }
        
        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            this.init();
            this.file.write(b, off, len);
        }
        
        @Override
        public void flush() throws IOException {
            this.init();
            this.file.flush();
        }
        
        @Override
        public void close() throws IOException {
            this.init();
            this.file.close();
        }
    }
}

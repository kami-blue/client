// 
// Decompiled by Procyon v0.5.36
// 

package javassist.tools.reflect;

import java.util.Iterator;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ClassFile;
import javassist.Modifier;
import javassist.CtNewMethod;
import javassist.CtField;
import javassist.CannotCompileException;
import javassist.bytecode.BadBytecode;
import javassist.NotFoundException;
import javassist.CodeConverter;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Translator;

public class Reflection implements Translator
{
    static final String classobjectField = "_classobject";
    static final String classobjectAccessor = "_getClass";
    static final String metaobjectField = "_metaobject";
    static final String metaobjectGetter = "_getMetaobject";
    static final String metaobjectSetter = "_setMetaobject";
    static final String readPrefix = "_r_";
    static final String writePrefix = "_w_";
    static final String metaobjectClassName = "javassist.tools.reflect.Metaobject";
    static final String classMetaobjectClassName = "javassist.tools.reflect.ClassMetaobject";
    protected CtMethod trapMethod;
    protected CtMethod trapStaticMethod;
    protected CtMethod trapRead;
    protected CtMethod trapWrite;
    protected CtClass[] readParam;
    protected ClassPool classPool;
    protected CodeConverter converter;
    
    private boolean isExcluded(final String name) {
        return name.startsWith("_m_") || name.equals("_getClass") || name.equals("_setMetaobject") || name.equals("_getMetaobject") || name.startsWith("_r_") || name.startsWith("_w_");
    }
    
    public Reflection() {
        this.classPool = null;
        this.converter = new CodeConverter();
    }
    
    @Override
    public void start(final ClassPool pool) throws NotFoundException {
        this.classPool = pool;
        final String msg = "javassist.tools.reflect.Sample is not found or broken.";
        try {
            final CtClass c = this.classPool.get("javassist.tools.reflect.Sample");
            this.rebuildClassFile(c.getClassFile());
            this.trapMethod = c.getDeclaredMethod("trap");
            this.trapStaticMethod = c.getDeclaredMethod("trapStatic");
            this.trapRead = c.getDeclaredMethod("trapRead");
            this.trapWrite = c.getDeclaredMethod("trapWrite");
            this.readParam = new CtClass[] { this.classPool.get("java.lang.Object") };
        }
        catch (NotFoundException e) {
            throw new RuntimeException("javassist.tools.reflect.Sample is not found or broken.");
        }
        catch (BadBytecode e2) {
            throw new RuntimeException("javassist.tools.reflect.Sample is not found or broken.");
        }
    }
    
    @Override
    public void onLoad(final ClassPool pool, final String classname) throws CannotCompileException, NotFoundException {
        final CtClass clazz = pool.get(classname);
        clazz.instrument(this.converter);
    }
    
    public boolean makeReflective(final String classname, final String metaobject, final String metaclass) throws CannotCompileException, NotFoundException {
        return this.makeReflective(this.classPool.get(classname), this.classPool.get(metaobject), this.classPool.get(metaclass));
    }
    
    public boolean makeReflective(final Class clazz, final Class metaobject, final Class metaclass) throws CannotCompileException, NotFoundException {
        return this.makeReflective(clazz.getName(), metaobject.getName(), metaclass.getName());
    }
    
    public boolean makeReflective(final CtClass clazz, final CtClass metaobject, final CtClass metaclass) throws CannotCompileException, CannotReflectException, NotFoundException {
        if (clazz.isInterface()) {
            throw new CannotReflectException("Cannot reflect an interface: " + clazz.getName());
        }
        if (clazz.subclassOf(this.classPool.get("javassist.tools.reflect.ClassMetaobject"))) {
            throw new CannotReflectException("Cannot reflect a subclass of ClassMetaobject: " + clazz.getName());
        }
        if (clazz.subclassOf(this.classPool.get("javassist.tools.reflect.Metaobject"))) {
            throw new CannotReflectException("Cannot reflect a subclass of Metaobject: " + clazz.getName());
        }
        this.registerReflectiveClass(clazz);
        return this.modifyClassfile(clazz, metaobject, metaclass);
    }
    
    private void registerReflectiveClass(final CtClass clazz) {
        final CtField[] fs = clazz.getDeclaredFields();
        for (int i = 0; i < fs.length; ++i) {
            final CtField f = fs[i];
            final int mod = f.getModifiers();
            if ((mod & 0x1) != 0x0 && (mod & 0x10) == 0x0) {
                final String name = f.getName();
                this.converter.replaceFieldRead(f, clazz, "_r_" + name);
                this.converter.replaceFieldWrite(f, clazz, "_w_" + name);
            }
        }
    }
    
    private boolean modifyClassfile(final CtClass clazz, final CtClass metaobject, final CtClass metaclass) throws CannotCompileException, NotFoundException {
        if (clazz.getAttribute("Reflective") != null) {
            return false;
        }
        clazz.setAttribute("Reflective", new byte[0]);
        final CtClass mlevel = this.classPool.get("javassist.tools.reflect.Metalevel");
        final boolean addMeta = !clazz.subtypeOf(mlevel);
        if (addMeta) {
            clazz.addInterface(mlevel);
        }
        this.processMethods(clazz, addMeta);
        this.processFields(clazz);
        if (addMeta) {
            final CtField f = new CtField(this.classPool.get("javassist.tools.reflect.Metaobject"), "_metaobject", clazz);
            f.setModifiers(4);
            clazz.addField(f, CtField.Initializer.byNewWithParams(metaobject));
            clazz.addMethod(CtNewMethod.getter("_getMetaobject", f));
            clazz.addMethod(CtNewMethod.setter("_setMetaobject", f));
        }
        final CtField f = new CtField(this.classPool.get("javassist.tools.reflect.ClassMetaobject"), "_classobject", clazz);
        f.setModifiers(10);
        clazz.addField(f, CtField.Initializer.byNew(metaclass, new String[] { clazz.getName() }));
        clazz.addMethod(CtNewMethod.getter("_getClass", f));
        return true;
    }
    
    private void processMethods(final CtClass clazz, final boolean dontSearch) throws CannotCompileException, NotFoundException {
        final CtMethod[] ms = clazz.getMethods();
        for (int i = 0; i < ms.length; ++i) {
            final CtMethod m = ms[i];
            final int mod = m.getModifiers();
            if (Modifier.isPublic(mod) && !Modifier.isAbstract(mod)) {
                this.processMethods0(mod, clazz, m, i, dontSearch);
            }
        }
    }
    
    private void processMethods0(int mod, final CtClass clazz, final CtMethod m, final int identifier, final boolean dontSearch) throws CannotCompileException, NotFoundException {
        final String name = m.getName();
        if (this.isExcluded(name)) {
            return;
        }
        CtMethod m2;
        if (m.getDeclaringClass() == clazz) {
            if (Modifier.isNative(mod)) {
                return;
            }
            m2 = m;
            if (Modifier.isFinal(mod)) {
                mod &= 0xFFFFFFEF;
                m2.setModifiers(mod);
            }
        }
        else {
            if (Modifier.isFinal(mod)) {
                return;
            }
            mod &= 0xFFFFFEFF;
            m2 = CtNewMethod.delegator(this.findOriginal(m, dontSearch), clazz);
            m2.setModifiers(mod);
            clazz.addMethod(m2);
        }
        m2.setName("_m_" + identifier + "_" + name);
        CtMethod body;
        if (Modifier.isStatic(mod)) {
            body = this.trapStaticMethod;
        }
        else {
            body = this.trapMethod;
        }
        final CtMethod wmethod = CtNewMethod.wrapped(m.getReturnType(), name, m.getParameterTypes(), m.getExceptionTypes(), body, CtMethod.ConstParameter.integer(identifier), clazz);
        wmethod.setModifiers(mod);
        clazz.addMethod(wmethod);
    }
    
    private CtMethod findOriginal(final CtMethod m, final boolean dontSearch) throws NotFoundException {
        if (dontSearch) {
            return m;
        }
        final String name = m.getName();
        final CtMethod[] ms = m.getDeclaringClass().getDeclaredMethods();
        for (int i = 0; i < ms.length; ++i) {
            final String orgName = ms[i].getName();
            if (orgName.endsWith(name) && orgName.startsWith("_m_") && ms[i].getSignature().equals(m.getSignature())) {
                return ms[i];
            }
        }
        return m;
    }
    
    private void processFields(final CtClass clazz) throws CannotCompileException, NotFoundException {
        final CtField[] fs = clazz.getDeclaredFields();
        for (int i = 0; i < fs.length; ++i) {
            final CtField f = fs[i];
            int mod = f.getModifiers();
            if ((mod & 0x1) != 0x0 && (mod & 0x10) == 0x0) {
                mod |= 0x8;
                final String name = f.getName();
                final CtClass ftype = f.getType();
                CtMethod wmethod = CtNewMethod.wrapped(ftype, "_r_" + name, this.readParam, null, this.trapRead, CtMethod.ConstParameter.string(name), clazz);
                wmethod.setModifiers(mod);
                clazz.addMethod(wmethod);
                final CtClass[] writeParam = { this.classPool.get("java.lang.Object"), ftype };
                wmethod = CtNewMethod.wrapped(CtClass.voidType, "_w_" + name, writeParam, null, this.trapWrite, CtMethod.ConstParameter.string(name), clazz);
                wmethod.setModifiers(mod);
                clazz.addMethod(wmethod);
            }
        }
    }
    
    public void rebuildClassFile(final ClassFile cf) throws BadBytecode {
        if (ClassFile.MAJOR_VERSION < 50) {
            return;
        }
        for (final MethodInfo mi : cf.getMethods()) {
            mi.rebuildStackMap(this.classPool);
        }
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler;

import javassist.compiler.ast.Keyword;
import java.util.Iterator;
import java.lang.ref.WeakReference;
import javassist.compiler.ast.Declarator;
import javassist.compiler.ast.ASTList;
import javassist.CtField;
import javassist.compiler.ast.ASTree;
import javassist.compiler.ast.Symbol;
import javassist.bytecode.Descriptor;
import java.util.List;
import javassist.bytecode.ClassFile;
import javassist.NotFoundException;
import javassist.Modifier;
import javassist.bytecode.MethodInfo;
import javassist.CtClass;
import java.util.Hashtable;
import java.util.WeakHashMap;
import javassist.ClassPool;

public class MemberResolver implements TokenId
{
    private ClassPool classPool;
    private static final int YES = 0;
    private static final int NO = -1;
    private static final String INVALID = "<invalid>";
    private static WeakHashMap invalidNamesMap;
    private Hashtable invalidNames;
    
    public MemberResolver(final ClassPool cp) {
        this.invalidNames = null;
        this.classPool = cp;
    }
    
    public ClassPool getClassPool() {
        return this.classPool;
    }
    
    private static void fatal() throws CompileError {
        throw new CompileError("fatal");
    }
    
    public Method lookupMethod(final CtClass clazz, final CtClass currentClass, final MethodInfo current, final String methodName, final int[] argTypes, final int[] argDims, final String[] argClassNames) throws CompileError {
        Method maybe = null;
        if (current != null && clazz == currentClass && current.getName().equals(methodName)) {
            final int res = this.compareSignature(current.getDescriptor(), argTypes, argDims, argClassNames);
            if (res != -1) {
                final Method r = new Method(clazz, current, res);
                if (res == 0) {
                    return r;
                }
                maybe = r;
            }
        }
        final Method m = this.lookupMethod(clazz, methodName, argTypes, argDims, argClassNames, maybe != null);
        if (m != null) {
            return m;
        }
        return maybe;
    }
    
    private Method lookupMethod(final CtClass clazz, final String methodName, final int[] argTypes, final int[] argDims, final String[] argClassNames, final boolean onlyExact) throws CompileError {
        Method maybe = null;
        final ClassFile cf = clazz.getClassFile2();
        if (cf != null) {
            final List list = cf.getMethods();
            for (int n = list.size(), i = 0; i < n; ++i) {
                final MethodInfo minfo = list.get(i);
                if (minfo.getName().equals(methodName) && (minfo.getAccessFlags() & 0x40) == 0x0) {
                    final int res = this.compareSignature(minfo.getDescriptor(), argTypes, argDims, argClassNames);
                    if (res != -1) {
                        final Method r = new Method(clazz, minfo, res);
                        if (res == 0) {
                            return r;
                        }
                        if (maybe == null || maybe.notmatch > res) {
                            maybe = r;
                        }
                    }
                }
            }
        }
        if (onlyExact) {
            maybe = null;
        }
        else if (maybe != null) {
            return maybe;
        }
        final int mod = clazz.getModifiers();
        final boolean isIntf = Modifier.isInterface(mod);
        try {
            if (!isIntf) {
                final CtClass pclazz = clazz.getSuperclass();
                if (pclazz != null) {
                    final Method r2 = this.lookupMethod(pclazz, methodName, argTypes, argDims, argClassNames, onlyExact);
                    if (r2 != null) {
                        return r2;
                    }
                }
            }
        }
        catch (NotFoundException ex) {}
        try {
            final CtClass[] ifs = clazz.getInterfaces();
            for (int size = ifs.length, j = 0; j < size; ++j) {
                final Method r = this.lookupMethod(ifs[j], methodName, argTypes, argDims, argClassNames, onlyExact);
                if (r != null) {
                    return r;
                }
            }
            if (isIntf) {
                final CtClass pclazz2 = clazz.getSuperclass();
                if (pclazz2 != null) {
                    final Method r = this.lookupMethod(pclazz2, methodName, argTypes, argDims, argClassNames, onlyExact);
                    if (r != null) {
                        return r;
                    }
                }
            }
        }
        catch (NotFoundException ex2) {}
        return maybe;
    }
    
    private int compareSignature(final String desc, final int[] argTypes, final int[] argDims, final String[] argClassNames) throws CompileError {
        int result = 0;
        int i = 1;
        final int nArgs = argTypes.length;
        if (nArgs != Descriptor.numOfParameters(desc)) {
            return -1;
        }
        final int len = desc.length();
        int n = 0;
        while (i < len) {
            char c = desc.charAt(i++);
            if (c == ')') {
                return (n == nArgs) ? result : -1;
            }
            if (n >= nArgs) {
                return -1;
            }
            int dim = 0;
            while (c == '[') {
                ++dim;
                c = desc.charAt(i++);
            }
            if (argTypes[n] == 412) {
                if (dim == 0 && c != 'L') {
                    return -1;
                }
                if (c == 'L') {
                    i = desc.indexOf(59, i) + 1;
                }
            }
            else if (argDims[n] != dim) {
                if (dim != 0 || c != 'L' || !desc.startsWith("java/lang/Object;", i)) {
                    return -1;
                }
                i = desc.indexOf(59, i) + 1;
                ++result;
                if (i <= 0) {
                    return -1;
                }
            }
            else if (c == 'L') {
                final int j = desc.indexOf(59, i);
                if (j < 0 || argTypes[n] != 307) {
                    return -1;
                }
                final String cname = desc.substring(i, j);
                if (!cname.equals(argClassNames[n])) {
                    final CtClass clazz = this.lookupClassByJvmName(argClassNames[n]);
                    try {
                        if (!clazz.subtypeOf(this.lookupClassByJvmName(cname))) {
                            return -1;
                        }
                        ++result;
                    }
                    catch (NotFoundException e) {
                        ++result;
                    }
                }
                i = j + 1;
            }
            else {
                final int t = descToType(c);
                final int at = argTypes[n];
                if (t != at) {
                    if (t != 324 || (at != 334 && at != 303 && at != 306)) {
                        return -1;
                    }
                    ++result;
                }
            }
            ++n;
        }
        return -1;
    }
    
    public CtField lookupFieldByJvmName2(String jvmClassName, final Symbol fieldSym, final ASTree expr) throws NoFieldException {
        final String field = fieldSym.get();
        CtClass cc = null;
        try {
            cc = this.lookupClass(jvmToJavaName(jvmClassName), true);
        }
        catch (CompileError e) {
            throw new NoFieldException(jvmClassName + "/" + field, expr);
        }
        try {
            return cc.getField(field);
        }
        catch (NotFoundException e2) {
            jvmClassName = javaToJvmName(cc.getName());
            throw new NoFieldException(jvmClassName + "$" + field, expr);
        }
    }
    
    public CtField lookupFieldByJvmName(final String jvmClassName, final Symbol fieldName) throws CompileError {
        return this.lookupField(jvmToJavaName(jvmClassName), fieldName);
    }
    
    public CtField lookupField(final String className, final Symbol fieldName) throws CompileError {
        final CtClass cc = this.lookupClass(className, false);
        try {
            return cc.getField(fieldName.get());
        }
        catch (NotFoundException ex) {
            throw new CompileError("no such field: " + fieldName.get());
        }
    }
    
    public CtClass lookupClassByName(final ASTList name) throws CompileError {
        return this.lookupClass(Declarator.astToClassName(name, '.'), false);
    }
    
    public CtClass lookupClassByJvmName(final String jvmName) throws CompileError {
        return this.lookupClass(jvmToJavaName(jvmName), false);
    }
    
    public CtClass lookupClass(final Declarator decl) throws CompileError {
        return this.lookupClass(decl.getType(), decl.getArrayDim(), decl.getClassName());
    }
    
    public CtClass lookupClass(final int type, int dim, final String classname) throws CompileError {
        String cname = "";
        if (type == 307) {
            final CtClass clazz = this.lookupClassByJvmName(classname);
            if (dim <= 0) {
                return clazz;
            }
            cname = clazz.getName();
        }
        else {
            cname = getTypeName(type);
        }
        while (dim-- > 0) {
            cname += "[]";
        }
        return this.lookupClass(cname, false);
    }
    
    static String getTypeName(final int type) throws CompileError {
        String cname = "";
        switch (type) {
            case 301: {
                cname = "boolean";
                break;
            }
            case 306: {
                cname = "char";
                break;
            }
            case 303: {
                cname = "byte";
                break;
            }
            case 334: {
                cname = "short";
                break;
            }
            case 324: {
                cname = "int";
                break;
            }
            case 326: {
                cname = "long";
                break;
            }
            case 317: {
                cname = "float";
                break;
            }
            case 312: {
                cname = "double";
                break;
            }
            case 344: {
                cname = "void";
                break;
            }
            default: {
                fatal();
                break;
            }
        }
        return cname;
    }
    
    public CtClass lookupClass(final String name, final boolean notCheckInner) throws CompileError {
        final Hashtable cache = this.getInvalidNames();
        final Object found = cache.get(name);
        if (found == "<invalid>") {
            throw new CompileError("no such class: " + name);
        }
        if (found != null) {
            try {
                return this.classPool.get((String)found);
            }
            catch (NotFoundException ex) {}
        }
        CtClass cc = null;
        try {
            cc = this.lookupClass0(name, notCheckInner);
        }
        catch (NotFoundException e) {
            cc = this.searchImports(name);
        }
        cache.put(name, cc.getName());
        return cc;
    }
    
    public static int getInvalidMapSize() {
        return MemberResolver.invalidNamesMap.size();
    }
    
    private Hashtable getInvalidNames() {
        Hashtable ht = this.invalidNames;
        if (ht == null) {
            synchronized (MemberResolver.class) {
                final WeakReference ref = MemberResolver.invalidNamesMap.get(this.classPool);
                if (ref != null) {
                    ht = (Hashtable)ref.get();
                }
                if (ht == null) {
                    ht = new Hashtable();
                    MemberResolver.invalidNamesMap.put(this.classPool, new WeakReference<Hashtable>(ht));
                }
            }
            this.invalidNames = ht;
        }
        return ht;
    }
    
    private CtClass searchImports(final String orgName) throws CompileError {
        if (orgName.indexOf(46) < 0) {
            final Iterator it = this.classPool.getImportedPackages();
            while (it.hasNext()) {
                final String pac = it.next();
                final String fqName = pac + '.' + orgName;
                try {
                    return this.classPool.get(fqName);
                }
                catch (NotFoundException e) {
                    try {
                        if (pac.endsWith("." + orgName)) {
                            return this.classPool.get(pac);
                        }
                        continue;
                    }
                    catch (NotFoundException ex) {}
                    continue;
                }
                break;
            }
        }
        this.getInvalidNames().put(orgName, "<invalid>");
        throw new CompileError("no such class: " + orgName);
    }
    
    private CtClass lookupClass0(String classname, final boolean notCheckInner) throws NotFoundException {
        CtClass cc = null;
        do {
            try {
                cc = this.classPool.get(classname);
            }
            catch (NotFoundException e) {
                final int i = classname.lastIndexOf(46);
                if (notCheckInner || i < 0) {
                    throw e;
                }
                final StringBuffer sbuf = new StringBuffer(classname);
                sbuf.setCharAt(i, '$');
                classname = sbuf.toString();
            }
        } while (cc == null);
        return cc;
    }
    
    public String resolveClassName(final ASTList name) throws CompileError {
        if (name == null) {
            return null;
        }
        return javaToJvmName(this.lookupClassByName(name).getName());
    }
    
    public String resolveJvmClassName(final String jvmName) throws CompileError {
        if (jvmName == null) {
            return null;
        }
        return javaToJvmName(this.lookupClassByJvmName(jvmName).getName());
    }
    
    public static CtClass getSuperclass(final CtClass c) throws CompileError {
        try {
            final CtClass sc = c.getSuperclass();
            if (sc != null) {
                return sc;
            }
        }
        catch (NotFoundException ex) {}
        throw new CompileError("cannot find the super class of " + c.getName());
    }
    
    public static CtClass getSuperInterface(final CtClass c, final String interfaceName) throws CompileError {
        try {
            final CtClass[] intfs = c.getInterfaces();
            for (int i = 0; i < intfs.length; ++i) {
                if (intfs[i].getName().equals(interfaceName)) {
                    return intfs[i];
                }
            }
        }
        catch (NotFoundException ex) {}
        throw new CompileError("cannot find the super inetrface " + interfaceName + " of " + c.getName());
    }
    
    public static String javaToJvmName(final String classname) {
        return classname.replace('.', '/');
    }
    
    public static String jvmToJavaName(final String classname) {
        return classname.replace('/', '.');
    }
    
    public static int descToType(final char c) throws CompileError {
        switch (c) {
            case 'Z': {
                return 301;
            }
            case 'C': {
                return 306;
            }
            case 'B': {
                return 303;
            }
            case 'S': {
                return 334;
            }
            case 'I': {
                return 324;
            }
            case 'J': {
                return 326;
            }
            case 'F': {
                return 317;
            }
            case 'D': {
                return 312;
            }
            case 'V': {
                return 344;
            }
            case 'L':
            case '[': {
                return 307;
            }
            default: {
                fatal();
                return 344;
            }
        }
    }
    
    public static int getModifiers(ASTList mods) {
        int m = 0;
        while (mods != null) {
            final Keyword k = (Keyword)mods.head();
            mods = mods.tail();
            switch (k.get()) {
                case 335: {
                    m |= 0x8;
                    continue;
                }
                case 315: {
                    m |= 0x10;
                    continue;
                }
                case 338: {
                    m |= 0x20;
                    continue;
                }
                case 300: {
                    m |= 0x400;
                    continue;
                }
                case 332: {
                    m |= 0x1;
                    continue;
                }
                case 331: {
                    m |= 0x4;
                    continue;
                }
                case 330: {
                    m |= 0x2;
                    continue;
                }
                case 345: {
                    m |= 0x40;
                    continue;
                }
                case 342: {
                    m |= 0x80;
                    continue;
                }
                case 347: {
                    m |= 0x800;
                    continue;
                }
            }
        }
        return m;
    }
    
    static {
        MemberResolver.invalidNamesMap = new WeakHashMap();
    }
    
    public static class Method
    {
        public CtClass declaring;
        public MethodInfo info;
        public int notmatch;
        
        public Method(final CtClass c, final MethodInfo i, final int n) {
            this.declaring = c;
            this.info = i;
            this.notmatch = n;
        }
        
        public boolean isStatic() {
            final int acc = this.info.getAccessFlags();
            return (acc & 0x8) != 0x0;
        }
    }
}

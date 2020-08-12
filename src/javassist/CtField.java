// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import javassist.compiler.SymbolTable;
import javassist.compiler.ast.StringL;
import javassist.compiler.ast.DoubleConst;
import javassist.compiler.ast.IntConst;
import javassist.bytecode.Bytecode;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AccessFlag;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;
import javassist.compiler.ast.ASTree;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import java.util.ListIterator;
import java.util.Map;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.Descriptor;
import javassist.bytecode.FieldInfo;

public class CtField extends CtMember
{
    static final String javaLangString = "java.lang.String";
    protected FieldInfo fieldInfo;
    
    public CtField(final CtClass type, final String name, final CtClass declaring) throws CannotCompileException {
        this(Descriptor.of(type), name, declaring);
    }
    
    public CtField(final CtField src, final CtClass declaring) throws CannotCompileException {
        this(src.fieldInfo.getDescriptor(), src.fieldInfo.getName(), declaring);
        final ListIterator iterator = src.fieldInfo.getAttributes().listIterator();
        final FieldInfo fi = this.fieldInfo;
        fi.setAccessFlags(src.fieldInfo.getAccessFlags());
        final ConstPool cp = fi.getConstPool();
        while (iterator.hasNext()) {
            final AttributeInfo ainfo = iterator.next();
            fi.addAttribute(ainfo.copy(cp, null));
        }
    }
    
    private CtField(final String typeDesc, final String name, final CtClass clazz) throws CannotCompileException {
        super(clazz);
        final ClassFile cf = clazz.getClassFile2();
        if (cf == null) {
            throw new CannotCompileException("bad declaring class: " + clazz.getName());
        }
        this.fieldInfo = new FieldInfo(cf.getConstPool(), name, typeDesc);
    }
    
    CtField(final FieldInfo fi, final CtClass clazz) {
        super(clazz);
        this.fieldInfo = fi;
    }
    
    @Override
    public String toString() {
        return this.getDeclaringClass().getName() + "." + this.getName() + ":" + this.fieldInfo.getDescriptor();
    }
    
    @Override
    protected void extendToString(final StringBuffer buffer) {
        buffer.append(' ');
        buffer.append(this.getName());
        buffer.append(' ');
        buffer.append(this.fieldInfo.getDescriptor());
    }
    
    protected ASTree getInitAST() {
        return null;
    }
    
    Initializer getInit() {
        final ASTree tree = this.getInitAST();
        if (tree == null) {
            return null;
        }
        return Initializer.byExpr(tree);
    }
    
    public static CtField make(final String src, final CtClass declaring) throws CannotCompileException {
        final Javac compiler = new Javac(declaring);
        try {
            final CtMember obj = compiler.compile(src);
            if (obj instanceof CtField) {
                return (CtField)obj;
            }
        }
        catch (CompileError e) {
            throw new CannotCompileException(e);
        }
        throw new CannotCompileException("not a field");
    }
    
    public FieldInfo getFieldInfo() {
        this.declaringClass.checkModify();
        return this.fieldInfo;
    }
    
    public FieldInfo getFieldInfo2() {
        return this.fieldInfo;
    }
    
    @Override
    public CtClass getDeclaringClass() {
        return super.getDeclaringClass();
    }
    
    @Override
    public String getName() {
        return this.fieldInfo.getName();
    }
    
    public void setName(final String newName) {
        this.declaringClass.checkModify();
        this.fieldInfo.setName(newName);
    }
    
    @Override
    public int getModifiers() {
        return AccessFlag.toModifier(this.fieldInfo.getAccessFlags());
    }
    
    @Override
    public void setModifiers(final int mod) {
        this.declaringClass.checkModify();
        this.fieldInfo.setAccessFlags(AccessFlag.of(mod));
    }
    
    @Override
    public boolean hasAnnotation(final String typeName) {
        final FieldInfo fi = this.getFieldInfo2();
        final AnnotationsAttribute ainfo = (AnnotationsAttribute)fi.getAttribute("RuntimeInvisibleAnnotations");
        final AnnotationsAttribute ainfo2 = (AnnotationsAttribute)fi.getAttribute("RuntimeVisibleAnnotations");
        return CtClassType.hasAnnotationType(typeName, this.getDeclaringClass().getClassPool(), ainfo, ainfo2);
    }
    
    @Override
    public Object getAnnotation(final Class clz) throws ClassNotFoundException {
        final FieldInfo fi = this.getFieldInfo2();
        final AnnotationsAttribute ainfo = (AnnotationsAttribute)fi.getAttribute("RuntimeInvisibleAnnotations");
        final AnnotationsAttribute ainfo2 = (AnnotationsAttribute)fi.getAttribute("RuntimeVisibleAnnotations");
        return CtClassType.getAnnotationType(clz, this.getDeclaringClass().getClassPool(), ainfo, ainfo2);
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
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    
    private Object[] getAnnotations(final boolean ignoreNotFound) throws ClassNotFoundException {
        final FieldInfo fi = this.getFieldInfo2();
        final AnnotationsAttribute ainfo = (AnnotationsAttribute)fi.getAttribute("RuntimeInvisibleAnnotations");
        final AnnotationsAttribute ainfo2 = (AnnotationsAttribute)fi.getAttribute("RuntimeVisibleAnnotations");
        return CtClassType.toAnnotationType(ignoreNotFound, this.getDeclaringClass().getClassPool(), ainfo, ainfo2);
    }
    
    @Override
    public String getSignature() {
        return this.fieldInfo.getDescriptor();
    }
    
    @Override
    public String getGenericSignature() {
        final SignatureAttribute sa = (SignatureAttribute)this.fieldInfo.getAttribute("Signature");
        return (sa == null) ? null : sa.getSignature();
    }
    
    @Override
    public void setGenericSignature(final String sig) {
        this.declaringClass.checkModify();
        this.fieldInfo.addAttribute(new SignatureAttribute(this.fieldInfo.getConstPool(), sig));
    }
    
    public CtClass getType() throws NotFoundException {
        return Descriptor.toCtClass(this.fieldInfo.getDescriptor(), this.declaringClass.getClassPool());
    }
    
    public void setType(final CtClass clazz) {
        this.declaringClass.checkModify();
        this.fieldInfo.setDescriptor(Descriptor.of(clazz));
    }
    
    public Object getConstantValue() {
        final int index = this.fieldInfo.getConstantValue();
        if (index == 0) {
            return null;
        }
        final ConstPool cp = this.fieldInfo.getConstPool();
        switch (cp.getTag(index)) {
            case 5: {
                return new Long(cp.getLongInfo(index));
            }
            case 4: {
                return new Float(cp.getFloatInfo(index));
            }
            case 6: {
                return new Double(cp.getDoubleInfo(index));
            }
            case 3: {
                final int value = cp.getIntegerInfo(index);
                if ("Z".equals(this.fieldInfo.getDescriptor())) {
                    return new Boolean(value != 0);
                }
                return new Integer(value);
            }
            case 8: {
                return cp.getStringInfo(index);
            }
            default: {
                throw new RuntimeException("bad tag: " + cp.getTag(index) + " at " + index);
            }
        }
    }
    
    @Override
    public byte[] getAttribute(final String name) {
        final AttributeInfo ai = this.fieldInfo.getAttribute(name);
        if (ai == null) {
            return null;
        }
        return ai.get();
    }
    
    @Override
    public void setAttribute(final String name, final byte[] data) {
        this.declaringClass.checkModify();
        this.fieldInfo.addAttribute(new AttributeInfo(this.fieldInfo.getConstPool(), name, data));
    }
    
    public abstract static class Initializer
    {
        public static Initializer constant(final int i) {
            return new IntInitializer(i);
        }
        
        public static Initializer constant(final boolean b) {
            return new IntInitializer(b ? 1 : 0);
        }
        
        public static Initializer constant(final long l) {
            return new LongInitializer(l);
        }
        
        public static Initializer constant(final float l) {
            return new FloatInitializer(l);
        }
        
        public static Initializer constant(final double d) {
            return new DoubleInitializer(d);
        }
        
        public static Initializer constant(final String s) {
            return new StringInitializer(s);
        }
        
        public static Initializer byParameter(final int nth) {
            final ParamInitializer i = new ParamInitializer();
            i.nthParam = nth;
            return i;
        }
        
        public static Initializer byNew(final CtClass objectType) {
            final NewInitializer i = new NewInitializer();
            i.objectType = objectType;
            i.stringParams = null;
            i.withConstructorParams = false;
            return i;
        }
        
        public static Initializer byNew(final CtClass objectType, final String[] stringParams) {
            final NewInitializer i = new NewInitializer();
            i.objectType = objectType;
            i.stringParams = stringParams;
            i.withConstructorParams = false;
            return i;
        }
        
        public static Initializer byNewWithParams(final CtClass objectType) {
            final NewInitializer i = new NewInitializer();
            i.objectType = objectType;
            i.stringParams = null;
            i.withConstructorParams = true;
            return i;
        }
        
        public static Initializer byNewWithParams(final CtClass objectType, final String[] stringParams) {
            final NewInitializer i = new NewInitializer();
            i.objectType = objectType;
            i.stringParams = stringParams;
            i.withConstructorParams = true;
            return i;
        }
        
        public static Initializer byCall(final CtClass methodClass, final String methodName) {
            final MethodInitializer i = new MethodInitializer();
            i.objectType = methodClass;
            i.methodName = methodName;
            i.stringParams = null;
            i.withConstructorParams = false;
            return i;
        }
        
        public static Initializer byCall(final CtClass methodClass, final String methodName, final String[] stringParams) {
            final MethodInitializer i = new MethodInitializer();
            i.objectType = methodClass;
            i.methodName = methodName;
            i.stringParams = stringParams;
            i.withConstructorParams = false;
            return i;
        }
        
        public static Initializer byCallWithParams(final CtClass methodClass, final String methodName) {
            final MethodInitializer i = new MethodInitializer();
            i.objectType = methodClass;
            i.methodName = methodName;
            i.stringParams = null;
            i.withConstructorParams = true;
            return i;
        }
        
        public static Initializer byCallWithParams(final CtClass methodClass, final String methodName, final String[] stringParams) {
            final MethodInitializer i = new MethodInitializer();
            i.objectType = methodClass;
            i.methodName = methodName;
            i.stringParams = stringParams;
            i.withConstructorParams = true;
            return i;
        }
        
        public static Initializer byNewArray(final CtClass type, final int size) throws NotFoundException {
            return new ArrayInitializer(type.getComponentType(), size);
        }
        
        public static Initializer byNewArray(final CtClass type, final int[] sizes) {
            return new MultiArrayInitializer(type, sizes);
        }
        
        public static Initializer byExpr(final String source) {
            return new CodeInitializer(source);
        }
        
        static Initializer byExpr(final ASTree source) {
            return new PtreeInitializer(source);
        }
        
        void check(final String desc) throws CannotCompileException {
        }
        
        abstract int compile(final CtClass p0, final String p1, final Bytecode p2, final CtClass[] p3, final Javac p4) throws CannotCompileException;
        
        abstract int compileIfStatic(final CtClass p0, final String p1, final Bytecode p2, final Javac p3) throws CannotCompileException;
        
        int getConstantValue(final ConstPool cp, final CtClass type) {
            return 0;
        }
    }
    
    abstract static class CodeInitializer0 extends Initializer
    {
        abstract void compileExpr(final Javac p0) throws CompileError;
        
        @Override
        int compile(final CtClass type, final String name, final Bytecode code, final CtClass[] parameters, final Javac drv) throws CannotCompileException {
            try {
                code.addAload(0);
                this.compileExpr(drv);
                code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
                return code.getMaxStack();
            }
            catch (CompileError e) {
                throw new CannotCompileException(e);
            }
        }
        
        @Override
        int compileIfStatic(final CtClass type, final String name, final Bytecode code, final Javac drv) throws CannotCompileException {
            try {
                this.compileExpr(drv);
                code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
                return code.getMaxStack();
            }
            catch (CompileError e) {
                throw new CannotCompileException(e);
            }
        }
        
        int getConstantValue2(final ConstPool cp, final CtClass type, final ASTree tree) {
            if (type.isPrimitive()) {
                if (tree instanceof IntConst) {
                    final long value = ((IntConst)tree).get();
                    if (type == CtClass.doubleType) {
                        return cp.addDoubleInfo((double)value);
                    }
                    if (type == CtClass.floatType) {
                        return cp.addFloatInfo((float)value);
                    }
                    if (type == CtClass.longType) {
                        return cp.addLongInfo(value);
                    }
                    if (type != CtClass.voidType) {
                        return cp.addIntegerInfo((int)value);
                    }
                }
                else if (tree instanceof DoubleConst) {
                    final double value2 = ((DoubleConst)tree).get();
                    if (type == CtClass.floatType) {
                        return cp.addFloatInfo((float)value2);
                    }
                    if (type == CtClass.doubleType) {
                        return cp.addDoubleInfo(value2);
                    }
                }
            }
            else if (tree instanceof StringL && type.getName().equals("java.lang.String")) {
                return cp.addStringInfo(((StringL)tree).get());
            }
            return 0;
        }
    }
    
    static class CodeInitializer extends CodeInitializer0
    {
        private String expression;
        
        CodeInitializer(final String expr) {
            this.expression = expr;
        }
        
        @Override
        void compileExpr(final Javac drv) throws CompileError {
            drv.compileExpr(this.expression);
        }
        
        @Override
        int getConstantValue(final ConstPool cp, final CtClass type) {
            try {
                final ASTree t = Javac.parseExpr(this.expression, new SymbolTable());
                return this.getConstantValue2(cp, type, t);
            }
            catch (CompileError e) {
                return 0;
            }
        }
    }
    
    static class PtreeInitializer extends CodeInitializer0
    {
        private ASTree expression;
        
        PtreeInitializer(final ASTree expr) {
            this.expression = expr;
        }
        
        @Override
        void compileExpr(final Javac drv) throws CompileError {
            drv.compileExpr(this.expression);
        }
        
        @Override
        int getConstantValue(final ConstPool cp, final CtClass type) {
            return this.getConstantValue2(cp, type, this.expression);
        }
    }
    
    static class ParamInitializer extends Initializer
    {
        int nthParam;
        
        @Override
        int compile(final CtClass type, final String name, final Bytecode code, final CtClass[] parameters, final Javac drv) throws CannotCompileException {
            if (parameters != null && this.nthParam < parameters.length) {
                code.addAload(0);
                final int nth = nthParamToLocal(this.nthParam, parameters, false);
                final int s = code.addLoad(nth, type) + 1;
                code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
                return s;
            }
            return 0;
        }
        
        static int nthParamToLocal(final int nth, final CtClass[] params, final boolean isStatic) {
            final CtClass longType = CtClass.longType;
            final CtClass doubleType = CtClass.doubleType;
            int k;
            if (isStatic) {
                k = 0;
            }
            else {
                k = 1;
            }
            for (final CtClass type : params) {
                if (type == longType || type == doubleType) {
                    k += 2;
                }
                else {
                    ++k;
                }
            }
            return k;
        }
        
        @Override
        int compileIfStatic(final CtClass type, final String name, final Bytecode code, final Javac drv) throws CannotCompileException {
            return 0;
        }
    }
    
    static class NewInitializer extends Initializer
    {
        CtClass objectType;
        String[] stringParams;
        boolean withConstructorParams;
        
        @Override
        int compile(final CtClass type, final String name, final Bytecode code, final CtClass[] parameters, final Javac drv) throws CannotCompileException {
            code.addAload(0);
            code.addNew(this.objectType);
            code.add(89);
            code.addAload(0);
            int stacksize;
            if (this.stringParams == null) {
                stacksize = 4;
            }
            else {
                stacksize = this.compileStringParameter(code) + 4;
            }
            if (this.withConstructorParams) {
                stacksize += CtNewWrappedMethod.compileParameterList(code, parameters, 1);
            }
            code.addInvokespecial(this.objectType, "<init>", this.getDescriptor());
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return stacksize;
        }
        
        private String getDescriptor() {
            final String desc3 = "(Ljava/lang/Object;[Ljava/lang/String;[Ljava/lang/Object;)V";
            if (this.stringParams == null) {
                if (this.withConstructorParams) {
                    return "(Ljava/lang/Object;[Ljava/lang/Object;)V";
                }
                return "(Ljava/lang/Object;)V";
            }
            else {
                if (this.withConstructorParams) {
                    return "(Ljava/lang/Object;[Ljava/lang/String;[Ljava/lang/Object;)V";
                }
                return "(Ljava/lang/Object;[Ljava/lang/String;)V";
            }
        }
        
        @Override
        int compileIfStatic(final CtClass type, final String name, final Bytecode code, final Javac drv) throws CannotCompileException {
            code.addNew(this.objectType);
            code.add(89);
            int stacksize = 2;
            String desc;
            if (this.stringParams == null) {
                desc = "()V";
            }
            else {
                desc = "([Ljava/lang/String;)V";
                stacksize += this.compileStringParameter(code);
            }
            code.addInvokespecial(this.objectType, "<init>", desc);
            code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
            return stacksize;
        }
        
        protected final int compileStringParameter(final Bytecode code) throws CannotCompileException {
            final int nparam = this.stringParams.length;
            code.addIconst(nparam);
            code.addAnewarray("java.lang.String");
            for (int j = 0; j < nparam; ++j) {
                code.add(89);
                code.addIconst(j);
                code.addLdc(this.stringParams[j]);
                code.add(83);
            }
            return 4;
        }
    }
    
    static class MethodInitializer extends NewInitializer
    {
        String methodName;
        
        @Override
        int compile(final CtClass type, final String name, final Bytecode code, final CtClass[] parameters, final Javac drv) throws CannotCompileException {
            code.addAload(0);
            code.addAload(0);
            int stacksize;
            if (this.stringParams == null) {
                stacksize = 2;
            }
            else {
                stacksize = this.compileStringParameter(code) + 2;
            }
            if (this.withConstructorParams) {
                stacksize += CtNewWrappedMethod.compileParameterList(code, parameters, 1);
            }
            final String typeDesc = Descriptor.of(type);
            final String mDesc = this.getDescriptor() + typeDesc;
            code.addInvokestatic(this.objectType, this.methodName, mDesc);
            code.addPutfield(Bytecode.THIS, name, typeDesc);
            return stacksize;
        }
        
        private String getDescriptor() {
            final String desc3 = "(Ljava/lang/Object;[Ljava/lang/String;[Ljava/lang/Object;)";
            if (this.stringParams == null) {
                if (this.withConstructorParams) {
                    return "(Ljava/lang/Object;[Ljava/lang/Object;)";
                }
                return "(Ljava/lang/Object;)";
            }
            else {
                if (this.withConstructorParams) {
                    return "(Ljava/lang/Object;[Ljava/lang/String;[Ljava/lang/Object;)";
                }
                return "(Ljava/lang/Object;[Ljava/lang/String;)";
            }
        }
        
        @Override
        int compileIfStatic(final CtClass type, final String name, final Bytecode code, final Javac drv) throws CannotCompileException {
            int stacksize = 1;
            String desc;
            if (this.stringParams == null) {
                desc = "()";
            }
            else {
                desc = "([Ljava/lang/String;)";
                stacksize += this.compileStringParameter(code);
            }
            final String typeDesc = Descriptor.of(type);
            code.addInvokestatic(this.objectType, this.methodName, desc + typeDesc);
            code.addPutstatic(Bytecode.THIS, name, typeDesc);
            return stacksize;
        }
    }
    
    static class IntInitializer extends Initializer
    {
        int value;
        
        IntInitializer(final int v) {
            this.value = v;
        }
        
        @Override
        void check(final String desc) throws CannotCompileException {
            final char c = desc.charAt(0);
            if (c != 'I' && c != 'S' && c != 'B' && c != 'C' && c != 'Z') {
                throw new CannotCompileException("type mismatch");
            }
        }
        
        @Override
        int compile(final CtClass type, final String name, final Bytecode code, final CtClass[] parameters, final Javac drv) throws CannotCompileException {
            code.addAload(0);
            code.addIconst(this.value);
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return 2;
        }
        
        @Override
        int compileIfStatic(final CtClass type, final String name, final Bytecode code, final Javac drv) throws CannotCompileException {
            code.addIconst(this.value);
            code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
            return 1;
        }
        
        @Override
        int getConstantValue(final ConstPool cp, final CtClass type) {
            return cp.addIntegerInfo(this.value);
        }
    }
    
    static class LongInitializer extends Initializer
    {
        long value;
        
        LongInitializer(final long v) {
            this.value = v;
        }
        
        @Override
        void check(final String desc) throws CannotCompileException {
            if (!desc.equals("J")) {
                throw new CannotCompileException("type mismatch");
            }
        }
        
        @Override
        int compile(final CtClass type, final String name, final Bytecode code, final CtClass[] parameters, final Javac drv) throws CannotCompileException {
            code.addAload(0);
            code.addLdc2w(this.value);
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return 3;
        }
        
        @Override
        int compileIfStatic(final CtClass type, final String name, final Bytecode code, final Javac drv) throws CannotCompileException {
            code.addLdc2w(this.value);
            code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
            return 2;
        }
        
        @Override
        int getConstantValue(final ConstPool cp, final CtClass type) {
            if (type == CtClass.longType) {
                return cp.addLongInfo(this.value);
            }
            return 0;
        }
    }
    
    static class FloatInitializer extends Initializer
    {
        float value;
        
        FloatInitializer(final float v) {
            this.value = v;
        }
        
        @Override
        void check(final String desc) throws CannotCompileException {
            if (!desc.equals("F")) {
                throw new CannotCompileException("type mismatch");
            }
        }
        
        @Override
        int compile(final CtClass type, final String name, final Bytecode code, final CtClass[] parameters, final Javac drv) throws CannotCompileException {
            code.addAload(0);
            code.addFconst(this.value);
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return 3;
        }
        
        @Override
        int compileIfStatic(final CtClass type, final String name, final Bytecode code, final Javac drv) throws CannotCompileException {
            code.addFconst(this.value);
            code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
            return 2;
        }
        
        @Override
        int getConstantValue(final ConstPool cp, final CtClass type) {
            if (type == CtClass.floatType) {
                return cp.addFloatInfo(this.value);
            }
            return 0;
        }
    }
    
    static class DoubleInitializer extends Initializer
    {
        double value;
        
        DoubleInitializer(final double v) {
            this.value = v;
        }
        
        @Override
        void check(final String desc) throws CannotCompileException {
            if (!desc.equals("D")) {
                throw new CannotCompileException("type mismatch");
            }
        }
        
        @Override
        int compile(final CtClass type, final String name, final Bytecode code, final CtClass[] parameters, final Javac drv) throws CannotCompileException {
            code.addAload(0);
            code.addLdc2w(this.value);
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return 3;
        }
        
        @Override
        int compileIfStatic(final CtClass type, final String name, final Bytecode code, final Javac drv) throws CannotCompileException {
            code.addLdc2w(this.value);
            code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
            return 2;
        }
        
        @Override
        int getConstantValue(final ConstPool cp, final CtClass type) {
            if (type == CtClass.doubleType) {
                return cp.addDoubleInfo(this.value);
            }
            return 0;
        }
    }
    
    static class StringInitializer extends Initializer
    {
        String value;
        
        StringInitializer(final String v) {
            this.value = v;
        }
        
        @Override
        int compile(final CtClass type, final String name, final Bytecode code, final CtClass[] parameters, final Javac drv) throws CannotCompileException {
            code.addAload(0);
            code.addLdc(this.value);
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return 2;
        }
        
        @Override
        int compileIfStatic(final CtClass type, final String name, final Bytecode code, final Javac drv) throws CannotCompileException {
            code.addLdc(this.value);
            code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
            return 1;
        }
        
        @Override
        int getConstantValue(final ConstPool cp, final CtClass type) {
            if (type.getName().equals("java.lang.String")) {
                return cp.addStringInfo(this.value);
            }
            return 0;
        }
    }
    
    static class ArrayInitializer extends Initializer
    {
        CtClass type;
        int size;
        
        ArrayInitializer(final CtClass t, final int s) {
            this.type = t;
            this.size = s;
        }
        
        private void addNewarray(final Bytecode code) {
            if (this.type.isPrimitive()) {
                code.addNewarray(((CtPrimitiveType)this.type).getArrayType(), this.size);
            }
            else {
                code.addAnewarray(this.type, this.size);
            }
        }
        
        @Override
        int compile(final CtClass type, final String name, final Bytecode code, final CtClass[] parameters, final Javac drv) throws CannotCompileException {
            code.addAload(0);
            this.addNewarray(code);
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return 2;
        }
        
        @Override
        int compileIfStatic(final CtClass type, final String name, final Bytecode code, final Javac drv) throws CannotCompileException {
            this.addNewarray(code);
            code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
            return 1;
        }
    }
    
    static class MultiArrayInitializer extends Initializer
    {
        CtClass type;
        int[] dim;
        
        MultiArrayInitializer(final CtClass t, final int[] d) {
            this.type = t;
            this.dim = d;
        }
        
        @Override
        void check(final String desc) throws CannotCompileException {
            if (desc.charAt(0) != '[') {
                throw new CannotCompileException("type mismatch");
            }
        }
        
        @Override
        int compile(final CtClass type, final String name, final Bytecode code, final CtClass[] parameters, final Javac drv) throws CannotCompileException {
            code.addAload(0);
            final int s = code.addMultiNewarray(type, this.dim);
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return s + 1;
        }
        
        @Override
        int compileIfStatic(final CtClass type, final String name, final Bytecode code, final Javac drv) throws CannotCompileException {
            final int s = code.addMultiNewarray(type, this.dim);
            code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
            return s;
        }
    }
}

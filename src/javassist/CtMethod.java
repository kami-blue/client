// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.MethodInfo;

public final class CtMethod extends CtBehavior
{
    protected String cachedStringRep;
    
    CtMethod(final MethodInfo minfo, final CtClass declaring) {
        super(declaring, minfo);
        this.cachedStringRep = null;
    }
    
    public CtMethod(final CtClass returnType, final String mname, final CtClass[] parameters, final CtClass declaring) {
        this(null, declaring);
        final ConstPool cp = declaring.getClassFile2().getConstPool();
        final String desc = Descriptor.ofMethod(returnType, parameters);
        this.methodInfo = new MethodInfo(cp, mname, desc);
        this.setModifiers(1025);
    }
    
    public CtMethod(final CtMethod src, final CtClass declaring, final ClassMap map) throws CannotCompileException {
        this(null, declaring);
        this.copy(src, false, map);
    }
    
    public static CtMethod make(final String src, final CtClass declaring) throws CannotCompileException {
        return CtNewMethod.make(src, declaring);
    }
    
    public static CtMethod make(final MethodInfo minfo, final CtClass declaring) throws CannotCompileException {
        if (declaring.getClassFile2().getConstPool() != minfo.getConstPool()) {
            throw new CannotCompileException("bad declaring class");
        }
        return new CtMethod(minfo, declaring);
    }
    
    @Override
    public int hashCode() {
        return this.getStringRep().hashCode();
    }
    
    @Override
    void nameReplaced() {
        this.cachedStringRep = null;
    }
    
    final String getStringRep() {
        if (this.cachedStringRep == null) {
            this.cachedStringRep = this.methodInfo.getName() + Descriptor.getParamDescriptor(this.methodInfo.getDescriptor());
        }
        return this.cachedStringRep;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj != null && obj instanceof CtMethod && ((CtMethod)obj).getStringRep().equals(this.getStringRep());
    }
    
    @Override
    public String getLongName() {
        return this.getDeclaringClass().getName() + "." + this.getName() + Descriptor.toString(this.getSignature());
    }
    
    @Override
    public String getName() {
        return this.methodInfo.getName();
    }
    
    public void setName(final String newname) {
        this.declaringClass.checkModify();
        this.methodInfo.setName(newname);
    }
    
    public CtClass getReturnType() throws NotFoundException {
        return this.getReturnType0();
    }
    
    @Override
    public boolean isEmpty() {
        final CodeAttribute ca = this.getMethodInfo2().getCodeAttribute();
        if (ca == null) {
            return (this.getModifiers() & 0x400) != 0x0;
        }
        final CodeIterator it = ca.iterator();
        try {
            return it.hasNext() && it.byteAt(it.next()) == 177 && !it.hasNext();
        }
        catch (BadBytecode badBytecode) {
            return false;
        }
    }
    
    public void setBody(final CtMethod src, final ClassMap map) throws CannotCompileException {
        CtBehavior.setBody0(src.declaringClass, src.methodInfo, this.declaringClass, this.methodInfo, map);
    }
    
    public void setWrappedBody(final CtMethod mbody, final ConstParameter constParam) throws CannotCompileException {
        this.declaringClass.checkModify();
        final CtClass clazz = this.getDeclaringClass();
        CtClass[] params;
        CtClass retType;
        try {
            params = this.getParameterTypes();
            retType = this.getReturnType();
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        final Bytecode code = CtNewWrappedMethod.makeBody(clazz, clazz.getClassFile2(), mbody, params, retType, constParam);
        final CodeAttribute cattr = code.toCodeAttribute();
        this.methodInfo.setCodeAttribute(cattr);
        this.methodInfo.setAccessFlags(this.methodInfo.getAccessFlags() & 0xFFFFFBFF);
    }
    
    public static class ConstParameter
    {
        public static ConstParameter integer(final int i) {
            return new IntConstParameter(i);
        }
        
        public static ConstParameter integer(final long i) {
            return new LongConstParameter(i);
        }
        
        public static ConstParameter string(final String s) {
            return new StringConstParameter(s);
        }
        
        ConstParameter() {
        }
        
        int compile(final Bytecode code) throws CannotCompileException {
            return 0;
        }
        
        String descriptor() {
            return defaultDescriptor();
        }
        
        static String defaultDescriptor() {
            return "([Ljava/lang/Object;)Ljava/lang/Object;";
        }
        
        String constDescriptor() {
            return defaultConstDescriptor();
        }
        
        static String defaultConstDescriptor() {
            return "([Ljava/lang/Object;)V";
        }
    }
    
    static class IntConstParameter extends ConstParameter
    {
        int param;
        
        IntConstParameter(final int i) {
            this.param = i;
        }
        
        @Override
        int compile(final Bytecode code) throws CannotCompileException {
            code.addIconst(this.param);
            return 1;
        }
        
        @Override
        String descriptor() {
            return "([Ljava/lang/Object;I)Ljava/lang/Object;";
        }
        
        @Override
        String constDescriptor() {
            return "([Ljava/lang/Object;I)V";
        }
    }
    
    static class LongConstParameter extends ConstParameter
    {
        long param;
        
        LongConstParameter(final long l) {
            this.param = l;
        }
        
        @Override
        int compile(final Bytecode code) throws CannotCompileException {
            code.addLconst(this.param);
            return 2;
        }
        
        @Override
        String descriptor() {
            return "([Ljava/lang/Object;J)Ljava/lang/Object;";
        }
        
        @Override
        String constDescriptor() {
            return "([Ljava/lang/Object;J)V";
        }
    }
    
    static class StringConstParameter extends ConstParameter
    {
        String param;
        
        StringConstParameter(final String s) {
            this.param = s;
        }
        
        @Override
        int compile(final Bytecode code) throws CannotCompileException {
            code.addLdc(this.param);
            return 1;
        }
        
        @Override
        String descriptor() {
            return "([Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;";
        }
        
        @Override
        String constDescriptor() {
            return "([Ljava/lang/Object;Ljava/lang/String;)V";
        }
    }
}

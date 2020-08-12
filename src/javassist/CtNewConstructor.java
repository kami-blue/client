// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import javassist.bytecode.ConstPool;
import javassist.bytecode.Bytecode;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;

public class CtNewConstructor
{
    public static final int PASS_NONE = 0;
    public static final int PASS_ARRAY = 1;
    public static final int PASS_PARAMS = 2;
    
    public static CtConstructor make(final String src, final CtClass declaring) throws CannotCompileException {
        final Javac compiler = new Javac(declaring);
        try {
            final CtMember obj = compiler.compile(src);
            if (obj instanceof CtConstructor) {
                return (CtConstructor)obj;
            }
        }
        catch (CompileError e) {
            throw new CannotCompileException(e);
        }
        throw new CannotCompileException("not a constructor");
    }
    
    public static CtConstructor make(final CtClass[] parameters, final CtClass[] exceptions, final String body, final CtClass declaring) throws CannotCompileException {
        try {
            final CtConstructor cc = new CtConstructor(parameters, declaring);
            cc.setExceptionTypes(exceptions);
            cc.setBody(body);
            return cc;
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
    }
    
    public static CtConstructor copy(final CtConstructor c, final CtClass declaring, final ClassMap map) throws CannotCompileException {
        return new CtConstructor(c, declaring, map);
    }
    
    public static CtConstructor defaultConstructor(final CtClass declaring) throws CannotCompileException {
        final CtConstructor cons = new CtConstructor((CtClass[])null, declaring);
        final ConstPool cp = declaring.getClassFile2().getConstPool();
        final Bytecode code = new Bytecode(cp, 1, 1);
        code.addAload(0);
        try {
            code.addInvokespecial(declaring.getSuperclass(), "<init>", "()V");
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        code.add(177);
        cons.getMethodInfo2().setCodeAttribute(code.toCodeAttribute());
        return cons;
    }
    
    public static CtConstructor skeleton(final CtClass[] parameters, final CtClass[] exceptions, final CtClass declaring) throws CannotCompileException {
        return make(parameters, exceptions, 0, null, null, declaring);
    }
    
    public static CtConstructor make(final CtClass[] parameters, final CtClass[] exceptions, final CtClass declaring) throws CannotCompileException {
        return make(parameters, exceptions, 2, null, null, declaring);
    }
    
    public static CtConstructor make(final CtClass[] parameters, final CtClass[] exceptions, final int howto, final CtMethod body, final CtMethod.ConstParameter cparam, final CtClass declaring) throws CannotCompileException {
        return CtNewWrappedConstructor.wrapped(parameters, exceptions, howto, body, cparam, declaring);
    }
}

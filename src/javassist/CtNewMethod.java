// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import java.util.Map;
import javassist.bytecode.ExceptionsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.Bytecode;
import javassist.bytecode.MethodInfo;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;

public class CtNewMethod
{
    public static CtMethod make(final String src, final CtClass declaring) throws CannotCompileException {
        return make(src, declaring, null, null);
    }
    
    public static CtMethod make(final String src, final CtClass declaring, final String delegateObj, final String delegateMethod) throws CannotCompileException {
        final Javac compiler = new Javac(declaring);
        try {
            if (delegateMethod != null) {
                compiler.recordProceed(delegateObj, delegateMethod);
            }
            final CtMember obj = compiler.compile(src);
            if (obj instanceof CtMethod) {
                return (CtMethod)obj;
            }
        }
        catch (CompileError e) {
            throw new CannotCompileException(e);
        }
        throw new CannotCompileException("not a method");
    }
    
    public static CtMethod make(final CtClass returnType, final String mname, final CtClass[] parameters, final CtClass[] exceptions, final String body, final CtClass declaring) throws CannotCompileException {
        return make(1, returnType, mname, parameters, exceptions, body, declaring);
    }
    
    public static CtMethod make(final int modifiers, final CtClass returnType, final String mname, final CtClass[] parameters, final CtClass[] exceptions, final String body, final CtClass declaring) throws CannotCompileException {
        try {
            final CtMethod cm = new CtMethod(returnType, mname, parameters, declaring);
            cm.setModifiers(modifiers);
            cm.setExceptionTypes(exceptions);
            cm.setBody(body);
            return cm;
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
    }
    
    public static CtMethod copy(final CtMethod src, final CtClass declaring, final ClassMap map) throws CannotCompileException {
        return new CtMethod(src, declaring, map);
    }
    
    public static CtMethod copy(final CtMethod src, final String name, final CtClass declaring, final ClassMap map) throws CannotCompileException {
        final CtMethod cm = new CtMethod(src, declaring, map);
        cm.setName(name);
        return cm;
    }
    
    public static CtMethod abstractMethod(final CtClass returnType, final String mname, final CtClass[] parameters, final CtClass[] exceptions, final CtClass declaring) throws NotFoundException {
        final CtMethod cm = new CtMethod(returnType, mname, parameters, declaring);
        cm.setExceptionTypes(exceptions);
        return cm;
    }
    
    public static CtMethod getter(final String methodName, final CtField field) throws CannotCompileException {
        final FieldInfo finfo = field.getFieldInfo2();
        final String fieldType = finfo.getDescriptor();
        final String desc = "()" + fieldType;
        final ConstPool cp = finfo.getConstPool();
        final MethodInfo minfo = new MethodInfo(cp, methodName, desc);
        minfo.setAccessFlags(1);
        final Bytecode code = new Bytecode(cp, 2, 1);
        try {
            final String fieldName = finfo.getName();
            if ((finfo.getAccessFlags() & 0x8) == 0x0) {
                code.addAload(0);
                code.addGetfield(Bytecode.THIS, fieldName, fieldType);
            }
            else {
                code.addGetstatic(Bytecode.THIS, fieldName, fieldType);
            }
            code.addReturn(field.getType());
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        minfo.setCodeAttribute(code.toCodeAttribute());
        final CtClass cc = field.getDeclaringClass();
        return new CtMethod(minfo, cc);
    }
    
    public static CtMethod setter(final String methodName, final CtField field) throws CannotCompileException {
        final FieldInfo finfo = field.getFieldInfo2();
        final String fieldType = finfo.getDescriptor();
        final String desc = "(" + fieldType + ")V";
        final ConstPool cp = finfo.getConstPool();
        final MethodInfo minfo = new MethodInfo(cp, methodName, desc);
        minfo.setAccessFlags(1);
        final Bytecode code = new Bytecode(cp, 3, 3);
        try {
            final String fieldName = finfo.getName();
            if ((finfo.getAccessFlags() & 0x8) == 0x0) {
                code.addAload(0);
                code.addLoad(1, field.getType());
                code.addPutfield(Bytecode.THIS, fieldName, fieldType);
            }
            else {
                code.addLoad(1, field.getType());
                code.addPutstatic(Bytecode.THIS, fieldName, fieldType);
            }
            code.addReturn(null);
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        minfo.setCodeAttribute(code.toCodeAttribute());
        final CtClass cc = field.getDeclaringClass();
        return new CtMethod(minfo, cc);
    }
    
    public static CtMethod delegator(final CtMethod delegate, final CtClass declaring) throws CannotCompileException {
        try {
            return delegator0(delegate, declaring);
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
    }
    
    private static CtMethod delegator0(final CtMethod delegate, final CtClass declaring) throws CannotCompileException, NotFoundException {
        final MethodInfo deleInfo = delegate.getMethodInfo2();
        final String methodName = deleInfo.getName();
        final String desc = deleInfo.getDescriptor();
        final ConstPool cp = declaring.getClassFile2().getConstPool();
        final MethodInfo minfo = new MethodInfo(cp, methodName, desc);
        minfo.setAccessFlags(deleInfo.getAccessFlags());
        final ExceptionsAttribute eattr = deleInfo.getExceptionsAttribute();
        if (eattr != null) {
            minfo.setExceptionsAttribute((ExceptionsAttribute)eattr.copy(cp, null));
        }
        final Bytecode code = new Bytecode(cp, 0, 0);
        final boolean isStatic = Modifier.isStatic(delegate.getModifiers());
        final CtClass deleClass = delegate.getDeclaringClass();
        final CtClass[] params = delegate.getParameterTypes();
        int s;
        if (isStatic) {
            s = code.addLoadParameters(params, 0);
            code.addInvokestatic(deleClass, methodName, desc);
        }
        else {
            code.addLoad(0, deleClass);
            s = code.addLoadParameters(params, 1);
            code.addInvokespecial(deleClass, methodName, desc);
        }
        code.addReturn(delegate.getReturnType());
        code.setMaxLocals(++s);
        code.setMaxStack((s < 2) ? 2 : s);
        minfo.setCodeAttribute(code.toCodeAttribute());
        return new CtMethod(minfo, declaring);
    }
    
    public static CtMethod wrapped(final CtClass returnType, final String mname, final CtClass[] parameterTypes, final CtClass[] exceptionTypes, final CtMethod body, final CtMethod.ConstParameter constParam, final CtClass declaring) throws CannotCompileException {
        return CtNewWrappedMethod.wrapped(returnType, mname, parameterTypes, exceptionTypes, body, constParam, declaring);
    }
}

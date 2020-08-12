// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import javassist.compiler.JvstCodeGen;
import java.util.Hashtable;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.SyntheticAttribute;
import javassist.bytecode.AccessFlag;
import java.util.Map;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Bytecode;

class CtNewWrappedMethod
{
    private static final String addedWrappedMethod = "_added_m$";
    
    public static CtMethod wrapped(final CtClass returnType, final String mname, final CtClass[] parameterTypes, final CtClass[] exceptionTypes, final CtMethod body, final CtMethod.ConstParameter constParam, final CtClass declaring) throws CannotCompileException {
        final CtMethod mt = new CtMethod(returnType, mname, parameterTypes, declaring);
        mt.setModifiers(body.getModifiers());
        try {
            mt.setExceptionTypes(exceptionTypes);
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        final Bytecode code = makeBody(declaring, declaring.getClassFile2(), body, parameterTypes, returnType, constParam);
        final MethodInfo minfo = mt.getMethodInfo2();
        minfo.setCodeAttribute(code.toCodeAttribute());
        return mt;
    }
    
    static Bytecode makeBody(final CtClass clazz, final ClassFile classfile, final CtMethod wrappedBody, final CtClass[] parameters, final CtClass returnType, final CtMethod.ConstParameter cparam) throws CannotCompileException {
        final boolean isStatic = Modifier.isStatic(wrappedBody.getModifiers());
        final Bytecode code = new Bytecode(classfile.getConstPool(), 0, 0);
        final int stacksize = makeBody0(clazz, classfile, wrappedBody, isStatic, parameters, returnType, cparam, code);
        code.setMaxStack(stacksize);
        code.setMaxLocals(isStatic, parameters, 0);
        return code;
    }
    
    protected static int makeBody0(final CtClass clazz, final ClassFile classfile, final CtMethod wrappedBody, final boolean isStatic, final CtClass[] parameters, final CtClass returnType, final CtMethod.ConstParameter cparam, final Bytecode code) throws CannotCompileException {
        if (!(clazz instanceof CtClassType)) {
            throw new CannotCompileException("bad declaring class" + clazz.getName());
        }
        if (!isStatic) {
            code.addAload(0);
        }
        int stacksize = compileParameterList(code, parameters, isStatic ? 0 : 1);
        int stacksize2;
        String desc;
        if (cparam == null) {
            stacksize2 = 0;
            desc = CtMethod.ConstParameter.defaultDescriptor();
        }
        else {
            stacksize2 = cparam.compile(code);
            desc = cparam.descriptor();
        }
        checkSignature(wrappedBody, desc);
        String bodyname;
        try {
            bodyname = addBodyMethod((CtClassType)clazz, classfile, wrappedBody);
        }
        catch (BadBytecode e) {
            throw new CannotCompileException(e);
        }
        if (isStatic) {
            code.addInvokestatic(Bytecode.THIS, bodyname, desc);
        }
        else {
            code.addInvokespecial(Bytecode.THIS, bodyname, desc);
        }
        compileReturn(code, returnType);
        if (stacksize < stacksize2 + 2) {
            stacksize = stacksize2 + 2;
        }
        return stacksize;
    }
    
    private static void checkSignature(final CtMethod wrappedBody, final String descriptor) throws CannotCompileException {
        if (!descriptor.equals(wrappedBody.getMethodInfo2().getDescriptor())) {
            throw new CannotCompileException("wrapped method with a bad signature: " + wrappedBody.getDeclaringClass().getName() + '.' + wrappedBody.getName());
        }
    }
    
    private static String addBodyMethod(final CtClassType clazz, final ClassFile classfile, final CtMethod src) throws BadBytecode, CannotCompileException {
        final Hashtable bodies = clazz.getHiddenMethods();
        String bodyname = bodies.get(src);
        if (bodyname == null) {
            do {
                bodyname = "_added_m$" + clazz.getUniqueNumber();
            } while (classfile.getMethod(bodyname) != null);
            final ClassMap map = new ClassMap();
            map.put(src.getDeclaringClass().getName(), clazz.getName());
            final MethodInfo body = new MethodInfo(classfile.getConstPool(), bodyname, src.getMethodInfo2(), map);
            final int acc = body.getAccessFlags();
            body.setAccessFlags(AccessFlag.setPrivate(acc));
            body.addAttribute(new SyntheticAttribute(classfile.getConstPool()));
            classfile.addMethod(body);
            bodies.put(src, bodyname);
            final CtMember.Cache cache = clazz.hasMemberCache();
            if (cache != null) {
                cache.addMethod(new CtMethod(body, clazz));
            }
        }
        return bodyname;
    }
    
    static int compileParameterList(final Bytecode code, final CtClass[] params, final int regno) {
        return JvstCodeGen.compileParameterList(code, params, regno);
    }
    
    private static void compileReturn(final Bytecode code, final CtClass type) {
        if (type.isPrimitive()) {
            final CtPrimitiveType pt = (CtPrimitiveType)type;
            if (pt != CtClass.voidType) {
                final String wrapper = pt.getWrapperName();
                code.addCheckcast(wrapper);
                code.addInvokevirtual(wrapper, pt.getGetMethodName(), pt.getGetMethodDescriptor());
            }
            code.addOpcode(pt.getReturnOp());
        }
        else {
            code.addCheckcast(type);
            code.addOpcode(176);
        }
    }
}

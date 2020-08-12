// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import javassist.bytecode.Descriptor;
import javassist.bytecode.ClassFile;
import javassist.bytecode.Bytecode;

class CtNewWrappedConstructor extends CtNewWrappedMethod
{
    private static final int PASS_NONE = 0;
    private static final int PASS_PARAMS = 2;
    
    public static CtConstructor wrapped(final CtClass[] parameterTypes, final CtClass[] exceptionTypes, final int howToCallSuper, final CtMethod body, final CtMethod.ConstParameter constParam, final CtClass declaring) throws CannotCompileException {
        try {
            final CtConstructor cons = new CtConstructor(parameterTypes, declaring);
            cons.setExceptionTypes(exceptionTypes);
            final Bytecode code = makeBody(declaring, declaring.getClassFile2(), howToCallSuper, body, parameterTypes, constParam);
            cons.getMethodInfo2().setCodeAttribute(code.toCodeAttribute());
            return cons;
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
    }
    
    protected static Bytecode makeBody(final CtClass declaring, final ClassFile classfile, final int howToCallSuper, final CtMethod wrappedBody, final CtClass[] parameters, final CtMethod.ConstParameter cparam) throws CannotCompileException {
        final int superclazz = classfile.getSuperclassId();
        final Bytecode code = new Bytecode(classfile.getConstPool(), 0, 0);
        code.setMaxLocals(false, parameters, 0);
        code.addAload(0);
        int stacksize;
        if (howToCallSuper == 0) {
            stacksize = 1;
            code.addInvokespecial(superclazz, "<init>", "()V");
        }
        else if (howToCallSuper == 2) {
            stacksize = code.addLoadParameters(parameters, 1) + 1;
            code.addInvokespecial(superclazz, "<init>", Descriptor.ofConstructor(parameters));
        }
        else {
            stacksize = CtNewWrappedMethod.compileParameterList(code, parameters, 1);
            int stacksize2;
            String desc;
            if (cparam == null) {
                stacksize2 = 2;
                desc = CtMethod.ConstParameter.defaultConstDescriptor();
            }
            else {
                stacksize2 = cparam.compile(code) + 2;
                desc = cparam.constDescriptor();
            }
            if (stacksize < stacksize2) {
                stacksize = stacksize2;
            }
            code.addInvokespecial(superclazz, "<init>", desc);
        }
        if (wrappedBody == null) {
            code.add(177);
        }
        else {
            final int stacksize2 = CtNewWrappedMethod.makeBody0(declaring, classfile, wrappedBody, false, parameters, CtClass.voidType, cparam, code);
            if (stacksize < stacksize2) {
                stacksize = stacksize2;
            }
        }
        code.setMaxStack(stacksize);
        return code;
    }
}

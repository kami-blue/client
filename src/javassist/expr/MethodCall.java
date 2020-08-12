// 
// Decompiled by Procyon v0.5.36
// 

package javassist.expr;

import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.ClassPool;
import javassist.bytecode.BadBytecode;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;
import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.bytecode.Descriptor;
import javassist.NotFoundException;
import javassist.CtBehavior;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.CtClass;
import javassist.bytecode.CodeIterator;

public class MethodCall extends Expr
{
    protected MethodCall(final int pos, final CodeIterator i, final CtClass declaring, final MethodInfo m) {
        super(pos, i, declaring, m);
    }
    
    private int getNameAndType(final ConstPool cp) {
        final int pos = this.currentPos;
        final int c = this.iterator.byteAt(pos);
        final int index = this.iterator.u16bitAt(pos + 1);
        if (c == 185) {
            return cp.getInterfaceMethodrefNameAndType(index);
        }
        return cp.getMethodrefNameAndType(index);
    }
    
    @Override
    public CtBehavior where() {
        return super.where();
    }
    
    @Override
    public int getLineNumber() {
        return super.getLineNumber();
    }
    
    @Override
    public String getFileName() {
        return super.getFileName();
    }
    
    protected CtClass getCtClass() throws NotFoundException {
        return this.thisClass.getClassPool().get(this.getClassName());
    }
    
    public String getClassName() {
        final ConstPool cp = this.getConstPool();
        final int pos = this.currentPos;
        final int c = this.iterator.byteAt(pos);
        final int index = this.iterator.u16bitAt(pos + 1);
        String cname;
        if (c == 185) {
            cname = cp.getInterfaceMethodrefClassName(index);
        }
        else {
            cname = cp.getMethodrefClassName(index);
        }
        if (cname.charAt(0) == '[') {
            cname = Descriptor.toClassName(cname);
        }
        return cname;
    }
    
    public String getMethodName() {
        final ConstPool cp = this.getConstPool();
        final int nt = this.getNameAndType(cp);
        return cp.getUtf8Info(cp.getNameAndTypeName(nt));
    }
    
    public CtMethod getMethod() throws NotFoundException {
        return this.getCtClass().getMethod(this.getMethodName(), this.getSignature());
    }
    
    public String getSignature() {
        final ConstPool cp = this.getConstPool();
        final int nt = this.getNameAndType(cp);
        return cp.getUtf8Info(cp.getNameAndTypeDescriptor(nt));
    }
    
    @Override
    public CtClass[] mayThrow() {
        return super.mayThrow();
    }
    
    public boolean isSuper() {
        return this.iterator.byteAt(this.currentPos) == 183 && !this.where().getDeclaringClass().getName().equals(this.getClassName());
    }
    
    @Override
    public void replace(final String statement) throws CannotCompileException {
        this.thisClass.getClassFile();
        final ConstPool constPool = this.getConstPool();
        final int pos = this.currentPos;
        final int index = this.iterator.u16bitAt(pos + 1);
        final int c = this.iterator.byteAt(pos);
        int opcodeSize;
        String classname;
        String methodname;
        String signature;
        if (c == 185) {
            opcodeSize = 5;
            classname = constPool.getInterfaceMethodrefClassName(index);
            methodname = constPool.getInterfaceMethodrefName(index);
            signature = constPool.getInterfaceMethodrefType(index);
        }
        else {
            if (c != 184 && c != 183 && c != 182) {
                throw new CannotCompileException("not method invocation");
            }
            opcodeSize = 3;
            classname = constPool.getMethodrefClassName(index);
            methodname = constPool.getMethodrefName(index);
            signature = constPool.getMethodrefType(index);
        }
        final Javac jc = new Javac(this.thisClass);
        final ClassPool cp = this.thisClass.getClassPool();
        final CodeAttribute ca = this.iterator.get();
        try {
            final CtClass[] params = Descriptor.getParameterTypes(signature, cp);
            final CtClass retType = Descriptor.getReturnType(signature, cp);
            final int paramVar = ca.getMaxLocals();
            jc.recordParams(classname, params, true, paramVar, this.withinStatic());
            final int retVar = jc.recordReturnType(retType, true);
            if (c == 184) {
                jc.recordStaticProceed(classname, methodname);
            }
            else if (c == 183) {
                jc.recordSpecialProceed("$0", classname, methodname, signature, index);
            }
            else {
                jc.recordProceed("$0", methodname);
            }
            Expr.checkResultValue(retType, statement);
            final Bytecode bytecode = jc.getBytecode();
            Expr.storeStack(params, c == 184, paramVar, bytecode);
            jc.recordLocalVariables(ca, pos);
            if (retType != CtClass.voidType) {
                bytecode.addConstZero(retType);
                bytecode.addStore(retVar, retType);
            }
            jc.compileStmnt(statement);
            if (retType != CtClass.voidType) {
                bytecode.addLoad(retVar, retType);
            }
            this.replace0(pos, bytecode, opcodeSize);
        }
        catch (CompileError e) {
            throw new CannotCompileException(e);
        }
        catch (NotFoundException e2) {
            throw new CannotCompileException(e2);
        }
        catch (BadBytecode e3) {
            throw new CannotCompileException("broken method");
        }
    }
}

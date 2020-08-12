// 
// Decompiled by Procyon v0.5.36
// 

package javassist.expr;

import javassist.compiler.JvstTypeChecker;
import javassist.compiler.MemberResolver;
import javassist.compiler.ast.ASTList;
import javassist.compiler.JvstCodeGen;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.ClassPool;
import javassist.bytecode.BadBytecode;
import javassist.compiler.CompileError;
import javassist.compiler.ProceedHandler;
import javassist.bytecode.Descriptor;
import javassist.compiler.Javac;
import javassist.CannotCompileException;
import javassist.CtConstructor;
import javassist.bytecode.ConstPool;
import javassist.NotFoundException;
import javassist.CtBehavior;
import javassist.bytecode.MethodInfo;
import javassist.CtClass;
import javassist.bytecode.CodeIterator;

public class NewExpr extends Expr
{
    String newTypeName;
    int newPos;
    
    protected NewExpr(final int pos, final CodeIterator i, final CtClass declaring, final MethodInfo m, final String type, final int np) {
        super(pos, i, declaring, m);
        this.newTypeName = type;
        this.newPos = np;
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
    
    private CtClass getCtClass() throws NotFoundException {
        return this.thisClass.getClassPool().get(this.newTypeName);
    }
    
    public String getClassName() {
        return this.newTypeName;
    }
    
    public String getSignature() {
        final ConstPool constPool = this.getConstPool();
        final int methodIndex = this.iterator.u16bitAt(this.currentPos + 1);
        return constPool.getMethodrefType(methodIndex);
    }
    
    public CtConstructor getConstructor() throws NotFoundException {
        final ConstPool cp = this.getConstPool();
        final int index = this.iterator.u16bitAt(this.currentPos + 1);
        final String desc = cp.getMethodrefType(index);
        return this.getCtClass().getConstructor(desc);
    }
    
    @Override
    public CtClass[] mayThrow() {
        return super.mayThrow();
    }
    
    private int canReplace() throws CannotCompileException {
        final int op = this.iterator.byteAt(this.newPos + 3);
        if (op == 89) {
            return (this.iterator.byteAt(this.newPos + 4) == 94 && this.iterator.byteAt(this.newPos + 5) == 88) ? 6 : 4;
        }
        if (op == 90 && this.iterator.byteAt(this.newPos + 4) == 95) {
            return 5;
        }
        return 3;
    }
    
    @Override
    public void replace(final String statement) throws CannotCompileException {
        this.thisClass.getClassFile();
        final int bytecodeSize = 3;
        int pos = this.newPos;
        final int newIndex = this.iterator.u16bitAt(pos + 1);
        final int codeSize = this.canReplace();
        for (int end = pos + codeSize, i = pos; i < end; ++i) {
            this.iterator.writeByte(0, i);
        }
        final ConstPool constPool = this.getConstPool();
        pos = this.currentPos;
        final int methodIndex = this.iterator.u16bitAt(pos + 1);
        final String signature = constPool.getMethodrefType(methodIndex);
        final Javac jc = new Javac(this.thisClass);
        final ClassPool cp = this.thisClass.getClassPool();
        final CodeAttribute ca = this.iterator.get();
        try {
            final CtClass[] params = Descriptor.getParameterTypes(signature, cp);
            final CtClass newType = cp.get(this.newTypeName);
            final int paramVar = ca.getMaxLocals();
            jc.recordParams(this.newTypeName, params, true, paramVar, this.withinStatic());
            final int retVar = jc.recordReturnType(newType, true);
            jc.recordProceed(new ProceedForNew(newType, newIndex, methodIndex));
            Expr.checkResultValue(newType, statement);
            final Bytecode bytecode = jc.getBytecode();
            Expr.storeStack(params, true, paramVar, bytecode);
            jc.recordLocalVariables(ca, pos);
            bytecode.addConstZero(newType);
            bytecode.addStore(retVar, newType);
            jc.compileStmnt(statement);
            if (codeSize > 3) {
                bytecode.addAload(retVar);
            }
            this.replace0(pos, bytecode, 3);
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
    
    static class ProceedForNew implements ProceedHandler
    {
        CtClass newType;
        int newIndex;
        int methodIndex;
        
        ProceedForNew(final CtClass nt, final int ni, final int mi) {
            this.newType = nt;
            this.newIndex = ni;
            this.methodIndex = mi;
        }
        
        @Override
        public void doit(final JvstCodeGen gen, final Bytecode bytecode, final ASTList args) throws CompileError {
            bytecode.addOpcode(187);
            bytecode.addIndex(this.newIndex);
            bytecode.addOpcode(89);
            gen.atMethodCallCore(this.newType, "<init>", args, false, true, -1, null);
            gen.setType(this.newType);
        }
        
        @Override
        public void setReturnType(final JvstTypeChecker c, final ASTList args) throws CompileError {
            c.atMethodCallCore(this.newType, "<init>", args);
            c.setType(this.newType);
        }
    }
}

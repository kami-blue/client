// 
// Decompiled by Procyon v0.5.36
// 

package javassist.expr;

import javassist.compiler.JvstTypeChecker;
import javassist.CtPrimitiveType;
import javassist.compiler.ast.ASTList;
import javassist.compiler.JvstCodeGen;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.BadBytecode;
import javassist.compiler.CompileError;
import javassist.CannotCompileException;
import javassist.compiler.ProceedHandler;
import javassist.bytecode.Descriptor;
import javassist.compiler.Javac;
import javassist.bytecode.ConstPool;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.CtBehavior;
import javassist.bytecode.MethodInfo;
import javassist.CtClass;
import javassist.bytecode.CodeIterator;

public class FieldAccess extends Expr
{
    int opcode;
    
    protected FieldAccess(final int pos, final CodeIterator i, final CtClass declaring, final MethodInfo m, final int op) {
        super(pos, i, declaring, m);
        this.opcode = op;
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
    
    public boolean isStatic() {
        return isStatic(this.opcode);
    }
    
    static boolean isStatic(final int c) {
        return c == 178 || c == 179;
    }
    
    public boolean isReader() {
        return this.opcode == 180 || this.opcode == 178;
    }
    
    public boolean isWriter() {
        return this.opcode == 181 || this.opcode == 179;
    }
    
    private CtClass getCtClass() throws NotFoundException {
        return this.thisClass.getClassPool().get(this.getClassName());
    }
    
    public String getClassName() {
        final int index = this.iterator.u16bitAt(this.currentPos + 1);
        return this.getConstPool().getFieldrefClassName(index);
    }
    
    public String getFieldName() {
        final int index = this.iterator.u16bitAt(this.currentPos + 1);
        return this.getConstPool().getFieldrefName(index);
    }
    
    public CtField getField() throws NotFoundException {
        final CtClass cc = this.getCtClass();
        final int index = this.iterator.u16bitAt(this.currentPos + 1);
        final ConstPool cp = this.getConstPool();
        return cc.getField(cp.getFieldrefName(index), cp.getFieldrefType(index));
    }
    
    @Override
    public CtClass[] mayThrow() {
        return super.mayThrow();
    }
    
    public String getSignature() {
        final int index = this.iterator.u16bitAt(this.currentPos + 1);
        return this.getConstPool().getFieldrefType(index);
    }
    
    @Override
    public void replace(final String statement) throws CannotCompileException {
        this.thisClass.getClassFile();
        final ConstPool constPool = this.getConstPool();
        final int pos = this.currentPos;
        final int index = this.iterator.u16bitAt(pos + 1);
        final Javac jc = new Javac(this.thisClass);
        final CodeAttribute ca = this.iterator.get();
        try {
            final CtClass fieldType = Descriptor.toCtClass(constPool.getFieldrefType(index), this.thisClass.getClassPool());
            final boolean read = this.isReader();
            CtClass[] params;
            CtClass retType;
            if (read) {
                params = new CtClass[0];
                retType = fieldType;
            }
            else {
                params = new CtClass[] { fieldType };
                retType = CtClass.voidType;
            }
            final int paramVar = ca.getMaxLocals();
            jc.recordParams(constPool.getFieldrefClassName(index), params, true, paramVar, this.withinStatic());
            boolean included = Expr.checkResultValue(retType, statement);
            if (read) {
                included = true;
            }
            final int retVar = jc.recordReturnType(retType, included);
            if (read) {
                jc.recordProceed(new ProceedForRead(retType, this.opcode, index, paramVar));
            }
            else {
                jc.recordType(fieldType);
                jc.recordProceed(new ProceedForWrite(params[0], this.opcode, index, paramVar));
            }
            final Bytecode bytecode = jc.getBytecode();
            Expr.storeStack(params, this.isStatic(), paramVar, bytecode);
            jc.recordLocalVariables(ca, pos);
            if (included) {
                if (retType == CtClass.voidType) {
                    bytecode.addOpcode(1);
                    bytecode.addAstore(retVar);
                }
                else {
                    bytecode.addConstZero(retType);
                    bytecode.addStore(retVar, retType);
                }
            }
            jc.compileStmnt(statement);
            if (read) {
                bytecode.addLoad(retVar, retType);
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
    
    static class ProceedForRead implements ProceedHandler
    {
        CtClass fieldType;
        int opcode;
        int targetVar;
        int index;
        
        ProceedForRead(final CtClass type, final int op, final int i, final int var) {
            this.fieldType = type;
            this.targetVar = var;
            this.opcode = op;
            this.index = i;
        }
        
        @Override
        public void doit(final JvstCodeGen gen, final Bytecode bytecode, final ASTList args) throws CompileError {
            if (args != null && !gen.isParamListName(args)) {
                throw new CompileError("$proceed() cannot take a parameter for field reading");
            }
            int stack;
            if (FieldAccess.isStatic(this.opcode)) {
                stack = 0;
            }
            else {
                stack = -1;
                bytecode.addAload(this.targetVar);
            }
            if (this.fieldType instanceof CtPrimitiveType) {
                stack += ((CtPrimitiveType)this.fieldType).getDataSize();
            }
            else {
                ++stack;
            }
            bytecode.add(this.opcode);
            bytecode.addIndex(this.index);
            bytecode.growStack(stack);
            gen.setType(this.fieldType);
        }
        
        @Override
        public void setReturnType(final JvstTypeChecker c, final ASTList args) throws CompileError {
            c.setType(this.fieldType);
        }
    }
    
    static class ProceedForWrite implements ProceedHandler
    {
        CtClass fieldType;
        int opcode;
        int targetVar;
        int index;
        
        ProceedForWrite(final CtClass type, final int op, final int i, final int var) {
            this.fieldType = type;
            this.targetVar = var;
            this.opcode = op;
            this.index = i;
        }
        
        @Override
        public void doit(final JvstCodeGen gen, final Bytecode bytecode, final ASTList args) throws CompileError {
            if (gen.getMethodArgsLength(args) != 1) {
                throw new CompileError("$proceed() cannot take more than one parameter for field writing");
            }
            int stack;
            if (FieldAccess.isStatic(this.opcode)) {
                stack = 0;
            }
            else {
                stack = -1;
                bytecode.addAload(this.targetVar);
            }
            gen.atMethodArgs(args, new int[1], new int[1], new String[1]);
            gen.doNumCast(this.fieldType);
            if (this.fieldType instanceof CtPrimitiveType) {
                stack -= ((CtPrimitiveType)this.fieldType).getDataSize();
            }
            else {
                --stack;
            }
            bytecode.add(this.opcode);
            bytecode.addIndex(this.index);
            bytecode.growStack(stack);
            gen.setType(CtClass.voidType);
            gen.addNullIfVoid();
        }
        
        @Override
        public void setReturnType(final JvstTypeChecker c, final ASTList args) throws CompileError {
            c.atMethodArgs(args, new int[1], new int[1], new String[1]);
            c.setType(CtClass.voidType);
            c.addNullIfVoid();
        }
    }
}

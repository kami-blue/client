// 
// Decompiled by Procyon v0.5.36
// 

package javassist.expr;

import javassist.compiler.JvstTypeChecker;
import javassist.compiler.ast.ASTList;
import javassist.compiler.JvstCodeGen;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.compiler.ProceedHandler;
import javassist.compiler.Javac;
import javassist.CtPrimitiveType;
import javassist.bytecode.BadBytecode;
import javassist.compiler.CompileError;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import javassist.CtBehavior;
import javassist.bytecode.MethodInfo;
import javassist.CtClass;
import javassist.bytecode.CodeIterator;

public class NewArray extends Expr
{
    int opcode;
    
    protected NewArray(final int pos, final CodeIterator i, final CtClass declaring, final MethodInfo m, final int op) {
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
    
    @Override
    public CtClass[] mayThrow() {
        return super.mayThrow();
    }
    
    public CtClass getComponentType() throws NotFoundException {
        if (this.opcode == 188) {
            final int atype = this.iterator.byteAt(this.currentPos + 1);
            return this.getPrimitiveType(atype);
        }
        if (this.opcode == 189 || this.opcode == 197) {
            final int index = this.iterator.u16bitAt(this.currentPos + 1);
            String desc = this.getConstPool().getClassInfo(index);
            final int dim = Descriptor.arrayDimension(desc);
            desc = Descriptor.toArrayComponent(desc, dim);
            return Descriptor.toCtClass(desc, this.thisClass.getClassPool());
        }
        throw new RuntimeException("bad opcode: " + this.opcode);
    }
    
    CtClass getPrimitiveType(final int atype) {
        switch (atype) {
            case 4: {
                return CtClass.booleanType;
            }
            case 5: {
                return CtClass.charType;
            }
            case 6: {
                return CtClass.floatType;
            }
            case 7: {
                return CtClass.doubleType;
            }
            case 8: {
                return CtClass.byteType;
            }
            case 9: {
                return CtClass.shortType;
            }
            case 10: {
                return CtClass.intType;
            }
            case 11: {
                return CtClass.longType;
            }
            default: {
                throw new RuntimeException("bad atype: " + atype);
            }
        }
    }
    
    public int getDimension() {
        if (this.opcode == 188) {
            return 1;
        }
        if (this.opcode == 189 || this.opcode == 197) {
            final int index = this.iterator.u16bitAt(this.currentPos + 1);
            final String desc = this.getConstPool().getClassInfo(index);
            return Descriptor.arrayDimension(desc) + ((this.opcode == 189) ? 1 : 0);
        }
        throw new RuntimeException("bad opcode: " + this.opcode);
    }
    
    public int getCreatedDimensions() {
        if (this.opcode == 197) {
            return this.iterator.byteAt(this.currentPos + 3);
        }
        return 1;
    }
    
    @Override
    public void replace(final String statement) throws CannotCompileException {
        try {
            this.replace2(statement);
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
    
    private void replace2(final String statement) throws CompileError, NotFoundException, BadBytecode, CannotCompileException {
        this.thisClass.getClassFile();
        final ConstPool constPool = this.getConstPool();
        final int pos = this.currentPos;
        int index = 0;
        int dim = 1;
        String desc;
        int codeLength;
        if (this.opcode == 188) {
            index = this.iterator.byteAt(this.currentPos + 1);
            final CtPrimitiveType cpt = (CtPrimitiveType)this.getPrimitiveType(index);
            desc = "[" + cpt.getDescriptor();
            codeLength = 2;
        }
        else if (this.opcode == 189) {
            index = this.iterator.u16bitAt(pos + 1);
            desc = constPool.getClassInfo(index);
            if (desc.startsWith("[")) {
                desc = "[" + desc;
            }
            else {
                desc = "[L" + desc + ";";
            }
            codeLength = 3;
        }
        else {
            if (this.opcode != 197) {
                throw new RuntimeException("bad opcode: " + this.opcode);
            }
            index = this.iterator.u16bitAt(this.currentPos + 1);
            desc = constPool.getClassInfo(index);
            dim = this.iterator.byteAt(this.currentPos + 3);
            codeLength = 4;
        }
        final CtClass retType = Descriptor.toCtClass(desc, this.thisClass.getClassPool());
        final Javac jc = new Javac(this.thisClass);
        final CodeAttribute ca = this.iterator.get();
        final CtClass[] params = new CtClass[dim];
        for (int i = 0; i < dim; ++i) {
            params[i] = CtClass.intType;
        }
        final int paramVar = ca.getMaxLocals();
        jc.recordParams("java.lang.Object", params, true, paramVar, this.withinStatic());
        Expr.checkResultValue(retType, statement);
        final int retVar = jc.recordReturnType(retType, true);
        jc.recordProceed(new ProceedForArray(retType, this.opcode, index, dim));
        final Bytecode bytecode = jc.getBytecode();
        Expr.storeStack(params, true, paramVar, bytecode);
        jc.recordLocalVariables(ca, pos);
        bytecode.addOpcode(1);
        bytecode.addAstore(retVar);
        jc.compileStmnt(statement);
        bytecode.addAload(retVar);
        this.replace0(pos, bytecode, codeLength);
    }
    
    static class ProceedForArray implements ProceedHandler
    {
        CtClass arrayType;
        int opcode;
        int index;
        int dimension;
        
        ProceedForArray(final CtClass type, final int op, final int i, final int dim) {
            this.arrayType = type;
            this.opcode = op;
            this.index = i;
            this.dimension = dim;
        }
        
        @Override
        public void doit(final JvstCodeGen gen, final Bytecode bytecode, final ASTList args) throws CompileError {
            final int num = gen.getMethodArgsLength(args);
            if (num != this.dimension) {
                throw new CompileError("$proceed() with a wrong number of parameters");
            }
            gen.atMethodArgs(args, new int[num], new int[num], new String[num]);
            bytecode.addOpcode(this.opcode);
            if (this.opcode == 189) {
                bytecode.addIndex(this.index);
            }
            else if (this.opcode == 188) {
                bytecode.add(this.index);
            }
            else {
                bytecode.addIndex(this.index);
                bytecode.add(this.dimension);
                bytecode.growStack(1 - this.dimension);
            }
            gen.setType(this.arrayType);
        }
        
        @Override
        public void setReturnType(final JvstTypeChecker c, final ASTList args) throws CompileError {
            c.setType(this.arrayType);
        }
    }
}

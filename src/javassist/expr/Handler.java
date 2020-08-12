// 
// Decompiled by Procyon v0.5.36
// 

package javassist.expr;

import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.bytecode.ConstPool;
import javassist.CtBehavior;
import javassist.bytecode.MethodInfo;
import javassist.CtClass;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ExceptionTable;

public class Handler extends Expr
{
    private static String EXCEPTION_NAME;
    private ExceptionTable etable;
    private int index;
    
    protected Handler(final ExceptionTable et, final int nth, final CodeIterator it, final CtClass declaring, final MethodInfo m) {
        super(et.handlerPc(nth), it, declaring, m);
        this.etable = et;
        this.index = nth;
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
    
    public CtClass getType() throws NotFoundException {
        final int type = this.etable.catchType(this.index);
        if (type == 0) {
            return null;
        }
        final ConstPool cp = this.getConstPool();
        final String name = cp.getClassInfo(type);
        return this.thisClass.getClassPool().getCtClass(name);
    }
    
    public boolean isFinally() {
        return this.etable.catchType(this.index) == 0;
    }
    
    @Override
    public void replace(final String statement) throws CannotCompileException {
        throw new RuntimeException("not implemented yet");
    }
    
    public void insertBefore(final String src) throws CannotCompileException {
        this.edited = true;
        final ConstPool cp = this.getConstPool();
        final CodeAttribute ca = this.iterator.get();
        final Javac jv = new Javac(this.thisClass);
        final Bytecode b = jv.getBytecode();
        b.setStackDepth(1);
        b.setMaxLocals(ca.getMaxLocals());
        try {
            final CtClass type = this.getType();
            final int var = jv.recordVariable(type, Handler.EXCEPTION_NAME);
            jv.recordReturnType(type, false);
            b.addAstore(var);
            jv.compileStmnt(src);
            b.addAload(var);
            final int oldHandler = this.etable.handlerPc(this.index);
            b.addOpcode(167);
            b.addIndex(oldHandler - this.iterator.getCodeLength() - b.currentPc() + 1);
            this.maxStack = b.getMaxStack();
            this.maxLocals = b.getMaxLocals();
            final int pos = this.iterator.append(b.get());
            this.iterator.append(b.getExceptionTable(), pos);
            this.etable.setHandlerPc(this.index, pos);
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        catch (CompileError e2) {
            throw new CannotCompileException(e2);
        }
    }
    
    static {
        Handler.EXCEPTION_NAME = "$1";
    }
}

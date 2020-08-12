// 
// Decompiled by Procyon v0.5.36
// 

package javassist.expr;

import javassist.bytecode.ExceptionTable;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.BadBytecode;
import javassist.CannotCompileException;
import javassist.bytecode.MethodInfo;
import javassist.CtClass;

public class ExprEditor
{
    public boolean doit(final CtClass clazz, final MethodInfo minfo) throws CannotCompileException {
        final CodeAttribute codeAttr = minfo.getCodeAttribute();
        if (codeAttr == null) {
            return false;
        }
        final CodeIterator iterator = codeAttr.iterator();
        boolean edited = false;
        final LoopContext context = new LoopContext(codeAttr.getMaxLocals());
        while (iterator.hasNext()) {
            if (this.loopBody(iterator, clazz, minfo, context)) {
                edited = true;
            }
        }
        final ExceptionTable et = codeAttr.getExceptionTable();
        for (int n = et.size(), i = 0; i < n; ++i) {
            final Handler h = new Handler(et, i, iterator, clazz, minfo);
            this.edit(h);
            if (h.edited()) {
                edited = true;
                context.updateMax(h.locals(), h.stack());
            }
        }
        if (codeAttr.getMaxLocals() < context.maxLocals) {
            codeAttr.setMaxLocals(context.maxLocals);
        }
        codeAttr.setMaxStack(codeAttr.getMaxStack() + context.maxStack);
        try {
            if (edited) {
                minfo.rebuildStackMapIf6(clazz.getClassPool(), clazz.getClassFile2());
            }
        }
        catch (BadBytecode b) {
            throw new CannotCompileException(b.getMessage(), b);
        }
        return edited;
    }
    
    boolean doit(final CtClass clazz, final MethodInfo minfo, final LoopContext context, final CodeIterator iterator, int endPos) throws CannotCompileException {
        boolean edited = false;
        while (iterator.hasNext() && iterator.lookAhead() < endPos) {
            final int size = iterator.getCodeLength();
            if (this.loopBody(iterator, clazz, minfo, context)) {
                edited = true;
                final int size2 = iterator.getCodeLength();
                if (size == size2) {
                    continue;
                }
                endPos += size2 - size;
            }
        }
        return edited;
    }
    
    final boolean loopBody(final CodeIterator iterator, final CtClass clazz, final MethodInfo minfo, final LoopContext context) throws CannotCompileException {
        try {
            Expr expr = null;
            final int pos = iterator.next();
            final int c = iterator.byteAt(pos);
            if (c >= 178) {
                if (c < 188) {
                    if (c == 184 || c == 185 || c == 182) {
                        expr = new MethodCall(pos, iterator, clazz, minfo);
                        this.edit((MethodCall)expr);
                    }
                    else if (c == 180 || c == 178 || c == 181 || c == 179) {
                        expr = new FieldAccess(pos, iterator, clazz, minfo, c);
                        this.edit((FieldAccess)expr);
                    }
                    else if (c == 187) {
                        final int index = iterator.u16bitAt(pos + 1);
                        context.newList = new NewOp(context.newList, pos, minfo.getConstPool().getClassInfo(index));
                    }
                    else if (c == 183) {
                        final NewOp newList = context.newList;
                        if (newList != null && minfo.getConstPool().isConstructor(newList.type, iterator.u16bitAt(pos + 1)) > 0) {
                            expr = new NewExpr(pos, iterator, clazz, minfo, newList.type, newList.pos);
                            this.edit((NewExpr)expr);
                            context.newList = newList.next;
                        }
                        else {
                            final MethodCall mcall = new MethodCall(pos, iterator, clazz, minfo);
                            if (mcall.getMethodName().equals("<init>")) {
                                final ConstructorCall ccall = (ConstructorCall)(expr = new ConstructorCall(pos, iterator, clazz, minfo));
                                this.edit(ccall);
                            }
                            else {
                                expr = mcall;
                                this.edit(mcall);
                            }
                        }
                    }
                }
                else if (c == 188 || c == 189 || c == 197) {
                    expr = new NewArray(pos, iterator, clazz, minfo, c);
                    this.edit((NewArray)expr);
                }
                else if (c == 193) {
                    expr = new Instanceof(pos, iterator, clazz, minfo);
                    this.edit((Instanceof)expr);
                }
                else if (c == 192) {
                    expr = new Cast(pos, iterator, clazz, minfo);
                    this.edit((Cast)expr);
                }
            }
            if (expr != null && expr.edited()) {
                context.updateMax(expr.locals(), expr.stack());
                return true;
            }
            return false;
        }
        catch (BadBytecode e) {
            throw new CannotCompileException(e);
        }
    }
    
    public void edit(final NewExpr e) throws CannotCompileException {
    }
    
    public void edit(final NewArray a) throws CannotCompileException {
    }
    
    public void edit(final MethodCall m) throws CannotCompileException {
    }
    
    public void edit(final ConstructorCall c) throws CannotCompileException {
    }
    
    public void edit(final FieldAccess f) throws CannotCompileException {
    }
    
    public void edit(final Instanceof i) throws CannotCompileException {
    }
    
    public void edit(final Cast c) throws CannotCompileException {
    }
    
    public void edit(final Handler h) throws CannotCompileException {
    }
    
    static final class NewOp
    {
        NewOp next;
        int pos;
        String type;
        
        NewOp(final NewOp n, final int p, final String t) {
            this.next = n;
            this.pos = p;
            this.type = t;
        }
    }
    
    static final class LoopContext
    {
        NewOp newList;
        int maxLocals;
        int maxStack;
        
        LoopContext(final int locals) {
            this.maxLocals = locals;
            this.maxStack = 0;
            this.newList = null;
        }
        
        void updateMax(final int locals, final int stack) {
            if (this.maxLocals < locals) {
                this.maxLocals = locals;
            }
            if (this.maxStack < stack) {
                this.maxStack = stack;
            }
        }
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler;

import javassist.NotFoundException;
import javassist.compiler.ast.CallExpr;
import javassist.CtPrimitiveType;
import javassist.compiler.ast.ASTList;
import javassist.compiler.ast.Symbol;
import javassist.compiler.ast.CastExpr;
import javassist.compiler.ast.Visitor;
import javassist.compiler.ast.ASTree;
import javassist.compiler.ast.Expr;
import javassist.compiler.ast.Member;
import javassist.ClassPool;
import javassist.CtClass;

public class JvstTypeChecker extends TypeChecker
{
    private JvstCodeGen codeGen;
    
    public JvstTypeChecker(final CtClass cc, final ClassPool cp, final JvstCodeGen gen) {
        super(cc, cp);
        this.codeGen = gen;
    }
    
    public void addNullIfVoid() {
        if (this.exprType == 344) {
            this.exprType = 307;
            this.arrayDim = 0;
            this.className = "java/lang/Object";
        }
    }
    
    @Override
    public void atMember(final Member mem) throws CompileError {
        final String name = mem.get();
        if (name.equals(this.codeGen.paramArrayName)) {
            this.exprType = 307;
            this.arrayDim = 1;
            this.className = "java/lang/Object";
        }
        else if (name.equals("$sig")) {
            this.exprType = 307;
            this.arrayDim = 1;
            this.className = "java/lang/Class";
        }
        else if (name.equals("$type") || name.equals("$class")) {
            this.exprType = 307;
            this.arrayDim = 0;
            this.className = "java/lang/Class";
        }
        else {
            super.atMember(mem);
        }
    }
    
    @Override
    protected void atFieldAssign(final Expr expr, final int op, final ASTree left, final ASTree right) throws CompileError {
        if (left instanceof Member && ((Member)left).get().equals(this.codeGen.paramArrayName)) {
            right.accept(this);
            final CtClass[] params = this.codeGen.paramTypeList;
            if (params == null) {
                return;
            }
            for (int n = params.length, i = 0; i < n; ++i) {
                this.compileUnwrapValue(params[i]);
            }
        }
        else {
            super.atFieldAssign(expr, op, left, right);
        }
    }
    
    @Override
    public void atCastExpr(final CastExpr expr) throws CompileError {
        final ASTList classname = expr.getClassName();
        if (classname != null && expr.getArrayDim() == 0) {
            final ASTree p = classname.head();
            if (p instanceof Symbol && classname.tail() == null) {
                final String typename = ((Symbol)p).get();
                if (typename.equals(this.codeGen.returnCastName)) {
                    this.atCastToRtype(expr);
                    return;
                }
                if (typename.equals("$w")) {
                    this.atCastToWrapper(expr);
                    return;
                }
            }
        }
        super.atCastExpr(expr);
    }
    
    protected void atCastToRtype(final CastExpr expr) throws CompileError {
        final CtClass returnType = this.codeGen.returnType;
        expr.getOprand().accept(this);
        if (this.exprType == 344 || CodeGen.isRefType(this.exprType) || this.arrayDim > 0) {
            this.compileUnwrapValue(returnType);
        }
        else if (returnType instanceof CtPrimitiveType) {
            final CtPrimitiveType pt = (CtPrimitiveType)returnType;
            final int destType = MemberResolver.descToType(pt.getDescriptor());
            this.exprType = destType;
            this.arrayDim = 0;
            this.className = null;
        }
    }
    
    protected void atCastToWrapper(final CastExpr expr) throws CompileError {
        expr.getOprand().accept(this);
        if (CodeGen.isRefType(this.exprType) || this.arrayDim > 0) {
            return;
        }
        final CtClass clazz = this.resolver.lookupClass(this.exprType, this.arrayDim, this.className);
        if (clazz instanceof CtPrimitiveType) {
            this.exprType = 307;
            this.arrayDim = 0;
            this.className = "java/lang/Object";
        }
    }
    
    @Override
    public void atCallExpr(final CallExpr expr) throws CompileError {
        final ASTree method = expr.oprand1();
        if (method instanceof Member) {
            final String name = ((Member)method).get();
            if (this.codeGen.procHandler != null && name.equals(this.codeGen.proceedName)) {
                this.codeGen.procHandler.setReturnType(this, (ASTList)expr.oprand2());
                return;
            }
            if (name.equals("$cflow")) {
                this.atCflow((ASTList)expr.oprand2());
                return;
            }
        }
        super.atCallExpr(expr);
    }
    
    protected void atCflow(final ASTList cname) throws CompileError {
        this.exprType = 324;
        this.arrayDim = 0;
        this.className = null;
    }
    
    public boolean isParamListName(final ASTList args) {
        if (this.codeGen.paramTypeList != null && args != null && args.tail() == null) {
            final ASTree left = args.head();
            return left instanceof Member && ((Member)left).get().equals(this.codeGen.paramListName);
        }
        return false;
    }
    
    @Override
    public int getMethodArgsLength(ASTList args) {
        final String pname = this.codeGen.paramListName;
        int n = 0;
        while (args != null) {
            final ASTree a = args.head();
            if (a instanceof Member && ((Member)a).get().equals(pname)) {
                if (this.codeGen.paramTypeList != null) {
                    n += this.codeGen.paramTypeList.length;
                }
            }
            else {
                ++n;
            }
            args = args.tail();
        }
        return n;
    }
    
    @Override
    public void atMethodArgs(ASTList args, final int[] types, final int[] dims, final String[] cnames) throws CompileError {
        final CtClass[] params = this.codeGen.paramTypeList;
        final String pname = this.codeGen.paramListName;
        int i = 0;
        while (args != null) {
            final ASTree a = args.head();
            if (a instanceof Member && ((Member)a).get().equals(pname)) {
                if (params != null) {
                    for (final CtClass p : params) {
                        this.setType(p);
                        types[i] = this.exprType;
                        dims[i] = this.arrayDim;
                        cnames[i] = this.className;
                        ++i;
                    }
                }
            }
            else {
                a.accept(this);
                types[i] = this.exprType;
                dims[i] = this.arrayDim;
                cnames[i] = this.className;
                ++i;
            }
            args = args.tail();
        }
    }
    
    void compileInvokeSpecial(final ASTree target, final String classname, final String methodname, final String descriptor, final ASTList args) throws CompileError {
        target.accept(this);
        final int nargs = this.getMethodArgsLength(args);
        this.atMethodArgs(args, new int[nargs], new int[nargs], new String[nargs]);
        this.setReturnType(descriptor);
        this.addNullIfVoid();
    }
    
    protected void compileUnwrapValue(final CtClass type) throws CompileError {
        if (type == CtClass.voidType) {
            this.addNullIfVoid();
        }
        else {
            this.setType(type);
        }
    }
    
    public void setType(final CtClass type) throws CompileError {
        this.setType(type, 0);
    }
    
    private void setType(final CtClass type, final int dim) throws CompileError {
        if (type.isPrimitive()) {
            final CtPrimitiveType pt = (CtPrimitiveType)type;
            this.exprType = MemberResolver.descToType(pt.getDescriptor());
            this.arrayDim = dim;
            this.className = null;
        }
        else {
            if (type.isArray()) {
                try {
                    this.setType(type.getComponentType(), dim + 1);
                    return;
                }
                catch (NotFoundException e) {
                    throw new CompileError("undefined type: " + type.getName());
                }
            }
            this.exprType = 307;
            this.arrayDim = dim;
            this.className = MemberResolver.javaToJvmName(type.getName());
        }
    }
}

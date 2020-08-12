// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler;

import javassist.NotFoundException;
import javassist.compiler.ast.Declarator;
import javassist.compiler.ast.Stmnt;
import javassist.compiler.ast.CallExpr;
import javassist.CtPrimitiveType;
import javassist.compiler.ast.ASTList;
import javassist.compiler.ast.Symbol;
import javassist.compiler.ast.CastExpr;
import javassist.compiler.ast.Visitor;
import javassist.compiler.ast.ASTree;
import javassist.compiler.ast.Expr;
import javassist.bytecode.Descriptor;
import javassist.compiler.ast.Member;
import javassist.ClassPool;
import javassist.bytecode.Bytecode;
import javassist.CtClass;

public class JvstCodeGen extends MemberCodeGen
{
    String paramArrayName;
    String paramListName;
    CtClass[] paramTypeList;
    private int paramVarBase;
    private boolean useParam0;
    private String param0Type;
    public static final String sigName = "$sig";
    public static final String dollarTypeName = "$type";
    public static final String clazzName = "$class";
    private CtClass dollarType;
    CtClass returnType;
    String returnCastName;
    private String returnVarName;
    public static final String wrapperCastName = "$w";
    String proceedName;
    public static final String cflowName = "$cflow";
    ProceedHandler procHandler;
    
    public JvstCodeGen(final Bytecode b, final CtClass cc, final ClassPool cp) {
        super(b, cc, cp);
        this.paramArrayName = null;
        this.paramListName = null;
        this.paramTypeList = null;
        this.paramVarBase = 0;
        this.useParam0 = false;
        this.param0Type = null;
        this.dollarType = null;
        this.returnType = null;
        this.returnCastName = null;
        this.returnVarName = null;
        this.proceedName = null;
        this.procHandler = null;
        this.setTypeChecker(new JvstTypeChecker(cc, cp, this));
    }
    
    private int indexOfParam1() {
        return this.paramVarBase + (this.useParam0 ? 1 : 0);
    }
    
    public void setProceedHandler(final ProceedHandler h, final String name) {
        this.proceedName = name;
        this.procHandler = h;
    }
    
    public void addNullIfVoid() {
        if (this.exprType == 344) {
            this.bytecode.addOpcode(1);
            this.exprType = 307;
            this.arrayDim = 0;
            this.className = "java/lang/Object";
        }
    }
    
    @Override
    public void atMember(final Member mem) throws CompileError {
        final String name = mem.get();
        if (name.equals(this.paramArrayName)) {
            compileParameterList(this.bytecode, this.paramTypeList, this.indexOfParam1());
            this.exprType = 307;
            this.arrayDim = 1;
            this.className = "java/lang/Object";
        }
        else if (name.equals("$sig")) {
            this.bytecode.addLdc(Descriptor.ofMethod(this.returnType, this.paramTypeList));
            this.bytecode.addInvokestatic("javassist/runtime/Desc", "getParams", "(Ljava/lang/String;)[Ljava/lang/Class;");
            this.exprType = 307;
            this.arrayDim = 1;
            this.className = "java/lang/Class";
        }
        else if (name.equals("$type")) {
            if (this.dollarType == null) {
                throw new CompileError("$type is not available");
            }
            this.bytecode.addLdc(Descriptor.of(this.dollarType));
            this.callGetType("getType");
        }
        else if (name.equals("$class")) {
            if (this.param0Type == null) {
                throw new CompileError("$class is not available");
            }
            this.bytecode.addLdc(this.param0Type);
            this.callGetType("getClazz");
        }
        else {
            super.atMember(mem);
        }
    }
    
    private void callGetType(final String method) {
        this.bytecode.addInvokestatic("javassist/runtime/Desc", method, "(Ljava/lang/String;)Ljava/lang/Class;");
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = "java/lang/Class";
    }
    
    @Override
    protected void atFieldAssign(final Expr expr, final int op, final ASTree left, final ASTree right, final boolean doDup) throws CompileError {
        if (left instanceof Member && ((Member)left).get().equals(this.paramArrayName)) {
            if (op != 61) {
                throw new CompileError("bad operator for " + this.paramArrayName);
            }
            right.accept(this);
            if (this.arrayDim != 1 || this.exprType != 307) {
                throw new CompileError("invalid type for " + this.paramArrayName);
            }
            this.atAssignParamList(this.paramTypeList, this.bytecode);
            if (!doDup) {
                this.bytecode.addOpcode(87);
            }
        }
        else {
            super.atFieldAssign(expr, op, left, right, doDup);
        }
    }
    
    protected void atAssignParamList(final CtClass[] params, final Bytecode code) throws CompileError {
        if (params == null) {
            return;
        }
        int varNo = this.indexOfParam1();
        for (int n = params.length, i = 0; i < n; ++i) {
            code.addOpcode(89);
            code.addIconst(i);
            code.addOpcode(50);
            this.compileUnwrapValue(params[i], code);
            code.addStore(varNo, params[i]);
            varNo += (CodeGen.is2word(this.exprType, this.arrayDim) ? 2 : 1);
        }
    }
    
    @Override
    public void atCastExpr(final CastExpr expr) throws CompileError {
        final ASTList classname = expr.getClassName();
        if (classname != null && expr.getArrayDim() == 0) {
            final ASTree p = classname.head();
            if (p instanceof Symbol && classname.tail() == null) {
                final String typename = ((Symbol)p).get();
                if (typename.equals(this.returnCastName)) {
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
        expr.getOprand().accept(this);
        if (this.exprType == 344 || CodeGen.isRefType(this.exprType) || this.arrayDim > 0) {
            this.compileUnwrapValue(this.returnType, this.bytecode);
        }
        else {
            if (!(this.returnType instanceof CtPrimitiveType)) {
                throw new CompileError("invalid cast");
            }
            final CtPrimitiveType pt = (CtPrimitiveType)this.returnType;
            final int destType = MemberResolver.descToType(pt.getDescriptor());
            this.atNumCastExpr(this.exprType, destType);
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
            final CtPrimitiveType pt = (CtPrimitiveType)clazz;
            final String wrapper = pt.getWrapperName();
            this.bytecode.addNew(wrapper);
            this.bytecode.addOpcode(89);
            if (pt.getDataSize() > 1) {
                this.bytecode.addOpcode(94);
            }
            else {
                this.bytecode.addOpcode(93);
            }
            this.bytecode.addOpcode(88);
            this.bytecode.addInvokespecial(wrapper, "<init>", "(" + pt.getDescriptor() + ")V");
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
            if (this.procHandler != null && name.equals(this.proceedName)) {
                this.procHandler.doit(this, this.bytecode, (ASTList)expr.oprand2());
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
        final StringBuffer sbuf = new StringBuffer();
        if (cname == null || cname.tail() != null) {
            throw new CompileError("bad $cflow");
        }
        makeCflowName(sbuf, cname.head());
        final String name = sbuf.toString();
        final Object[] names = this.resolver.getClassPool().lookupCflow(name);
        if (names == null) {
            throw new CompileError("no such $cflow: " + name);
        }
        this.bytecode.addGetstatic((String)names[0], (String)names[1], "Ljavassist/runtime/Cflow;");
        this.bytecode.addInvokevirtual("javassist.runtime.Cflow", "value", "()I");
        this.exprType = 324;
        this.arrayDim = 0;
        this.className = null;
    }
    
    private static void makeCflowName(final StringBuffer sbuf, final ASTree name) throws CompileError {
        if (name instanceof Symbol) {
            sbuf.append(((Symbol)name).get());
            return;
        }
        if (name instanceof Expr) {
            final Expr expr = (Expr)name;
            if (expr.getOperator() == 46) {
                makeCflowName(sbuf, expr.oprand1());
                sbuf.append('.');
                makeCflowName(sbuf, expr.oprand2());
                return;
            }
        }
        throw new CompileError("bad $cflow");
    }
    
    public boolean isParamListName(final ASTList args) {
        if (this.paramTypeList != null && args != null && args.tail() == null) {
            final ASTree left = args.head();
            return left instanceof Member && ((Member)left).get().equals(this.paramListName);
        }
        return false;
    }
    
    @Override
    public int getMethodArgsLength(ASTList args) {
        final String pname = this.paramListName;
        int n = 0;
        while (args != null) {
            final ASTree a = args.head();
            if (a instanceof Member && ((Member)a).get().equals(pname)) {
                if (this.paramTypeList != null) {
                    n += this.paramTypeList.length;
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
        final CtClass[] params = this.paramTypeList;
        final String pname = this.paramListName;
        int i = 0;
        while (args != null) {
            final ASTree a = args.head();
            if (a instanceof Member && ((Member)a).get().equals(pname)) {
                if (params != null) {
                    final int n = params.length;
                    int regno = this.indexOfParam1();
                    for (final CtClass p : params) {
                        regno += this.bytecode.addLoad(regno, p);
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
    
    void compileInvokeSpecial(final ASTree target, final int methodIndex, final String descriptor, final ASTList args) throws CompileError {
        target.accept(this);
        final int nargs = this.getMethodArgsLength(args);
        this.atMethodArgs(args, new int[nargs], new int[nargs], new String[nargs]);
        this.bytecode.addInvokespecial(methodIndex, descriptor);
        this.setReturnType(descriptor, false, false);
        this.addNullIfVoid();
    }
    
    @Override
    protected void atReturnStmnt(final Stmnt st) throws CompileError {
        ASTree result = st.getLeft();
        if (result != null && this.returnType == CtClass.voidType) {
            this.compileExpr(result);
            if (CodeGen.is2word(this.exprType, this.arrayDim)) {
                this.bytecode.addOpcode(88);
            }
            else if (this.exprType != 344) {
                this.bytecode.addOpcode(87);
            }
            result = null;
        }
        this.atReturnStmnt2(result);
    }
    
    public int recordReturnType(final CtClass type, final String castName, final String resultName, final SymbolTable tbl) throws CompileError {
        this.returnType = type;
        this.returnCastName = castName;
        this.returnVarName = resultName;
        if (resultName == null) {
            return -1;
        }
        final int varNo = this.getMaxLocals();
        final int locals = varNo + this.recordVar(type, resultName, varNo, tbl);
        this.setMaxLocals(locals);
        return varNo;
    }
    
    public void recordType(final CtClass t) {
        this.dollarType = t;
    }
    
    public int recordParams(final CtClass[] params, final boolean isStatic, final String prefix, final String paramVarName, final String paramsName, final SymbolTable tbl) throws CompileError {
        return this.recordParams(params, isStatic, prefix, paramVarName, paramsName, !isStatic, 0, this.getThisName(), tbl);
    }
    
    public int recordParams(final CtClass[] params, final boolean isStatic, final String prefix, final String paramVarName, final String paramsName, final boolean use0, final int paramBase, final String target, final SymbolTable tbl) throws CompileError {
        this.paramTypeList = params;
        this.paramArrayName = paramVarName;
        this.paramListName = paramsName;
        this.paramVarBase = paramBase;
        this.useParam0 = use0;
        if (target != null) {
            this.param0Type = MemberResolver.jvmToJavaName(target);
        }
        this.inStaticMethod = isStatic;
        int varNo = paramBase;
        if (use0) {
            final String varName = prefix + "0";
            final Declarator decl = new Declarator(307, MemberResolver.javaToJvmName(target), 0, varNo++, new Symbol(varName));
            tbl.append(varName, decl);
        }
        for (int i = 0; i < params.length; ++i) {
            varNo += this.recordVar(params[i], prefix + (i + 1), varNo, tbl);
        }
        if (this.getMaxLocals() < varNo) {
            this.setMaxLocals(varNo);
        }
        return varNo;
    }
    
    public int recordVariable(final CtClass type, final String varName, final SymbolTable tbl) throws CompileError {
        if (varName == null) {
            return -1;
        }
        final int varNo = this.getMaxLocals();
        final int locals = varNo + this.recordVar(type, varName, varNo, tbl);
        this.setMaxLocals(locals);
        return varNo;
    }
    
    private int recordVar(final CtClass cc, final String varName, final int varNo, final SymbolTable tbl) throws CompileError {
        if (cc == CtClass.voidType) {
            this.exprType = 307;
            this.arrayDim = 0;
            this.className = "java/lang/Object";
        }
        else {
            this.setType(cc);
        }
        final Declarator decl = new Declarator(this.exprType, this.className, this.arrayDim, varNo, new Symbol(varName));
        tbl.append(varName, decl);
        return CodeGen.is2word(this.exprType, this.arrayDim) ? 2 : 1;
    }
    
    public void recordVariable(final String typeDesc, final String varName, final int varNo, final SymbolTable tbl) throws CompileError {
        int dim;
        char c;
        for (dim = 0; (c = typeDesc.charAt(dim)) == '['; ++dim) {}
        final int type = MemberResolver.descToType(c);
        String cname = null;
        if (type == 307) {
            if (dim == 0) {
                cname = typeDesc.substring(1, typeDesc.length() - 1);
            }
            else {
                cname = typeDesc.substring(dim + 1, typeDesc.length() - 1);
            }
        }
        final Declarator decl = new Declarator(type, cname, dim, varNo, new Symbol(varName));
        tbl.append(varName, decl);
    }
    
    public static int compileParameterList(final Bytecode code, final CtClass[] params, int regno) {
        if (params == null) {
            code.addIconst(0);
            code.addAnewarray("java.lang.Object");
            return 1;
        }
        final CtClass[] args = { null };
        final int n = params.length;
        code.addIconst(n);
        code.addAnewarray("java.lang.Object");
        for (int i = 0; i < n; ++i) {
            code.addOpcode(89);
            code.addIconst(i);
            if (params[i].isPrimitive()) {
                final CtPrimitiveType pt = (CtPrimitiveType)params[i];
                final String wrapper = pt.getWrapperName();
                code.addNew(wrapper);
                code.addOpcode(89);
                final int s = code.addLoad(regno, pt);
                regno += s;
                args[0] = pt;
                code.addInvokespecial(wrapper, "<init>", Descriptor.ofMethod(CtClass.voidType, args));
            }
            else {
                code.addAload(regno);
                ++regno;
            }
            code.addOpcode(83);
        }
        return 8;
    }
    
    protected void compileUnwrapValue(final CtClass type, final Bytecode code) throws CompileError {
        if (type == CtClass.voidType) {
            this.addNullIfVoid();
            return;
        }
        if (this.exprType == 344) {
            throw new CompileError("invalid type for " + this.returnCastName);
        }
        if (type instanceof CtPrimitiveType) {
            final CtPrimitiveType pt = (CtPrimitiveType)type;
            final String wrapper = pt.getWrapperName();
            code.addCheckcast(wrapper);
            code.addInvokevirtual(wrapper, pt.getGetMethodName(), pt.getGetMethodDescriptor());
            this.setType(type);
        }
        else {
            code.addCheckcast(type);
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
    
    public void doNumCast(final CtClass type) throws CompileError {
        if (this.arrayDim == 0 && !CodeGen.isRefType(this.exprType)) {
            if (!(type instanceof CtPrimitiveType)) {
                throw new CompileError("type mismatch");
            }
            final CtPrimitiveType pt = (CtPrimitiveType)type;
            this.atNumCastExpr(this.exprType, MemberResolver.descToType(pt.getDescriptor()));
        }
    }
}

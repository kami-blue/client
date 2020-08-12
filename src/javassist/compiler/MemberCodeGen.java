// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler;

import javassist.compiler.ast.MethodDecl;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.Modifier;
import javassist.bytecode.Descriptor;
import javassist.bytecode.AccessFlag;
import javassist.compiler.ast.Symbol;
import javassist.compiler.ast.Expr;
import javassist.compiler.ast.Keyword;
import javassist.compiler.ast.Member;
import javassist.compiler.ast.CallExpr;
import javassist.compiler.ast.ASTree;
import javassist.compiler.ast.ArrayInit;
import javassist.compiler.ast.NewExpr;
import javassist.compiler.ast.Declarator;
import javassist.compiler.ast.Pair;
import javassist.compiler.ast.Visitor;
import java.util.ArrayList;
import javassist.compiler.ast.ASTList;
import javassist.compiler.ast.Stmnt;
import javassist.CtMethod;
import javassist.bytecode.ClassFile;
import javassist.ClassPool;
import javassist.bytecode.Bytecode;
import javassist.bytecode.MethodInfo;
import javassist.CtClass;

public class MemberCodeGen extends CodeGen
{
    protected MemberResolver resolver;
    protected CtClass thisClass;
    protected MethodInfo thisMethod;
    protected boolean resultStatic;
    
    public MemberCodeGen(final Bytecode b, final CtClass cc, final ClassPool cp) {
        super(b);
        this.resolver = new MemberResolver(cp);
        this.thisClass = cc;
        this.thisMethod = null;
    }
    
    public int getMajorVersion() {
        final ClassFile cf = this.thisClass.getClassFile2();
        if (cf == null) {
            return ClassFile.MAJOR_VERSION;
        }
        return cf.getMajorVersion();
    }
    
    public void setThisMethod(final CtMethod m) {
        this.thisMethod = m.getMethodInfo2();
        if (this.typeChecker != null) {
            this.typeChecker.setThisMethod(this.thisMethod);
        }
    }
    
    public CtClass getThisClass() {
        return this.thisClass;
    }
    
    @Override
    protected String getThisName() {
        return MemberResolver.javaToJvmName(this.thisClass.getName());
    }
    
    @Override
    protected String getSuperName() throws CompileError {
        return MemberResolver.javaToJvmName(MemberResolver.getSuperclass(this.thisClass).getName());
    }
    
    @Override
    protected void insertDefaultSuperCall() throws CompileError {
        this.bytecode.addAload(0);
        this.bytecode.addInvokespecial(MemberResolver.getSuperclass(this.thisClass), "<init>", "()V");
    }
    
    @Override
    protected void atTryStmnt(final Stmnt st) throws CompileError {
        final Bytecode bc = this.bytecode;
        final Stmnt body = (Stmnt)st.getLeft();
        if (body == null) {
            return;
        }
        ASTList catchList = (ASTList)st.getRight().getLeft();
        final Stmnt finallyBlock = (Stmnt)st.getRight().getRight().getLeft();
        final ArrayList gotoList = new ArrayList();
        JsrHook jsrHook = null;
        if (finallyBlock != null) {
            jsrHook = new JsrHook(this);
        }
        final int start = bc.currentPc();
        body.accept(this);
        final int end = bc.currentPc();
        if (start == end) {
            throw new CompileError("empty try block");
        }
        boolean tryNotReturn = !this.hasReturned;
        if (tryNotReturn) {
            bc.addOpcode(167);
            gotoList.add(new Integer(bc.currentPc()));
            bc.addIndex(0);
        }
        final int var = this.getMaxLocals();
        this.incMaxLocals(1);
        while (catchList != null) {
            final Pair p = (Pair)catchList.head();
            catchList = catchList.tail();
            final Declarator decl = (Declarator)p.getLeft();
            final Stmnt block = (Stmnt)p.getRight();
            decl.setLocalVar(var);
            final CtClass type = this.resolver.lookupClassByJvmName(decl.getClassName());
            decl.setClassName(MemberResolver.javaToJvmName(type.getName()));
            bc.addExceptionHandler(start, end, bc.currentPc(), type);
            bc.growStack(1);
            bc.addAstore(var);
            this.hasReturned = false;
            if (block != null) {
                block.accept(this);
            }
            if (!this.hasReturned) {
                bc.addOpcode(167);
                gotoList.add(new Integer(bc.currentPc()));
                bc.addIndex(0);
                tryNotReturn = true;
            }
        }
        if (finallyBlock != null) {
            jsrHook.remove(this);
            final int pcAnyCatch = bc.currentPc();
            bc.addExceptionHandler(start, pcAnyCatch, pcAnyCatch, 0);
            bc.growStack(1);
            bc.addAstore(var);
            this.hasReturned = false;
            finallyBlock.accept(this);
            if (!this.hasReturned) {
                bc.addAload(var);
                bc.addOpcode(191);
            }
            this.addFinally(jsrHook.jsrList, finallyBlock);
        }
        final int pcEnd = bc.currentPc();
        this.patchGoto(gotoList, pcEnd);
        this.hasReturned = !tryNotReturn;
        if (finallyBlock != null && tryNotReturn) {
            finallyBlock.accept(this);
        }
    }
    
    private void addFinally(final ArrayList returnList, final Stmnt finallyBlock) throws CompileError {
        final Bytecode bc = this.bytecode;
        for (int n = returnList.size(), i = 0; i < n; ++i) {
            final int[] ret = returnList.get(i);
            final int pc = ret[0];
            bc.write16bit(pc, bc.currentPc() - pc + 1);
            final ReturnHook hook = new JsrHook2(this, ret);
            finallyBlock.accept(this);
            hook.remove(this);
            if (!this.hasReturned) {
                bc.addOpcode(167);
                bc.addIndex(pc + 3 - bc.currentPc());
            }
        }
    }
    
    @Override
    public void atNewExpr(final NewExpr expr) throws CompileError {
        if (expr.isArray()) {
            this.atNewArrayExpr(expr);
        }
        else {
            final CtClass clazz = this.resolver.lookupClassByName(expr.getClassName());
            final String cname = clazz.getName();
            final ASTList args = expr.getArguments();
            this.bytecode.addNew(cname);
            this.bytecode.addOpcode(89);
            this.atMethodCallCore(clazz, "<init>", args, false, true, -1, null);
            this.exprType = 307;
            this.arrayDim = 0;
            this.className = MemberResolver.javaToJvmName(cname);
        }
    }
    
    public void atNewArrayExpr(final NewExpr expr) throws CompileError {
        final int type = expr.getArrayType();
        final ASTList size = expr.getArraySize();
        final ASTList classname = expr.getClassName();
        final ArrayInit init = expr.getInitializer();
        if (size.length() <= 1) {
            final ASTree sizeExpr = size.head();
            this.atNewArrayExpr2(type, sizeExpr, Declarator.astToClassName(classname, '/'), init);
            return;
        }
        if (init != null) {
            throw new CompileError("sorry, multi-dimensional array initializer for new is not supported");
        }
        this.atMultiNewArray(type, classname, size);
    }
    
    private void atNewArrayExpr2(final int type, final ASTree sizeExpr, final String jvmClassname, final ArrayInit init) throws CompileError {
        if (init == null) {
            if (sizeExpr == null) {
                throw new CompileError("no array size");
            }
            sizeExpr.accept(this);
        }
        else {
            if (sizeExpr != null) {
                throw new CompileError("unnecessary array size specified for new");
            }
            final int s = init.length();
            this.bytecode.addIconst(s);
        }
        String elementClass;
        if (type == 307) {
            elementClass = this.resolveClassName(jvmClassname);
            this.bytecode.addAnewarray(MemberResolver.jvmToJavaName(elementClass));
        }
        else {
            elementClass = null;
            int atype = 0;
            switch (type) {
                case 301: {
                    atype = 4;
                    break;
                }
                case 306: {
                    atype = 5;
                    break;
                }
                case 317: {
                    atype = 6;
                    break;
                }
                case 312: {
                    atype = 7;
                    break;
                }
                case 303: {
                    atype = 8;
                    break;
                }
                case 334: {
                    atype = 9;
                    break;
                }
                case 324: {
                    atype = 10;
                    break;
                }
                case 326: {
                    atype = 11;
                    break;
                }
                default: {
                    badNewExpr();
                    break;
                }
            }
            this.bytecode.addOpcode(188);
            this.bytecode.add(atype);
        }
        if (init != null) {
            final int s2 = init.length();
            ASTList list = init;
            for (int i = 0; i < s2; ++i) {
                this.bytecode.addOpcode(89);
                this.bytecode.addIconst(i);
                list.head().accept(this);
                if (!CodeGen.isRefType(type)) {
                    this.atNumCastExpr(this.exprType, type);
                }
                this.bytecode.addOpcode(CodeGen.getArrayWriteOp(type, 0));
                list = list.tail();
            }
        }
        this.exprType = type;
        this.arrayDim = 1;
        this.className = elementClass;
    }
    
    private static void badNewExpr() throws CompileError {
        throw new CompileError("bad new expression");
    }
    
    @Override
    protected void atArrayVariableAssign(final ArrayInit init, final int varType, final int varArray, final String varClass) throws CompileError {
        this.atNewArrayExpr2(varType, null, varClass, init);
    }
    
    @Override
    public void atArrayInit(final ArrayInit init) throws CompileError {
        throw new CompileError("array initializer is not supported");
    }
    
    protected void atMultiNewArray(final int type, final ASTList classname, ASTList size) throws CompileError {
        final int dim = size.length();
        int count = 0;
        while (size != null) {
            final ASTree s = size.head();
            if (s == null) {
                break;
            }
            ++count;
            s.accept(this);
            if (this.exprType != 324) {
                throw new CompileError("bad type for array size");
            }
            size = size.tail();
        }
        this.exprType = type;
        this.arrayDim = dim;
        String desc;
        if (type == 307) {
            this.className = this.resolveClassName(classname);
            desc = CodeGen.toJvmArrayName(this.className, dim);
        }
        else {
            desc = CodeGen.toJvmTypeName(type, dim);
        }
        this.bytecode.addMultiNewarray(desc, count);
    }
    
    @Override
    public void atCallExpr(final CallExpr expr) throws CompileError {
        String mname = null;
        CtClass targetClass = null;
        final ASTree method = expr.oprand1();
        final ASTList args = (ASTList)expr.oprand2();
        boolean isStatic = false;
        boolean isSpecial = false;
        int aload0pos = -1;
        final MemberResolver.Method cached = expr.getMethod();
        if (method instanceof Member) {
            mname = ((Member)method).get();
            targetClass = this.thisClass;
            if (this.inStaticMethod || (cached != null && cached.isStatic())) {
                isStatic = true;
            }
            else {
                aload0pos = this.bytecode.currentPc();
                this.bytecode.addAload(0);
            }
        }
        else if (method instanceof Keyword) {
            isSpecial = true;
            mname = "<init>";
            targetClass = this.thisClass;
            if (this.inStaticMethod) {
                throw new CompileError("a constructor cannot be static");
            }
            this.bytecode.addAload(0);
            if (((Keyword)method).get() == 336) {
                targetClass = MemberResolver.getSuperclass(targetClass);
            }
        }
        else if (method instanceof Expr) {
            final Expr e = (Expr)method;
            mname = ((Symbol)e.oprand2()).get();
            final int op = e.getOperator();
            if (op == 35) {
                targetClass = this.resolver.lookupClass(((Symbol)e.oprand1()).get(), false);
                isStatic = true;
            }
            else if (op == 46) {
                final ASTree target = e.oprand1();
                final String classFollowedByDotSuper = TypeChecker.isDotSuper(target);
                if (classFollowedByDotSuper != null) {
                    isSpecial = true;
                    targetClass = MemberResolver.getSuperInterface(this.thisClass, classFollowedByDotSuper);
                    if (this.inStaticMethod || (cached != null && cached.isStatic())) {
                        isStatic = true;
                    }
                    else {
                        aload0pos = this.bytecode.currentPc();
                        this.bytecode.addAload(0);
                    }
                }
                else {
                    if (target instanceof Keyword && ((Keyword)target).get() == 336) {
                        isSpecial = true;
                    }
                    try {
                        target.accept(this);
                    }
                    catch (NoFieldException nfe) {
                        if (nfe.getExpr() != target) {
                            throw nfe;
                        }
                        this.exprType = 307;
                        this.arrayDim = 0;
                        this.className = nfe.getField();
                        isStatic = true;
                    }
                    if (this.arrayDim > 0) {
                        targetClass = this.resolver.lookupClass("java.lang.Object", true);
                    }
                    else if (this.exprType == 307) {
                        targetClass = this.resolver.lookupClassByJvmName(this.className);
                    }
                    else {
                        badMethod();
                    }
                }
            }
            else {
                badMethod();
            }
        }
        else {
            fatal();
        }
        this.atMethodCallCore(targetClass, mname, args, isStatic, isSpecial, aload0pos, cached);
    }
    
    private static void badMethod() throws CompileError {
        throw new CompileError("bad method");
    }
    
    public void atMethodCallCore(final CtClass targetClass, final String mname, final ASTList args, boolean isStatic, final boolean isSpecial, final int aload0pos, MemberResolver.Method found) throws CompileError {
        final int nargs = this.getMethodArgsLength(args);
        final int[] types = new int[nargs];
        final int[] dims = new int[nargs];
        final String[] cnames = new String[nargs];
        if (!isStatic && found != null && found.isStatic()) {
            this.bytecode.addOpcode(87);
            isStatic = true;
        }
        final int stack = this.bytecode.getStackDepth();
        this.atMethodArgs(args, types, dims, cnames);
        if (found == null) {
            found = this.resolver.lookupMethod(targetClass, this.thisClass, this.thisMethod, mname, types, dims, cnames);
        }
        if (found == null) {
            String msg;
            if (mname.equals("<init>")) {
                msg = "constructor not found";
            }
            else {
                msg = "Method " + mname + " not found in " + targetClass.getName();
            }
            throw new CompileError(msg);
        }
        this.atMethodCallCore2(targetClass, mname, isStatic, isSpecial, aload0pos, found);
    }
    
    private void atMethodCallCore2(final CtClass targetClass, String mname, boolean isStatic, boolean isSpecial, final int aload0pos, final MemberResolver.Method found) throws CompileError {
        CtClass declClass = found.declaring;
        final MethodInfo minfo = found.info;
        String desc = minfo.getDescriptor();
        int acc = minfo.getAccessFlags();
        if (mname.equals("<init>")) {
            isSpecial = true;
            if (declClass != targetClass) {
                throw new CompileError("no such constructor: " + targetClass.getName());
            }
            if (declClass != this.thisClass && AccessFlag.isPrivate(acc)) {
                desc = this.getAccessibleConstructor(desc, declClass, minfo);
                this.bytecode.addOpcode(1);
            }
        }
        else if (AccessFlag.isPrivate(acc)) {
            if (declClass == this.thisClass) {
                isSpecial = true;
            }
            else {
                isSpecial = false;
                isStatic = true;
                final String origDesc = desc;
                if ((acc & 0x8) == 0x0) {
                    desc = Descriptor.insertParameter(declClass.getName(), origDesc);
                }
                acc = (AccessFlag.setPackage(acc) | 0x8);
                mname = this.getAccessiblePrivate(mname, origDesc, desc, minfo, declClass);
            }
        }
        boolean popTarget = false;
        if ((acc & 0x8) != 0x0) {
            if (!isStatic) {
                isStatic = true;
                if (aload0pos >= 0) {
                    this.bytecode.write(aload0pos, 0);
                }
                else {
                    popTarget = true;
                }
            }
            this.bytecode.addInvokestatic(declClass, mname, desc);
        }
        else if (isSpecial) {
            this.bytecode.addInvokespecial(targetClass, mname, desc);
        }
        else {
            if (!Modifier.isPublic(declClass.getModifiers()) || declClass.isInterface() != targetClass.isInterface()) {
                declClass = targetClass;
            }
            if (declClass.isInterface()) {
                final int nargs = Descriptor.paramSize(desc) + 1;
                this.bytecode.addInvokeinterface(declClass, mname, desc, nargs);
            }
            else {
                if (isStatic) {
                    throw new CompileError(mname + " is not static");
                }
                this.bytecode.addInvokevirtual(declClass, mname, desc);
            }
        }
        this.setReturnType(desc, isStatic, popTarget);
    }
    
    protected String getAccessiblePrivate(final String methodName, final String desc, final String newDesc, final MethodInfo minfo, final CtClass declClass) throws CompileError {
        if (this.isEnclosing(declClass, this.thisClass)) {
            final AccessorMaker maker = declClass.getAccessorMaker();
            if (maker != null) {
                return maker.getMethodAccessor(methodName, desc, newDesc, minfo);
            }
        }
        throw new CompileError("Method " + methodName + " is private");
    }
    
    protected String getAccessibleConstructor(final String desc, final CtClass declClass, final MethodInfo minfo) throws CompileError {
        if (this.isEnclosing(declClass, this.thisClass)) {
            final AccessorMaker maker = declClass.getAccessorMaker();
            if (maker != null) {
                return maker.getConstructor(declClass, desc, minfo);
            }
        }
        throw new CompileError("the called constructor is private in " + declClass.getName());
    }
    
    private boolean isEnclosing(final CtClass outer, CtClass inner) {
        try {
            while (inner != null) {
                inner = inner.getDeclaringClass();
                if (inner == outer) {
                    return true;
                }
            }
        }
        catch (NotFoundException ex) {}
        return false;
    }
    
    public int getMethodArgsLength(final ASTList args) {
        return ASTList.length(args);
    }
    
    public void atMethodArgs(ASTList args, final int[] types, final int[] dims, final String[] cnames) throws CompileError {
        int i = 0;
        while (args != null) {
            final ASTree a = args.head();
            a.accept(this);
            types[i] = this.exprType;
            dims[i] = this.arrayDim;
            cnames[i] = this.className;
            ++i;
            args = args.tail();
        }
    }
    
    void setReturnType(final String desc, final boolean isStatic, final boolean popTarget) throws CompileError {
        int i = desc.indexOf(41);
        if (i < 0) {
            badMethod();
        }
        char c = desc.charAt(++i);
        int dim = 0;
        while (c == '[') {
            ++dim;
            c = desc.charAt(++i);
        }
        this.arrayDim = dim;
        if (c == 'L') {
            final int j = desc.indexOf(59, i + 1);
            if (j < 0) {
                badMethod();
            }
            this.exprType = 307;
            this.className = desc.substring(i + 1, j);
        }
        else {
            this.exprType = MemberResolver.descToType(c);
            this.className = null;
        }
        final int etype = this.exprType;
        if (isStatic && popTarget) {
            if (CodeGen.is2word(etype, dim)) {
                this.bytecode.addOpcode(93);
                this.bytecode.addOpcode(88);
                this.bytecode.addOpcode(87);
            }
            else if (etype == 344) {
                this.bytecode.addOpcode(87);
            }
            else {
                this.bytecode.addOpcode(95);
                this.bytecode.addOpcode(87);
            }
        }
    }
    
    @Override
    protected void atFieldAssign(final Expr expr, final int op, final ASTree left, final ASTree right, final boolean doDup) throws CompileError {
        final CtField f = this.fieldAccess(left, false);
        final boolean is_static = this.resultStatic;
        if (op != 61 && !is_static) {
            this.bytecode.addOpcode(89);
        }
        int fi;
        if (op == 61) {
            final FieldInfo finfo = f.getFieldInfo2();
            this.setFieldType(finfo);
            final AccessorMaker maker = this.isAccessibleField(f, finfo);
            if (maker == null) {
                fi = this.addFieldrefInfo(f, finfo);
            }
            else {
                fi = 0;
            }
        }
        else {
            fi = this.atFieldRead(f, is_static);
        }
        final int fType = this.exprType;
        final int fDim = this.arrayDim;
        final String cname = this.className;
        this.atAssignCore(expr, op, right, fType, fDim, cname);
        final boolean is2w = CodeGen.is2word(fType, fDim);
        if (doDup) {
            int dup_code;
            if (is_static) {
                dup_code = (is2w ? 92 : 89);
            }
            else {
                dup_code = (is2w ? 93 : 90);
            }
            this.bytecode.addOpcode(dup_code);
        }
        this.atFieldAssignCore(f, is_static, fi, is2w);
        this.exprType = fType;
        this.arrayDim = fDim;
        this.className = cname;
    }
    
    private void atFieldAssignCore(final CtField f, final boolean is_static, final int fi, final boolean is2byte) throws CompileError {
        if (fi != 0) {
            if (is_static) {
                this.bytecode.add(179);
                this.bytecode.growStack(is2byte ? -2 : -1);
            }
            else {
                this.bytecode.add(181);
                this.bytecode.growStack(is2byte ? -3 : -2);
            }
            this.bytecode.addIndex(fi);
        }
        else {
            final CtClass declClass = f.getDeclaringClass();
            final AccessorMaker maker = declClass.getAccessorMaker();
            final FieldInfo finfo = f.getFieldInfo2();
            final MethodInfo minfo = maker.getFieldSetter(finfo, is_static);
            this.bytecode.addInvokestatic(declClass, minfo.getName(), minfo.getDescriptor());
        }
    }
    
    @Override
    public void atMember(final Member mem) throws CompileError {
        this.atFieldRead(mem);
    }
    
    @Override
    protected void atFieldRead(final ASTree expr) throws CompileError {
        final CtField f = this.fieldAccess(expr, true);
        if (f == null) {
            this.atArrayLength(expr);
            return;
        }
        final boolean is_static = this.resultStatic;
        final ASTree cexpr = TypeChecker.getConstantFieldValue(f);
        if (cexpr == null) {
            this.atFieldRead(f, is_static);
        }
        else {
            cexpr.accept(this);
            this.setFieldType(f.getFieldInfo2());
        }
    }
    
    private void atArrayLength(final ASTree expr) throws CompileError {
        if (this.arrayDim == 0) {
            throw new CompileError(".length applied to a non array");
        }
        this.bytecode.addOpcode(190);
        this.exprType = 324;
        this.arrayDim = 0;
    }
    
    private int atFieldRead(final CtField f, final boolean isStatic) throws CompileError {
        final FieldInfo finfo = f.getFieldInfo2();
        final boolean is2byte = this.setFieldType(finfo);
        final AccessorMaker maker = this.isAccessibleField(f, finfo);
        if (maker != null) {
            final MethodInfo minfo = maker.getFieldGetter(finfo, isStatic);
            this.bytecode.addInvokestatic(f.getDeclaringClass(), minfo.getName(), minfo.getDescriptor());
            return 0;
        }
        final int fi = this.addFieldrefInfo(f, finfo);
        if (isStatic) {
            this.bytecode.add(178);
            this.bytecode.growStack(is2byte ? 2 : 1);
        }
        else {
            this.bytecode.add(180);
            this.bytecode.growStack(is2byte ? 1 : 0);
        }
        this.bytecode.addIndex(fi);
        return fi;
    }
    
    private AccessorMaker isAccessibleField(final CtField f, final FieldInfo finfo) throws CompileError {
        if (!AccessFlag.isPrivate(finfo.getAccessFlags()) || f.getDeclaringClass() == this.thisClass) {
            return null;
        }
        final CtClass declClass = f.getDeclaringClass();
        if (!this.isEnclosing(declClass, this.thisClass)) {
            throw new CompileError("Field " + f.getName() + " in " + declClass.getName() + " is private.");
        }
        final AccessorMaker maker = declClass.getAccessorMaker();
        if (maker != null) {
            return maker;
        }
        throw new CompileError("fatal error.  bug?");
    }
    
    private boolean setFieldType(final FieldInfo finfo) throws CompileError {
        final String type = finfo.getDescriptor();
        int i = 0;
        int dim = 0;
        char c;
        for (c = type.charAt(i); c == '['; c = type.charAt(++i)) {
            ++dim;
        }
        this.arrayDim = dim;
        this.exprType = MemberResolver.descToType(c);
        if (c == 'L') {
            this.className = type.substring(i + 1, type.indexOf(59, i + 1));
        }
        else {
            this.className = null;
        }
        final boolean is2byte = dim == 0 && (c == 'J' || c == 'D');
        return is2byte;
    }
    
    private int addFieldrefInfo(final CtField f, final FieldInfo finfo) {
        final ConstPool cp = this.bytecode.getConstPool();
        final String cname = f.getDeclaringClass().getName();
        final int ci = cp.addClassInfo(cname);
        final String name = finfo.getName();
        final String type = finfo.getDescriptor();
        return cp.addFieldrefInfo(ci, name, type);
    }
    
    @Override
    protected void atClassObject2(final String cname) throws CompileError {
        if (this.getMajorVersion() < 49) {
            super.atClassObject2(cname);
        }
        else {
            this.bytecode.addLdc(this.bytecode.getConstPool().addClassInfo(cname));
        }
    }
    
    @Override
    protected void atFieldPlusPlus(final int token, final boolean isPost, final ASTree oprand, final Expr expr, final boolean doDup) throws CompileError {
        final CtField f = this.fieldAccess(oprand, false);
        final boolean is_static = this.resultStatic;
        if (!is_static) {
            this.bytecode.addOpcode(89);
        }
        final int fi = this.atFieldRead(f, is_static);
        final int t = this.exprType;
        final boolean is2w = CodeGen.is2word(t, this.arrayDim);
        int dup_code;
        if (is_static) {
            dup_code = (is2w ? 92 : 89);
        }
        else {
            dup_code = (is2w ? 93 : 90);
        }
        this.atPlusPlusCore(dup_code, doDup, token, isPost, expr);
        this.atFieldAssignCore(f, is_static, fi, is2w);
    }
    
    protected CtField fieldAccess(final ASTree expr, final boolean acceptLength) throws CompileError {
        if (expr instanceof Member) {
            final String name = ((Member)expr).get();
            CtField f = null;
            try {
                f = this.thisClass.getField(name);
            }
            catch (NotFoundException e2) {
                throw new NoFieldException(name, expr);
            }
            final boolean is_static = Modifier.isStatic(f.getModifiers());
            if (!is_static) {
                if (this.inStaticMethod) {
                    throw new CompileError("not available in a static method: " + name);
                }
                this.bytecode.addAload(0);
            }
            this.resultStatic = is_static;
            return f;
        }
        if (expr instanceof Expr) {
            final Expr e = (Expr)expr;
            final int op = e.getOperator();
            if (op == 35) {
                final CtField f2 = this.resolver.lookupField(((Symbol)e.oprand1()).get(), (Symbol)e.oprand2());
                this.resultStatic = true;
                return f2;
            }
            if (op == 46) {
                CtField f2 = null;
                try {
                    e.oprand1().accept(this);
                    if (this.exprType == 307 && this.arrayDim == 0) {
                        f2 = this.resolver.lookupFieldByJvmName(this.className, (Symbol)e.oprand2());
                    }
                    else {
                        if (acceptLength && this.arrayDim > 0 && ((Symbol)e.oprand2()).get().equals("length")) {
                            return null;
                        }
                        badLvalue();
                    }
                    final boolean is_static2 = Modifier.isStatic(f2.getModifiers());
                    if (is_static2) {
                        this.bytecode.addOpcode(87);
                    }
                    this.resultStatic = is_static2;
                    return f2;
                }
                catch (NoFieldException nfe) {
                    if (nfe.getExpr() != e.oprand1()) {
                        throw nfe;
                    }
                    final Symbol fname = (Symbol)e.oprand2();
                    final String cname = nfe.getField();
                    f2 = this.resolver.lookupFieldByJvmName2(cname, fname, expr);
                    this.resultStatic = true;
                    return f2;
                }
            }
            badLvalue();
        }
        else {
            badLvalue();
        }
        this.resultStatic = false;
        return null;
    }
    
    private static void badLvalue() throws CompileError {
        throw new CompileError("bad l-value");
    }
    
    public CtClass[] makeParamList(final MethodDecl md) throws CompileError {
        ASTList plist = md.getParams();
        CtClass[] params;
        if (plist == null) {
            params = new CtClass[0];
        }
        else {
            int i = 0;
            params = new CtClass[plist.length()];
            while (plist != null) {
                params[i++] = this.resolver.lookupClass((Declarator)plist.head());
                plist = plist.tail();
            }
        }
        return params;
    }
    
    public CtClass[] makeThrowsList(final MethodDecl md) throws CompileError {
        ASTList list = md.getThrows();
        if (list == null) {
            return null;
        }
        int i = 0;
        final CtClass[] clist = new CtClass[list.length()];
        while (list != null) {
            clist[i++] = this.resolver.lookupClassByName((ASTList)list.head());
            list = list.tail();
        }
        return clist;
    }
    
    @Override
    protected String resolveClassName(final ASTList name) throws CompileError {
        return this.resolver.resolveClassName(name);
    }
    
    @Override
    protected String resolveClassName(final String jvmName) throws CompileError {
        return this.resolver.resolveJvmClassName(jvmName);
    }
    
    static class JsrHook extends ReturnHook
    {
        ArrayList jsrList;
        CodeGen cgen;
        int var;
        
        JsrHook(final CodeGen gen) {
            super(gen);
            this.jsrList = new ArrayList();
            this.cgen = gen;
            this.var = -1;
        }
        
        private int getVar(final int size) {
            if (this.var < 0) {
                this.var = this.cgen.getMaxLocals();
                this.cgen.incMaxLocals(size);
            }
            return this.var;
        }
        
        private void jsrJmp(final Bytecode b) {
            b.addOpcode(167);
            this.jsrList.add(new int[] { b.currentPc(), this.var });
            b.addIndex(0);
        }
        
        @Override
        protected boolean doit(final Bytecode b, final int opcode) {
            switch (opcode) {
                case 177: {
                    this.jsrJmp(b);
                    break;
                }
                case 176: {
                    b.addAstore(this.getVar(1));
                    this.jsrJmp(b);
                    b.addAload(this.var);
                    break;
                }
                case 172: {
                    b.addIstore(this.getVar(1));
                    this.jsrJmp(b);
                    b.addIload(this.var);
                    break;
                }
                case 173: {
                    b.addLstore(this.getVar(2));
                    this.jsrJmp(b);
                    b.addLload(this.var);
                    break;
                }
                case 175: {
                    b.addDstore(this.getVar(2));
                    this.jsrJmp(b);
                    b.addDload(this.var);
                    break;
                }
                case 174: {
                    b.addFstore(this.getVar(1));
                    this.jsrJmp(b);
                    b.addFload(this.var);
                    break;
                }
                default: {
                    throw new RuntimeException("fatal");
                }
            }
            return false;
        }
    }
    
    static class JsrHook2 extends ReturnHook
    {
        int var;
        int target;
        
        JsrHook2(final CodeGen gen, final int[] retTarget) {
            super(gen);
            this.target = retTarget[0];
            this.var = retTarget[1];
        }
        
        @Override
        protected boolean doit(final Bytecode b, final int opcode) {
            switch (opcode) {
                case 177: {
                    break;
                }
                case 176: {
                    b.addAstore(this.var);
                    break;
                }
                case 172: {
                    b.addIstore(this.var);
                    break;
                }
                case 173: {
                    b.addLstore(this.var);
                    break;
                }
                case 175: {
                    b.addDstore(this.var);
                    break;
                }
                case 174: {
                    b.addFstore(this.var);
                    break;
                }
                default: {
                    throw new RuntimeException("fatal");
                }
            }
            b.addOpcode(167);
            b.addIndex(this.target - b.currentPc() + 3);
            return true;
        }
    }
}

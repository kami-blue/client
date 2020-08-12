// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler;

import javassist.NotFoundException;
import javassist.Modifier;
import javassist.bytecode.FieldInfo;
import javassist.compiler.ast.InstanceOfExpr;
import javassist.compiler.ast.Keyword;
import javassist.compiler.ast.DoubleConst;
import javassist.compiler.ast.IntConst;
import javassist.compiler.ast.StringL;
import javassist.compiler.ast.Symbol;
import javassist.compiler.ast.CallExpr;
import javassist.compiler.ast.Member;
import javassist.compiler.ast.BinExpr;
import javassist.compiler.ast.CastExpr;
import javassist.compiler.ast.CondExpr;
import javassist.CtField;
import javassist.compiler.ast.Declarator;
import javassist.compiler.ast.Expr;
import javassist.compiler.ast.Variable;
import javassist.compiler.ast.AssignExpr;
import javassist.compiler.ast.ArrayInit;
import javassist.compiler.ast.ASTree;
import javassist.compiler.ast.NewExpr;
import javassist.compiler.ast.ASTList;
import javassist.ClassPool;
import javassist.bytecode.MethodInfo;
import javassist.CtClass;
import javassist.bytecode.Opcode;
import javassist.compiler.ast.Visitor;

public class TypeChecker extends Visitor implements Opcode, TokenId
{
    static final String javaLangObject = "java.lang.Object";
    static final String jvmJavaLangObject = "java/lang/Object";
    static final String jvmJavaLangString = "java/lang/String";
    static final String jvmJavaLangClass = "java/lang/Class";
    protected int exprType;
    protected int arrayDim;
    protected String className;
    protected MemberResolver resolver;
    protected CtClass thisClass;
    protected MethodInfo thisMethod;
    
    public TypeChecker(final CtClass cc, final ClassPool cp) {
        this.resolver = new MemberResolver(cp);
        this.thisClass = cc;
        this.thisMethod = null;
    }
    
    protected static String argTypesToString(final int[] types, final int[] dims, final String[] cnames) {
        final StringBuffer sbuf = new StringBuffer();
        sbuf.append('(');
        final int n = types.length;
        if (n > 0) {
            int i = 0;
            while (true) {
                typeToString(sbuf, types[i], dims[i], cnames[i]);
                if (++i >= n) {
                    break;
                }
                sbuf.append(',');
            }
        }
        sbuf.append(')');
        return sbuf.toString();
    }
    
    protected static StringBuffer typeToString(final StringBuffer sbuf, final int type, int dim, final String cname) {
        String s;
        if (type == 307) {
            s = MemberResolver.jvmToJavaName(cname);
        }
        else if (type == 412) {
            s = "Object";
        }
        else {
            try {
                s = MemberResolver.getTypeName(type);
            }
            catch (CompileError e) {
                s = "?";
            }
        }
        sbuf.append(s);
        while (dim-- > 0) {
            sbuf.append("[]");
        }
        return sbuf;
    }
    
    public void setThisMethod(final MethodInfo m) {
        this.thisMethod = m;
    }
    
    protected static void fatal() throws CompileError {
        throw new CompileError("fatal");
    }
    
    protected String getThisName() {
        return MemberResolver.javaToJvmName(this.thisClass.getName());
    }
    
    protected String getSuperName() throws CompileError {
        return MemberResolver.javaToJvmName(MemberResolver.getSuperclass(this.thisClass).getName());
    }
    
    protected String resolveClassName(final ASTList name) throws CompileError {
        return this.resolver.resolveClassName(name);
    }
    
    protected String resolveClassName(final String jvmName) throws CompileError {
        return this.resolver.resolveJvmClassName(jvmName);
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
            this.atMethodCallCore(clazz, "<init>", args);
            this.exprType = 307;
            this.arrayDim = 0;
            this.className = MemberResolver.javaToJvmName(cname);
        }
    }
    
    public void atNewArrayExpr(final NewExpr expr) throws CompileError {
        final int type = expr.getArrayType();
        final ASTList size = expr.getArraySize();
        final ASTList classname = expr.getClassName();
        final ASTree init = expr.getInitializer();
        if (init != null) {
            init.accept(this);
        }
        if (size.length() > 1) {
            this.atMultiNewArray(type, classname, size);
        }
        else {
            final ASTree sizeExpr = size.head();
            if (sizeExpr != null) {
                sizeExpr.accept(this);
            }
            this.exprType = type;
            this.arrayDim = 1;
            if (type == 307) {
                this.className = this.resolveClassName(classname);
            }
            else {
                this.className = null;
            }
        }
    }
    
    @Override
    public void atArrayInit(final ArrayInit init) throws CompileError {
        ASTList list = init;
        while (list != null) {
            final ASTree h = list.head();
            list = list.tail();
            if (h != null) {
                h.accept(this);
            }
        }
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
            size = size.tail();
        }
        this.exprType = type;
        this.arrayDim = dim;
        if (type == 307) {
            this.className = this.resolveClassName(classname);
        }
        else {
            this.className = null;
        }
    }
    
    @Override
    public void atAssignExpr(final AssignExpr expr) throws CompileError {
        final int op = expr.getOperator();
        final ASTree left = expr.oprand1();
        final ASTree right = expr.oprand2();
        if (left instanceof Variable) {
            this.atVariableAssign(expr, op, (Variable)left, ((Variable)left).getDeclarator(), right);
        }
        else {
            if (left instanceof Expr) {
                final Expr e = (Expr)left;
                if (e.getOperator() == 65) {
                    this.atArrayAssign(expr, op, (Expr)left, right);
                    return;
                }
            }
            this.atFieldAssign(expr, op, left, right);
        }
    }
    
    private void atVariableAssign(final Expr expr, final int op, final Variable var, final Declarator d, final ASTree right) throws CompileError {
        final int varType = d.getType();
        final int varArray = d.getArrayDim();
        final String varClass = d.getClassName();
        if (op != 61) {
            this.atVariable(var);
        }
        right.accept(this);
        this.exprType = varType;
        this.arrayDim = varArray;
        this.className = varClass;
    }
    
    private void atArrayAssign(final Expr expr, final int op, final Expr array, final ASTree right) throws CompileError {
        this.atArrayRead(array.oprand1(), array.oprand2());
        final int aType = this.exprType;
        final int aDim = this.arrayDim;
        final String cname = this.className;
        right.accept(this);
        this.exprType = aType;
        this.arrayDim = aDim;
        this.className = cname;
    }
    
    protected void atFieldAssign(final Expr expr, final int op, final ASTree left, final ASTree right) throws CompileError {
        final CtField f = this.fieldAccess(left);
        this.atFieldRead(f);
        final int fType = this.exprType;
        final int fDim = this.arrayDim;
        final String cname = this.className;
        right.accept(this);
        this.exprType = fType;
        this.arrayDim = fDim;
        this.className = cname;
    }
    
    @Override
    public void atCondExpr(final CondExpr expr) throws CompileError {
        this.booleanExpr(expr.condExpr());
        expr.thenExpr().accept(this);
        final int type1 = this.exprType;
        final int dim1 = this.arrayDim;
        final String cname1 = this.className;
        expr.elseExpr().accept(this);
        if (dim1 == 0 && dim1 == this.arrayDim) {
            if (CodeGen.rightIsStrong(type1, this.exprType)) {
                expr.setThen(new CastExpr(this.exprType, 0, expr.thenExpr()));
            }
            else if (CodeGen.rightIsStrong(this.exprType, type1)) {
                expr.setElse(new CastExpr(type1, 0, expr.elseExpr()));
                this.exprType = type1;
            }
        }
    }
    
    @Override
    public void atBinExpr(final BinExpr expr) throws CompileError {
        final int token = expr.getOperator();
        final int k = CodeGen.lookupBinOp(token);
        if (k >= 0) {
            if (token == 43) {
                Expr e = this.atPlusExpr(expr);
                if (e != null) {
                    e = CallExpr.makeCall(Expr.make(46, e, new Member("toString")), null);
                    expr.setOprand1(e);
                    expr.setOprand2(null);
                    this.className = "java/lang/String";
                }
            }
            else {
                final ASTree left = expr.oprand1();
                final ASTree right = expr.oprand2();
                left.accept(this);
                final int type1 = this.exprType;
                right.accept(this);
                if (!this.isConstant(expr, token, left, right)) {
                    this.computeBinExprType(expr, token, type1);
                }
            }
        }
        else {
            this.booleanExpr(expr);
        }
    }
    
    private Expr atPlusExpr(final BinExpr expr) throws CompileError {
        final ASTree left = expr.oprand1();
        final ASTree right = expr.oprand2();
        if (right == null) {
            left.accept(this);
            return null;
        }
        if (isPlusExpr(left)) {
            final Expr newExpr = this.atPlusExpr((BinExpr)left);
            if (newExpr != null) {
                right.accept(this);
                this.exprType = 307;
                this.arrayDim = 0;
                this.className = "java/lang/StringBuffer";
                return makeAppendCall(newExpr, right);
            }
        }
        else {
            left.accept(this);
        }
        final int type1 = this.exprType;
        final int dim1 = this.arrayDim;
        final String cname = this.className;
        right.accept(this);
        if (this.isConstant(expr, 43, left, right)) {
            return null;
        }
        if ((type1 == 307 && dim1 == 0 && "java/lang/String".equals(cname)) || (this.exprType == 307 && this.arrayDim == 0 && "java/lang/String".equals(this.className))) {
            final ASTList sbufClass = ASTList.make(new Symbol("java"), new Symbol("lang"), new Symbol("StringBuffer"));
            final ASTree e = new NewExpr(sbufClass, null);
            this.exprType = 307;
            this.arrayDim = 0;
            this.className = "java/lang/StringBuffer";
            return makeAppendCall(makeAppendCall(e, left), right);
        }
        this.computeBinExprType(expr, 43, type1);
        return null;
    }
    
    private boolean isConstant(final BinExpr expr, final int op, ASTree left, ASTree right) throws CompileError {
        left = stripPlusExpr(left);
        right = stripPlusExpr(right);
        ASTree newExpr = null;
        if (left instanceof StringL && right instanceof StringL && op == 43) {
            newExpr = new StringL(((StringL)left).get() + ((StringL)right).get());
        }
        else if (left instanceof IntConst) {
            newExpr = ((IntConst)left).compute(op, right);
        }
        else if (left instanceof DoubleConst) {
            newExpr = ((DoubleConst)left).compute(op, right);
        }
        if (newExpr == null) {
            return false;
        }
        expr.setOperator(43);
        expr.setOprand1(newExpr);
        expr.setOprand2(null);
        newExpr.accept(this);
        return true;
    }
    
    static ASTree stripPlusExpr(final ASTree expr) {
        if (expr instanceof BinExpr) {
            final BinExpr e = (BinExpr)expr;
            if (e.getOperator() == 43 && e.oprand2() == null) {
                return e.getLeft();
            }
        }
        else if (expr instanceof Expr) {
            final Expr e2 = (Expr)expr;
            final int op = e2.getOperator();
            if (op == 35) {
                final ASTree cexpr = getConstantFieldValue((Member)e2.oprand2());
                if (cexpr != null) {
                    return cexpr;
                }
            }
            else if (op == 43 && e2.getRight() == null) {
                return e2.getLeft();
            }
        }
        else if (expr instanceof Member) {
            final ASTree cexpr2 = getConstantFieldValue((Member)expr);
            if (cexpr2 != null) {
                return cexpr2;
            }
        }
        return expr;
    }
    
    private static ASTree getConstantFieldValue(final Member mem) {
        return getConstantFieldValue(mem.getField());
    }
    
    public static ASTree getConstantFieldValue(final CtField f) {
        if (f == null) {
            return null;
        }
        final Object value = f.getConstantValue();
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return new StringL((String)value);
        }
        if (value instanceof Double || value instanceof Float) {
            final int token = (value instanceof Double) ? 405 : 404;
            return new DoubleConst(((Number)value).doubleValue(), token);
        }
        if (value instanceof Number) {
            final int token = (value instanceof Long) ? 403 : 402;
            return new IntConst(((Number)value).longValue(), token);
        }
        if (value instanceof Boolean) {
            return new Keyword(value ? 410 : 411);
        }
        return null;
    }
    
    private static boolean isPlusExpr(final ASTree expr) {
        if (expr instanceof BinExpr) {
            final BinExpr bexpr = (BinExpr)expr;
            final int token = bexpr.getOperator();
            return token == 43;
        }
        return false;
    }
    
    private static Expr makeAppendCall(final ASTree target, final ASTree arg) {
        return CallExpr.makeCall(Expr.make(46, target, new Member("append")), new ASTList(arg));
    }
    
    private void computeBinExprType(final BinExpr expr, final int token, final int type1) throws CompileError {
        final int type2 = this.exprType;
        if (token == 364 || token == 366 || token == 370) {
            this.exprType = type1;
        }
        else {
            this.insertCast(expr, type1, type2);
        }
        if (CodeGen.isP_INT(this.exprType) && this.exprType != 301) {
            this.exprType = 324;
        }
    }
    
    private void booleanExpr(final ASTree expr) throws CompileError {
        final int op = CodeGen.getCompOperator(expr);
        if (op == 358) {
            final BinExpr bexpr = (BinExpr)expr;
            bexpr.oprand1().accept(this);
            final int type1 = this.exprType;
            final int dim1 = this.arrayDim;
            bexpr.oprand2().accept(this);
            if (dim1 == 0 && this.arrayDim == 0) {
                this.insertCast(bexpr, type1, this.exprType);
            }
        }
        else if (op == 33) {
            ((Expr)expr).oprand1().accept(this);
        }
        else if (op == 369 || op == 368) {
            final BinExpr bexpr = (BinExpr)expr;
            bexpr.oprand1().accept(this);
            bexpr.oprand2().accept(this);
        }
        else {
            expr.accept(this);
        }
        this.exprType = 301;
        this.arrayDim = 0;
    }
    
    private void insertCast(final BinExpr expr, final int type1, final int type2) throws CompileError {
        if (CodeGen.rightIsStrong(type1, type2)) {
            expr.setLeft(new CastExpr(type2, 0, expr.oprand1()));
        }
        else {
            this.exprType = type1;
        }
    }
    
    @Override
    public void atCastExpr(final CastExpr expr) throws CompileError {
        final String cname = this.resolveClassName(expr.getClassName());
        expr.getOprand().accept(this);
        this.exprType = expr.getType();
        this.arrayDim = expr.getArrayDim();
        this.className = cname;
    }
    
    @Override
    public void atInstanceOfExpr(final InstanceOfExpr expr) throws CompileError {
        expr.getOprand().accept(this);
        this.exprType = 301;
        this.arrayDim = 0;
    }
    
    @Override
    public void atExpr(final Expr expr) throws CompileError {
        final int token = expr.getOperator();
        final ASTree oprand = expr.oprand1();
        if (token == 46) {
            final String member = ((Symbol)expr.oprand2()).get();
            if (member.equals("length")) {
                try {
                    this.atArrayLength(expr);
                }
                catch (NoFieldException nfe) {
                    this.atFieldRead(expr);
                }
            }
            else if (member.equals("class")) {
                this.atClassObject(expr);
            }
            else {
                this.atFieldRead(expr);
            }
        }
        else if (token == 35) {
            final String member = ((Symbol)expr.oprand2()).get();
            if (member.equals("class")) {
                this.atClassObject(expr);
            }
            else {
                this.atFieldRead(expr);
            }
        }
        else if (token == 65) {
            this.atArrayRead(oprand, expr.oprand2());
        }
        else if (token == 362 || token == 363) {
            this.atPlusPlus(token, oprand, expr);
        }
        else if (token == 33) {
            this.booleanExpr(expr);
        }
        else if (token == 67) {
            fatal();
        }
        else {
            oprand.accept(this);
            if (!this.isConstant(expr, token, oprand) && (token == 45 || token == 126) && CodeGen.isP_INT(this.exprType)) {
                this.exprType = 324;
            }
        }
    }
    
    private boolean isConstant(final Expr expr, final int op, ASTree oprand) {
        oprand = stripPlusExpr(oprand);
        if (oprand instanceof IntConst) {
            final IntConst c = (IntConst)oprand;
            long v = c.get();
            if (op == 45) {
                v = -v;
            }
            else {
                if (op != 126) {
                    return false;
                }
                v ^= -1L;
            }
            c.set(v);
        }
        else {
            if (!(oprand instanceof DoubleConst)) {
                return false;
            }
            final DoubleConst c2 = (DoubleConst)oprand;
            if (op != 45) {
                return false;
            }
            c2.set(-c2.get());
        }
        expr.setOperator(43);
        return true;
    }
    
    @Override
    public void atCallExpr(final CallExpr expr) throws CompileError {
        String mname = null;
        CtClass targetClass = null;
        final ASTree method = expr.oprand1();
        final ASTList args = (ASTList)expr.oprand2();
        if (method instanceof Member) {
            mname = ((Member)method).get();
            targetClass = this.thisClass;
        }
        else if (method instanceof Keyword) {
            mname = "<init>";
            if (((Keyword)method).get() == 336) {
                targetClass = MemberResolver.getSuperclass(this.thisClass);
            }
            else {
                targetClass = this.thisClass;
            }
        }
        else if (method instanceof Expr) {
            final Expr e = (Expr)method;
            mname = ((Symbol)e.oprand2()).get();
            final int op = e.getOperator();
            if (op == 35) {
                targetClass = this.resolver.lookupClass(((Symbol)e.oprand1()).get(), false);
            }
            else if (op == 46) {
                final ASTree target = e.oprand1();
                final String classFollowedByDotSuper = isDotSuper(target);
                if (classFollowedByDotSuper != null) {
                    targetClass = MemberResolver.getSuperInterface(this.thisClass, classFollowedByDotSuper);
                }
                else {
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
                        e.setOperator(35);
                        e.setOprand1(new Symbol(MemberResolver.jvmToJavaName(this.className)));
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
        final MemberResolver.Method minfo = this.atMethodCallCore(targetClass, mname, args);
        expr.setMethod(minfo);
    }
    
    private static void badMethod() throws CompileError {
        throw new CompileError("bad method");
    }
    
    static String isDotSuper(final ASTree target) {
        if (target instanceof Expr) {
            final Expr e = (Expr)target;
            if (e.getOperator() == 46) {
                final ASTree right = e.oprand2();
                if (right instanceof Keyword && ((Keyword)right).get() == 336) {
                    return ((Symbol)e.oprand1()).get();
                }
            }
        }
        return null;
    }
    
    public MemberResolver.Method atMethodCallCore(final CtClass targetClass, final String mname, final ASTList args) throws CompileError {
        final int nargs = this.getMethodArgsLength(args);
        final int[] types = new int[nargs];
        final int[] dims = new int[nargs];
        final String[] cnames = new String[nargs];
        this.atMethodArgs(args, types, dims, cnames);
        final MemberResolver.Method found = this.resolver.lookupMethod(targetClass, this.thisClass, this.thisMethod, mname, types, dims, cnames);
        if (found == null) {
            final String clazz = targetClass.getName();
            final String signature = argTypesToString(types, dims, cnames);
            String msg;
            if (mname.equals("<init>")) {
                msg = "cannot find constructor " + clazz + signature;
            }
            else {
                msg = mname + signature + " not found in " + clazz;
            }
            throw new CompileError(msg);
        }
        final String desc = found.info.getDescriptor();
        this.setReturnType(desc);
        return found;
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
    
    void setReturnType(final String desc) throws CompileError {
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
    }
    
    private void atFieldRead(final ASTree expr) throws CompileError {
        this.atFieldRead(this.fieldAccess(expr));
    }
    
    private void atFieldRead(final CtField f) throws CompileError {
        final FieldInfo finfo = f.getFieldInfo2();
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
    }
    
    protected CtField fieldAccess(final ASTree expr) throws CompileError {
        if (expr instanceof Member) {
            final Member mem = (Member)expr;
            final String name = mem.get();
            try {
                final CtField f = this.thisClass.getField(name);
                if (Modifier.isStatic(f.getModifiers())) {
                    mem.setField(f);
                }
                return f;
            }
            catch (NotFoundException e2) {
                throw new NoFieldException(name, expr);
            }
        }
        if (expr instanceof Expr) {
            final Expr e = (Expr)expr;
            final int op = e.getOperator();
            if (op == 35) {
                final Member mem2 = (Member)e.oprand2();
                final CtField f2 = this.resolver.lookupField(((Symbol)e.oprand1()).get(), mem2);
                mem2.setField(f2);
                return f2;
            }
            if (op == 46) {
                try {
                    e.oprand1().accept(this);
                }
                catch (NoFieldException nfe) {
                    if (nfe.getExpr() != e.oprand1()) {
                        throw nfe;
                    }
                    return this.fieldAccess2(e, nfe.getField());
                }
                CompileError err = null;
                try {
                    if (this.exprType == 307 && this.arrayDim == 0) {
                        return this.resolver.lookupFieldByJvmName(this.className, (Symbol)e.oprand2());
                    }
                }
                catch (CompileError ce) {
                    err = ce;
                }
                final ASTree oprnd1 = e.oprand1();
                if (oprnd1 instanceof Symbol) {
                    return this.fieldAccess2(e, ((Symbol)oprnd1).get());
                }
                if (err != null) {
                    throw err;
                }
            }
        }
        throw new CompileError("bad filed access");
    }
    
    private CtField fieldAccess2(final Expr e, final String jvmClassName) throws CompileError {
        final Member fname = (Member)e.oprand2();
        final CtField f = this.resolver.lookupFieldByJvmName2(jvmClassName, fname, e);
        e.setOperator(35);
        e.setOprand1(new Symbol(MemberResolver.jvmToJavaName(jvmClassName)));
        fname.setField(f);
        return f;
    }
    
    public void atClassObject(final Expr expr) throws CompileError {
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = "java/lang/Class";
    }
    
    public void atArrayLength(final Expr expr) throws CompileError {
        expr.oprand1().accept(this);
        if (this.arrayDim == 0) {
            throw new NoFieldException("length", expr);
        }
        this.exprType = 324;
        this.arrayDim = 0;
    }
    
    public void atArrayRead(final ASTree array, final ASTree index) throws CompileError {
        array.accept(this);
        final int type = this.exprType;
        final int dim = this.arrayDim;
        final String cname = this.className;
        index.accept(this);
        this.exprType = type;
        this.arrayDim = dim - 1;
        this.className = cname;
    }
    
    private void atPlusPlus(final int token, ASTree oprand, final Expr expr) throws CompileError {
        final boolean isPost = oprand == null;
        if (isPost) {
            oprand = expr.oprand2();
        }
        if (oprand instanceof Variable) {
            final Declarator d = ((Variable)oprand).getDeclarator();
            this.exprType = d.getType();
            this.arrayDim = d.getArrayDim();
        }
        else {
            if (oprand instanceof Expr) {
                final Expr e = (Expr)oprand;
                if (e.getOperator() == 65) {
                    this.atArrayRead(e.oprand1(), e.oprand2());
                    final int t = this.exprType;
                    if (t == 324 || t == 303 || t == 306 || t == 334) {
                        this.exprType = 324;
                    }
                    return;
                }
            }
            this.atFieldPlusPlus(oprand);
        }
    }
    
    protected void atFieldPlusPlus(final ASTree oprand) throws CompileError {
        final CtField f = this.fieldAccess(oprand);
        this.atFieldRead(f);
        final int t = this.exprType;
        if (t == 324 || t == 303 || t == 306 || t == 334) {
            this.exprType = 324;
        }
    }
    
    @Override
    public void atMember(final Member mem) throws CompileError {
        this.atFieldRead(mem);
    }
    
    @Override
    public void atVariable(final Variable v) throws CompileError {
        final Declarator d = v.getDeclarator();
        this.exprType = d.getType();
        this.arrayDim = d.getArrayDim();
        this.className = d.getClassName();
    }
    
    @Override
    public void atKeyword(final Keyword k) throws CompileError {
        this.arrayDim = 0;
        final int token = k.get();
        switch (token) {
            case 410:
            case 411: {
                this.exprType = 301;
                break;
            }
            case 412: {
                this.exprType = 412;
                break;
            }
            case 336:
            case 339: {
                this.exprType = 307;
                if (token == 339) {
                    this.className = this.getThisName();
                    break;
                }
                this.className = this.getSuperName();
                break;
            }
            default: {
                fatal();
                break;
            }
        }
    }
    
    @Override
    public void atStringL(final StringL s) throws CompileError {
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = "java/lang/String";
    }
    
    @Override
    public void atIntConst(final IntConst i) throws CompileError {
        this.arrayDim = 0;
        final int type = i.getType();
        if (type == 402 || type == 401) {
            this.exprType = ((type == 402) ? 324 : 306);
        }
        else {
            this.exprType = 326;
        }
    }
    
    @Override
    public void atDoubleConst(final DoubleConst d) throws CompileError {
        this.arrayDim = 0;
        if (d.getType() == 405) {
            this.exprType = 312;
        }
        else {
            this.exprType = 317;
        }
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler;

import javassist.compiler.ast.DoubleConst;
import javassist.compiler.ast.StringL;
import javassist.compiler.ast.Member;
import javassist.compiler.ast.CallExpr;
import javassist.compiler.ast.InstanceOfExpr;
import javassist.compiler.ast.CastExpr;
import javassist.compiler.ast.BinExpr;
import javassist.compiler.ast.CondExpr;
import javassist.compiler.ast.ArrayInit;
import javassist.compiler.ast.NewExpr;
import javassist.compiler.ast.Variable;
import javassist.compiler.ast.IntConst;
import java.util.Arrays;
import javassist.compiler.ast.AssignExpr;
import javassist.compiler.ast.Expr;
import javassist.compiler.ast.Stmnt;
import javassist.compiler.ast.Keyword;
import javassist.compiler.ast.MethodDecl;
import javassist.compiler.ast.FieldDecl;
import javassist.compiler.ast.Symbol;
import javassist.compiler.ast.Pair;
import javassist.compiler.ast.ASTree;
import javassist.compiler.ast.ASTList;
import javassist.compiler.ast.Declarator;
import java.util.ArrayList;
import javassist.bytecode.Bytecode;
import javassist.bytecode.Opcode;
import javassist.compiler.ast.Visitor;

public abstract class CodeGen extends Visitor implements Opcode, TokenId
{
    static final String javaLangObject = "java.lang.Object";
    static final String jvmJavaLangObject = "java/lang/Object";
    static final String javaLangString = "java.lang.String";
    static final String jvmJavaLangString = "java/lang/String";
    protected Bytecode bytecode;
    private int tempVar;
    TypeChecker typeChecker;
    protected boolean hasReturned;
    public boolean inStaticMethod;
    protected ArrayList breakList;
    protected ArrayList continueList;
    protected ReturnHook returnHooks;
    protected int exprType;
    protected int arrayDim;
    protected String className;
    static final int[] binOp;
    private static final int[] ifOp;
    private static final int[] ifOp2;
    private static final int P_DOUBLE = 0;
    private static final int P_FLOAT = 1;
    private static final int P_LONG = 2;
    private static final int P_INT = 3;
    private static final int P_OTHER = -1;
    private static final int[] castOp;
    
    public CodeGen(final Bytecode b) {
        this.bytecode = b;
        this.tempVar = -1;
        this.typeChecker = null;
        this.hasReturned = false;
        this.inStaticMethod = false;
        this.breakList = null;
        this.continueList = null;
        this.returnHooks = null;
    }
    
    public void setTypeChecker(final TypeChecker checker) {
        this.typeChecker = checker;
    }
    
    protected static void fatal() throws CompileError {
        throw new CompileError("fatal");
    }
    
    public static boolean is2word(final int type, final int dim) {
        return dim == 0 && (type == 312 || type == 326);
    }
    
    public int getMaxLocals() {
        return this.bytecode.getMaxLocals();
    }
    
    public void setMaxLocals(final int n) {
        this.bytecode.setMaxLocals(n);
    }
    
    protected void incMaxLocals(final int size) {
        this.bytecode.incMaxLocals(size);
    }
    
    protected int getTempVar() {
        if (this.tempVar < 0) {
            this.tempVar = this.getMaxLocals();
            this.incMaxLocals(2);
        }
        return this.tempVar;
    }
    
    protected int getLocalVar(final Declarator d) {
        int v = d.getLocalVar();
        if (v < 0) {
            v = this.getMaxLocals();
            d.setLocalVar(v);
            this.incMaxLocals(1);
        }
        return v;
    }
    
    protected abstract String getThisName();
    
    protected abstract String getSuperName() throws CompileError;
    
    protected abstract String resolveClassName(final ASTList p0) throws CompileError;
    
    protected abstract String resolveClassName(final String p0) throws CompileError;
    
    protected static String toJvmArrayName(final String name, final int dim) {
        if (name == null) {
            return null;
        }
        if (dim == 0) {
            return name;
        }
        final StringBuffer sbuf = new StringBuffer();
        int d = dim;
        while (d-- > 0) {
            sbuf.append('[');
        }
        sbuf.append('L');
        sbuf.append(name);
        sbuf.append(';');
        return sbuf.toString();
    }
    
    protected static String toJvmTypeName(final int type, int dim) {
        char c = 'I';
        switch (type) {
            case 301: {
                c = 'Z';
                break;
            }
            case 303: {
                c = 'B';
                break;
            }
            case 306: {
                c = 'C';
                break;
            }
            case 334: {
                c = 'S';
                break;
            }
            case 324: {
                c = 'I';
                break;
            }
            case 326: {
                c = 'J';
                break;
            }
            case 317: {
                c = 'F';
                break;
            }
            case 312: {
                c = 'D';
                break;
            }
            case 344: {
                c = 'V';
                break;
            }
        }
        final StringBuffer sbuf = new StringBuffer();
        while (dim-- > 0) {
            sbuf.append('[');
        }
        sbuf.append(c);
        return sbuf.toString();
    }
    
    public void compileExpr(final ASTree expr) throws CompileError {
        this.doTypeCheck(expr);
        expr.accept(this);
    }
    
    public boolean compileBooleanExpr(final boolean branchIf, final ASTree expr) throws CompileError {
        this.doTypeCheck(expr);
        return this.booleanExpr(branchIf, expr);
    }
    
    public void doTypeCheck(final ASTree expr) throws CompileError {
        if (this.typeChecker != null) {
            expr.accept(this.typeChecker);
        }
    }
    
    @Override
    public void atASTList(final ASTList n) throws CompileError {
        fatal();
    }
    
    @Override
    public void atPair(final Pair n) throws CompileError {
        fatal();
    }
    
    @Override
    public void atSymbol(final Symbol n) throws CompileError {
        fatal();
    }
    
    @Override
    public void atFieldDecl(final FieldDecl field) throws CompileError {
        field.getInit().accept(this);
    }
    
    @Override
    public void atMethodDecl(final MethodDecl method) throws CompileError {
        ASTList mods = method.getModifiers();
        this.setMaxLocals(1);
        while (mods != null) {
            final Keyword k = (Keyword)mods.head();
            mods = mods.tail();
            if (k.get() == 335) {
                this.setMaxLocals(0);
                this.inStaticMethod = true;
            }
        }
        for (ASTList params = method.getParams(); params != null; params = params.tail()) {
            this.atDeclarator((Declarator)params.head());
        }
        final Stmnt s = method.getBody();
        this.atMethodBody(s, method.isConstructor(), method.getReturn().getType() == 344);
    }
    
    public void atMethodBody(final Stmnt s, final boolean isCons, final boolean isVoid) throws CompileError {
        if (s == null) {
            return;
        }
        if (isCons && this.needsSuperCall(s)) {
            this.insertDefaultSuperCall();
        }
        this.hasReturned = false;
        s.accept(this);
        if (!this.hasReturned) {
            if (!isVoid) {
                throw new CompileError("no return statement");
            }
            this.bytecode.addOpcode(177);
            this.hasReturned = true;
        }
    }
    
    private boolean needsSuperCall(Stmnt body) throws CompileError {
        if (body.getOperator() == 66) {
            body = (Stmnt)body.head();
        }
        if (body != null && body.getOperator() == 69) {
            final ASTree expr = body.head();
            if (expr != null && expr instanceof Expr && ((Expr)expr).getOperator() == 67) {
                final ASTree target = ((Expr)expr).head();
                if (target instanceof Keyword) {
                    final int token = ((Keyword)target).get();
                    return token != 339 && token != 336;
                }
            }
        }
        return true;
    }
    
    protected abstract void insertDefaultSuperCall() throws CompileError;
    
    @Override
    public void atStmnt(final Stmnt st) throws CompileError {
        if (st == null) {
            return;
        }
        final int op = st.getOperator();
        if (op == 69) {
            final ASTree expr = st.getLeft();
            this.doTypeCheck(expr);
            if (expr instanceof AssignExpr) {
                this.atAssignExpr((AssignExpr)expr, false);
            }
            else if (isPlusPlusExpr(expr)) {
                final Expr e = (Expr)expr;
                this.atPlusPlus(e.getOperator(), e.oprand1(), e, false);
            }
            else {
                expr.accept(this);
                if (is2word(this.exprType, this.arrayDim)) {
                    this.bytecode.addOpcode(88);
                }
                else if (this.exprType != 344) {
                    this.bytecode.addOpcode(87);
                }
            }
        }
        else if (op == 68 || op == 66) {
            ASTList list = st;
            while (list != null) {
                final ASTree h = list.head();
                list = list.tail();
                if (h != null) {
                    h.accept(this);
                }
            }
        }
        else if (op == 320) {
            this.atIfStmnt(st);
        }
        else if (op == 346 || op == 311) {
            this.atWhileStmnt(st, op == 346);
        }
        else if (op == 318) {
            this.atForStmnt(st);
        }
        else if (op == 302 || op == 309) {
            this.atBreakStmnt(st, op == 302);
        }
        else if (op == 333) {
            this.atReturnStmnt(st);
        }
        else if (op == 340) {
            this.atThrowStmnt(st);
        }
        else if (op == 343) {
            this.atTryStmnt(st);
        }
        else if (op == 337) {
            this.atSwitchStmnt(st);
        }
        else {
            if (op != 338) {
                this.hasReturned = false;
                throw new CompileError("sorry, not supported statement: TokenId " + op);
            }
            this.atSyncStmnt(st);
        }
    }
    
    private void atIfStmnt(final Stmnt st) throws CompileError {
        final ASTree expr = st.head();
        final Stmnt thenp = (Stmnt)st.tail().head();
        final Stmnt elsep = (Stmnt)st.tail().tail().head();
        if (this.compileBooleanExpr(false, expr)) {
            this.hasReturned = false;
            if (elsep != null) {
                elsep.accept(this);
            }
            return;
        }
        final int pc = this.bytecode.currentPc();
        int pc2 = 0;
        this.bytecode.addIndex(0);
        this.hasReturned = false;
        if (thenp != null) {
            thenp.accept(this);
        }
        final boolean thenHasReturned = this.hasReturned;
        this.hasReturned = false;
        if (elsep != null && !thenHasReturned) {
            this.bytecode.addOpcode(167);
            pc2 = this.bytecode.currentPc();
            this.bytecode.addIndex(0);
        }
        this.bytecode.write16bit(pc, this.bytecode.currentPc() - pc + 1);
        if (elsep != null) {
            elsep.accept(this);
            if (!thenHasReturned) {
                this.bytecode.write16bit(pc2, this.bytecode.currentPc() - pc2 + 1);
            }
            this.hasReturned = (thenHasReturned && this.hasReturned);
        }
    }
    
    private void atWhileStmnt(final Stmnt st, final boolean notDo) throws CompileError {
        final ArrayList prevBreakList = this.breakList;
        final ArrayList prevContList = this.continueList;
        this.breakList = new ArrayList();
        this.continueList = new ArrayList();
        final ASTree expr = st.head();
        final Stmnt body = (Stmnt)st.tail();
        int pc = 0;
        if (notDo) {
            this.bytecode.addOpcode(167);
            pc = this.bytecode.currentPc();
            this.bytecode.addIndex(0);
        }
        final int pc2 = this.bytecode.currentPc();
        if (body != null) {
            body.accept(this);
        }
        final int pc3 = this.bytecode.currentPc();
        if (notDo) {
            this.bytecode.write16bit(pc, pc3 - pc + 1);
        }
        boolean alwaysBranch = this.compileBooleanExpr(true, expr);
        if (alwaysBranch) {
            this.bytecode.addOpcode(167);
            alwaysBranch = (this.breakList.size() == 0);
        }
        this.bytecode.addIndex(pc2 - this.bytecode.currentPc() + 1);
        this.patchGoto(this.breakList, this.bytecode.currentPc());
        this.patchGoto(this.continueList, pc3);
        this.continueList = prevContList;
        this.breakList = prevBreakList;
        this.hasReturned = alwaysBranch;
    }
    
    protected void patchGoto(final ArrayList list, final int targetPc) {
        for (int n = list.size(), i = 0; i < n; ++i) {
            final int pc = list.get(i);
            this.bytecode.write16bit(pc, targetPc - pc + 1);
        }
    }
    
    private void atForStmnt(final Stmnt st) throws CompileError {
        final ArrayList prevBreakList = this.breakList;
        final ArrayList prevContList = this.continueList;
        this.breakList = new ArrayList();
        this.continueList = new ArrayList();
        final Stmnt init = (Stmnt)st.head();
        ASTList p = st.tail();
        final ASTree expr = p.head();
        p = p.tail();
        final Stmnt update = (Stmnt)p.head();
        final Stmnt body = (Stmnt)p.tail();
        if (init != null) {
            init.accept(this);
        }
        final int pc = this.bytecode.currentPc();
        int pc2 = 0;
        if (expr != null) {
            if (this.compileBooleanExpr(false, expr)) {
                this.continueList = prevContList;
                this.breakList = prevBreakList;
                this.hasReturned = false;
                return;
            }
            pc2 = this.bytecode.currentPc();
            this.bytecode.addIndex(0);
        }
        if (body != null) {
            body.accept(this);
        }
        final int pc3 = this.bytecode.currentPc();
        if (update != null) {
            update.accept(this);
        }
        this.bytecode.addOpcode(167);
        this.bytecode.addIndex(pc - this.bytecode.currentPc() + 1);
        final int pc4 = this.bytecode.currentPc();
        if (expr != null) {
            this.bytecode.write16bit(pc2, pc4 - pc2 + 1);
        }
        this.patchGoto(this.breakList, pc4);
        this.patchGoto(this.continueList, pc3);
        this.continueList = prevContList;
        this.breakList = prevBreakList;
        this.hasReturned = false;
    }
    
    private void atSwitchStmnt(final Stmnt st) throws CompileError {
        this.compileExpr(st.head());
        final ArrayList prevBreakList = this.breakList;
        this.breakList = new ArrayList();
        final int opcodePc = this.bytecode.currentPc();
        this.bytecode.addOpcode(171);
        int npads = 3 - (opcodePc & 0x3);
        while (npads-- > 0) {
            this.bytecode.add(0);
        }
        final Stmnt body = (Stmnt)st.tail();
        int npairs = 0;
        for (ASTList list = body; list != null; list = list.tail()) {
            if (((Stmnt)list.head()).getOperator() == 304) {
                ++npairs;
            }
        }
        final int opcodePc2 = this.bytecode.currentPc();
        this.bytecode.addGap(4);
        this.bytecode.add32bit(npairs);
        this.bytecode.addGap(npairs * 8);
        final long[] pairs = new long[npairs];
        int ipairs = 0;
        int defaultPc = -1;
        for (ASTList list2 = body; list2 != null; list2 = list2.tail()) {
            final Stmnt label = (Stmnt)list2.head();
            final int op = label.getOperator();
            if (op == 310) {
                defaultPc = this.bytecode.currentPc();
            }
            else if (op != 304) {
                fatal();
            }
            else {
                pairs[ipairs++] = ((long)this.computeLabel(label.head()) << 32) + ((long)(this.bytecode.currentPc() - opcodePc) & -1L);
            }
            this.hasReturned = false;
            ((Stmnt)label.tail()).accept(this);
        }
        Arrays.sort(pairs);
        int pc = opcodePc2 + 8;
        for (int i = 0; i < npairs; ++i) {
            this.bytecode.write32bit(pc, (int)(pairs[i] >>> 32));
            this.bytecode.write32bit(pc + 4, (int)pairs[i]);
            pc += 8;
        }
        if (defaultPc < 0 || this.breakList.size() > 0) {
            this.hasReturned = false;
        }
        final int endPc = this.bytecode.currentPc();
        if (defaultPc < 0) {
            defaultPc = endPc;
        }
        this.bytecode.write32bit(opcodePc2, defaultPc - opcodePc);
        this.patchGoto(this.breakList, endPc);
        this.breakList = prevBreakList;
    }
    
    private int computeLabel(ASTree expr) throws CompileError {
        this.doTypeCheck(expr);
        expr = TypeChecker.stripPlusExpr(expr);
        if (expr instanceof IntConst) {
            return (int)((IntConst)expr).get();
        }
        throw new CompileError("bad case label");
    }
    
    private void atBreakStmnt(final Stmnt st, final boolean notCont) throws CompileError {
        if (st.head() != null) {
            throw new CompileError("sorry, not support labeled break or continue");
        }
        this.bytecode.addOpcode(167);
        final Integer pc = new Integer(this.bytecode.currentPc());
        this.bytecode.addIndex(0);
        if (notCont) {
            this.breakList.add(pc);
        }
        else {
            this.continueList.add(pc);
        }
    }
    
    protected void atReturnStmnt(final Stmnt st) throws CompileError {
        this.atReturnStmnt2(st.getLeft());
    }
    
    protected final void atReturnStmnt2(final ASTree result) throws CompileError {
        int op;
        if (result == null) {
            op = 177;
        }
        else {
            this.compileExpr(result);
            if (this.arrayDim > 0) {
                op = 176;
            }
            else {
                final int type = this.exprType;
                if (type == 312) {
                    op = 175;
                }
                else if (type == 317) {
                    op = 174;
                }
                else if (type == 326) {
                    op = 173;
                }
                else if (isRefType(type)) {
                    op = 176;
                }
                else {
                    op = 172;
                }
            }
        }
        for (ReturnHook har = this.returnHooks; har != null; har = har.next) {
            if (har.doit(this.bytecode, op)) {
                this.hasReturned = true;
                return;
            }
        }
        this.bytecode.addOpcode(op);
        this.hasReturned = true;
    }
    
    private void atThrowStmnt(final Stmnt st) throws CompileError {
        final ASTree e = st.getLeft();
        this.compileExpr(e);
        if (this.exprType != 307 || this.arrayDim > 0) {
            throw new CompileError("bad throw statement");
        }
        this.bytecode.addOpcode(191);
        this.hasReturned = true;
    }
    
    protected void atTryStmnt(final Stmnt st) throws CompileError {
        this.hasReturned = false;
    }
    
    private void atSyncStmnt(final Stmnt st) throws CompileError {
        final int nbreaks = getListSize(this.breakList);
        final int ncontinues = getListSize(this.continueList);
        this.compileExpr(st.head());
        if (this.exprType != 307 && this.arrayDim == 0) {
            throw new CompileError("bad type expr for synchronized block");
        }
        final Bytecode bc = this.bytecode;
        final int var = bc.getMaxLocals();
        bc.incMaxLocals(1);
        bc.addOpcode(89);
        bc.addAstore(var);
        bc.addOpcode(194);
        final ReturnHook rh = new ReturnHook(this) {
            @Override
            protected boolean doit(final Bytecode b, final int opcode) {
                b.addAload(var);
                b.addOpcode(195);
                return false;
            }
        };
        final int pc = bc.currentPc();
        final Stmnt body = (Stmnt)st.tail();
        if (body != null) {
            body.accept(this);
        }
        final int pc2 = bc.currentPc();
        int pc3 = 0;
        if (!this.hasReturned) {
            rh.doit(bc, 0);
            bc.addOpcode(167);
            pc3 = bc.currentPc();
            bc.addIndex(0);
        }
        if (pc < pc2) {
            final int pc4 = bc.currentPc();
            rh.doit(bc, 0);
            bc.addOpcode(191);
            bc.addExceptionHandler(pc, pc2, pc4, 0);
        }
        if (!this.hasReturned) {
            bc.write16bit(pc3, bc.currentPc() - pc3 + 1);
        }
        rh.remove(this);
        if (getListSize(this.breakList) != nbreaks || getListSize(this.continueList) != ncontinues) {
            throw new CompileError("sorry, cannot break/continue in synchronized block");
        }
    }
    
    private static int getListSize(final ArrayList list) {
        return (list == null) ? 0 : list.size();
    }
    
    private static boolean isPlusPlusExpr(final ASTree expr) {
        if (expr instanceof Expr) {
            final int op = ((Expr)expr).getOperator();
            return op == 362 || op == 363;
        }
        return false;
    }
    
    @Override
    public void atDeclarator(final Declarator d) throws CompileError {
        d.setLocalVar(this.getMaxLocals());
        d.setClassName(this.resolveClassName(d.getClassName()));
        int size;
        if (is2word(d.getType(), d.getArrayDim())) {
            size = 2;
        }
        else {
            size = 1;
        }
        this.incMaxLocals(size);
        final ASTree init = d.getInitializer();
        if (init != null) {
            this.doTypeCheck(init);
            this.atVariableAssign(null, 61, null, d, init, false);
        }
    }
    
    @Override
    public abstract void atNewExpr(final NewExpr p0) throws CompileError;
    
    @Override
    public abstract void atArrayInit(final ArrayInit p0) throws CompileError;
    
    @Override
    public void atAssignExpr(final AssignExpr expr) throws CompileError {
        this.atAssignExpr(expr, true);
    }
    
    protected void atAssignExpr(final AssignExpr expr, final boolean doDup) throws CompileError {
        final int op = expr.getOperator();
        final ASTree left = expr.oprand1();
        final ASTree right = expr.oprand2();
        if (left instanceof Variable) {
            this.atVariableAssign(expr, op, (Variable)left, ((Variable)left).getDeclarator(), right, doDup);
        }
        else {
            if (left instanceof Expr) {
                final Expr e = (Expr)left;
                if (e.getOperator() == 65) {
                    this.atArrayAssign(expr, op, (Expr)left, right, doDup);
                    return;
                }
            }
            this.atFieldAssign(expr, op, left, right, doDup);
        }
    }
    
    protected static void badAssign(final Expr expr) throws CompileError {
        String msg;
        if (expr == null) {
            msg = "incompatible type for assignment";
        }
        else {
            msg = "incompatible type for " + expr.getName();
        }
        throw new CompileError(msg);
    }
    
    private void atVariableAssign(final Expr expr, final int op, final Variable var, final Declarator d, final ASTree right, final boolean doDup) throws CompileError {
        final int varType = d.getType();
        final int varArray = d.getArrayDim();
        final String varClass = d.getClassName();
        final int varNo = this.getLocalVar(d);
        if (op != 61) {
            this.atVariable(var);
        }
        if (expr == null && right instanceof ArrayInit) {
            this.atArrayVariableAssign((ArrayInit)right, varType, varArray, varClass);
        }
        else {
            this.atAssignCore(expr, op, right, varType, varArray, varClass);
        }
        if (doDup) {
            if (is2word(varType, varArray)) {
                this.bytecode.addOpcode(92);
            }
            else {
                this.bytecode.addOpcode(89);
            }
        }
        if (varArray > 0) {
            this.bytecode.addAstore(varNo);
        }
        else if (varType == 312) {
            this.bytecode.addDstore(varNo);
        }
        else if (varType == 317) {
            this.bytecode.addFstore(varNo);
        }
        else if (varType == 326) {
            this.bytecode.addLstore(varNo);
        }
        else if (isRefType(varType)) {
            this.bytecode.addAstore(varNo);
        }
        else {
            this.bytecode.addIstore(varNo);
        }
        this.exprType = varType;
        this.arrayDim = varArray;
        this.className = varClass;
    }
    
    protected abstract void atArrayVariableAssign(final ArrayInit p0, final int p1, final int p2, final String p3) throws CompileError;
    
    private void atArrayAssign(final Expr expr, final int op, final Expr array, final ASTree right, final boolean doDup) throws CompileError {
        this.arrayAccess(array.oprand1(), array.oprand2());
        if (op != 61) {
            this.bytecode.addOpcode(92);
            this.bytecode.addOpcode(getArrayReadOp(this.exprType, this.arrayDim));
        }
        final int aType = this.exprType;
        final int aDim = this.arrayDim;
        final String cname = this.className;
        this.atAssignCore(expr, op, right, aType, aDim, cname);
        if (doDup) {
            if (is2word(aType, aDim)) {
                this.bytecode.addOpcode(94);
            }
            else {
                this.bytecode.addOpcode(91);
            }
        }
        this.bytecode.addOpcode(getArrayWriteOp(aType, aDim));
        this.exprType = aType;
        this.arrayDim = aDim;
        this.className = cname;
    }
    
    protected abstract void atFieldAssign(final Expr p0, final int p1, final ASTree p2, final ASTree p3, final boolean p4) throws CompileError;
    
    protected void atAssignCore(final Expr expr, final int op, final ASTree right, final int type, final int dim, final String cname) throws CompileError {
        if (op == 354 && dim == 0 && type == 307) {
            this.atStringPlusEq(expr, type, dim, cname, right);
        }
        else {
            right.accept(this);
            if (this.invalidDim(this.exprType, this.arrayDim, this.className, type, dim, cname, false) || (op != 61 && dim > 0)) {
                badAssign(expr);
            }
            if (op != 61) {
                final int token = CodeGen.assignOps[op - 351];
                final int k = lookupBinOp(token);
                if (k < 0) {
                    fatal();
                }
                this.atArithBinExpr(expr, token, k, type);
            }
        }
        if (op != 61 || (dim == 0 && !isRefType(type))) {
            this.atNumCastExpr(this.exprType, type);
        }
    }
    
    private void atStringPlusEq(final Expr expr, final int type, final int dim, final String cname, final ASTree right) throws CompileError {
        if (!"java/lang/String".equals(cname)) {
            badAssign(expr);
        }
        this.convToString(type, dim);
        right.accept(this);
        this.convToString(this.exprType, this.arrayDim);
        this.bytecode.addInvokevirtual("java.lang.String", "concat", "(Ljava/lang/String;)Ljava/lang/String;");
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = "java/lang/String";
    }
    
    private boolean invalidDim(final int srcType, final int srcDim, final String srcClass, final int destType, final int destDim, final String destClass, final boolean isCast) {
        return srcDim != destDim && srcType != 412 && (destDim != 0 || destType != 307 || !"java/lang/Object".equals(destClass)) && (!isCast || srcDim != 0 || srcType != 307 || !"java/lang/Object".equals(srcClass));
    }
    
    @Override
    public void atCondExpr(final CondExpr expr) throws CompileError {
        if (this.booleanExpr(false, expr.condExpr())) {
            expr.elseExpr().accept(this);
        }
        else {
            final int pc = this.bytecode.currentPc();
            this.bytecode.addIndex(0);
            expr.thenExpr().accept(this);
            final int dim1 = this.arrayDim;
            this.bytecode.addOpcode(167);
            final int pc2 = this.bytecode.currentPc();
            this.bytecode.addIndex(0);
            this.bytecode.write16bit(pc, this.bytecode.currentPc() - pc + 1);
            expr.elseExpr().accept(this);
            if (dim1 != this.arrayDim) {
                throw new CompileError("type mismatch in ?:");
            }
            this.bytecode.write16bit(pc2, this.bytecode.currentPc() - pc2 + 1);
        }
    }
    
    static int lookupBinOp(final int token) {
        final int[] code = CodeGen.binOp;
        for (int s = code.length, k = 0; k < s; k += 5) {
            if (code[k] == token) {
                return k;
            }
        }
        return -1;
    }
    
    @Override
    public void atBinExpr(final BinExpr expr) throws CompileError {
        final int token = expr.getOperator();
        final int k = lookupBinOp(token);
        if (k >= 0) {
            expr.oprand1().accept(this);
            final ASTree right = expr.oprand2();
            if (right == null) {
                return;
            }
            final int type1 = this.exprType;
            final int dim1 = this.arrayDim;
            final String cname1 = this.className;
            right.accept(this);
            if (dim1 != this.arrayDim) {
                throw new CompileError("incompatible array types");
            }
            if (token == 43 && dim1 == 0 && (type1 == 307 || this.exprType == 307)) {
                this.atStringConcatExpr(expr, type1, dim1, cname1);
            }
            else {
                this.atArithBinExpr(expr, token, k, type1);
            }
        }
        else {
            if (!this.booleanExpr(true, expr)) {
                this.bytecode.addIndex(7);
                this.bytecode.addIconst(0);
                this.bytecode.addOpcode(167);
                this.bytecode.addIndex(4);
            }
            this.bytecode.addIconst(1);
        }
    }
    
    private void atArithBinExpr(final Expr expr, final int token, final int index, final int type1) throws CompileError {
        if (this.arrayDim != 0) {
            badTypes(expr);
        }
        final int type2 = this.exprType;
        if (token == 364 || token == 366 || token == 370) {
            if (type2 == 324 || type2 == 334 || type2 == 306 || type2 == 303) {
                this.exprType = type1;
            }
            else {
                badTypes(expr);
            }
        }
        else {
            this.convertOprandTypes(type1, type2, expr);
        }
        final int p = typePrecedence(this.exprType);
        if (p >= 0) {
            final int op = CodeGen.binOp[index + p + 1];
            if (op != 0) {
                if (p == 3 && this.exprType != 301) {
                    this.exprType = 324;
                }
                this.bytecode.addOpcode(op);
                return;
            }
        }
        badTypes(expr);
    }
    
    private void atStringConcatExpr(final Expr expr, final int type1, final int dim1, final String cname1) throws CompileError {
        final int type2 = this.exprType;
        final int dim2 = this.arrayDim;
        final boolean type2Is2 = is2word(type2, dim2);
        final boolean type2IsString = type2 == 307 && "java/lang/String".equals(this.className);
        if (type2Is2) {
            this.convToString(type2, dim2);
        }
        if (is2word(type1, dim1)) {
            this.bytecode.addOpcode(91);
            this.bytecode.addOpcode(87);
        }
        else {
            this.bytecode.addOpcode(95);
        }
        this.convToString(type1, dim1);
        this.bytecode.addOpcode(95);
        if (!type2Is2 && !type2IsString) {
            this.convToString(type2, dim2);
        }
        this.bytecode.addInvokevirtual("java.lang.String", "concat", "(Ljava/lang/String;)Ljava/lang/String;");
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = "java/lang/String";
    }
    
    private void convToString(final int type, final int dim) throws CompileError {
        final String method = "valueOf";
        if (isRefType(type) || dim > 0) {
            this.bytecode.addInvokestatic("java.lang.String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;");
        }
        else if (type == 312) {
            this.bytecode.addInvokestatic("java.lang.String", "valueOf", "(D)Ljava/lang/String;");
        }
        else if (type == 317) {
            this.bytecode.addInvokestatic("java.lang.String", "valueOf", "(F)Ljava/lang/String;");
        }
        else if (type == 326) {
            this.bytecode.addInvokestatic("java.lang.String", "valueOf", "(J)Ljava/lang/String;");
        }
        else if (type == 301) {
            this.bytecode.addInvokestatic("java.lang.String", "valueOf", "(Z)Ljava/lang/String;");
        }
        else if (type == 306) {
            this.bytecode.addInvokestatic("java.lang.String", "valueOf", "(C)Ljava/lang/String;");
        }
        else {
            if (type == 344) {
                throw new CompileError("void type expression");
            }
            this.bytecode.addInvokestatic("java.lang.String", "valueOf", "(I)Ljava/lang/String;");
        }
    }
    
    private boolean booleanExpr(final boolean branchIf, final ASTree expr) throws CompileError {
        final int op = getCompOperator(expr);
        if (op == 358) {
            final BinExpr bexpr = (BinExpr)expr;
            final int type1 = this.compileOprands(bexpr);
            this.compareExpr(branchIf, bexpr.getOperator(), type1, bexpr);
        }
        else {
            if (op == 33) {
                return this.booleanExpr(!branchIf, ((Expr)expr).oprand1());
            }
            final boolean isAndAnd;
            if ((isAndAnd = (op == 369)) || op == 368) {
                final BinExpr bexpr = (BinExpr)expr;
                if (this.booleanExpr(!isAndAnd, bexpr.oprand1())) {
                    this.exprType = 301;
                    this.arrayDim = 0;
                    return true;
                }
                final int pc = this.bytecode.currentPc();
                this.bytecode.addIndex(0);
                if (this.booleanExpr(isAndAnd, bexpr.oprand2())) {
                    this.bytecode.addOpcode(167);
                }
                this.bytecode.write16bit(pc, this.bytecode.currentPc() - pc + 3);
                if (branchIf != isAndAnd) {
                    this.bytecode.addIndex(6);
                    this.bytecode.addOpcode(167);
                }
            }
            else {
                if (isAlwaysBranch(expr, branchIf)) {
                    this.exprType = 301;
                    this.arrayDim = 0;
                    return true;
                }
                expr.accept(this);
                if (this.exprType != 301 || this.arrayDim != 0) {
                    throw new CompileError("boolean expr is required");
                }
                this.bytecode.addOpcode(branchIf ? 154 : 153);
            }
        }
        this.exprType = 301;
        this.arrayDim = 0;
        return false;
    }
    
    private static boolean isAlwaysBranch(final ASTree expr, final boolean branchIf) {
        if (expr instanceof Keyword) {
            final int t = ((Keyword)expr).get();
            return branchIf ? (t == 410) : (t == 411);
        }
        return false;
    }
    
    static int getCompOperator(final ASTree expr) throws CompileError {
        if (!(expr instanceof Expr)) {
            return 32;
        }
        final Expr bexpr = (Expr)expr;
        final int token = bexpr.getOperator();
        if (token == 33) {
            return 33;
        }
        if (bexpr instanceof BinExpr && token != 368 && token != 369 && token != 38 && token != 124) {
            return 358;
        }
        return token;
    }
    
    private int compileOprands(final BinExpr expr) throws CompileError {
        expr.oprand1().accept(this);
        final int type1 = this.exprType;
        final int dim1 = this.arrayDim;
        expr.oprand2().accept(this);
        if (dim1 != this.arrayDim) {
            if (type1 != 412 && this.exprType != 412) {
                throw new CompileError("incompatible array types");
            }
            if (this.exprType == 412) {
                this.arrayDim = dim1;
            }
        }
        if (type1 == 412) {
            return this.exprType;
        }
        return type1;
    }
    
    private void compareExpr(final boolean branchIf, final int token, final int type1, final BinExpr expr) throws CompileError {
        if (this.arrayDim == 0) {
            this.convertOprandTypes(type1, this.exprType, expr);
        }
        final int p = typePrecedence(this.exprType);
        if (p == -1 || this.arrayDim > 0) {
            if (token == 358) {
                this.bytecode.addOpcode(branchIf ? 165 : 166);
            }
            else if (token == 350) {
                this.bytecode.addOpcode(branchIf ? 166 : 165);
            }
            else {
                badTypes(expr);
            }
        }
        else if (p == 3) {
            final int[] op = CodeGen.ifOp;
            for (int i = 0; i < op.length; i += 3) {
                if (op[i] == token) {
                    this.bytecode.addOpcode(op[i + (branchIf ? 1 : 2)]);
                    return;
                }
            }
            badTypes(expr);
        }
        else {
            if (p == 0) {
                if (token == 60 || token == 357) {
                    this.bytecode.addOpcode(152);
                }
                else {
                    this.bytecode.addOpcode(151);
                }
            }
            else if (p == 1) {
                if (token == 60 || token == 357) {
                    this.bytecode.addOpcode(150);
                }
                else {
                    this.bytecode.addOpcode(149);
                }
            }
            else if (p == 2) {
                this.bytecode.addOpcode(148);
            }
            else {
                fatal();
            }
            final int[] op = CodeGen.ifOp2;
            for (int i = 0; i < op.length; i += 3) {
                if (op[i] == token) {
                    this.bytecode.addOpcode(op[i + (branchIf ? 1 : 2)]);
                    return;
                }
            }
            badTypes(expr);
        }
    }
    
    protected static void badTypes(final Expr expr) throws CompileError {
        throw new CompileError("invalid types for " + expr.getName());
    }
    
    protected static boolean isRefType(final int type) {
        return type == 307 || type == 412;
    }
    
    private static int typePrecedence(final int type) {
        if (type == 312) {
            return 0;
        }
        if (type == 317) {
            return 1;
        }
        if (type == 326) {
            return 2;
        }
        if (isRefType(type)) {
            return -1;
        }
        if (type == 344) {
            return -1;
        }
        return 3;
    }
    
    static boolean isP_INT(final int type) {
        return typePrecedence(type) == 3;
    }
    
    static boolean rightIsStrong(final int type1, final int type2) {
        final int type1_p = typePrecedence(type1);
        final int type2_p = typePrecedence(type2);
        return type1_p >= 0 && type2_p >= 0 && type1_p > type2_p;
    }
    
    private void convertOprandTypes(final int type1, final int type2, final Expr expr) throws CompileError {
        final int type1_p = typePrecedence(type1);
        final int type2_p = typePrecedence(type2);
        if (type2_p < 0 && type1_p < 0) {
            return;
        }
        if (type2_p < 0 || type1_p < 0) {
            badTypes(expr);
        }
        boolean rightStrong;
        int op;
        int result_type;
        if (type1_p <= type2_p) {
            rightStrong = false;
            this.exprType = type1;
            op = CodeGen.castOp[type2_p * 4 + type1_p];
            result_type = type1_p;
        }
        else {
            rightStrong = true;
            op = CodeGen.castOp[type1_p * 4 + type2_p];
            result_type = type2_p;
        }
        if (rightStrong) {
            if (result_type == 0 || result_type == 2) {
                if (type1_p == 0 || type1_p == 2) {
                    this.bytecode.addOpcode(94);
                }
                else {
                    this.bytecode.addOpcode(93);
                }
                this.bytecode.addOpcode(88);
                this.bytecode.addOpcode(op);
                this.bytecode.addOpcode(94);
                this.bytecode.addOpcode(88);
            }
            else if (result_type == 1) {
                if (type1_p == 2) {
                    this.bytecode.addOpcode(91);
                    this.bytecode.addOpcode(87);
                }
                else {
                    this.bytecode.addOpcode(95);
                }
                this.bytecode.addOpcode(op);
                this.bytecode.addOpcode(95);
            }
            else {
                fatal();
            }
        }
        else if (op != 0) {
            this.bytecode.addOpcode(op);
        }
    }
    
    @Override
    public void atCastExpr(final CastExpr expr) throws CompileError {
        final String cname = this.resolveClassName(expr.getClassName());
        final String toClass = this.checkCastExpr(expr, cname);
        final int srcType = this.exprType;
        this.exprType = expr.getType();
        this.arrayDim = expr.getArrayDim();
        this.className = cname;
        if (toClass == null) {
            this.atNumCastExpr(srcType, this.exprType);
        }
        else {
            this.bytecode.addCheckcast(toClass);
        }
    }
    
    @Override
    public void atInstanceOfExpr(final InstanceOfExpr expr) throws CompileError {
        final String cname = this.resolveClassName(expr.getClassName());
        final String toClass = this.checkCastExpr(expr, cname);
        this.bytecode.addInstanceof(toClass);
        this.exprType = 301;
        this.arrayDim = 0;
    }
    
    private String checkCastExpr(final CastExpr expr, final String name) throws CompileError {
        final String msg = "invalid cast";
        final ASTree oprand = expr.getOprand();
        final int dim = expr.getArrayDim();
        final int type = expr.getType();
        oprand.accept(this);
        final int srcType = this.exprType;
        final int srcDim = this.arrayDim;
        if (this.invalidDim(srcType, this.arrayDim, this.className, type, dim, name, true) || srcType == 344 || type == 344) {
            throw new CompileError("invalid cast");
        }
        if (type == 307) {
            if (!isRefType(srcType) && srcDim == 0) {
                throw new CompileError("invalid cast");
            }
            return toJvmArrayName(name, dim);
        }
        else {
            if (dim > 0) {
                return toJvmTypeName(type, dim);
            }
            return null;
        }
    }
    
    void atNumCastExpr(final int srcType, final int destType) throws CompileError {
        if (srcType == destType) {
            return;
        }
        final int stype = typePrecedence(srcType);
        final int dtype = typePrecedence(destType);
        int op;
        if (0 <= stype && stype < 3) {
            op = CodeGen.castOp[stype * 4 + dtype];
        }
        else {
            op = 0;
        }
        int op2;
        if (destType == 312) {
            op2 = 135;
        }
        else if (destType == 317) {
            op2 = 134;
        }
        else if (destType == 326) {
            op2 = 133;
        }
        else if (destType == 334) {
            op2 = 147;
        }
        else if (destType == 306) {
            op2 = 146;
        }
        else if (destType == 303) {
            op2 = 145;
        }
        else {
            op2 = 0;
        }
        if (op != 0) {
            this.bytecode.addOpcode(op);
        }
        if ((op == 0 || op == 136 || op == 139 || op == 142) && op2 != 0) {
            this.bytecode.addOpcode(op2);
        }
    }
    
    @Override
    public void atExpr(final Expr expr) throws CompileError {
        final int token = expr.getOperator();
        final ASTree oprand = expr.oprand1();
        if (token == 46) {
            final String member = ((Symbol)expr.oprand2()).get();
            if (member.equals("class")) {
                this.atClassObject(expr);
            }
            else {
                this.atFieldRead(expr);
            }
        }
        else if (token == 35) {
            this.atFieldRead(expr);
        }
        else if (token == 65) {
            this.atArrayRead(oprand, expr.oprand2());
        }
        else if (token == 362 || token == 363) {
            this.atPlusPlus(token, oprand, expr, true);
        }
        else if (token == 33) {
            if (!this.booleanExpr(false, expr)) {
                this.bytecode.addIndex(7);
                this.bytecode.addIconst(1);
                this.bytecode.addOpcode(167);
                this.bytecode.addIndex(4);
            }
            this.bytecode.addIconst(0);
        }
        else if (token == 67) {
            fatal();
        }
        else {
            expr.oprand1().accept(this);
            final int type = typePrecedence(this.exprType);
            if (this.arrayDim > 0) {
                badType(expr);
            }
            if (token == 45) {
                if (type == 0) {
                    this.bytecode.addOpcode(119);
                }
                else if (type == 1) {
                    this.bytecode.addOpcode(118);
                }
                else if (type == 2) {
                    this.bytecode.addOpcode(117);
                }
                else if (type == 3) {
                    this.bytecode.addOpcode(116);
                    this.exprType = 324;
                }
                else {
                    badType(expr);
                }
            }
            else if (token == 126) {
                if (type == 3) {
                    this.bytecode.addIconst(-1);
                    this.bytecode.addOpcode(130);
                    this.exprType = 324;
                }
                else if (type == 2) {
                    this.bytecode.addLconst(-1L);
                    this.bytecode.addOpcode(131);
                }
                else {
                    badType(expr);
                }
            }
            else if (token == 43) {
                if (type == -1) {
                    badType(expr);
                }
            }
            else {
                fatal();
            }
        }
    }
    
    protected static void badType(final Expr expr) throws CompileError {
        throw new CompileError("invalid type for " + expr.getName());
    }
    
    @Override
    public abstract void atCallExpr(final CallExpr p0) throws CompileError;
    
    protected abstract void atFieldRead(final ASTree p0) throws CompileError;
    
    public void atClassObject(final Expr expr) throws CompileError {
        final ASTree op1 = expr.oprand1();
        if (!(op1 instanceof Symbol)) {
            throw new CompileError("fatal error: badly parsed .class expr");
        }
        String cname = ((Symbol)op1).get();
        if (cname.startsWith("[")) {
            int i = cname.indexOf("[L");
            if (i >= 0) {
                final String name = cname.substring(i + 2, cname.length() - 1);
                String name2 = this.resolveClassName(name);
                if (!name.equals(name2)) {
                    name2 = MemberResolver.jvmToJavaName(name2);
                    final StringBuffer sbuf = new StringBuffer();
                    while (i-- >= 0) {
                        sbuf.append('[');
                    }
                    sbuf.append('L').append(name2).append(';');
                    cname = sbuf.toString();
                }
            }
        }
        else {
            cname = this.resolveClassName(MemberResolver.javaToJvmName(cname));
            cname = MemberResolver.jvmToJavaName(cname);
        }
        this.atClassObject2(cname);
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = "java/lang/Class";
    }
    
    protected void atClassObject2(final String cname) throws CompileError {
        final int start = this.bytecode.currentPc();
        this.bytecode.addLdc(cname);
        this.bytecode.addInvokestatic("java.lang.Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
        final int end = this.bytecode.currentPc();
        this.bytecode.addOpcode(167);
        final int pc = this.bytecode.currentPc();
        this.bytecode.addIndex(0);
        this.bytecode.addExceptionHandler(start, end, this.bytecode.currentPc(), "java.lang.ClassNotFoundException");
        this.bytecode.growStack(1);
        this.bytecode.addInvokestatic("javassist.runtime.DotClass", "fail", "(Ljava/lang/ClassNotFoundException;)Ljava/lang/NoClassDefFoundError;");
        this.bytecode.addOpcode(191);
        this.bytecode.write16bit(pc, this.bytecode.currentPc() - pc + 1);
    }
    
    public void atArrayRead(final ASTree array, final ASTree index) throws CompileError {
        this.arrayAccess(array, index);
        this.bytecode.addOpcode(getArrayReadOp(this.exprType, this.arrayDim));
    }
    
    protected void arrayAccess(final ASTree array, final ASTree index) throws CompileError {
        array.accept(this);
        final int type = this.exprType;
        final int dim = this.arrayDim;
        if (dim == 0) {
            throw new CompileError("bad array access");
        }
        final String cname = this.className;
        index.accept(this);
        if (typePrecedence(this.exprType) != 3 || this.arrayDim > 0) {
            throw new CompileError("bad array index");
        }
        this.exprType = type;
        this.arrayDim = dim - 1;
        this.className = cname;
    }
    
    protected static int getArrayReadOp(final int type, final int dim) {
        if (dim > 0) {
            return 50;
        }
        switch (type) {
            case 312: {
                return 49;
            }
            case 317: {
                return 48;
            }
            case 326: {
                return 47;
            }
            case 324: {
                return 46;
            }
            case 334: {
                return 53;
            }
            case 306: {
                return 52;
            }
            case 301:
            case 303: {
                return 51;
            }
            default: {
                return 50;
            }
        }
    }
    
    protected static int getArrayWriteOp(final int type, final int dim) {
        if (dim > 0) {
            return 83;
        }
        switch (type) {
            case 312: {
                return 82;
            }
            case 317: {
                return 81;
            }
            case 326: {
                return 80;
            }
            case 324: {
                return 79;
            }
            case 334: {
                return 86;
            }
            case 306: {
                return 85;
            }
            case 301:
            case 303: {
                return 84;
            }
            default: {
                return 83;
            }
        }
    }
    
    private void atPlusPlus(final int token, ASTree oprand, final Expr expr, final boolean doDup) throws CompileError {
        final boolean isPost = oprand == null;
        if (isPost) {
            oprand = expr.oprand2();
        }
        if (oprand instanceof Variable) {
            final Declarator d = ((Variable)oprand).getDeclarator();
            final int type = d.getType();
            this.exprType = type;
            final int t = type;
            this.arrayDim = d.getArrayDim();
            final int var = this.getLocalVar(d);
            if (this.arrayDim > 0) {
                badType(expr);
            }
            if (t == 312) {
                this.bytecode.addDload(var);
                if (doDup && isPost) {
                    this.bytecode.addOpcode(92);
                }
                this.bytecode.addDconst(1.0);
                this.bytecode.addOpcode((token == 362) ? 99 : 103);
                if (doDup && !isPost) {
                    this.bytecode.addOpcode(92);
                }
                this.bytecode.addDstore(var);
            }
            else if (t == 326) {
                this.bytecode.addLload(var);
                if (doDup && isPost) {
                    this.bytecode.addOpcode(92);
                }
                this.bytecode.addLconst(1L);
                this.bytecode.addOpcode((token == 362) ? 97 : 101);
                if (doDup && !isPost) {
                    this.bytecode.addOpcode(92);
                }
                this.bytecode.addLstore(var);
            }
            else if (t == 317) {
                this.bytecode.addFload(var);
                if (doDup && isPost) {
                    this.bytecode.addOpcode(89);
                }
                this.bytecode.addFconst(1.0f);
                this.bytecode.addOpcode((token == 362) ? 98 : 102);
                if (doDup && !isPost) {
                    this.bytecode.addOpcode(89);
                }
                this.bytecode.addFstore(var);
            }
            else if (t == 303 || t == 306 || t == 334 || t == 324) {
                if (doDup && isPost) {
                    this.bytecode.addIload(var);
                }
                final int delta = (token == 362) ? 1 : -1;
                if (var > 255) {
                    this.bytecode.addOpcode(196);
                    this.bytecode.addOpcode(132);
                    this.bytecode.addIndex(var);
                    this.bytecode.addIndex(delta);
                }
                else {
                    this.bytecode.addOpcode(132);
                    this.bytecode.add(var);
                    this.bytecode.add(delta);
                }
                if (doDup && !isPost) {
                    this.bytecode.addIload(var);
                }
            }
            else {
                badType(expr);
            }
        }
        else {
            if (oprand instanceof Expr) {
                final Expr e = (Expr)oprand;
                if (e.getOperator() == 65) {
                    this.atArrayPlusPlus(token, isPost, e, doDup);
                    return;
                }
            }
            this.atFieldPlusPlus(token, isPost, oprand, expr, doDup);
        }
    }
    
    public void atArrayPlusPlus(final int token, final boolean isPost, final Expr expr, final boolean doDup) throws CompileError {
        this.arrayAccess(expr.oprand1(), expr.oprand2());
        final int t = this.exprType;
        final int dim = this.arrayDim;
        if (dim > 0) {
            badType(expr);
        }
        this.bytecode.addOpcode(92);
        this.bytecode.addOpcode(getArrayReadOp(t, this.arrayDim));
        final int dup_code = is2word(t, dim) ? 94 : 91;
        this.atPlusPlusCore(dup_code, doDup, token, isPost, expr);
        this.bytecode.addOpcode(getArrayWriteOp(t, dim));
    }
    
    protected void atPlusPlusCore(final int dup_code, final boolean doDup, final int token, final boolean isPost, final Expr expr) throws CompileError {
        final int t = this.exprType;
        if (doDup && isPost) {
            this.bytecode.addOpcode(dup_code);
        }
        if (t == 324 || t == 303 || t == 306 || t == 334) {
            this.bytecode.addIconst(1);
            this.bytecode.addOpcode((token == 362) ? 96 : 100);
            this.exprType = 324;
        }
        else if (t == 326) {
            this.bytecode.addLconst(1L);
            this.bytecode.addOpcode((token == 362) ? 97 : 101);
        }
        else if (t == 317) {
            this.bytecode.addFconst(1.0f);
            this.bytecode.addOpcode((token == 362) ? 98 : 102);
        }
        else if (t == 312) {
            this.bytecode.addDconst(1.0);
            this.bytecode.addOpcode((token == 362) ? 99 : 103);
        }
        else {
            badType(expr);
        }
        if (doDup && !isPost) {
            this.bytecode.addOpcode(dup_code);
        }
    }
    
    protected abstract void atFieldPlusPlus(final int p0, final boolean p1, final ASTree p2, final Expr p3, final boolean p4) throws CompileError;
    
    @Override
    public abstract void atMember(final Member p0) throws CompileError;
    
    @Override
    public void atVariable(final Variable v) throws CompileError {
        final Declarator d = v.getDeclarator();
        this.exprType = d.getType();
        this.arrayDim = d.getArrayDim();
        this.className = d.getClassName();
        final int var = this.getLocalVar(d);
        if (this.arrayDim > 0) {
            this.bytecode.addAload(var);
        }
        else {
            switch (this.exprType) {
                case 307: {
                    this.bytecode.addAload(var);
                    break;
                }
                case 326: {
                    this.bytecode.addLload(var);
                    break;
                }
                case 317: {
                    this.bytecode.addFload(var);
                    break;
                }
                case 312: {
                    this.bytecode.addDload(var);
                    break;
                }
                default: {
                    this.bytecode.addIload(var);
                    break;
                }
            }
        }
    }
    
    @Override
    public void atKeyword(final Keyword k) throws CompileError {
        this.arrayDim = 0;
        final int token = k.get();
        switch (token) {
            case 410: {
                this.bytecode.addIconst(1);
                this.exprType = 301;
                break;
            }
            case 411: {
                this.bytecode.addIconst(0);
                this.exprType = 301;
                break;
            }
            case 412: {
                this.bytecode.addOpcode(1);
                this.exprType = 412;
                break;
            }
            case 336:
            case 339: {
                if (this.inStaticMethod) {
                    throw new CompileError("not-available: " + ((token == 339) ? "this" : "super"));
                }
                this.bytecode.addAload(0);
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
        this.bytecode.addLdc(s.get());
    }
    
    @Override
    public void atIntConst(final IntConst i) throws CompileError {
        this.arrayDim = 0;
        final long value = i.get();
        final int type = i.getType();
        if (type == 402 || type == 401) {
            this.exprType = ((type == 402) ? 324 : 306);
            this.bytecode.addIconst((int)value);
        }
        else {
            this.exprType = 326;
            this.bytecode.addLconst(value);
        }
    }
    
    @Override
    public void atDoubleConst(final DoubleConst d) throws CompileError {
        this.arrayDim = 0;
        if (d.getType() == 405) {
            this.exprType = 312;
            this.bytecode.addDconst(d.get());
        }
        else {
            this.exprType = 317;
            this.bytecode.addFconst((float)d.get());
        }
    }
    
    static {
        binOp = new int[] { 43, 99, 98, 97, 96, 45, 103, 102, 101, 100, 42, 107, 106, 105, 104, 47, 111, 110, 109, 108, 37, 115, 114, 113, 112, 124, 0, 0, 129, 128, 94, 0, 0, 131, 130, 38, 0, 0, 127, 126, 364, 0, 0, 121, 120, 366, 0, 0, 123, 122, 370, 0, 0, 125, 124 };
        ifOp = new int[] { 358, 159, 160, 350, 160, 159, 357, 164, 163, 359, 162, 161, 60, 161, 162, 62, 163, 164 };
        ifOp2 = new int[] { 358, 153, 154, 350, 154, 153, 357, 158, 157, 359, 156, 155, 60, 155, 156, 62, 157, 158 };
        castOp = new int[] { 0, 144, 143, 142, 141, 0, 140, 139, 138, 137, 0, 136, 135, 134, 133, 0 };
    }
    
    protected abstract static class ReturnHook
    {
        ReturnHook next;
        
        protected abstract boolean doit(final Bytecode p0, final int p1);
        
        protected ReturnHook(final CodeGen gen) {
            this.next = gen.returnHooks;
            gen.returnHooks = this;
        }
        
        protected void remove(final CodeGen gen) {
            gen.returnHooks = this.next;
        }
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler;

import javassist.compiler.ast.NewExpr;
import javassist.compiler.ast.StringL;
import javassist.compiler.ast.Variable;
import javassist.compiler.ast.CallExpr;
import javassist.compiler.ast.Member;
import javassist.compiler.ast.CastExpr;
import javassist.compiler.ast.Expr;
import javassist.compiler.ast.DoubleConst;
import javassist.compiler.ast.IntConst;
import javassist.compiler.ast.BinExpr;
import javassist.compiler.ast.InstanceOfExpr;
import javassist.compiler.ast.CondExpr;
import javassist.compiler.ast.AssignExpr;
import javassist.compiler.ast.ArrayInit;
import javassist.compiler.ast.Pair;
import javassist.compiler.ast.Keyword;
import javassist.compiler.ast.Stmnt;
import javassist.compiler.ast.ASTree;
import javassist.compiler.ast.FieldDecl;
import javassist.compiler.ast.Symbol;
import javassist.compiler.ast.Declarator;
import javassist.compiler.ast.MethodDecl;
import javassist.compiler.ast.ASTList;

public final class Parser implements TokenId
{
    private Lex lex;
    private static final int[] binaryOpPrecedence;
    
    public Parser(final Lex lex) {
        this.lex = lex;
    }
    
    public boolean hasMore() {
        return this.lex.lookAhead() >= 0;
    }
    
    public ASTList parseMember(final SymbolTable tbl) throws CompileError {
        final ASTList mem = this.parseMember1(tbl);
        if (mem instanceof MethodDecl) {
            return this.parseMethod2(tbl, (MethodDecl)mem);
        }
        return mem;
    }
    
    public ASTList parseMember1(final SymbolTable tbl) throws CompileError {
        final ASTList mods = this.parseMemberMods();
        boolean isConstructor = false;
        Declarator d;
        if (this.lex.lookAhead() == 400 && this.lex.lookAhead(1) == 40) {
            d = new Declarator(344, 0);
            isConstructor = true;
        }
        else {
            d = this.parseFormalType(tbl);
        }
        if (this.lex.get() != 400) {
            throw new SyntaxError(this.lex);
        }
        String name;
        if (isConstructor) {
            name = "<init>";
        }
        else {
            name = this.lex.getString();
        }
        d.setVariable(new Symbol(name));
        if (isConstructor || this.lex.lookAhead() == 40) {
            return this.parseMethod1(tbl, isConstructor, mods, d);
        }
        return this.parseField(tbl, mods, d);
    }
    
    private FieldDecl parseField(final SymbolTable tbl, final ASTList mods, final Declarator d) throws CompileError {
        ASTree expr = null;
        if (this.lex.lookAhead() == 61) {
            this.lex.get();
            expr = this.parseExpression(tbl);
        }
        final int c = this.lex.get();
        if (c == 59) {
            return new FieldDecl(mods, new ASTList(d, new ASTList(expr)));
        }
        if (c == 44) {
            throw new CompileError("only one field can be declared in one declaration", this.lex);
        }
        throw new SyntaxError(this.lex);
    }
    
    private MethodDecl parseMethod1(final SymbolTable tbl, final boolean isConstructor, final ASTList mods, final Declarator d) throws CompileError {
        if (this.lex.get() != 40) {
            throw new SyntaxError(this.lex);
        }
        ASTList parms = null;
        if (this.lex.lookAhead() != 41) {
            while (true) {
                parms = ASTList.append(parms, this.parseFormalParam(tbl));
                final int t = this.lex.lookAhead();
                if (t == 44) {
                    this.lex.get();
                }
                else {
                    if (t == 41) {
                        break;
                    }
                    continue;
                }
            }
        }
        this.lex.get();
        d.addArrayDim(this.parseArrayDimension());
        if (isConstructor && d.getArrayDim() > 0) {
            throw new SyntaxError(this.lex);
        }
        ASTList throwsList = null;
        if (this.lex.lookAhead() == 341) {
            this.lex.get();
            while (true) {
                throwsList = ASTList.append(throwsList, this.parseClassType(tbl));
                if (this.lex.lookAhead() != 44) {
                    break;
                }
                this.lex.get();
            }
        }
        return new MethodDecl(mods, new ASTList(d, ASTList.make(parms, throwsList, null)));
    }
    
    public MethodDecl parseMethod2(final SymbolTable tbl, final MethodDecl md) throws CompileError {
        Stmnt body = null;
        if (this.lex.lookAhead() == 59) {
            this.lex.get();
        }
        else {
            body = this.parseBlock(tbl);
            if (body == null) {
                body = new Stmnt(66);
            }
        }
        md.sublist(4).setHead(body);
        return md;
    }
    
    private ASTList parseMemberMods() {
        ASTList list = null;
        while (true) {
            final int t = this.lex.lookAhead();
            if (t != 300 && t != 315 && t != 332 && t != 331 && t != 330 && t != 338 && t != 335 && t != 345 && t != 342 && t != 347) {
                break;
            }
            list = new ASTList(new Keyword(this.lex.get()), list);
        }
        return list;
    }
    
    private Declarator parseFormalType(final SymbolTable tbl) throws CompileError {
        final int t = this.lex.lookAhead();
        if (isBuiltinType(t) || t == 344) {
            this.lex.get();
            final int dim = this.parseArrayDimension();
            return new Declarator(t, dim);
        }
        final ASTList name = this.parseClassType(tbl);
        final int dim2 = this.parseArrayDimension();
        return new Declarator(name, dim2);
    }
    
    private static boolean isBuiltinType(final int t) {
        return t == 301 || t == 303 || t == 306 || t == 334 || t == 324 || t == 326 || t == 317 || t == 312;
    }
    
    private Declarator parseFormalParam(final SymbolTable tbl) throws CompileError {
        final Declarator d = this.parseFormalType(tbl);
        if (this.lex.get() != 400) {
            throw new SyntaxError(this.lex);
        }
        final String name = this.lex.getString();
        d.setVariable(new Symbol(name));
        d.addArrayDim(this.parseArrayDimension());
        tbl.append(name, d);
        return d;
    }
    
    public Stmnt parseStatement(final SymbolTable tbl) throws CompileError {
        final int t = this.lex.lookAhead();
        if (t == 123) {
            return this.parseBlock(tbl);
        }
        if (t == 59) {
            this.lex.get();
            return new Stmnt(66);
        }
        if (t == 400 && this.lex.lookAhead(1) == 58) {
            this.lex.get();
            final String label = this.lex.getString();
            this.lex.get();
            return Stmnt.make(76, new Symbol(label), this.parseStatement(tbl));
        }
        if (t == 320) {
            return this.parseIf(tbl);
        }
        if (t == 346) {
            return this.parseWhile(tbl);
        }
        if (t == 311) {
            return this.parseDo(tbl);
        }
        if (t == 318) {
            return this.parseFor(tbl);
        }
        if (t == 343) {
            return this.parseTry(tbl);
        }
        if (t == 337) {
            return this.parseSwitch(tbl);
        }
        if (t == 338) {
            return this.parseSynchronized(tbl);
        }
        if (t == 333) {
            return this.parseReturn(tbl);
        }
        if (t == 340) {
            return this.parseThrow(tbl);
        }
        if (t == 302) {
            return this.parseBreak(tbl);
        }
        if (t == 309) {
            return this.parseContinue(tbl);
        }
        return this.parseDeclarationOrExpression(tbl, false);
    }
    
    private Stmnt parseBlock(final SymbolTable tbl) throws CompileError {
        if (this.lex.get() != 123) {
            throw new SyntaxError(this.lex);
        }
        Stmnt body = null;
        final SymbolTable tbl2 = new SymbolTable(tbl);
        while (this.lex.lookAhead() != 125) {
            final Stmnt s = this.parseStatement(tbl2);
            if (s != null) {
                body = (Stmnt)ASTList.concat(body, new Stmnt(66, s));
            }
        }
        this.lex.get();
        if (body == null) {
            return new Stmnt(66);
        }
        return body;
    }
    
    private Stmnt parseIf(final SymbolTable tbl) throws CompileError {
        final int t = this.lex.get();
        final ASTree expr = this.parseParExpression(tbl);
        final Stmnt thenp = this.parseStatement(tbl);
        Stmnt elsep;
        if (this.lex.lookAhead() == 313) {
            this.lex.get();
            elsep = this.parseStatement(tbl);
        }
        else {
            elsep = null;
        }
        return new Stmnt(t, expr, new ASTList(thenp, new ASTList(elsep)));
    }
    
    private Stmnt parseWhile(final SymbolTable tbl) throws CompileError {
        final int t = this.lex.get();
        final ASTree expr = this.parseParExpression(tbl);
        final Stmnt body = this.parseStatement(tbl);
        return new Stmnt(t, expr, body);
    }
    
    private Stmnt parseDo(final SymbolTable tbl) throws CompileError {
        final int t = this.lex.get();
        final Stmnt body = this.parseStatement(tbl);
        if (this.lex.get() != 346 || this.lex.get() != 40) {
            throw new SyntaxError(this.lex);
        }
        final ASTree expr = this.parseExpression(tbl);
        if (this.lex.get() != 41 || this.lex.get() != 59) {
            throw new SyntaxError(this.lex);
        }
        return new Stmnt(t, expr, body);
    }
    
    private Stmnt parseFor(final SymbolTable tbl) throws CompileError {
        final int t = this.lex.get();
        final SymbolTable tbl2 = new SymbolTable(tbl);
        if (this.lex.get() != 40) {
            throw new SyntaxError(this.lex);
        }
        Stmnt expr1;
        if (this.lex.lookAhead() == 59) {
            this.lex.get();
            expr1 = null;
        }
        else {
            expr1 = this.parseDeclarationOrExpression(tbl2, true);
        }
        ASTree expr2;
        if (this.lex.lookAhead() == 59) {
            expr2 = null;
        }
        else {
            expr2 = this.parseExpression(tbl2);
        }
        if (this.lex.get() != 59) {
            throw new CompileError("; is missing", this.lex);
        }
        Stmnt expr3;
        if (this.lex.lookAhead() == 41) {
            expr3 = null;
        }
        else {
            expr3 = this.parseExprList(tbl2);
        }
        if (this.lex.get() != 41) {
            throw new CompileError(") is missing", this.lex);
        }
        final Stmnt body = this.parseStatement(tbl2);
        return new Stmnt(t, expr1, new ASTList(expr2, new ASTList(expr3, body)));
    }
    
    private Stmnt parseSwitch(final SymbolTable tbl) throws CompileError {
        final int t = this.lex.get();
        final ASTree expr = this.parseParExpression(tbl);
        final Stmnt body = this.parseSwitchBlock(tbl);
        return new Stmnt(t, expr, body);
    }
    
    private Stmnt parseSwitchBlock(final SymbolTable tbl) throws CompileError {
        if (this.lex.get() != 123) {
            throw new SyntaxError(this.lex);
        }
        final SymbolTable tbl2 = new SymbolTable(tbl);
        Stmnt s = this.parseStmntOrCase(tbl2);
        if (s == null) {
            throw new CompileError("empty switch block", this.lex);
        }
        final int op = s.getOperator();
        if (op != 304 && op != 310) {
            throw new CompileError("no case or default in a switch block", this.lex);
        }
        Stmnt body = new Stmnt(66, s);
        while (this.lex.lookAhead() != 125) {
            final Stmnt s2 = this.parseStmntOrCase(tbl2);
            if (s2 != null) {
                final int op2 = s2.getOperator();
                if (op2 == 304 || op2 == 310) {
                    body = (Stmnt)ASTList.concat(body, new Stmnt(66, s2));
                    s = s2;
                }
                else {
                    s = (Stmnt)ASTList.concat(s, new Stmnt(66, s2));
                }
            }
        }
        this.lex.get();
        return body;
    }
    
    private Stmnt parseStmntOrCase(final SymbolTable tbl) throws CompileError {
        final int t = this.lex.lookAhead();
        if (t != 304 && t != 310) {
            return this.parseStatement(tbl);
        }
        this.lex.get();
        Stmnt s;
        if (t == 304) {
            s = new Stmnt(t, this.parseExpression(tbl));
        }
        else {
            s = new Stmnt(310);
        }
        if (this.lex.get() != 58) {
            throw new CompileError(": is missing", this.lex);
        }
        return s;
    }
    
    private Stmnt parseSynchronized(final SymbolTable tbl) throws CompileError {
        final int t = this.lex.get();
        if (this.lex.get() != 40) {
            throw new SyntaxError(this.lex);
        }
        final ASTree expr = this.parseExpression(tbl);
        if (this.lex.get() != 41) {
            throw new SyntaxError(this.lex);
        }
        final Stmnt body = this.parseBlock(tbl);
        return new Stmnt(t, expr, body);
    }
    
    private Stmnt parseTry(final SymbolTable tbl) throws CompileError {
        this.lex.get();
        final Stmnt block = this.parseBlock(tbl);
        ASTList catchList = null;
        while (this.lex.lookAhead() == 305) {
            this.lex.get();
            if (this.lex.get() != 40) {
                throw new SyntaxError(this.lex);
            }
            final SymbolTable tbl2 = new SymbolTable(tbl);
            final Declarator d = this.parseFormalParam(tbl2);
            if (d.getArrayDim() > 0 || d.getType() != 307) {
                throw new SyntaxError(this.lex);
            }
            if (this.lex.get() != 41) {
                throw new SyntaxError(this.lex);
            }
            final Stmnt b = this.parseBlock(tbl2);
            catchList = ASTList.append(catchList, new Pair(d, b));
        }
        Stmnt finallyBlock = null;
        if (this.lex.lookAhead() == 316) {
            this.lex.get();
            finallyBlock = this.parseBlock(tbl);
        }
        return Stmnt.make(343, block, catchList, finallyBlock);
    }
    
    private Stmnt parseReturn(final SymbolTable tbl) throws CompileError {
        final int t = this.lex.get();
        final Stmnt s = new Stmnt(t);
        if (this.lex.lookAhead() != 59) {
            s.setLeft(this.parseExpression(tbl));
        }
        if (this.lex.get() != 59) {
            throw new CompileError("; is missing", this.lex);
        }
        return s;
    }
    
    private Stmnt parseThrow(final SymbolTable tbl) throws CompileError {
        final int t = this.lex.get();
        final ASTree expr = this.parseExpression(tbl);
        if (this.lex.get() != 59) {
            throw new CompileError("; is missing", this.lex);
        }
        return new Stmnt(t, expr);
    }
    
    private Stmnt parseBreak(final SymbolTable tbl) throws CompileError {
        return this.parseContinue(tbl);
    }
    
    private Stmnt parseContinue(final SymbolTable tbl) throws CompileError {
        final int t = this.lex.get();
        final Stmnt s = new Stmnt(t);
        int t2 = this.lex.get();
        if (t2 == 400) {
            s.setLeft(new Symbol(this.lex.getString()));
            t2 = this.lex.get();
        }
        if (t2 != 59) {
            throw new CompileError("; is missing", this.lex);
        }
        return s;
    }
    
    private Stmnt parseDeclarationOrExpression(final SymbolTable tbl, final boolean exprList) throws CompileError {
        int t;
        for (t = this.lex.lookAhead(); t == 315; t = this.lex.lookAhead()) {
            this.lex.get();
        }
        if (isBuiltinType(t)) {
            t = this.lex.get();
            final int dim = this.parseArrayDimension();
            return this.parseDeclarators(tbl, new Declarator(t, dim));
        }
        if (t == 400) {
            final int i = this.nextIsClassType(0);
            if (i >= 0 && this.lex.lookAhead(i) == 400) {
                final ASTList name = this.parseClassType(tbl);
                final int dim2 = this.parseArrayDimension();
                return this.parseDeclarators(tbl, new Declarator(name, dim2));
            }
        }
        Stmnt expr;
        if (exprList) {
            expr = this.parseExprList(tbl);
        }
        else {
            expr = new Stmnt(69, this.parseExpression(tbl));
        }
        if (this.lex.get() != 59) {
            throw new CompileError("; is missing", this.lex);
        }
        return expr;
    }
    
    private Stmnt parseExprList(final SymbolTable tbl) throws CompileError {
        Stmnt expr = null;
        while (true) {
            final Stmnt e = new Stmnt(69, this.parseExpression(tbl));
            expr = (Stmnt)ASTList.concat(expr, new Stmnt(66, e));
            if (this.lex.lookAhead() != 44) {
                break;
            }
            this.lex.get();
        }
        return expr;
    }
    
    private Stmnt parseDeclarators(final SymbolTable tbl, final Declarator d) throws CompileError {
        Stmnt decl = null;
        while (true) {
            decl = (Stmnt)ASTList.concat(decl, new Stmnt(68, this.parseDeclarator(tbl, d)));
            final int t = this.lex.get();
            if (t == 59) {
                return decl;
            }
            if (t != 44) {
                throw new CompileError("; is missing", this.lex);
            }
        }
    }
    
    private Declarator parseDeclarator(final SymbolTable tbl, final Declarator d) throws CompileError {
        if (this.lex.get() != 400 || d.getType() == 344) {
            throw new SyntaxError(this.lex);
        }
        final String name = this.lex.getString();
        final Symbol symbol = new Symbol(name);
        final int dim = this.parseArrayDimension();
        ASTree init = null;
        if (this.lex.lookAhead() == 61) {
            this.lex.get();
            init = this.parseInitializer(tbl);
        }
        final Declarator decl = d.make(symbol, dim, init);
        tbl.append(name, decl);
        return decl;
    }
    
    private ASTree parseInitializer(final SymbolTable tbl) throws CompileError {
        if (this.lex.lookAhead() == 123) {
            return this.parseArrayInitializer(tbl);
        }
        return this.parseExpression(tbl);
    }
    
    private ArrayInit parseArrayInitializer(final SymbolTable tbl) throws CompileError {
        this.lex.get();
        ASTree expr = this.parseExpression(tbl);
        final ArrayInit init = new ArrayInit(expr);
        while (this.lex.lookAhead() == 44) {
            this.lex.get();
            expr = this.parseExpression(tbl);
            ASTList.append(init, expr);
        }
        if (this.lex.get() != 125) {
            throw new SyntaxError(this.lex);
        }
        return init;
    }
    
    private ASTree parseParExpression(final SymbolTable tbl) throws CompileError {
        if (this.lex.get() != 40) {
            throw new SyntaxError(this.lex);
        }
        final ASTree expr = this.parseExpression(tbl);
        if (this.lex.get() != 41) {
            throw new SyntaxError(this.lex);
        }
        return expr;
    }
    
    public ASTree parseExpression(final SymbolTable tbl) throws CompileError {
        final ASTree left = this.parseConditionalExpr(tbl);
        if (!isAssignOp(this.lex.lookAhead())) {
            return left;
        }
        final int t = this.lex.get();
        final ASTree right = this.parseExpression(tbl);
        return AssignExpr.makeAssign(t, left, right);
    }
    
    private static boolean isAssignOp(final int t) {
        return t == 61 || t == 351 || t == 352 || t == 353 || t == 354 || t == 355 || t == 356 || t == 360 || t == 361 || t == 365 || t == 367 || t == 371;
    }
    
    private ASTree parseConditionalExpr(final SymbolTable tbl) throws CompileError {
        final ASTree cond = this.parseBinaryExpr(tbl);
        if (this.lex.lookAhead() != 63) {
            return cond;
        }
        this.lex.get();
        final ASTree thenExpr = this.parseExpression(tbl);
        if (this.lex.get() != 58) {
            throw new CompileError(": is missing", this.lex);
        }
        final ASTree elseExpr = this.parseExpression(tbl);
        return new CondExpr(cond, thenExpr, elseExpr);
    }
    
    private ASTree parseBinaryExpr(final SymbolTable tbl) throws CompileError {
        ASTree expr = this.parseUnaryExpr(tbl);
        while (true) {
            final int t = this.lex.lookAhead();
            final int p = this.getOpPrecedence(t);
            if (p == 0) {
                break;
            }
            expr = this.binaryExpr2(tbl, expr, p);
        }
        return expr;
    }
    
    private ASTree parseInstanceOf(final SymbolTable tbl, final ASTree expr) throws CompileError {
        final int t = this.lex.lookAhead();
        if (isBuiltinType(t)) {
            this.lex.get();
            final int dim = this.parseArrayDimension();
            return new InstanceOfExpr(t, dim, expr);
        }
        final ASTList name = this.parseClassType(tbl);
        final int dim2 = this.parseArrayDimension();
        return new InstanceOfExpr(name, dim2, expr);
    }
    
    private ASTree binaryExpr2(final SymbolTable tbl, final ASTree expr, final int prec) throws CompileError {
        final int t = this.lex.get();
        if (t == 323) {
            return this.parseInstanceOf(tbl, expr);
        }
        ASTree expr2 = this.parseUnaryExpr(tbl);
        while (true) {
            final int t2 = this.lex.lookAhead();
            final int p2 = this.getOpPrecedence(t2);
            if (p2 == 0 || prec <= p2) {
                break;
            }
            expr2 = this.binaryExpr2(tbl, expr2, p2);
        }
        return BinExpr.makeBin(t, expr, expr2);
    }
    
    private int getOpPrecedence(final int c) {
        if (33 <= c && c <= 63) {
            return Parser.binaryOpPrecedence[c - 33];
        }
        if (c == 94) {
            return 7;
        }
        if (c == 124) {
            return 8;
        }
        if (c == 369) {
            return 9;
        }
        if (c == 368) {
            return 10;
        }
        if (c == 358 || c == 350) {
            return 5;
        }
        if (c == 357 || c == 359 || c == 323) {
            return 4;
        }
        if (c == 364 || c == 366 || c == 370) {
            return 3;
        }
        return 0;
    }
    
    private ASTree parseUnaryExpr(final SymbolTable tbl) throws CompileError {
        switch (this.lex.lookAhead()) {
            case 33:
            case 43:
            case 45:
            case 126:
            case 362:
            case 363: {
                final int t = this.lex.get();
                if (t == 45) {
                    final int t2 = this.lex.lookAhead();
                    switch (t2) {
                        case 401:
                        case 402:
                        case 403: {
                            this.lex.get();
                            return new IntConst(-this.lex.getLong(), t2);
                        }
                        case 404:
                        case 405: {
                            this.lex.get();
                            return new DoubleConst(-this.lex.getDouble(), t2);
                        }
                    }
                }
                return Expr.make(t, this.parseUnaryExpr(tbl));
            }
            case 40: {
                return this.parseCast(tbl);
            }
            default: {
                return this.parsePostfix(tbl);
            }
        }
    }
    
    private ASTree parseCast(final SymbolTable tbl) throws CompileError {
        final int t = this.lex.lookAhead(1);
        if (isBuiltinType(t) && this.nextIsBuiltinCast()) {
            this.lex.get();
            this.lex.get();
            final int dim = this.parseArrayDimension();
            if (this.lex.get() != 41) {
                throw new CompileError(") is missing", this.lex);
            }
            return new CastExpr(t, dim, this.parseUnaryExpr(tbl));
        }
        else {
            if (t != 400 || !this.nextIsClassCast()) {
                return this.parsePostfix(tbl);
            }
            this.lex.get();
            final ASTList name = this.parseClassType(tbl);
            final int dim2 = this.parseArrayDimension();
            if (this.lex.get() != 41) {
                throw new CompileError(") is missing", this.lex);
            }
            return new CastExpr(name, dim2, this.parseUnaryExpr(tbl));
        }
    }
    
    private boolean nextIsBuiltinCast() {
        int i = 2;
        int t;
        while ((t = this.lex.lookAhead(i++)) == 91) {
            if (this.lex.lookAhead(i++) != 93) {
                return false;
            }
        }
        return this.lex.lookAhead(i - 1) == 41;
    }
    
    private boolean nextIsClassCast() {
        final int i = this.nextIsClassType(1);
        if (i < 0) {
            return false;
        }
        int t = this.lex.lookAhead(i);
        if (t != 41) {
            return false;
        }
        t = this.lex.lookAhead(i + 1);
        return t == 40 || t == 412 || t == 406 || t == 400 || t == 339 || t == 336 || t == 328 || t == 410 || t == 411 || t == 403 || t == 402 || t == 401 || t == 405 || t == 404;
    }
    
    private int nextIsClassType(int i) {
        while (this.lex.lookAhead(++i) == 46) {
            if (this.lex.lookAhead(++i) != 400) {
                return -1;
            }
        }
        int t;
        while ((t = this.lex.lookAhead(i++)) == 91) {
            if (this.lex.lookAhead(i++) != 93) {
                return -1;
            }
        }
        return i - 1;
    }
    
    private int parseArrayDimension() throws CompileError {
        int arrayDim = 0;
        while (this.lex.lookAhead() == 91) {
            ++arrayDim;
            this.lex.get();
            if (this.lex.get() != 93) {
                throw new CompileError("] is missing", this.lex);
            }
        }
        return arrayDim;
    }
    
    private ASTList parseClassType(final SymbolTable tbl) throws CompileError {
        ASTList list = null;
        while (this.lex.get() == 400) {
            list = ASTList.append(list, new Symbol(this.lex.getString()));
            if (this.lex.lookAhead() != 46) {
                return list;
            }
            this.lex.get();
        }
        throw new SyntaxError(this.lex);
    }
    
    private ASTree parsePostfix(final SymbolTable tbl) throws CompileError {
        final int token = this.lex.lookAhead();
        switch (token) {
            case 401:
            case 402:
            case 403: {
                this.lex.get();
                return new IntConst(this.lex.getLong(), token);
            }
            case 404:
            case 405: {
                this.lex.get();
                return new DoubleConst(this.lex.getDouble(), token);
            }
            default: {
                ASTree expr = this.parsePrimaryExpr(tbl);
                while (true) {
                    switch (this.lex.lookAhead()) {
                        case 40: {
                            expr = this.parseMethodCall(tbl, expr);
                            continue;
                        }
                        case 91: {
                            if (this.lex.lookAhead(1) == 93) {
                                final int dim = this.parseArrayDimension();
                                if (this.lex.get() != 46 || this.lex.get() != 307) {
                                    throw new SyntaxError(this.lex);
                                }
                                expr = this.parseDotClass(expr, dim);
                                continue;
                            }
                            else {
                                final ASTree index = this.parseArrayIndex(tbl);
                                if (index == null) {
                                    throw new SyntaxError(this.lex);
                                }
                                expr = Expr.make(65, expr, index);
                                continue;
                            }
                            break;
                        }
                        case 362:
                        case 363: {
                            final int t = this.lex.get();
                            expr = Expr.make(t, null, expr);
                            continue;
                        }
                        case 46: {
                            this.lex.get();
                            final int t = this.lex.get();
                            if (t == 307) {
                                expr = this.parseDotClass(expr, 0);
                                continue;
                            }
                            if (t == 336) {
                                expr = Expr.make(46, new Symbol(this.toClassName(expr)), new Keyword(t));
                                continue;
                            }
                            if (t == 400) {
                                final String str = this.lex.getString();
                                expr = Expr.make(46, expr, new Member(str));
                                continue;
                            }
                            throw new CompileError("missing member name", this.lex);
                        }
                        case 35: {
                            this.lex.get();
                            final int t = this.lex.get();
                            if (t != 400) {
                                throw new CompileError("missing static member name", this.lex);
                            }
                            final String str = this.lex.getString();
                            expr = Expr.make(35, new Symbol(this.toClassName(expr)), new Member(str));
                            continue;
                        }
                        default: {
                            return expr;
                        }
                    }
                }
                break;
            }
        }
    }
    
    private ASTree parseDotClass(final ASTree className, int dim) throws CompileError {
        String cname = this.toClassName(className);
        if (dim > 0) {
            final StringBuffer sbuf = new StringBuffer();
            while (dim-- > 0) {
                sbuf.append('[');
            }
            sbuf.append('L').append(cname.replace('.', '/')).append(';');
            cname = sbuf.toString();
        }
        return Expr.make(46, new Symbol(cname), new Member("class"));
    }
    
    private ASTree parseDotClass(final int builtinType, final int dim) throws CompileError {
        String cname = null;
        if (dim > 0) {
            cname = CodeGen.toJvmTypeName(builtinType, dim);
            return Expr.make(46, new Symbol(cname), new Member("class"));
        }
        switch (builtinType) {
            case 301: {
                cname = "java.lang.Boolean";
                break;
            }
            case 303: {
                cname = "java.lang.Byte";
                break;
            }
            case 306: {
                cname = "java.lang.Character";
                break;
            }
            case 334: {
                cname = "java.lang.Short";
                break;
            }
            case 324: {
                cname = "java.lang.Integer";
                break;
            }
            case 326: {
                cname = "java.lang.Long";
                break;
            }
            case 317: {
                cname = "java.lang.Float";
                break;
            }
            case 312: {
                cname = "java.lang.Double";
                break;
            }
            case 344: {
                cname = "java.lang.Void";
                break;
            }
            default: {
                throw new CompileError("invalid builtin type: " + builtinType);
            }
        }
        return Expr.make(35, new Symbol(cname), new Member("TYPE"));
    }
    
    private ASTree parseMethodCall(final SymbolTable tbl, final ASTree expr) throws CompileError {
        if (expr instanceof Keyword) {
            final int token = ((Keyword)expr).get();
            if (token != 339 && token != 336) {
                throw new SyntaxError(this.lex);
            }
        }
        else if (!(expr instanceof Symbol)) {
            if (expr instanceof Expr) {
                final int op = ((Expr)expr).getOperator();
                if (op != 46 && op != 35) {
                    throw new SyntaxError(this.lex);
                }
            }
        }
        return CallExpr.makeCall(expr, this.parseArgumentList(tbl));
    }
    
    private String toClassName(final ASTree name) throws CompileError {
        final StringBuffer sbuf = new StringBuffer();
        this.toClassName(name, sbuf);
        return sbuf.toString();
    }
    
    private void toClassName(final ASTree name, final StringBuffer sbuf) throws CompileError {
        if (name instanceof Symbol) {
            sbuf.append(((Symbol)name).get());
            return;
        }
        if (name instanceof Expr) {
            final Expr expr = (Expr)name;
            if (expr.getOperator() == 46) {
                this.toClassName(expr.oprand1(), sbuf);
                sbuf.append('.');
                this.toClassName(expr.oprand2(), sbuf);
                return;
            }
        }
        throw new CompileError("bad static member access", this.lex);
    }
    
    private ASTree parsePrimaryExpr(final SymbolTable tbl) throws CompileError {
        final int t;
        switch (t = this.lex.get()) {
            case 336:
            case 339:
            case 410:
            case 411:
            case 412: {
                return new Keyword(t);
            }
            case 400: {
                final String name = this.lex.getString();
                final Declarator decl = tbl.lookup(name);
                if (decl == null) {
                    return new Member(name);
                }
                return new Variable(name, decl);
            }
            case 406: {
                return new StringL(this.lex.getString());
            }
            case 328: {
                return this.parseNew(tbl);
            }
            case 40: {
                final ASTree expr = this.parseExpression(tbl);
                if (this.lex.get() == 41) {
                    return expr;
                }
                throw new CompileError(") is missing", this.lex);
            }
            default: {
                if (isBuiltinType(t) || t == 344) {
                    final int dim = this.parseArrayDimension();
                    if (this.lex.get() == 46 && this.lex.get() == 307) {
                        return this.parseDotClass(t, dim);
                    }
                }
                throw new SyntaxError(this.lex);
            }
        }
    }
    
    private NewExpr parseNew(final SymbolTable tbl) throws CompileError {
        ArrayInit init = null;
        int t = this.lex.lookAhead();
        if (isBuiltinType(t)) {
            this.lex.get();
            final ASTList size = this.parseArraySize(tbl);
            if (this.lex.lookAhead() == 123) {
                init = this.parseArrayInitializer(tbl);
            }
            return new NewExpr(t, size, init);
        }
        if (t == 400) {
            final ASTList name = this.parseClassType(tbl);
            t = this.lex.lookAhead();
            if (t == 40) {
                final ASTList args = this.parseArgumentList(tbl);
                return new NewExpr(name, args);
            }
            if (t == 91) {
                final ASTList size2 = this.parseArraySize(tbl);
                if (this.lex.lookAhead() == 123) {
                    init = this.parseArrayInitializer(tbl);
                }
                return NewExpr.makeObjectArray(name, size2, init);
            }
        }
        throw new SyntaxError(this.lex);
    }
    
    private ASTList parseArraySize(final SymbolTable tbl) throws CompileError {
        ASTList list = null;
        while (this.lex.lookAhead() == 91) {
            list = ASTList.append(list, this.parseArrayIndex(tbl));
        }
        return list;
    }
    
    private ASTree parseArrayIndex(final SymbolTable tbl) throws CompileError {
        this.lex.get();
        if (this.lex.lookAhead() == 93) {
            this.lex.get();
            return null;
        }
        final ASTree index = this.parseExpression(tbl);
        if (this.lex.get() != 93) {
            throw new CompileError("] is missing", this.lex);
        }
        return index;
    }
    
    private ASTList parseArgumentList(final SymbolTable tbl) throws CompileError {
        if (this.lex.get() != 40) {
            throw new CompileError("( is missing", this.lex);
        }
        ASTList list = null;
        if (this.lex.lookAhead() != 41) {
            while (true) {
                list = ASTList.append(list, this.parseExpression(tbl));
                if (this.lex.lookAhead() != 44) {
                    break;
                }
                this.lex.get();
            }
        }
        if (this.lex.get() != 41) {
            throw new CompileError(") is missing", this.lex);
        }
        return list;
    }
    
    static {
        binaryOpPrecedence = new int[] { 0, 0, 0, 0, 1, 6, 0, 0, 0, 1, 2, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 4, 0 };
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler;

import javassist.compiler.ast.Symbol;
import javassist.compiler.ast.CallExpr;
import javassist.compiler.ast.Expr;
import javassist.compiler.ast.Member;
import javassist.compiler.ast.ASTree;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.CodeAttribute;
import javassist.CtPrimitiveType;
import javassist.compiler.ast.Stmnt;
import javassist.NotFoundException;
import javassist.CtMethod;
import javassist.compiler.ast.Visitor;
import javassist.CtConstructor;
import javassist.Modifier;
import javassist.compiler.ast.Declarator;
import javassist.CtField;
import javassist.CtBehavior;
import javassist.compiler.ast.ASTList;
import javassist.CannotCompileException;
import javassist.bytecode.BadBytecode;
import javassist.compiler.ast.MethodDecl;
import javassist.compiler.ast.FieldDecl;
import javassist.CtMember;
import javassist.CtClass;
import javassist.bytecode.Bytecode;

public class Javac
{
    JvstCodeGen gen;
    SymbolTable stable;
    private Bytecode bytecode;
    public static final String param0Name = "$0";
    public static final String resultVarName = "$_";
    public static final String proceedName = "$proceed";
    
    public Javac(final CtClass thisClass) {
        this(new Bytecode(thisClass.getClassFile2().getConstPool(), 0, 0), thisClass);
    }
    
    public Javac(final Bytecode b, final CtClass thisClass) {
        this.gen = new JvstCodeGen(b, thisClass, thisClass.getClassPool());
        this.stable = new SymbolTable();
        this.bytecode = b;
    }
    
    public Bytecode getBytecode() {
        return this.bytecode;
    }
    
    public CtMember compile(final String src) throws CompileError {
        final Parser p = new Parser(new Lex(src));
        final ASTList mem = p.parseMember1(this.stable);
        try {
            if (mem instanceof FieldDecl) {
                return this.compileField((FieldDecl)mem);
            }
            final CtBehavior cb = this.compileMethod(p, (MethodDecl)mem);
            final CtClass decl = cb.getDeclaringClass();
            cb.getMethodInfo2().rebuildStackMapIf6(decl.getClassPool(), decl.getClassFile2());
            return cb;
        }
        catch (BadBytecode bb) {
            throw new CompileError(bb.getMessage());
        }
        catch (CannotCompileException e) {
            throw new CompileError(e.getMessage());
        }
    }
    
    private CtField compileField(final FieldDecl fd) throws CompileError, CannotCompileException {
        final Declarator d = fd.getDeclarator();
        final CtFieldWithInit f = new CtFieldWithInit(this.gen.resolver.lookupClass(d), d.getVariable().get(), this.gen.getThisClass());
        f.setModifiers(MemberResolver.getModifiers(fd.getModifiers()));
        if (fd.getInit() != null) {
            f.setInit(fd.getInit());
        }
        return f;
    }
    
    private CtBehavior compileMethod(final Parser p, MethodDecl md) throws CompileError {
        final int mod = MemberResolver.getModifiers(md.getModifiers());
        final CtClass[] plist = this.gen.makeParamList(md);
        final CtClass[] tlist = this.gen.makeThrowsList(md);
        this.recordParams(plist, Modifier.isStatic(mod));
        md = p.parseMethod2(this.stable, md);
        try {
            if (md.isConstructor()) {
                final CtConstructor cons = new CtConstructor(plist, this.gen.getThisClass());
                cons.setModifiers(mod);
                md.accept(this.gen);
                cons.getMethodInfo().setCodeAttribute(this.bytecode.toCodeAttribute());
                cons.setExceptionTypes(tlist);
                return cons;
            }
            final Declarator r = md.getReturn();
            final CtClass rtype = this.gen.resolver.lookupClass(r);
            this.recordReturnType(rtype, false);
            final CtMethod method = new CtMethod(rtype, r.getVariable().get(), plist, this.gen.getThisClass());
            method.setModifiers(mod);
            this.gen.setThisMethod(method);
            md.accept(this.gen);
            if (md.getBody() != null) {
                method.getMethodInfo().setCodeAttribute(this.bytecode.toCodeAttribute());
            }
            else {
                method.setModifiers(mod | 0x400);
            }
            method.setExceptionTypes(tlist);
            return method;
        }
        catch (NotFoundException e) {
            throw new CompileError(e.toString());
        }
    }
    
    public Bytecode compileBody(final CtBehavior method, final String src) throws CompileError {
        try {
            final int mod = method.getModifiers();
            this.recordParams(method.getParameterTypes(), Modifier.isStatic(mod));
            CtClass rtype;
            if (method instanceof CtMethod) {
                this.gen.setThisMethod((CtMethod)method);
                rtype = ((CtMethod)method).getReturnType();
            }
            else {
                rtype = CtClass.voidType;
            }
            this.recordReturnType(rtype, false);
            final boolean isVoid = rtype == CtClass.voidType;
            if (src == null) {
                makeDefaultBody(this.bytecode, rtype);
            }
            else {
                final Parser p = new Parser(new Lex(src));
                final SymbolTable stb = new SymbolTable(this.stable);
                final Stmnt s = p.parseStatement(stb);
                if (p.hasMore()) {
                    throw new CompileError("the method/constructor body must be surrounded by {}");
                }
                boolean callSuper = false;
                if (method instanceof CtConstructor) {
                    callSuper = !((CtConstructor)method).isClassInitializer();
                }
                this.gen.atMethodBody(s, callSuper, isVoid);
            }
            return this.bytecode;
        }
        catch (NotFoundException e) {
            throw new CompileError(e.toString());
        }
    }
    
    private static void makeDefaultBody(final Bytecode b, final CtClass type) {
        int op;
        int value;
        if (type instanceof CtPrimitiveType) {
            final CtPrimitiveType pt = (CtPrimitiveType)type;
            op = pt.getReturnOp();
            if (op == 175) {
                value = 14;
            }
            else if (op == 174) {
                value = 11;
            }
            else if (op == 173) {
                value = 9;
            }
            else if (op == 177) {
                value = 0;
            }
            else {
                value = 3;
            }
        }
        else {
            op = 176;
            value = 1;
        }
        if (value != 0) {
            b.addOpcode(value);
        }
        b.addOpcode(op);
    }
    
    public boolean recordLocalVariables(final CodeAttribute ca, final int pc) throws CompileError {
        final LocalVariableAttribute va = (LocalVariableAttribute)ca.getAttribute("LocalVariableTable");
        if (va == null) {
            return false;
        }
        for (int n = va.tableLength(), i = 0; i < n; ++i) {
            final int start = va.startPc(i);
            final int len = va.codeLength(i);
            if (start <= pc && pc < start + len) {
                this.gen.recordVariable(va.descriptor(i), va.variableName(i), va.index(i), this.stable);
            }
        }
        return true;
    }
    
    public boolean recordParamNames(final CodeAttribute ca, final int numOfLocalVars) throws CompileError {
        final LocalVariableAttribute va = (LocalVariableAttribute)ca.getAttribute("LocalVariableTable");
        if (va == null) {
            return false;
        }
        for (int n = va.tableLength(), i = 0; i < n; ++i) {
            final int index = va.index(i);
            if (index < numOfLocalVars) {
                this.gen.recordVariable(va.descriptor(i), va.variableName(i), index, this.stable);
            }
        }
        return true;
    }
    
    public int recordParams(final CtClass[] params, final boolean isStatic) throws CompileError {
        return this.gen.recordParams(params, isStatic, "$", "$args", "$$", this.stable);
    }
    
    public int recordParams(final String target, final CtClass[] params, final boolean use0, final int varNo, final boolean isStatic) throws CompileError {
        return this.gen.recordParams(params, isStatic, "$", "$args", "$$", use0, varNo, target, this.stable);
    }
    
    public void setMaxLocals(final int max) {
        this.gen.setMaxLocals(max);
    }
    
    public int recordReturnType(final CtClass type, final boolean useResultVar) throws CompileError {
        this.gen.recordType(type);
        return this.gen.recordReturnType(type, "$r", useResultVar ? "$_" : null, this.stable);
    }
    
    public void recordType(final CtClass t) {
        this.gen.recordType(t);
    }
    
    public int recordVariable(final CtClass type, final String name) throws CompileError {
        return this.gen.recordVariable(type, name, this.stable);
    }
    
    public void recordProceed(final String target, final String method) throws CompileError {
        final Parser p = new Parser(new Lex(target));
        final ASTree texpr = p.parseExpression(this.stable);
        final String m = method;
        final ProceedHandler h = new ProceedHandler() {
            @Override
            public void doit(final JvstCodeGen gen, final Bytecode b, final ASTList args) throws CompileError {
                ASTree expr = new Member(m);
                if (texpr != null) {
                    expr = Expr.make(46, texpr, expr);
                }
                expr = CallExpr.makeCall(expr, args);
                gen.compileExpr(expr);
                gen.addNullIfVoid();
            }
            
            @Override
            public void setReturnType(final JvstTypeChecker check, final ASTList args) throws CompileError {
                ASTree expr = new Member(m);
                if (texpr != null) {
                    expr = Expr.make(46, texpr, expr);
                }
                expr = CallExpr.makeCall(expr, args);
                expr.accept(check);
                check.addNullIfVoid();
            }
        };
        this.gen.setProceedHandler(h, "$proceed");
    }
    
    public void recordStaticProceed(final String targetClass, final String method) throws CompileError {
        final String c = targetClass;
        final String m = method;
        final ProceedHandler h = new ProceedHandler() {
            @Override
            public void doit(final JvstCodeGen gen, final Bytecode b, final ASTList args) throws CompileError {
                Expr expr = Expr.make(35, new Symbol(c), new Member(m));
                expr = CallExpr.makeCall(expr, args);
                gen.compileExpr(expr);
                gen.addNullIfVoid();
            }
            
            @Override
            public void setReturnType(final JvstTypeChecker check, final ASTList args) throws CompileError {
                Expr expr = Expr.make(35, new Symbol(c), new Member(m));
                expr = CallExpr.makeCall(expr, args);
                expr.accept(check);
                check.addNullIfVoid();
            }
        };
        this.gen.setProceedHandler(h, "$proceed");
    }
    
    public void recordSpecialProceed(final String target, final String classname, final String methodname, final String descriptor, final int methodIndex) throws CompileError {
        final Parser p = new Parser(new Lex(target));
        final ASTree texpr = p.parseExpression(this.stable);
        final ProceedHandler h = new ProceedHandler() {
            @Override
            public void doit(final JvstCodeGen gen, final Bytecode b, final ASTList args) throws CompileError {
                gen.compileInvokeSpecial(texpr, methodIndex, descriptor, args);
            }
            
            @Override
            public void setReturnType(final JvstTypeChecker c, final ASTList args) throws CompileError {
                c.compileInvokeSpecial(texpr, classname, methodname, descriptor, args);
            }
        };
        this.gen.setProceedHandler(h, "$proceed");
    }
    
    public void recordProceed(final ProceedHandler h) {
        this.gen.setProceedHandler(h, "$proceed");
    }
    
    public void compileStmnt(final String src) throws CompileError {
        final Parser p = new Parser(new Lex(src));
        final SymbolTable stb = new SymbolTable(this.stable);
        while (p.hasMore()) {
            final Stmnt s = p.parseStatement(stb);
            if (s != null) {
                s.accept(this.gen);
            }
        }
    }
    
    public void compileExpr(final String src) throws CompileError {
        final ASTree e = parseExpr(src, this.stable);
        this.compileExpr(e);
    }
    
    public static ASTree parseExpr(final String src, final SymbolTable st) throws CompileError {
        final Parser p = new Parser(new Lex(src));
        return p.parseExpression(st);
    }
    
    public void compileExpr(final ASTree e) throws CompileError {
        if (e != null) {
            this.gen.compileExpr(e);
        }
    }
    
    public static class CtFieldWithInit extends CtField
    {
        private ASTree init;
        
        CtFieldWithInit(final CtClass type, final String name, final CtClass declaring) throws CannotCompileException {
            super(type, name, declaring);
            this.init = null;
        }
        
        protected void setInit(final ASTree i) {
            this.init = i;
        }
        
        @Override
        protected ASTree getInitAST() {
            return this.init;
        }
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler.ast;

import javassist.compiler.CompileError;
import javassist.compiler.TokenId;

public class Declarator extends ASTList implements TokenId
{
    protected int varType;
    protected int arrayDim;
    protected int localVar;
    protected String qualifiedClass;
    
    public Declarator(final int type, final int dim) {
        super(null);
        this.varType = type;
        this.arrayDim = dim;
        this.localVar = -1;
        this.qualifiedClass = null;
    }
    
    public Declarator(final ASTList className, final int dim) {
        super(null);
        this.varType = 307;
        this.arrayDim = dim;
        this.localVar = -1;
        this.qualifiedClass = astToClassName(className, '/');
    }
    
    public Declarator(final int type, final String jvmClassName, final int dim, final int var, final Symbol sym) {
        super(null);
        this.varType = type;
        this.arrayDim = dim;
        this.localVar = var;
        this.qualifiedClass = jvmClassName;
        this.setLeft(sym);
        ASTList.append(this, null);
    }
    
    public Declarator make(final Symbol sym, final int dim, final ASTree init) {
        final Declarator d = new Declarator(this.varType, this.arrayDim + dim);
        d.qualifiedClass = this.qualifiedClass;
        d.setLeft(sym);
        ASTList.append(d, init);
        return d;
    }
    
    public int getType() {
        return this.varType;
    }
    
    public int getArrayDim() {
        return this.arrayDim;
    }
    
    public void addArrayDim(final int d) {
        this.arrayDim += d;
    }
    
    public String getClassName() {
        return this.qualifiedClass;
    }
    
    public void setClassName(final String s) {
        this.qualifiedClass = s;
    }
    
    public Symbol getVariable() {
        return (Symbol)this.getLeft();
    }
    
    public void setVariable(final Symbol sym) {
        this.setLeft(sym);
    }
    
    public ASTree getInitializer() {
        final ASTList t = this.tail();
        if (t != null) {
            return t.head();
        }
        return null;
    }
    
    public void setLocalVar(final int n) {
        this.localVar = n;
    }
    
    public int getLocalVar() {
        return this.localVar;
    }
    
    public String getTag() {
        return "decl";
    }
    
    @Override
    public void accept(final Visitor v) throws CompileError {
        v.atDeclarator(this);
    }
    
    public static String astToClassName(final ASTList name, final char sep) {
        if (name == null) {
            return null;
        }
        final StringBuffer sbuf = new StringBuffer();
        astToClassName(sbuf, name, sep);
        return sbuf.toString();
    }
    
    private static void astToClassName(final StringBuffer sbuf, ASTList name, final char sep) {
        while (true) {
            final ASTree h = name.head();
            if (h instanceof Symbol) {
                sbuf.append(((Symbol)h).get());
            }
            else if (h instanceof ASTList) {
                astToClassName(sbuf, (ASTList)h, sep);
            }
            name = name.tail();
            if (name == null) {
                break;
            }
            sbuf.append(sep);
        }
    }
}

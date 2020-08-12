// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler.ast;

import javassist.compiler.CompileError;

public class Variable extends Symbol
{
    protected Declarator declarator;
    
    public Variable(final String sym, final Declarator d) {
        super(sym);
        this.declarator = d;
    }
    
    public Declarator getDeclarator() {
        return this.declarator;
    }
    
    @Override
    public String toString() {
        return this.identifier + ":" + this.declarator.getType();
    }
    
    @Override
    public void accept(final Visitor v) throws CompileError {
        v.atVariable(this);
    }
}

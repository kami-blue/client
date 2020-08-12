// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler.ast;

import javassist.compiler.CompileError;

public class MethodDecl extends ASTList
{
    public static final String initName = "<init>";
    
    public MethodDecl(final ASTree _head, final ASTList _tail) {
        super(_head, _tail);
    }
    
    public boolean isConstructor() {
        final Symbol sym = this.getReturn().getVariable();
        return sym != null && "<init>".equals(sym.get());
    }
    
    public ASTList getModifiers() {
        return (ASTList)this.getLeft();
    }
    
    public Declarator getReturn() {
        return (Declarator)this.tail().head();
    }
    
    public ASTList getParams() {
        return (ASTList)this.sublist(2).head();
    }
    
    public ASTList getThrows() {
        return (ASTList)this.sublist(3).head();
    }
    
    public Stmnt getBody() {
        return (Stmnt)this.sublist(4).head();
    }
    
    @Override
    public void accept(final Visitor v) throws CompileError {
        v.atMethodDecl(this);
    }
}

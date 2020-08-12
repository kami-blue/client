// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler;

import javassist.compiler.ast.ASTree;

public class NoFieldException extends CompileError
{
    private String fieldName;
    private ASTree expr;
    
    public NoFieldException(final String name, final ASTree e) {
        super("no such field: " + name);
        this.fieldName = name;
        this.expr = e;
    }
    
    public String getField() {
        return this.fieldName;
    }
    
    public ASTree getExpr() {
        return this.expr;
    }
}

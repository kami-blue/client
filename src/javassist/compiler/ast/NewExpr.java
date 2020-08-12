// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler.ast;

import javassist.compiler.CompileError;
import javassist.compiler.TokenId;

public class NewExpr extends ASTList implements TokenId
{
    protected boolean newArray;
    protected int arrayType;
    
    public NewExpr(final ASTList className, final ASTList args) {
        super(className, new ASTList(args));
        this.newArray = false;
        this.arrayType = 307;
    }
    
    public NewExpr(final int type, final ASTList arraySize, final ArrayInit init) {
        super(null, new ASTList(arraySize));
        this.newArray = true;
        this.arrayType = type;
        if (init != null) {
            ASTList.append(this, init);
        }
    }
    
    public static NewExpr makeObjectArray(final ASTList className, final ASTList arraySize, final ArrayInit init) {
        final NewExpr e = new NewExpr(className, arraySize);
        e.newArray = true;
        if (init != null) {
            ASTList.append(e, init);
        }
        return e;
    }
    
    public boolean isArray() {
        return this.newArray;
    }
    
    public int getArrayType() {
        return this.arrayType;
    }
    
    public ASTList getClassName() {
        return (ASTList)this.getLeft();
    }
    
    public ASTList getArguments() {
        return (ASTList)this.getRight().getLeft();
    }
    
    public ASTList getArraySize() {
        return this.getArguments();
    }
    
    public ArrayInit getInitializer() {
        final ASTree t = this.getRight().getRight();
        if (t == null) {
            return null;
        }
        return (ArrayInit)t.getLeft();
    }
    
    @Override
    public void accept(final Visitor v) throws CompileError {
        v.atNewExpr(this);
    }
    
    @Override
    protected String getTag() {
        return this.newArray ? "new[]" : "new";
    }
}

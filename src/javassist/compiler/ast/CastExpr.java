// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler.ast;

import javassist.compiler.CompileError;
import javassist.compiler.TokenId;

public class CastExpr extends ASTList implements TokenId
{
    protected int castType;
    protected int arrayDim;
    
    public CastExpr(final ASTList className, final int dim, final ASTree expr) {
        super(className, new ASTList(expr));
        this.castType = 307;
        this.arrayDim = dim;
    }
    
    public CastExpr(final int type, final int dim, final ASTree expr) {
        super(null, new ASTList(expr));
        this.castType = type;
        this.arrayDim = dim;
    }
    
    public int getType() {
        return this.castType;
    }
    
    public int getArrayDim() {
        return this.arrayDim;
    }
    
    public ASTList getClassName() {
        return (ASTList)this.getLeft();
    }
    
    public ASTree getOprand() {
        return this.getRight().getLeft();
    }
    
    public void setOprand(final ASTree t) {
        this.getRight().setLeft(t);
    }
    
    public String getTag() {
        return "cast:" + this.castType + ":" + this.arrayDim;
    }
    
    @Override
    public void accept(final Visitor v) throws CompileError {
        v.atCastExpr(this);
    }
}

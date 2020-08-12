// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler.ast;

import javassist.compiler.CompileError;
import javassist.compiler.TokenId;

public class Expr extends ASTList implements TokenId
{
    protected int operatorId;
    
    Expr(final int op, final ASTree _head, final ASTList _tail) {
        super(_head, _tail);
        this.operatorId = op;
    }
    
    Expr(final int op, final ASTree _head) {
        super(_head);
        this.operatorId = op;
    }
    
    public static Expr make(final int op, final ASTree oprand1, final ASTree oprand2) {
        return new Expr(op, oprand1, new ASTList(oprand2));
    }
    
    public static Expr make(final int op, final ASTree oprand1) {
        return new Expr(op, oprand1);
    }
    
    public int getOperator() {
        return this.operatorId;
    }
    
    public void setOperator(final int op) {
        this.operatorId = op;
    }
    
    public ASTree oprand1() {
        return this.getLeft();
    }
    
    public void setOprand1(final ASTree expr) {
        this.setLeft(expr);
    }
    
    public ASTree oprand2() {
        return this.getRight().getLeft();
    }
    
    public void setOprand2(final ASTree expr) {
        this.getRight().setLeft(expr);
    }
    
    @Override
    public void accept(final Visitor v) throws CompileError {
        v.atExpr(this);
    }
    
    public String getName() {
        final int id = this.operatorId;
        if (id < 128) {
            return String.valueOf((char)id);
        }
        if (350 <= id && id <= 371) {
            return Expr.opNames[id - 350];
        }
        if (id == 323) {
            return "instanceof";
        }
        return String.valueOf(id);
    }
    
    @Override
    protected String getTag() {
        return "op:" + this.getName();
    }
}

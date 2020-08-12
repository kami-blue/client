// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler.ast;

import javassist.compiler.CompileError;

public class DoubleConst extends ASTree
{
    protected double value;
    protected int type;
    
    public DoubleConst(final double v, final int tokenId) {
        this.value = v;
        this.type = tokenId;
    }
    
    public double get() {
        return this.value;
    }
    
    public void set(final double v) {
        this.value = v;
    }
    
    public int getType() {
        return this.type;
    }
    
    @Override
    public String toString() {
        return Double.toString(this.value);
    }
    
    @Override
    public void accept(final Visitor v) throws CompileError {
        v.atDoubleConst(this);
    }
    
    public ASTree compute(final int op, final ASTree right) {
        if (right instanceof IntConst) {
            return this.compute0(op, (IntConst)right);
        }
        if (right instanceof DoubleConst) {
            return this.compute0(op, (DoubleConst)right);
        }
        return null;
    }
    
    private DoubleConst compute0(final int op, final DoubleConst right) {
        int newType;
        if (this.type == 405 || right.type == 405) {
            newType = 405;
        }
        else {
            newType = 404;
        }
        return compute(op, this.value, right.value, newType);
    }
    
    private DoubleConst compute0(final int op, final IntConst right) {
        return compute(op, this.value, (double)right.value, this.type);
    }
    
    private static DoubleConst compute(final int op, final double value1, final double value2, final int newType) {
        double newValue = 0.0;
        switch (op) {
            case 43: {
                newValue = value1 + value2;
                break;
            }
            case 45: {
                newValue = value1 - value2;
                break;
            }
            case 42: {
                newValue = value1 * value2;
                break;
            }
            case 47: {
                newValue = value1 / value2;
                break;
            }
            case 37: {
                newValue = value1 % value2;
                break;
            }
            default: {
                return null;
            }
        }
        return new DoubleConst(newValue, newType);
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler.ast;

import javassist.compiler.CompileError;

public class IntConst extends ASTree
{
    protected long value;
    protected int type;
    
    public IntConst(final long v, final int tokenId) {
        this.value = v;
        this.type = tokenId;
    }
    
    public long get() {
        return this.value;
    }
    
    public void set(final long v) {
        this.value = v;
    }
    
    public int getType() {
        return this.type;
    }
    
    @Override
    public String toString() {
        return Long.toString(this.value);
    }
    
    @Override
    public void accept(final Visitor v) throws CompileError {
        v.atIntConst(this);
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
    
    private IntConst compute0(final int op, final IntConst right) {
        final int type1 = this.type;
        final int type2 = right.type;
        int newType;
        if (type1 == 403 || type2 == 403) {
            newType = 403;
        }
        else if (type1 == 401 && type2 == 401) {
            newType = 401;
        }
        else {
            newType = 402;
        }
        final long value1 = this.value;
        final long value2 = right.value;
        long newValue = 0L;
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
            case 124: {
                newValue = (value1 | value2);
                break;
            }
            case 94: {
                newValue = (value1 ^ value2);
                break;
            }
            case 38: {
                newValue = (value1 & value2);
                break;
            }
            case 364: {
                newValue = this.value << (int)value2;
                newType = type1;
                break;
            }
            case 366: {
                newValue = this.value >> (int)value2;
                newType = type1;
                break;
            }
            case 370: {
                newValue = this.value >>> (int)value2;
                newType = type1;
                break;
            }
            default: {
                return null;
            }
        }
        return new IntConst(newValue, newType);
    }
    
    private DoubleConst compute0(final int op, final DoubleConst right) {
        final double value1 = (double)this.value;
        final double value2 = right.value;
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
        return new DoubleConst(newValue, right.type);
    }
}

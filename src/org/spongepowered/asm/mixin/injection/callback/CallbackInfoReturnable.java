// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.callback;

import org.spongepowered.asm.lib.Type;

public class CallbackInfoReturnable<R> extends CallbackInfo
{
    private R returnValue;
    
    public CallbackInfoReturnable(final String name, final boolean cancellable) {
        super(name, cancellable);
        this.returnValue = null;
    }
    
    public CallbackInfoReturnable(final String name, final boolean cancellable, final R returnValue) {
        super(name, cancellable);
        this.returnValue = returnValue;
    }
    
    public CallbackInfoReturnable(final String name, final boolean cancellable, final byte returnValue) {
        super(name, cancellable);
        this.returnValue = (R)Byte.valueOf(returnValue);
    }
    
    public CallbackInfoReturnable(final String name, final boolean cancellable, final char returnValue) {
        super(name, cancellable);
        this.returnValue = (R)Character.valueOf(returnValue);
    }
    
    public CallbackInfoReturnable(final String name, final boolean cancellable, final double returnValue) {
        super(name, cancellable);
        this.returnValue = (R)Double.valueOf(returnValue);
    }
    
    public CallbackInfoReturnable(final String name, final boolean cancellable, final float returnValue) {
        super(name, cancellable);
        this.returnValue = (R)Float.valueOf(returnValue);
    }
    
    public CallbackInfoReturnable(final String name, final boolean cancellable, final int returnValue) {
        super(name, cancellable);
        this.returnValue = (R)Integer.valueOf(returnValue);
    }
    
    public CallbackInfoReturnable(final String name, final boolean cancellable, final long returnValue) {
        super(name, cancellable);
        this.returnValue = (R)Long.valueOf(returnValue);
    }
    
    public CallbackInfoReturnable(final String name, final boolean cancellable, final short returnValue) {
        super(name, cancellable);
        this.returnValue = (R)Short.valueOf(returnValue);
    }
    
    public CallbackInfoReturnable(final String name, final boolean cancellable, final boolean returnValue) {
        super(name, cancellable);
        this.returnValue = (R)Boolean.valueOf(returnValue);
    }
    
    public void setReturnValue(final R returnValue) throws CancellationException {
        super.cancel();
        this.returnValue = returnValue;
    }
    
    public R getReturnValue() {
        return this.returnValue;
    }
    
    public byte getReturnValueB() {
        if (this.returnValue == null) {
            return 0;
        }
        return (byte)this.returnValue;
    }
    
    public char getReturnValueC() {
        if (this.returnValue == null) {
            return '\0';
        }
        return (char)this.returnValue;
    }
    
    public double getReturnValueD() {
        if (this.returnValue == null) {
            return 0.0;
        }
        return (double)this.returnValue;
    }
    
    public float getReturnValueF() {
        if (this.returnValue == null) {
            return 0.0f;
        }
        return (float)this.returnValue;
    }
    
    public int getReturnValueI() {
        if (this.returnValue == null) {
            return 0;
        }
        return (int)this.returnValue;
    }
    
    public long getReturnValueJ() {
        if (this.returnValue == null) {
            return 0L;
        }
        return (long)this.returnValue;
    }
    
    public short getReturnValueS() {
        if (this.returnValue == null) {
            return 0;
        }
        return (short)this.returnValue;
    }
    
    public boolean getReturnValueZ() {
        return this.returnValue != null && (boolean)this.returnValue;
    }
    
    static String getReturnAccessor(final Type returnType) {
        if (returnType.getSort() == 10 || returnType.getSort() == 9) {
            return "getReturnValue";
        }
        return String.format("getReturnValue%s", returnType.getDescriptor());
    }
    
    static String getReturnDescriptor(final Type returnType) {
        if (returnType.getSort() == 10 || returnType.getSort() == 9) {
            return String.format("()%s", "Ljava/lang/Object;");
        }
        return String.format("()%s", returnType.getDescriptor());
    }
}

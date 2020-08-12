// 
// Decompiled by Procyon v0.5.36
// 

package javassist.tools.reflect;

import java.lang.reflect.InvocationTargetException;

public class CannotInvokeException extends RuntimeException
{
    private Throwable err;
    
    public Throwable getReason() {
        return this.err;
    }
    
    public CannotInvokeException(final String reason) {
        super(reason);
        this.err = null;
    }
    
    public CannotInvokeException(final InvocationTargetException e) {
        super("by " + e.getTargetException().toString());
        this.err = null;
        this.err = e.getTargetException();
    }
    
    public CannotInvokeException(final IllegalAccessException e) {
        super("by " + e.toString());
        this.err = null;
        this.err = e;
    }
    
    public CannotInvokeException(final ClassNotFoundException e) {
        super("by " + e.toString());
        this.err = null;
        this.err = e;
    }
}

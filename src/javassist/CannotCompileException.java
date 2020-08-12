// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import javassist.compiler.CompileError;

public class CannotCompileException extends Exception
{
    private Throwable myCause;
    private String message;
    
    @Override
    public Throwable getCause() {
        return (this.myCause == this) ? null : this.myCause;
    }
    
    @Override
    public synchronized Throwable initCause(final Throwable cause) {
        this.myCause = cause;
        return this;
    }
    
    public String getReason() {
        if (this.message != null) {
            return this.message;
        }
        return this.toString();
    }
    
    public CannotCompileException(final String msg) {
        super(msg);
        this.message = msg;
        this.initCause(null);
    }
    
    public CannotCompileException(final Throwable e) {
        super("by " + e.toString());
        this.message = null;
        this.initCause(e);
    }
    
    public CannotCompileException(final String msg, final Throwable e) {
        this(msg);
        this.initCause(e);
    }
    
    public CannotCompileException(final NotFoundException e) {
        this("cannot find " + e.getMessage(), e);
    }
    
    public CannotCompileException(final CompileError e) {
        this("[source error] " + e.getMessage(), e);
    }
    
    public CannotCompileException(final ClassNotFoundException e, final String name) {
        this("cannot find " + name, e);
    }
    
    public CannotCompileException(final ClassFormatError e, final String name) {
        this("invalid class format: " + name, e);
    }
}

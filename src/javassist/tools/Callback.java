// 
// Decompiled by Procyon v0.5.36
// 

package javassist.tools;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import java.util.UUID;
import java.util.HashMap;

public abstract class Callback
{
    public static HashMap callbacks;
    private final String sourceCode;
    
    public Callback(final String src) {
        final String uuid = UUID.randomUUID().toString();
        Callback.callbacks.put(uuid, this);
        this.sourceCode = "((javassist.tools.Callback) javassist.tools.Callback.callbacks.get(\"" + uuid + "\")).result(new Object[]{" + src + "});";
    }
    
    public abstract void result(final Object[] p0);
    
    @Override
    public String toString() {
        return this.sourceCode();
    }
    
    public String sourceCode() {
        return this.sourceCode;
    }
    
    public static void insertBefore(final CtBehavior behavior, final Callback callback) throws CannotCompileException {
        behavior.insertBefore(callback.toString());
    }
    
    public static void insertAfter(final CtBehavior behavior, final Callback callback) throws CannotCompileException {
        behavior.insertAfter(callback.toString(), false);
    }
    
    public static void insertAfter(final CtBehavior behavior, final Callback callback, final boolean asFinally) throws CannotCompileException {
        behavior.insertAfter(callback.toString(), asFinally);
    }
    
    public static int insertAt(final CtBehavior behavior, final Callback callback, final int lineNum) throws CannotCompileException {
        return behavior.insertAt(lineNum, callback.toString());
    }
    
    static {
        Callback.callbacks = new HashMap();
    }
}

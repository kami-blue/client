// 
// Decompiled by Procyon v0.5.36
// 

package javassist.tools.rmi;

public class RemoteException extends RuntimeException
{
    public RemoteException(final String msg) {
        super(msg);
    }
    
    public RemoteException(final Exception e) {
        super("by " + e.toString());
    }
}

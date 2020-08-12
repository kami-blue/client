// 
// Decompiled by Procyon v0.5.36
// 

package javassist.tools.rmi;

public class Sample
{
    private ObjectImporter importer;
    private int objectId;
    
    public Object forward(final Object[] args, final int identifier) {
        return this.importer.call(this.objectId, identifier, args);
    }
    
    public static Object forwardStatic(final Object[] args, final int identifier) throws RemoteException {
        throw new RemoteException("cannot call a static method.");
    }
}

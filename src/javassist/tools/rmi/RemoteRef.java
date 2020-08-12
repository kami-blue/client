// 
// Decompiled by Procyon v0.5.36
// 

package javassist.tools.rmi;

import java.io.Serializable;

public class RemoteRef implements Serializable
{
    public int oid;
    public String classname;
    
    public RemoteRef(final int i) {
        this.oid = i;
        this.classname = null;
    }
    
    public RemoteRef(final int i, final String name) {
        this.oid = i;
        this.classname = name;
    }
}

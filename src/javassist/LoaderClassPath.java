// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import java.net.URL;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class LoaderClassPath implements ClassPath
{
    private WeakReference clref;
    
    public LoaderClassPath(final ClassLoader cl) {
        this.clref = new WeakReference((T)cl);
    }
    
    @Override
    public String toString() {
        Object cl = null;
        if (this.clref != null) {
            cl = this.clref.get();
        }
        return (cl == null) ? "<null>" : cl.toString();
    }
    
    @Override
    public InputStream openClassfile(final String classname) {
        final String cname = classname.replace('.', '/') + ".class";
        final ClassLoader cl = (ClassLoader)this.clref.get();
        if (cl == null) {
            return null;
        }
        return cl.getResourceAsStream(cname);
    }
    
    @Override
    public URL find(final String classname) {
        final String cname = classname.replace('.', '/') + ".class";
        final ClassLoader cl = (ClassLoader)this.clref.get();
        if (cl == null) {
            return null;
        }
        return cl.getResource(cname);
    }
    
    @Override
    public void close() {
        this.clref = null;
    }
}

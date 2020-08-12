// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ByteArrayClassPath implements ClassPath
{
    protected String classname;
    protected byte[] classfile;
    
    public ByteArrayClassPath(final String name, final byte[] classfile) {
        this.classname = name;
        this.classfile = classfile;
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public String toString() {
        return "byte[]:" + this.classname;
    }
    
    @Override
    public InputStream openClassfile(final String classname) {
        if (this.classname.equals(classname)) {
            return new ByteArrayInputStream(this.classfile);
        }
        return null;
    }
    
    @Override
    public URL find(final String classname) {
        if (this.classname.equals(classname)) {
            final String cname = classname.replace('.', '/') + ".class";
            try {
                return new URL("file:/ByteArrayClassPath/" + cname);
            }
            catch (MalformedURLException ex) {}
        }
        return null;
    }
}

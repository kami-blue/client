// 
// Decompiled by Procyon v0.5.36
// 

package javassist.util.proxy;

import java.io.ObjectStreamClass;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class ProxyObjectInputStream extends ObjectInputStream
{
    private ClassLoader loader;
    
    public ProxyObjectInputStream(final InputStream in) throws IOException {
        super(in);
        this.loader = Thread.currentThread().getContextClassLoader();
        if (this.loader == null) {
            this.loader = ClassLoader.getSystemClassLoader();
        }
    }
    
    public void setClassLoader(ClassLoader loader) {
        if (loader != null) {
            this.loader = loader;
        }
        else {
            loader = ClassLoader.getSystemClassLoader();
        }
    }
    
    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        final boolean isProxy = this.readBoolean();
        if (isProxy) {
            String name = (String)this.readObject();
            final Class superClass = this.loader.loadClass(name);
            int length = this.readInt();
            final Class[] interfaces = new Class[length];
            for (int i = 0; i < length; ++i) {
                name = (String)this.readObject();
                interfaces[i] = this.loader.loadClass(name);
            }
            length = this.readInt();
            final byte[] signature = new byte[length];
            this.read(signature);
            final ProxyFactory factory = new ProxyFactory();
            factory.setUseCache(true);
            factory.setUseWriteReplace(false);
            factory.setSuperclass(superClass);
            factory.setInterfaces(interfaces);
            final Class proxyClass = factory.createClass(signature);
            return ObjectStreamClass.lookup(proxyClass);
        }
        return super.readClassDescriptor();
    }
}

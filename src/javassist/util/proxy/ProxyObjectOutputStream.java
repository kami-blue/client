// 
// Decompiled by Procyon v0.5.36
// 

package javassist.util.proxy;

import java.io.ObjectStreamClass;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectOutputStream;

public class ProxyObjectOutputStream extends ObjectOutputStream
{
    public ProxyObjectOutputStream(final OutputStream out) throws IOException {
        super(out);
    }
    
    @Override
    protected void writeClassDescriptor(final ObjectStreamClass desc) throws IOException {
        final Class cl = desc.forClass();
        if (ProxyFactory.isProxyClass(cl)) {
            this.writeBoolean(true);
            final Class superClass = cl.getSuperclass();
            final Class[] interfaces = cl.getInterfaces();
            final byte[] signature = ProxyFactory.getFilterSignature(cl);
            String name = superClass.getName();
            this.writeObject(name);
            this.writeInt(interfaces.length - 1);
            for (int i = 0; i < interfaces.length; ++i) {
                final Class interfaze = interfaces[i];
                if (interfaze != ProxyObject.class && interfaze != Proxy.class) {
                    name = interfaces[i].getName();
                    this.writeObject(name);
                }
            }
            this.writeInt(signature.length);
            this.write(signature);
        }
        else {
            this.writeBoolean(false);
            super.writeClassDescriptor(desc);
        }
    }
}

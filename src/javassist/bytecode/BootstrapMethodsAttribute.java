// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

public class BootstrapMethodsAttribute extends AttributeInfo
{
    public static final String tag = "BootstrapMethods";
    
    BootstrapMethodsAttribute(final ConstPool cp, final int n, final DataInputStream in) throws IOException {
        super(cp, n, in);
    }
    
    public BootstrapMethodsAttribute(final ConstPool cp, final BootstrapMethod[] methods) {
        super(cp, "BootstrapMethods");
        int size = 2;
        for (int i = 0; i < methods.length; ++i) {
            size += 4 + methods[i].arguments.length * 2;
        }
        final byte[] data = new byte[size];
        ByteArray.write16bit(methods.length, data, 0);
        int pos = 2;
        for (int j = 0; j < methods.length; ++j) {
            ByteArray.write16bit(methods[j].methodRef, data, pos);
            ByteArray.write16bit(methods[j].arguments.length, data, pos + 2);
            final int[] args = methods[j].arguments;
            pos += 4;
            for (int k = 0; k < args.length; ++k) {
                ByteArray.write16bit(args[k], data, pos);
                pos += 2;
            }
        }
        this.set(data);
    }
    
    public BootstrapMethod[] getMethods() {
        final byte[] data = this.get();
        final int num = ByteArray.readU16bit(data, 0);
        final BootstrapMethod[] methods = new BootstrapMethod[num];
        int pos = 2;
        for (int i = 0; i < num; ++i) {
            final int ref = ByteArray.readU16bit(data, pos);
            final int len = ByteArray.readU16bit(data, pos + 2);
            final int[] args = new int[len];
            pos += 4;
            for (int k = 0; k < len; ++k) {
                args[k] = ByteArray.readU16bit(data, pos);
                pos += 2;
            }
            methods[i] = new BootstrapMethod(ref, args);
        }
        return methods;
    }
    
    @Override
    public AttributeInfo copy(final ConstPool newCp, final Map classnames) {
        final BootstrapMethod[] methods = this.getMethods();
        final ConstPool thisCp = this.getConstPool();
        for (int i = 0; i < methods.length; ++i) {
            final BootstrapMethod m = methods[i];
            m.methodRef = thisCp.copy(m.methodRef, newCp, classnames);
            for (int k = 0; k < m.arguments.length; ++k) {
                m.arguments[k] = thisCp.copy(m.arguments[k], newCp, classnames);
            }
        }
        return new BootstrapMethodsAttribute(newCp, methods);
    }
    
    public static class BootstrapMethod
    {
        public int methodRef;
        public int[] arguments;
        
        public BootstrapMethod(final int method, final int[] args) {
            this.methodRef = method;
            this.arguments = args;
        }
    }
}

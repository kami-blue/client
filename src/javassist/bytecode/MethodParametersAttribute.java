// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

public class MethodParametersAttribute extends AttributeInfo
{
    public static final String tag = "MethodParameters";
    
    MethodParametersAttribute(final ConstPool cp, final int n, final DataInputStream in) throws IOException {
        super(cp, n, in);
    }
    
    public MethodParametersAttribute(final ConstPool cp, final String[] names, final int[] flags) {
        super(cp, "MethodParameters");
        final byte[] data = new byte[names.length * 4 + 1];
        data[0] = (byte)names.length;
        for (int i = 0; i < names.length; ++i) {
            ByteArray.write16bit(cp.addUtf8Info(names[i]), data, i * 4 + 1);
            ByteArray.write16bit(flags[i], data, i * 4 + 3);
        }
        this.set(data);
    }
    
    public int size() {
        return this.info[0] & 0xFF;
    }
    
    public int name(final int i) {
        return ByteArray.readU16bit(this.info, i * 4 + 1);
    }
    
    public int accessFlags(final int i) {
        return ByteArray.readU16bit(this.info, i * 4 + 3);
    }
    
    @Override
    public AttributeInfo copy(final ConstPool newCp, final Map classnames) {
        final int s = this.size();
        final ConstPool cp = this.getConstPool();
        final String[] names = new String[s];
        final int[] flags = new int[s];
        for (int i = 0; i < s; ++i) {
            names[i] = cp.getUtf8Info(this.name(i));
            flags[i] = this.accessFlags(i);
        }
        return new MethodParametersAttribute(newCp, names, flags);
    }
}

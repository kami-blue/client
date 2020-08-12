// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

class LongInfo extends ConstInfo
{
    static final int tag = 5;
    long value;
    
    public LongInfo(final long l, final int index) {
        super(index);
        this.value = l;
    }
    
    public LongInfo(final DataInputStream in, final int index) throws IOException {
        super(index);
        this.value = in.readLong();
    }
    
    @Override
    public int hashCode() {
        return (int)(this.value ^ this.value >>> 32);
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof LongInfo && ((LongInfo)obj).value == this.value;
    }
    
    @Override
    public int getTag() {
        return 5;
    }
    
    @Override
    public int copy(final ConstPool src, final ConstPool dest, final Map map) {
        return dest.addLongInfo(this.value);
    }
    
    @Override
    public void write(final DataOutputStream out) throws IOException {
        out.writeByte(5);
        out.writeLong(this.value);
    }
    
    @Override
    public void print(final PrintWriter out) {
        out.print("Long ");
        out.println(this.value);
    }
}

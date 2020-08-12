// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

class IntegerInfo extends ConstInfo
{
    static final int tag = 3;
    int value;
    
    public IntegerInfo(final int v, final int index) {
        super(index);
        this.value = v;
    }
    
    public IntegerInfo(final DataInputStream in, final int index) throws IOException {
        super(index);
        this.value = in.readInt();
    }
    
    @Override
    public int hashCode() {
        return this.value;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof IntegerInfo && ((IntegerInfo)obj).value == this.value;
    }
    
    @Override
    public int getTag() {
        return 3;
    }
    
    @Override
    public int copy(final ConstPool src, final ConstPool dest, final Map map) {
        return dest.addIntegerInfo(this.value);
    }
    
    @Override
    public void write(final DataOutputStream out) throws IOException {
        out.writeByte(3);
        out.writeInt(this.value);
    }
    
    @Override
    public void print(final PrintWriter out) {
        out.print("Integer ");
        out.println(this.value);
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

class FloatInfo extends ConstInfo
{
    static final int tag = 4;
    float value;
    
    public FloatInfo(final float f, final int index) {
        super(index);
        this.value = f;
    }
    
    public FloatInfo(final DataInputStream in, final int index) throws IOException {
        super(index);
        this.value = in.readFloat();
    }
    
    @Override
    public int hashCode() {
        return Float.floatToIntBits(this.value);
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof FloatInfo && ((FloatInfo)obj).value == this.value;
    }
    
    @Override
    public int getTag() {
        return 4;
    }
    
    @Override
    public int copy(final ConstPool src, final ConstPool dest, final Map map) {
        return dest.addFloatInfo(this.value);
    }
    
    @Override
    public void write(final DataOutputStream out) throws IOException {
        out.writeByte(4);
        out.writeFloat(this.value);
    }
    
    @Override
    public void print(final PrintWriter out) {
        out.print("Float ");
        out.println(this.value);
    }
}

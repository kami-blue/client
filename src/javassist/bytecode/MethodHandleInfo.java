// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

class MethodHandleInfo extends ConstInfo
{
    static final int tag = 15;
    int refKind;
    int refIndex;
    
    public MethodHandleInfo(final int kind, final int referenceIndex, final int index) {
        super(index);
        this.refKind = kind;
        this.refIndex = referenceIndex;
    }
    
    public MethodHandleInfo(final DataInputStream in, final int index) throws IOException {
        super(index);
        this.refKind = in.readUnsignedByte();
        this.refIndex = in.readUnsignedShort();
    }
    
    @Override
    public int hashCode() {
        return this.refKind << 16 ^ this.refIndex;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof MethodHandleInfo) {
            final MethodHandleInfo mh = (MethodHandleInfo)obj;
            return mh.refKind == this.refKind && mh.refIndex == this.refIndex;
        }
        return false;
    }
    
    @Override
    public int getTag() {
        return 15;
    }
    
    @Override
    public int copy(final ConstPool src, final ConstPool dest, final Map map) {
        return dest.addMethodHandleInfo(this.refKind, src.getItem(this.refIndex).copy(src, dest, map));
    }
    
    @Override
    public void write(final DataOutputStream out) throws IOException {
        out.writeByte(15);
        out.writeByte(this.refKind);
        out.writeShort(this.refIndex);
    }
    
    @Override
    public void print(final PrintWriter out) {
        out.print("MethodHandle #");
        out.print(this.refKind);
        out.print(", index #");
        out.println(this.refIndex);
    }
}

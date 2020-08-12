// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

abstract class MemberrefInfo extends ConstInfo
{
    int classIndex;
    int nameAndTypeIndex;
    
    public MemberrefInfo(final int cindex, final int ntindex, final int thisIndex) {
        super(thisIndex);
        this.classIndex = cindex;
        this.nameAndTypeIndex = ntindex;
    }
    
    public MemberrefInfo(final DataInputStream in, final int thisIndex) throws IOException {
        super(thisIndex);
        this.classIndex = in.readUnsignedShort();
        this.nameAndTypeIndex = in.readUnsignedShort();
    }
    
    @Override
    public int hashCode() {
        return this.classIndex << 16 ^ this.nameAndTypeIndex;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof MemberrefInfo) {
            final MemberrefInfo mri = (MemberrefInfo)obj;
            return mri.classIndex == this.classIndex && mri.nameAndTypeIndex == this.nameAndTypeIndex && mri.getClass() == this.getClass();
        }
        return false;
    }
    
    @Override
    public int copy(final ConstPool src, final ConstPool dest, final Map map) {
        final int classIndex2 = src.getItem(this.classIndex).copy(src, dest, map);
        final int ntIndex2 = src.getItem(this.nameAndTypeIndex).copy(src, dest, map);
        return this.copy2(dest, classIndex2, ntIndex2);
    }
    
    protected abstract int copy2(final ConstPool p0, final int p1, final int p2);
    
    @Override
    public void write(final DataOutputStream out) throws IOException {
        out.writeByte(this.getTag());
        out.writeShort(this.classIndex);
        out.writeShort(this.nameAndTypeIndex);
    }
    
    @Override
    public void print(final PrintWriter out) {
        out.print(this.getTagName() + " #");
        out.print(this.classIndex);
        out.print(", name&type #");
        out.println(this.nameAndTypeIndex);
    }
    
    public abstract String getTagName();
}

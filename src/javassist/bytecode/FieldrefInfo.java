// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.IOException;
import java.io.DataInputStream;

class FieldrefInfo extends MemberrefInfo
{
    static final int tag = 9;
    
    public FieldrefInfo(final int cindex, final int ntindex, final int thisIndex) {
        super(cindex, ntindex, thisIndex);
    }
    
    public FieldrefInfo(final DataInputStream in, final int thisIndex) throws IOException {
        super(in, thisIndex);
    }
    
    @Override
    public int getTag() {
        return 9;
    }
    
    @Override
    public String getTagName() {
        return "Field";
    }
    
    @Override
    protected int copy2(final ConstPool dest, final int cindex, final int ntindex) {
        return dest.addFieldrefInfo(cindex, ntindex);
    }
}

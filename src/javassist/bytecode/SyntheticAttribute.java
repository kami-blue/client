// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

public class SyntheticAttribute extends AttributeInfo
{
    public static final String tag = "Synthetic";
    
    SyntheticAttribute(final ConstPool cp, final int n, final DataInputStream in) throws IOException {
        super(cp, n, in);
    }
    
    public SyntheticAttribute(final ConstPool cp) {
        super(cp, "Synthetic", new byte[0]);
    }
    
    @Override
    public AttributeInfo copy(final ConstPool newCp, final Map classnames) {
        return new SyntheticAttribute(newCp);
    }
}

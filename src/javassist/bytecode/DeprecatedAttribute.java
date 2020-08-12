// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

public class DeprecatedAttribute extends AttributeInfo
{
    public static final String tag = "Deprecated";
    
    DeprecatedAttribute(final ConstPool cp, final int n, final DataInputStream in) throws IOException {
        super(cp, n, in);
    }
    
    public DeprecatedAttribute(final ConstPool cp) {
        super(cp, "Deprecated", new byte[0]);
    }
    
    @Override
    public AttributeInfo copy(final ConstPool newCp, final Map classnames) {
        return new DeprecatedAttribute(newCp);
    }
}

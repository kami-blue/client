// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

public class LineNumberAttribute extends AttributeInfo
{
    public static final String tag = "LineNumberTable";
    
    LineNumberAttribute(final ConstPool cp, final int n, final DataInputStream in) throws IOException {
        super(cp, n, in);
    }
    
    private LineNumberAttribute(final ConstPool cp, final byte[] i) {
        super(cp, "LineNumberTable", i);
    }
    
    public int tableLength() {
        return ByteArray.readU16bit(this.info, 0);
    }
    
    public int startPc(final int i) {
        return ByteArray.readU16bit(this.info, i * 4 + 2);
    }
    
    public int lineNumber(final int i) {
        return ByteArray.readU16bit(this.info, i * 4 + 4);
    }
    
    public int toLineNumber(final int pc) {
        final int n = this.tableLength();
        int i = 0;
        while (i < n) {
            if (pc < this.startPc(i)) {
                if (i == 0) {
                    return this.lineNumber(0);
                }
                break;
            }
            else {
                ++i;
            }
        }
        return this.lineNumber(i - 1);
    }
    
    public int toStartPc(final int line) {
        for (int n = this.tableLength(), i = 0; i < n; ++i) {
            if (line == this.lineNumber(i)) {
                return this.startPc(i);
            }
        }
        return -1;
    }
    
    public Pc toNearPc(final int line) {
        final int n = this.tableLength();
        int nearPc = 0;
        int distance = 0;
        if (n > 0) {
            distance = this.lineNumber(0) - line;
            nearPc = this.startPc(0);
        }
        for (int i = 1; i < n; ++i) {
            final int d = this.lineNumber(i) - line;
            if ((d < 0 && d > distance) || (d >= 0 && (d < distance || distance < 0))) {
                distance = d;
                nearPc = this.startPc(i);
            }
        }
        final Pc res = new Pc();
        res.index = nearPc;
        res.line = line + distance;
        return res;
    }
    
    @Override
    public AttributeInfo copy(final ConstPool newCp, final Map classnames) {
        final byte[] src = this.info;
        final int num = src.length;
        final byte[] dest = new byte[num];
        for (int i = 0; i < num; ++i) {
            dest[i] = src[i];
        }
        final LineNumberAttribute attr = new LineNumberAttribute(newCp, dest);
        return attr;
    }
    
    void shiftPc(final int where, final int gapLength, final boolean exclusive) {
        for (int n = this.tableLength(), i = 0; i < n; ++i) {
            final int pos = i * 4 + 2;
            final int pc = ByteArray.readU16bit(this.info, pos);
            if (pc > where || (exclusive && pc == where)) {
                ByteArray.write16bit(pc + gapLength, this.info, pos);
            }
        }
    }
    
    public static class Pc
    {
        public int index;
        public int line;
    }
}

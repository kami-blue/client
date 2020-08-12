// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

public class ExceptionsAttribute extends AttributeInfo
{
    public static final String tag = "Exceptions";
    
    ExceptionsAttribute(final ConstPool cp, final int n, final DataInputStream in) throws IOException {
        super(cp, n, in);
    }
    
    private ExceptionsAttribute(final ConstPool cp, final ExceptionsAttribute src, final Map classnames) {
        super(cp, "Exceptions");
        this.copyFrom(src, classnames);
    }
    
    public ExceptionsAttribute(final ConstPool cp) {
        super(cp, "Exceptions");
        final byte[] data = new byte[2];
        data[0] = (data[1] = 0);
        this.info = data;
    }
    
    @Override
    public AttributeInfo copy(final ConstPool newCp, final Map classnames) {
        return new ExceptionsAttribute(newCp, this, classnames);
    }
    
    private void copyFrom(final ExceptionsAttribute srcAttr, final Map classnames) {
        final ConstPool srcCp = srcAttr.constPool;
        final ConstPool destCp = this.constPool;
        final byte[] src = srcAttr.info;
        final int num = src.length;
        final byte[] dest = new byte[num];
        dest[0] = src[0];
        dest[1] = src[1];
        for (int i = 2; i < num; i += 2) {
            final int index = ByteArray.readU16bit(src, i);
            ByteArray.write16bit(srcCp.copy(index, destCp, classnames), dest, i);
        }
        this.info = dest;
    }
    
    public int[] getExceptionIndexes() {
        final byte[] blist = this.info;
        final int n = blist.length;
        if (n <= 2) {
            return null;
        }
        final int[] elist = new int[n / 2 - 1];
        int k = 0;
        for (int j = 2; j < n; j += 2) {
            elist[k++] = ((blist[j] & 0xFF) << 8 | (blist[j + 1] & 0xFF));
        }
        return elist;
    }
    
    public String[] getExceptions() {
        final byte[] blist = this.info;
        final int n = blist.length;
        if (n <= 2) {
            return null;
        }
        final String[] elist = new String[n / 2 - 1];
        int k = 0;
        for (int j = 2; j < n; j += 2) {
            final int index = (blist[j] & 0xFF) << 8 | (blist[j + 1] & 0xFF);
            elist[k++] = this.constPool.getClassInfo(index);
        }
        return elist;
    }
    
    public void setExceptionIndexes(final int[] elist) {
        final int n = elist.length;
        final byte[] blist = new byte[n * 2 + 2];
        ByteArray.write16bit(n, blist, 0);
        for (int i = 0; i < n; ++i) {
            ByteArray.write16bit(elist[i], blist, i * 2 + 2);
        }
        this.info = blist;
    }
    
    public void setExceptions(final String[] elist) {
        final int n = elist.length;
        final byte[] blist = new byte[n * 2 + 2];
        ByteArray.write16bit(n, blist, 0);
        for (int i = 0; i < n; ++i) {
            ByteArray.write16bit(this.constPool.addClassInfo(elist[i]), blist, i * 2 + 2);
        }
        this.info = blist;
    }
    
    public int tableLength() {
        return this.info.length / 2 - 1;
    }
    
    public int getException(final int nth) {
        final int index = nth * 2 + 2;
        return (this.info[index] & 0xFF) << 8 | (this.info[index + 1] & 0xFF);
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

public class InnerClassesAttribute extends AttributeInfo
{
    public static final String tag = "InnerClasses";
    
    InnerClassesAttribute(final ConstPool cp, final int n, final DataInputStream in) throws IOException {
        super(cp, n, in);
    }
    
    private InnerClassesAttribute(final ConstPool cp, final byte[] info) {
        super(cp, "InnerClasses", info);
    }
    
    public InnerClassesAttribute(final ConstPool cp) {
        super(cp, "InnerClasses", new byte[2]);
        ByteArray.write16bit(0, this.get(), 0);
    }
    
    public int tableLength() {
        return ByteArray.readU16bit(this.get(), 0);
    }
    
    public int innerClassIndex(final int nth) {
        return ByteArray.readU16bit(this.get(), nth * 8 + 2);
    }
    
    public String innerClass(final int nth) {
        final int i = this.innerClassIndex(nth);
        if (i == 0) {
            return null;
        }
        return this.constPool.getClassInfo(i);
    }
    
    public void setInnerClassIndex(final int nth, final int index) {
        ByteArray.write16bit(index, this.get(), nth * 8 + 2);
    }
    
    public int outerClassIndex(final int nth) {
        return ByteArray.readU16bit(this.get(), nth * 8 + 4);
    }
    
    public String outerClass(final int nth) {
        final int i = this.outerClassIndex(nth);
        if (i == 0) {
            return null;
        }
        return this.constPool.getClassInfo(i);
    }
    
    public void setOuterClassIndex(final int nth, final int index) {
        ByteArray.write16bit(index, this.get(), nth * 8 + 4);
    }
    
    public int innerNameIndex(final int nth) {
        return ByteArray.readU16bit(this.get(), nth * 8 + 6);
    }
    
    public String innerName(final int nth) {
        final int i = this.innerNameIndex(nth);
        if (i == 0) {
            return null;
        }
        return this.constPool.getUtf8Info(i);
    }
    
    public void setInnerNameIndex(final int nth, final int index) {
        ByteArray.write16bit(index, this.get(), nth * 8 + 6);
    }
    
    public int accessFlags(final int nth) {
        return ByteArray.readU16bit(this.get(), nth * 8 + 8);
    }
    
    public void setAccessFlags(final int nth, final int flags) {
        ByteArray.write16bit(flags, this.get(), nth * 8 + 8);
    }
    
    public void append(final String inner, final String outer, final String name, final int flags) {
        final int i = this.constPool.addClassInfo(inner);
        final int o = this.constPool.addClassInfo(outer);
        final int n = this.constPool.addUtf8Info(name);
        this.append(i, o, n, flags);
    }
    
    public void append(final int inner, final int outer, final int name, final int flags) {
        final byte[] data = this.get();
        final int len = data.length;
        final byte[] newData = new byte[len + 8];
        for (int i = 2; i < len; ++i) {
            newData[i] = data[i];
        }
        final int n = ByteArray.readU16bit(data, 0);
        ByteArray.write16bit(n + 1, newData, 0);
        ByteArray.write16bit(inner, newData, len);
        ByteArray.write16bit(outer, newData, len + 2);
        ByteArray.write16bit(name, newData, len + 4);
        ByteArray.write16bit(flags, newData, len + 6);
        this.set(newData);
    }
    
    public int remove(final int nth) {
        final byte[] data = this.get();
        final int len = data.length;
        if (len < 10) {
            return 0;
        }
        final int n = ByteArray.readU16bit(data, 0);
        final int nthPos = 2 + nth * 8;
        if (n <= nth) {
            return n;
        }
        final byte[] newData = new byte[len - 8];
        ByteArray.write16bit(n - 1, newData, 0);
        int i = 2;
        int j = 2;
        while (i < len) {
            if (i == nthPos) {
                i += 8;
            }
            else {
                newData[j++] = data[i++];
            }
        }
        this.set(newData);
        return n - 1;
    }
    
    @Override
    public AttributeInfo copy(final ConstPool newCp, final Map classnames) {
        final byte[] src = this.get();
        final byte[] dest = new byte[src.length];
        final ConstPool cp = this.getConstPool();
        final InnerClassesAttribute attr = new InnerClassesAttribute(newCp, dest);
        final int n = ByteArray.readU16bit(src, 0);
        ByteArray.write16bit(n, dest, 0);
        int j = 2;
        for (int i = 0; i < n; ++i) {
            int innerClass = ByteArray.readU16bit(src, j);
            int outerClass = ByteArray.readU16bit(src, j + 2);
            int innerName = ByteArray.readU16bit(src, j + 4);
            final int innerAccess = ByteArray.readU16bit(src, j + 6);
            if (innerClass != 0) {
                innerClass = cp.copy(innerClass, newCp, classnames);
            }
            ByteArray.write16bit(innerClass, dest, j);
            if (outerClass != 0) {
                outerClass = cp.copy(outerClass, newCp, classnames);
            }
            ByteArray.write16bit(outerClass, dest, j + 2);
            if (innerName != 0) {
                innerName = cp.copy(innerName, newCp, classnames);
            }
            ByteArray.write16bit(innerName, dest, j + 4);
            ByteArray.write16bit(innerAccess, dest, j + 6);
            j += 8;
        }
        return attr;
    }
}

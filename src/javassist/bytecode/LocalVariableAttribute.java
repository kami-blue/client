// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

public class LocalVariableAttribute extends AttributeInfo
{
    public static final String tag = "LocalVariableTable";
    public static final String typeTag = "LocalVariableTypeTable";
    
    public LocalVariableAttribute(final ConstPool cp) {
        super(cp, "LocalVariableTable", new byte[2]);
        ByteArray.write16bit(0, this.info, 0);
    }
    
    @Deprecated
    public LocalVariableAttribute(final ConstPool cp, final String name) {
        super(cp, name, new byte[2]);
        ByteArray.write16bit(0, this.info, 0);
    }
    
    LocalVariableAttribute(final ConstPool cp, final int n, final DataInputStream in) throws IOException {
        super(cp, n, in);
    }
    
    LocalVariableAttribute(final ConstPool cp, final String name, final byte[] i) {
        super(cp, name, i);
    }
    
    public void addEntry(final int startPc, final int length, final int nameIndex, final int descriptorIndex, final int index) {
        final int size = this.info.length;
        final byte[] newInfo = new byte[size + 10];
        ByteArray.write16bit(this.tableLength() + 1, newInfo, 0);
        for (int i = 2; i < size; ++i) {
            newInfo[i] = this.info[i];
        }
        ByteArray.write16bit(startPc, newInfo, size);
        ByteArray.write16bit(length, newInfo, size + 2);
        ByteArray.write16bit(nameIndex, newInfo, size + 4);
        ByteArray.write16bit(descriptorIndex, newInfo, size + 6);
        ByteArray.write16bit(index, newInfo, size + 8);
        this.info = newInfo;
    }
    
    @Override
    void renameClass(final String oldname, final String newname) {
        final ConstPool cp = this.getConstPool();
        for (int n = this.tableLength(), i = 0; i < n; ++i) {
            final int pos = i * 10 + 2;
            final int index = ByteArray.readU16bit(this.info, pos + 6);
            if (index != 0) {
                String desc = cp.getUtf8Info(index);
                desc = this.renameEntry(desc, oldname, newname);
                ByteArray.write16bit(cp.addUtf8Info(desc), this.info, pos + 6);
            }
        }
    }
    
    String renameEntry(final String desc, final String oldname, final String newname) {
        return Descriptor.rename(desc, oldname, newname);
    }
    
    @Override
    void renameClass(final Map classnames) {
        final ConstPool cp = this.getConstPool();
        for (int n = this.tableLength(), i = 0; i < n; ++i) {
            final int pos = i * 10 + 2;
            final int index = ByteArray.readU16bit(this.info, pos + 6);
            if (index != 0) {
                String desc = cp.getUtf8Info(index);
                desc = this.renameEntry(desc, classnames);
                ByteArray.write16bit(cp.addUtf8Info(desc), this.info, pos + 6);
            }
        }
    }
    
    String renameEntry(final String desc, final Map classnames) {
        return Descriptor.rename(desc, classnames);
    }
    
    public void shiftIndex(final int lessThan, final int delta) {
        for (int size = this.info.length, i = 2; i < size; i += 10) {
            final int org = ByteArray.readU16bit(this.info, i + 8);
            if (org >= lessThan) {
                ByteArray.write16bit(org + delta, this.info, i + 8);
            }
        }
    }
    
    public int tableLength() {
        return ByteArray.readU16bit(this.info, 0);
    }
    
    public int startPc(final int i) {
        return ByteArray.readU16bit(this.info, i * 10 + 2);
    }
    
    public int codeLength(final int i) {
        return ByteArray.readU16bit(this.info, i * 10 + 4);
    }
    
    void shiftPc(final int where, final int gapLength, final boolean exclusive) {
        for (int n = this.tableLength(), i = 0; i < n; ++i) {
            final int pos = i * 10 + 2;
            final int pc = ByteArray.readU16bit(this.info, pos);
            final int len = ByteArray.readU16bit(this.info, pos + 2);
            if (pc > where || (exclusive && pc == where && pc != 0)) {
                ByteArray.write16bit(pc + gapLength, this.info, pos);
            }
            else if (pc + len > where || (exclusive && pc + len == where)) {
                ByteArray.write16bit(len + gapLength, this.info, pos + 2);
            }
        }
    }
    
    public int nameIndex(final int i) {
        return ByteArray.readU16bit(this.info, i * 10 + 6);
    }
    
    public String variableName(final int i) {
        return this.getConstPool().getUtf8Info(this.nameIndex(i));
    }
    
    public int descriptorIndex(final int i) {
        return ByteArray.readU16bit(this.info, i * 10 + 8);
    }
    
    public int signatureIndex(final int i) {
        return this.descriptorIndex(i);
    }
    
    public String descriptor(final int i) {
        return this.getConstPool().getUtf8Info(this.descriptorIndex(i));
    }
    
    public String signature(final int i) {
        return this.descriptor(i);
    }
    
    public int index(final int i) {
        return ByteArray.readU16bit(this.info, i * 10 + 10);
    }
    
    @Override
    public AttributeInfo copy(final ConstPool newCp, final Map classnames) {
        final byte[] src = this.get();
        final byte[] dest = new byte[src.length];
        final ConstPool cp = this.getConstPool();
        final LocalVariableAttribute attr = this.makeThisAttr(newCp, dest);
        final int n = ByteArray.readU16bit(src, 0);
        ByteArray.write16bit(n, dest, 0);
        int j = 2;
        for (int i = 0; i < n; ++i) {
            final int start = ByteArray.readU16bit(src, j);
            final int len = ByteArray.readU16bit(src, j + 2);
            int name = ByteArray.readU16bit(src, j + 4);
            int type = ByteArray.readU16bit(src, j + 6);
            final int index = ByteArray.readU16bit(src, j + 8);
            ByteArray.write16bit(start, dest, j);
            ByteArray.write16bit(len, dest, j + 2);
            if (name != 0) {
                name = cp.copy(name, newCp, null);
            }
            ByteArray.write16bit(name, dest, j + 4);
            if (type != 0) {
                String sig = cp.getUtf8Info(type);
                sig = Descriptor.rename(sig, classnames);
                type = newCp.addUtf8Info(sig);
            }
            ByteArray.write16bit(type, dest, j + 6);
            ByteArray.write16bit(index, dest, j + 8);
            j += 10;
        }
        return attr;
    }
    
    LocalVariableAttribute makeThisAttr(final ConstPool cp, final byte[] dest) {
        return new LocalVariableAttribute(cp, "LocalVariableTable", dest);
    }
}

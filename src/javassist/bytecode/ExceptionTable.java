// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.DataOutputStream;
import java.util.Map;
import java.util.Collection;
import java.io.IOException;
import java.io.DataInputStream;
import java.util.ArrayList;

public class ExceptionTable implements Cloneable
{
    private ConstPool constPool;
    private ArrayList entries;
    
    public ExceptionTable(final ConstPool cp) {
        this.constPool = cp;
        this.entries = new ArrayList();
    }
    
    ExceptionTable(final ConstPool cp, final DataInputStream in) throws IOException {
        this.constPool = cp;
        final int length = in.readUnsignedShort();
        final ArrayList list = new ArrayList(length);
        for (int i = 0; i < length; ++i) {
            final int start = in.readUnsignedShort();
            final int end = in.readUnsignedShort();
            final int handle = in.readUnsignedShort();
            final int type = in.readUnsignedShort();
            list.add(new ExceptionTableEntry(start, end, handle, type));
        }
        this.entries = list;
    }
    
    public Object clone() throws CloneNotSupportedException {
        final ExceptionTable r = (ExceptionTable)super.clone();
        r.entries = new ArrayList(this.entries);
        return r;
    }
    
    public int size() {
        return this.entries.size();
    }
    
    public int startPc(final int nth) {
        final ExceptionTableEntry e = this.entries.get(nth);
        return e.startPc;
    }
    
    public void setStartPc(final int nth, final int value) {
        final ExceptionTableEntry e = this.entries.get(nth);
        e.startPc = value;
    }
    
    public int endPc(final int nth) {
        final ExceptionTableEntry e = this.entries.get(nth);
        return e.endPc;
    }
    
    public void setEndPc(final int nth, final int value) {
        final ExceptionTableEntry e = this.entries.get(nth);
        e.endPc = value;
    }
    
    public int handlerPc(final int nth) {
        final ExceptionTableEntry e = this.entries.get(nth);
        return e.handlerPc;
    }
    
    public void setHandlerPc(final int nth, final int value) {
        final ExceptionTableEntry e = this.entries.get(nth);
        e.handlerPc = value;
    }
    
    public int catchType(final int nth) {
        final ExceptionTableEntry e = this.entries.get(nth);
        return e.catchType;
    }
    
    public void setCatchType(final int nth, final int value) {
        final ExceptionTableEntry e = this.entries.get(nth);
        e.catchType = value;
    }
    
    public void add(final int index, final ExceptionTable table, final int offset) {
        int len = table.size();
        while (--len >= 0) {
            final ExceptionTableEntry e = table.entries.get(len);
            this.add(index, e.startPc + offset, e.endPc + offset, e.handlerPc + offset, e.catchType);
        }
    }
    
    public void add(final int index, final int start, final int end, final int handler, final int type) {
        if (start < end) {
            this.entries.add(index, new ExceptionTableEntry(start, end, handler, type));
        }
    }
    
    public void add(final int start, final int end, final int handler, final int type) {
        if (start < end) {
            this.entries.add(new ExceptionTableEntry(start, end, handler, type));
        }
    }
    
    public void remove(final int index) {
        this.entries.remove(index);
    }
    
    public ExceptionTable copy(final ConstPool newCp, final Map classnames) {
        final ExceptionTable et = new ExceptionTable(newCp);
        final ConstPool srcCp = this.constPool;
        for (int len = this.size(), i = 0; i < len; ++i) {
            final ExceptionTableEntry e = this.entries.get(i);
            final int type = srcCp.copy(e.catchType, newCp, classnames);
            et.add(e.startPc, e.endPc, e.handlerPc, type);
        }
        return et;
    }
    
    void shiftPc(final int where, final int gapLength, final boolean exclusive) {
        for (int len = this.size(), i = 0; i < len; ++i) {
            final ExceptionTableEntry e = this.entries.get(i);
            e.startPc = shiftPc(e.startPc, where, gapLength, exclusive);
            e.endPc = shiftPc(e.endPc, where, gapLength, exclusive);
            e.handlerPc = shiftPc(e.handlerPc, where, gapLength, exclusive);
        }
    }
    
    private static int shiftPc(int pc, final int where, final int gapLength, final boolean exclusive) {
        if (pc > where || (exclusive && pc == where)) {
            pc += gapLength;
        }
        return pc;
    }
    
    void write(final DataOutputStream out) throws IOException {
        final int len = this.size();
        out.writeShort(len);
        for (int i = 0; i < len; ++i) {
            final ExceptionTableEntry e = this.entries.get(i);
            out.writeShort(e.startPc);
            out.writeShort(e.endPc);
            out.writeShort(e.handlerPc);
            out.writeShort(e.catchType);
        }
    }
}

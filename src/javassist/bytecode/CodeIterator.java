// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.util.ArrayList;

public class CodeIterator implements Opcode
{
    protected CodeAttribute codeAttr;
    protected byte[] bytecode;
    protected int endPos;
    protected int currentPos;
    protected int mark;
    private static final int[] opcodeLength;
    
    protected CodeIterator(final CodeAttribute ca) {
        this.codeAttr = ca;
        this.bytecode = ca.getCode();
        this.begin();
    }
    
    public void begin() {
        final int n = 0;
        this.mark = n;
        this.currentPos = n;
        this.endPos = this.getCodeLength();
    }
    
    public void move(final int index) {
        this.currentPos = index;
    }
    
    public void setMark(final int index) {
        this.mark = index;
    }
    
    public int getMark() {
        return this.mark;
    }
    
    public CodeAttribute get() {
        return this.codeAttr;
    }
    
    public int getCodeLength() {
        return this.bytecode.length;
    }
    
    public int byteAt(final int index) {
        return this.bytecode[index] & 0xFF;
    }
    
    public int signedByteAt(final int index) {
        return this.bytecode[index];
    }
    
    public void writeByte(final int value, final int index) {
        this.bytecode[index] = (byte)value;
    }
    
    public int u16bitAt(final int index) {
        return ByteArray.readU16bit(this.bytecode, index);
    }
    
    public int s16bitAt(final int index) {
        return ByteArray.readS16bit(this.bytecode, index);
    }
    
    public void write16bit(final int value, final int index) {
        ByteArray.write16bit(value, this.bytecode, index);
    }
    
    public int s32bitAt(final int index) {
        return ByteArray.read32bit(this.bytecode, index);
    }
    
    public void write32bit(final int value, final int index) {
        ByteArray.write32bit(value, this.bytecode, index);
    }
    
    public void write(final byte[] code, int index) {
        for (int len = code.length, j = 0; j < len; ++j) {
            this.bytecode[index++] = code[j];
        }
    }
    
    public boolean hasNext() {
        return this.currentPos < this.endPos;
    }
    
    public int next() throws BadBytecode {
        final int pos = this.currentPos;
        this.currentPos = nextOpcode(this.bytecode, pos);
        return pos;
    }
    
    public int lookAhead() {
        return this.currentPos;
    }
    
    public int skipConstructor() throws BadBytecode {
        return this.skipSuperConstructor0(-1);
    }
    
    public int skipSuperConstructor() throws BadBytecode {
        return this.skipSuperConstructor0(0);
    }
    
    public int skipThisConstructor() throws BadBytecode {
        return this.skipSuperConstructor0(1);
    }
    
    private int skipSuperConstructor0(final int skipThis) throws BadBytecode {
        this.begin();
        final ConstPool cp = this.codeAttr.getConstPool();
        final String thisClassName = this.codeAttr.getDeclaringClass();
        int nested = 0;
        while (this.hasNext()) {
            final int index = this.next();
            final int c = this.byteAt(index);
            if (c == 187) {
                ++nested;
            }
            else {
                if (c != 183) {
                    continue;
                }
                final int mref = ByteArray.readU16bit(this.bytecode, index + 1);
                if (!cp.getMethodrefName(mref).equals("<init>") || --nested >= 0) {
                    continue;
                }
                if (skipThis < 0) {
                    return index;
                }
                final String cname = cp.getMethodrefClassName(mref);
                if (cname.equals(thisClassName) == skipThis > 0) {
                    return index;
                }
                break;
            }
        }
        this.begin();
        return -1;
    }
    
    public int insert(final byte[] code) throws BadBytecode {
        return this.insert0(this.currentPos, code, false);
    }
    
    public void insert(final int pos, final byte[] code) throws BadBytecode {
        this.insert0(pos, code, false);
    }
    
    public int insertAt(final int pos, final byte[] code) throws BadBytecode {
        return this.insert0(pos, code, false);
    }
    
    public int insertEx(final byte[] code) throws BadBytecode {
        return this.insert0(this.currentPos, code, true);
    }
    
    public void insertEx(final int pos, final byte[] code) throws BadBytecode {
        this.insert0(pos, code, true);
    }
    
    public int insertExAt(final int pos, final byte[] code) throws BadBytecode {
        return this.insert0(pos, code, true);
    }
    
    private int insert0(int pos, final byte[] code, final boolean exclusive) throws BadBytecode {
        final int len = code.length;
        if (len <= 0) {
            return pos;
        }
        int p;
        pos = (p = this.insertGapAt(pos, len, exclusive).position);
        for (int j = 0; j < len; ++j) {
            this.bytecode[p++] = code[j];
        }
        return pos;
    }
    
    public int insertGap(final int length) throws BadBytecode {
        return this.insertGapAt(this.currentPos, length, false).position;
    }
    
    public int insertGap(final int pos, final int length) throws BadBytecode {
        return this.insertGapAt(pos, length, false).length;
    }
    
    public int insertExGap(final int length) throws BadBytecode {
        return this.insertGapAt(this.currentPos, length, true).position;
    }
    
    public int insertExGap(final int pos, final int length) throws BadBytecode {
        return this.insertGapAt(pos, length, true).length;
    }
    
    public Gap insertGapAt(int pos, final int length, final boolean exclusive) throws BadBytecode {
        final Gap gap = new Gap();
        if (length <= 0) {
            gap.position = pos;
            gap.length = 0;
            return gap;
        }
        byte[] c;
        int length2;
        if (this.bytecode.length + length > 32767) {
            c = this.insertGapCore0w(this.bytecode, pos, length, exclusive, this.get().getExceptionTable(), this.codeAttr, gap);
            pos = gap.position;
            length2 = length;
        }
        else {
            final int cur = this.currentPos;
            c = insertGapCore0(this.bytecode, pos, length, exclusive, this.get().getExceptionTable(), this.codeAttr);
            length2 = c.length - this.bytecode.length;
            gap.position = pos;
            gap.length = length2;
            if (cur >= pos) {
                this.currentPos = cur + length2;
            }
            if (this.mark > pos || (this.mark == pos && exclusive)) {
                this.mark += length2;
            }
        }
        this.codeAttr.setCode(c);
        this.bytecode = c;
        this.endPos = this.getCodeLength();
        this.updateCursors(pos, length2);
        return gap;
    }
    
    protected void updateCursors(final int pos, final int length) {
    }
    
    public void insert(final ExceptionTable et, final int offset) {
        this.codeAttr.getExceptionTable().add(0, et, offset);
    }
    
    public int append(final byte[] code) {
        final int size = this.getCodeLength();
        final int len = code.length;
        if (len <= 0) {
            return size;
        }
        this.appendGap(len);
        final byte[] dest = this.bytecode;
        for (int i = 0; i < len; ++i) {
            dest[i + size] = code[i];
        }
        return size;
    }
    
    public void appendGap(final int gapLength) {
        final byte[] code = this.bytecode;
        final int codeLength = code.length;
        final byte[] newcode = new byte[codeLength + gapLength];
        for (int i = 0; i < codeLength; ++i) {
            newcode[i] = code[i];
        }
        for (int i = codeLength; i < codeLength + gapLength; ++i) {
            newcode[i] = 0;
        }
        this.codeAttr.setCode(newcode);
        this.bytecode = newcode;
        this.endPos = this.getCodeLength();
    }
    
    public void append(final ExceptionTable et, final int offset) {
        final ExceptionTable table = this.codeAttr.getExceptionTable();
        table.add(table.size(), et, offset);
    }
    
    static int nextOpcode(final byte[] code, final int index) throws BadBytecode {
        int opcode;
        try {
            opcode = (code[index] & 0xFF);
        }
        catch (IndexOutOfBoundsException e) {
            throw new BadBytecode("invalid opcode address");
        }
        try {
            final int len = CodeIterator.opcodeLength[opcode];
            if (len > 0) {
                return index + len;
            }
            if (opcode == 196) {
                if (code[index + 1] == -124) {
                    return index + 6;
                }
                return index + 4;
            }
            else {
                final int index2 = (index & 0xFFFFFFFC) + 8;
                if (opcode == 171) {
                    final int npairs = ByteArray.read32bit(code, index2);
                    return index2 + npairs * 8 + 4;
                }
                if (opcode == 170) {
                    final int low = ByteArray.read32bit(code, index2);
                    final int high = ByteArray.read32bit(code, index2 + 4);
                    return index2 + (high - low + 1) * 4 + 8;
                }
            }
        }
        catch (IndexOutOfBoundsException ex) {}
        throw new BadBytecode(opcode);
    }
    
    static byte[] insertGapCore0(final byte[] code, final int where, final int gapLength, final boolean exclusive, final ExceptionTable etable, final CodeAttribute ca) throws BadBytecode {
        if (gapLength <= 0) {
            return code;
        }
        try {
            return insertGapCore1(code, where, gapLength, exclusive, etable, ca);
        }
        catch (AlignmentException e) {
            try {
                return insertGapCore1(code, where, gapLength + 3 & 0xFFFFFFFC, exclusive, etable, ca);
            }
            catch (AlignmentException e2) {
                throw new RuntimeException("fatal error?");
            }
        }
    }
    
    private static byte[] insertGapCore1(final byte[] code, final int where, final int gapLength, final boolean exclusive, final ExceptionTable etable, final CodeAttribute ca) throws BadBytecode, AlignmentException {
        final int codeLength = code.length;
        final byte[] newcode = new byte[codeLength + gapLength];
        insertGap2(code, where, gapLength, codeLength, newcode, exclusive);
        etable.shiftPc(where, gapLength, exclusive);
        final LineNumberAttribute na = (LineNumberAttribute)ca.getAttribute("LineNumberTable");
        if (na != null) {
            na.shiftPc(where, gapLength, exclusive);
        }
        final LocalVariableAttribute va = (LocalVariableAttribute)ca.getAttribute("LocalVariableTable");
        if (va != null) {
            va.shiftPc(where, gapLength, exclusive);
        }
        final LocalVariableAttribute vta = (LocalVariableAttribute)ca.getAttribute("LocalVariableTypeTable");
        if (vta != null) {
            vta.shiftPc(where, gapLength, exclusive);
        }
        final StackMapTable smt = (StackMapTable)ca.getAttribute("StackMapTable");
        if (smt != null) {
            smt.shiftPc(where, gapLength, exclusive);
        }
        final StackMap sm = (StackMap)ca.getAttribute("StackMap");
        if (sm != null) {
            sm.shiftPc(where, gapLength, exclusive);
        }
        return newcode;
    }
    
    private static void insertGap2(final byte[] code, final int where, final int gapLength, final int endPos, final byte[] newcode, final boolean exclusive) throws BadBytecode, AlignmentException {
        int i = 0;
        int j = 0;
        while (i < endPos) {
            if (i == where) {
                for (int j2 = j + gapLength; j < j2; newcode[j++] = 0) {}
            }
            final int nextPos = nextOpcode(code, i);
            final int inst = code[i] & 0xFF;
            if ((153 <= inst && inst <= 168) || inst == 198 || inst == 199) {
                int offset = code[i + 1] << 8 | (code[i + 2] & 0xFF);
                offset = newOffset(i, offset, where, gapLength, exclusive);
                newcode[j] = code[i];
                ByteArray.write16bit(offset, newcode, j + 1);
                j += 3;
            }
            else if (inst == 200 || inst == 201) {
                int offset = ByteArray.read32bit(code, i + 1);
                offset = newOffset(i, offset, where, gapLength, exclusive);
                newcode[j++] = code[i];
                ByteArray.write32bit(offset, newcode, j);
                j += 4;
            }
            else if (inst == 170) {
                if (i != j && (gapLength & 0x3) != 0x0) {
                    throw new AlignmentException();
                }
                int i2 = (i & 0xFFFFFFFC) + 4;
                j = copyGapBytes(newcode, j, code, i, i2);
                final int defaultbyte = newOffset(i, ByteArray.read32bit(code, i2), where, gapLength, exclusive);
                ByteArray.write32bit(defaultbyte, newcode, j);
                final int lowbyte = ByteArray.read32bit(code, i2 + 4);
                ByteArray.write32bit(lowbyte, newcode, j + 4);
                final int highbyte = ByteArray.read32bit(code, i2 + 8);
                ByteArray.write32bit(highbyte, newcode, j + 8);
                j += 12;
                int i3;
                for (i3 = i2 + 12, i2 = i3 + (highbyte - lowbyte + 1) * 4; i3 < i2; i3 += 4) {
                    final int offset2 = newOffset(i, ByteArray.read32bit(code, i3), where, gapLength, exclusive);
                    ByteArray.write32bit(offset2, newcode, j);
                    j += 4;
                }
            }
            else if (inst == 171) {
                if (i != j && (gapLength & 0x3) != 0x0) {
                    throw new AlignmentException();
                }
                int i2 = (i & 0xFFFFFFFC) + 4;
                j = copyGapBytes(newcode, j, code, i, i2);
                final int defaultbyte = newOffset(i, ByteArray.read32bit(code, i2), where, gapLength, exclusive);
                ByteArray.write32bit(defaultbyte, newcode, j);
                final int npairs = ByteArray.read32bit(code, i2 + 4);
                ByteArray.write32bit(npairs, newcode, j + 4);
                j += 8;
                int i4;
                for (i4 = i2 + 8, i2 = i4 + npairs * 8; i4 < i2; i4 += 8) {
                    ByteArray.copy32bit(code, i4, newcode, j);
                    final int offset3 = newOffset(i, ByteArray.read32bit(code, i4 + 4), where, gapLength, exclusive);
                    ByteArray.write32bit(offset3, newcode, j + 4);
                    j += 8;
                }
            }
            else {
                while (i < nextPos) {
                    newcode[j++] = code[i++];
                }
            }
            i = nextPos;
        }
    }
    
    private static int copyGapBytes(final byte[] newcode, int j, final byte[] code, int i, final int iEnd) {
        switch (iEnd - i) {
            case 4: {
                newcode[j++] = code[i++];
            }
            case 3: {
                newcode[j++] = code[i++];
            }
            case 2: {
                newcode[j++] = code[i++];
            }
            case 1: {
                newcode[j++] = code[i++];
                break;
            }
        }
        return j;
    }
    
    private static int newOffset(final int i, int offset, final int where, final int gapLength, final boolean exclusive) {
        final int target = i + offset;
        if (i < where) {
            if (where < target || (exclusive && where == target)) {
                offset += gapLength;
            }
        }
        else if (i == where) {
            if (target < where) {
                offset -= gapLength;
            }
        }
        else if (target < where || (!exclusive && where == target)) {
            offset -= gapLength;
        }
        return offset;
    }
    
    static byte[] changeLdcToLdcW(final byte[] code, final ExceptionTable etable, final CodeAttribute ca, CodeAttribute.LdcEntry ldcs) throws BadBytecode {
        final Pointers pointers = new Pointers(0, 0, 0, etable, ca);
        final ArrayList jumps = makeJumpList(code, code.length, pointers);
        while (ldcs != null) {
            addLdcW(ldcs, jumps);
            ldcs = ldcs.next;
        }
        final byte[] r = insertGap2w(code, 0, 0, false, jumps, pointers);
        return r;
    }
    
    private static void addLdcW(final CodeAttribute.LdcEntry ldcs, final ArrayList jumps) {
        final int where = ldcs.where;
        final LdcW ldcw = new LdcW(where, ldcs.index);
        for (int s = jumps.size(), i = 0; i < s; ++i) {
            if (where < jumps.get(i).orgPos) {
                jumps.add(i, ldcw);
                return;
            }
        }
        jumps.add(ldcw);
    }
    
    private byte[] insertGapCore0w(final byte[] code, final int where, final int gapLength, final boolean exclusive, final ExceptionTable etable, final CodeAttribute ca, final Gap newWhere) throws BadBytecode {
        if (gapLength <= 0) {
            return code;
        }
        final Pointers pointers = new Pointers(this.currentPos, this.mark, where, etable, ca);
        final ArrayList jumps = makeJumpList(code, code.length, pointers);
        final byte[] r = insertGap2w(code, where, gapLength, exclusive, jumps, pointers);
        this.currentPos = pointers.cursor;
        this.mark = pointers.mark;
        int where2 = pointers.mark0;
        if (where2 == this.currentPos && !exclusive) {
            this.currentPos += gapLength;
        }
        if (exclusive) {
            where2 -= gapLength;
        }
        newWhere.position = where2;
        newWhere.length = gapLength;
        return r;
    }
    
    private static byte[] insertGap2w(final byte[] code, final int where, final int gapLength, final boolean exclusive, final ArrayList jumps, final Pointers ptrs) throws BadBytecode {
        final int n = jumps.size();
        if (gapLength > 0) {
            ptrs.shiftPc(where, gapLength, exclusive);
            for (int i = 0; i < n; ++i) {
                jumps.get(i).shift(where, gapLength, exclusive);
            }
        }
        boolean unstable = true;
        while (true) {
            if (unstable) {
                unstable = false;
                for (int j = 0; j < n; ++j) {
                    final Branch b = jumps.get(j);
                    if (b.expanded()) {
                        unstable = true;
                        final int p = b.pos;
                        final int delta = b.deltaSize();
                        ptrs.shiftPc(p, delta, false);
                        for (int k = 0; k < n; ++k) {
                            jumps.get(k).shift(p, delta, false);
                        }
                    }
                }
            }
            else {
                for (int j = 0; j < n; ++j) {
                    final Branch b = jumps.get(j);
                    final int diff = b.gapChanged();
                    if (diff > 0) {
                        unstable = true;
                        final int p2 = b.pos;
                        ptrs.shiftPc(p2, diff, false);
                        for (int k = 0; k < n; ++k) {
                            jumps.get(k).shift(p2, diff, false);
                        }
                    }
                }
                if (!unstable) {
                    break;
                }
                continue;
            }
        }
        return makeExapndedCode(code, jumps, where, gapLength);
    }
    
    private static ArrayList makeJumpList(final byte[] code, final int endPos, final Pointers ptrs) throws BadBytecode {
        final ArrayList jumps = new ArrayList();
        int nextPos;
        for (int i = 0; i < endPos; i = nextPos) {
            nextPos = nextOpcode(code, i);
            final int inst = code[i] & 0xFF;
            if ((153 <= inst && inst <= 168) || inst == 198 || inst == 199) {
                final int offset = code[i + 1] << 8 | (code[i + 2] & 0xFF);
                Branch b;
                if (inst == 167 || inst == 168) {
                    b = new Jump16(i, offset);
                }
                else {
                    b = new If16(i, offset);
                }
                jumps.add(b);
            }
            else if (inst == 200 || inst == 201) {
                final int offset = ByteArray.read32bit(code, i + 1);
                jumps.add(new Jump32(i, offset));
            }
            else if (inst == 170) {
                final int i2 = (i & 0xFFFFFFFC) + 4;
                final int defaultbyte = ByteArray.read32bit(code, i2);
                final int lowbyte = ByteArray.read32bit(code, i2 + 4);
                final int highbyte = ByteArray.read32bit(code, i2 + 8);
                int i3 = i2 + 12;
                final int size = highbyte - lowbyte + 1;
                final int[] offsets = new int[size];
                for (int j = 0; j < size; ++j) {
                    offsets[j] = ByteArray.read32bit(code, i3);
                    i3 += 4;
                }
                jumps.add(new Table(i, defaultbyte, lowbyte, highbyte, offsets, ptrs));
            }
            else if (inst == 171) {
                final int i2 = (i & 0xFFFFFFFC) + 4;
                final int defaultbyte = ByteArray.read32bit(code, i2);
                final int npairs = ByteArray.read32bit(code, i2 + 4);
                int i4 = i2 + 8;
                final int[] matches = new int[npairs];
                final int[] offsets2 = new int[npairs];
                for (int k = 0; k < npairs; ++k) {
                    matches[k] = ByteArray.read32bit(code, i4);
                    offsets2[k] = ByteArray.read32bit(code, i4 + 4);
                    i4 += 8;
                }
                jumps.add(new Lookup(i, defaultbyte, matches, offsets2, ptrs));
            }
        }
        return jumps;
    }
    
    private static byte[] makeExapndedCode(final byte[] code, final ArrayList jumps, final int where, final int gapLength) throws BadBytecode {
        final int n = jumps.size();
        int size = code.length + gapLength;
        for (int i = 0; i < n; ++i) {
            final Branch b = jumps.get(i);
            size += b.deltaSize();
        }
        final byte[] newcode = new byte[size];
        int src = 0;
        int dest = 0;
        int bindex = 0;
        final int len = code.length;
        Branch b2;
        int bpos;
        if (0 < n) {
            b2 = jumps.get(0);
            bpos = b2.orgPos;
        }
        else {
            b2 = null;
            bpos = len;
        }
        while (src < len) {
            if (src == where) {
                for (int pos2 = dest + gapLength; dest < pos2; newcode[dest++] = 0) {}
            }
            if (src != bpos) {
                newcode[dest++] = code[src++];
            }
            else {
                final int s = b2.write(src, code, dest, newcode);
                src += s;
                dest += s + b2.deltaSize();
                if (++bindex < n) {
                    b2 = jumps.get(bindex);
                    bpos = b2.orgPos;
                }
                else {
                    b2 = null;
                    bpos = len;
                }
            }
        }
        return newcode;
    }
    
    static {
        opcodeLength = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 2, 3, 3, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 0, 0, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 5, 5, 3, 2, 3, 1, 1, 3, 3, 1, 1, 0, 4, 3, 3, 5, 5 };
    }
    
    public static class Gap
    {
        public int position;
        public int length;
    }
    
    static class AlignmentException extends Exception
    {
    }
    
    static class Pointers
    {
        int cursor;
        int mark0;
        int mark;
        ExceptionTable etable;
        LineNumberAttribute line;
        LocalVariableAttribute vars;
        LocalVariableAttribute types;
        StackMapTable stack;
        StackMap stack2;
        
        Pointers(final int cur, final int m, final int m0, final ExceptionTable et, final CodeAttribute ca) {
            this.cursor = cur;
            this.mark = m;
            this.mark0 = m0;
            this.etable = et;
            this.line = (LineNumberAttribute)ca.getAttribute("LineNumberTable");
            this.vars = (LocalVariableAttribute)ca.getAttribute("LocalVariableTable");
            this.types = (LocalVariableAttribute)ca.getAttribute("LocalVariableTypeTable");
            this.stack = (StackMapTable)ca.getAttribute("StackMapTable");
            this.stack2 = (StackMap)ca.getAttribute("StackMap");
        }
        
        void shiftPc(final int where, final int gapLength, final boolean exclusive) throws BadBytecode {
            if (where < this.cursor || (where == this.cursor && exclusive)) {
                this.cursor += gapLength;
            }
            if (where < this.mark || (where == this.mark && exclusive)) {
                this.mark += gapLength;
            }
            if (where < this.mark0 || (where == this.mark0 && exclusive)) {
                this.mark0 += gapLength;
            }
            this.etable.shiftPc(where, gapLength, exclusive);
            if (this.line != null) {
                this.line.shiftPc(where, gapLength, exclusive);
            }
            if (this.vars != null) {
                this.vars.shiftPc(where, gapLength, exclusive);
            }
            if (this.types != null) {
                this.types.shiftPc(where, gapLength, exclusive);
            }
            if (this.stack != null) {
                this.stack.shiftPc(where, gapLength, exclusive);
            }
            if (this.stack2 != null) {
                this.stack2.shiftPc(where, gapLength, exclusive);
            }
        }
        
        void shiftForSwitch(final int where, final int gapLength) throws BadBytecode {
            if (this.stack != null) {
                this.stack.shiftForSwitch(where, gapLength);
            }
            if (this.stack2 != null) {
                this.stack2.shiftForSwitch(where, gapLength);
            }
        }
    }
    
    abstract static class Branch
    {
        int pos;
        int orgPos;
        
        Branch(final int p) {
            this.orgPos = p;
            this.pos = p;
        }
        
        void shift(final int where, final int gapLength, final boolean exclusive) {
            if (where < this.pos || (where == this.pos && exclusive)) {
                this.pos += gapLength;
            }
        }
        
        static int shiftOffset(final int i, int offset, final int where, final int gapLength, final boolean exclusive) {
            final int target = i + offset;
            if (i < where) {
                if (where < target || (exclusive && where == target)) {
                    offset += gapLength;
                }
            }
            else if (i == where) {
                if (target < where && exclusive) {
                    offset -= gapLength;
                }
                else if (where < target && !exclusive) {
                    offset += gapLength;
                }
            }
            else if (target < where || (!exclusive && where == target)) {
                offset -= gapLength;
            }
            return offset;
        }
        
        boolean expanded() {
            return false;
        }
        
        int gapChanged() {
            return 0;
        }
        
        int deltaSize() {
            return 0;
        }
        
        abstract int write(final int p0, final byte[] p1, final int p2, final byte[] p3) throws BadBytecode;
    }
    
    static class LdcW extends Branch
    {
        int index;
        boolean state;
        
        LdcW(final int p, final int i) {
            super(p);
            this.index = i;
            this.state = true;
        }
        
        @Override
        boolean expanded() {
            if (this.state) {
                this.state = false;
                return true;
            }
            return false;
        }
        
        @Override
        int deltaSize() {
            return 1;
        }
        
        @Override
        int write(final int srcPos, final byte[] code, final int destPos, final byte[] newcode) {
            newcode[destPos] = 19;
            ByteArray.write16bit(this.index, newcode, destPos + 1);
            return 2;
        }
    }
    
    abstract static class Branch16 extends Branch
    {
        int offset;
        int state;
        static final int BIT16 = 0;
        static final int EXPAND = 1;
        static final int BIT32 = 2;
        
        Branch16(final int p, final int off) {
            super(p);
            this.offset = off;
            this.state = 0;
        }
        
        @Override
        void shift(final int where, final int gapLength, final boolean exclusive) {
            this.offset = Branch.shiftOffset(this.pos, this.offset, where, gapLength, exclusive);
            super.shift(where, gapLength, exclusive);
            if (this.state == 0 && (this.offset < -32768 || 32767 < this.offset)) {
                this.state = 1;
            }
        }
        
        @Override
        boolean expanded() {
            if (this.state == 1) {
                this.state = 2;
                return true;
            }
            return false;
        }
        
        @Override
        abstract int deltaSize();
        
        abstract void write32(final int p0, final byte[] p1, final int p2, final byte[] p3);
        
        @Override
        int write(final int src, final byte[] code, final int dest, final byte[] newcode) {
            if (this.state == 2) {
                this.write32(src, code, dest, newcode);
            }
            else {
                newcode[dest] = code[src];
                ByteArray.write16bit(this.offset, newcode, dest + 1);
            }
            return 3;
        }
    }
    
    static class Jump16 extends Branch16
    {
        Jump16(final int p, final int off) {
            super(p, off);
        }
        
        @Override
        int deltaSize() {
            return (this.state == 2) ? 2 : 0;
        }
        
        @Override
        void write32(final int src, final byte[] code, final int dest, final byte[] newcode) {
            newcode[dest] = (byte)(((code[src] & 0xFF) == 0xA7) ? 200 : 201);
            ByteArray.write32bit(this.offset, newcode, dest + 1);
        }
    }
    
    static class If16 extends Branch16
    {
        If16(final int p, final int off) {
            super(p, off);
        }
        
        @Override
        int deltaSize() {
            return (this.state == 2) ? 5 : 0;
        }
        
        @Override
        void write32(final int src, final byte[] code, final int dest, final byte[] newcode) {
            newcode[dest] = (byte)this.opcode(code[src] & 0xFF);
            newcode[dest + 1] = 0;
            newcode[dest + 2] = 8;
            newcode[dest + 3] = -56;
            ByteArray.write32bit(this.offset - 3, newcode, dest + 4);
        }
        
        int opcode(final int op) {
            if (op == 198) {
                return 199;
            }
            if (op == 199) {
                return 198;
            }
            if ((op - 153 & 0x1) == 0x0) {
                return op + 1;
            }
            return op - 1;
        }
    }
    
    static class Jump32 extends Branch
    {
        int offset;
        
        Jump32(final int p, final int off) {
            super(p);
            this.offset = off;
        }
        
        @Override
        void shift(final int where, final int gapLength, final boolean exclusive) {
            this.offset = Branch.shiftOffset(this.pos, this.offset, where, gapLength, exclusive);
            super.shift(where, gapLength, exclusive);
        }
        
        @Override
        int write(final int src, final byte[] code, final int dest, final byte[] newcode) {
            newcode[dest] = code[src];
            ByteArray.write32bit(this.offset, newcode, dest + 1);
            return 5;
        }
    }
    
    abstract static class Switcher extends Branch
    {
        int gap;
        int defaultByte;
        int[] offsets;
        Pointers pointers;
        
        Switcher(final int pos, final int defaultByte, final int[] offsets, final Pointers ptrs) {
            super(pos);
            this.gap = 3 - (pos & 0x3);
            this.defaultByte = defaultByte;
            this.offsets = offsets;
            this.pointers = ptrs;
        }
        
        @Override
        void shift(final int where, final int gapLength, final boolean exclusive) {
            final int p = this.pos;
            this.defaultByte = Branch.shiftOffset(p, this.defaultByte, where, gapLength, exclusive);
            for (int num = this.offsets.length, i = 0; i < num; ++i) {
                this.offsets[i] = Branch.shiftOffset(p, this.offsets[i], where, gapLength, exclusive);
            }
            super.shift(where, gapLength, exclusive);
        }
        
        @Override
        int gapChanged() {
            final int newGap = 3 - (this.pos & 0x3);
            if (newGap > this.gap) {
                final int diff = newGap - this.gap;
                this.gap = newGap;
                return diff;
            }
            return 0;
        }
        
        @Override
        int deltaSize() {
            return this.gap - (3 - (this.orgPos & 0x3));
        }
        
        @Override
        int write(final int src, final byte[] code, int dest, final byte[] newcode) throws BadBytecode {
            int padding = 3 - (this.pos & 0x3);
            int nops = this.gap - padding;
            final int bytecodeSize = 5 + (3 - (this.orgPos & 0x3)) + this.tableSize();
            if (nops > 0) {
                this.adjustOffsets(bytecodeSize, nops);
            }
            newcode[dest++] = code[src];
            while (padding-- > 0) {
                newcode[dest++] = 0;
            }
            ByteArray.write32bit(this.defaultByte, newcode, dest);
            final int size = this.write2(dest + 4, newcode);
            dest += size + 4;
            while (nops-- > 0) {
                newcode[dest++] = 0;
            }
            return 5 + (3 - (this.orgPos & 0x3)) + size;
        }
        
        abstract int write2(final int p0, final byte[] p1);
        
        abstract int tableSize();
        
        void adjustOffsets(final int size, final int nops) throws BadBytecode {
            this.pointers.shiftForSwitch(this.pos + size, nops);
            if (this.defaultByte == size) {
                this.defaultByte -= nops;
            }
            for (int i = 0; i < this.offsets.length; ++i) {
                if (this.offsets[i] == size) {
                    final int[] offsets = this.offsets;
                    final int n = i;
                    offsets[n] -= nops;
                }
            }
        }
    }
    
    static class Table extends Switcher
    {
        int low;
        int high;
        
        Table(final int pos, final int defaultByte, final int low, final int high, final int[] offsets, final Pointers ptrs) {
            super(pos, defaultByte, offsets, ptrs);
            this.low = low;
            this.high = high;
        }
        
        @Override
        int write2(int dest, final byte[] newcode) {
            ByteArray.write32bit(this.low, newcode, dest);
            ByteArray.write32bit(this.high, newcode, dest + 4);
            final int n = this.offsets.length;
            dest += 8;
            for (int i = 0; i < n; ++i) {
                ByteArray.write32bit(this.offsets[i], newcode, dest);
                dest += 4;
            }
            return 8 + 4 * n;
        }
        
        @Override
        int tableSize() {
            return 8 + 4 * this.offsets.length;
        }
    }
    
    static class Lookup extends Switcher
    {
        int[] matches;
        
        Lookup(final int pos, final int defaultByte, final int[] matches, final int[] offsets, final Pointers ptrs) {
            super(pos, defaultByte, offsets, ptrs);
            this.matches = matches;
        }
        
        @Override
        int write2(int dest, final byte[] newcode) {
            final int n = this.matches.length;
            ByteArray.write32bit(n, newcode, dest);
            dest += 4;
            for (int i = 0; i < n; ++i) {
                ByteArray.write32bit(this.matches[i], newcode, dest);
                ByteArray.write32bit(this.offsets[i], newcode, dest + 4);
                dest += 8;
            }
            return 4 + 8 * n;
        }
        
        @Override
        int tableSize() {
            return 4 + 8 * this.matches.length;
        }
    }
}

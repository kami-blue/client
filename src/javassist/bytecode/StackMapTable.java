// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.ByteArrayOutputStream;
import javassist.CannotCompileException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

public class StackMapTable extends AttributeInfo
{
    public static final String tag = "StackMapTable";
    public static final int TOP = 0;
    public static final int INTEGER = 1;
    public static final int FLOAT = 2;
    public static final int DOUBLE = 3;
    public static final int LONG = 4;
    public static final int NULL = 5;
    public static final int THIS = 6;
    public static final int OBJECT = 7;
    public static final int UNINIT = 8;
    
    StackMapTable(final ConstPool cp, final byte[] newInfo) {
        super(cp, "StackMapTable", newInfo);
    }
    
    StackMapTable(final ConstPool cp, final int name_id, final DataInputStream in) throws IOException {
        super(cp, name_id, in);
    }
    
    @Override
    public AttributeInfo copy(final ConstPool newCp, final Map classnames) throws RuntimeCopyException {
        try {
            return new StackMapTable(newCp, new Copier(this.constPool, this.info, newCp, classnames).doit());
        }
        catch (BadBytecode e) {
            throw new RuntimeCopyException("bad bytecode. fatal?");
        }
    }
    
    @Override
    void write(final DataOutputStream out) throws IOException {
        super.write(out);
    }
    
    public void insertLocal(final int index, final int tag, final int classInfo) throws BadBytecode {
        final byte[] data = new InsertLocal(this.get(), index, tag, classInfo).doit();
        this.set(data);
    }
    
    public static int typeTagOf(final char descriptor) {
        switch (descriptor) {
            case 'D': {
                return 3;
            }
            case 'F': {
                return 2;
            }
            case 'J': {
                return 4;
            }
            case 'L':
            case '[': {
                return 7;
            }
            default: {
                return 1;
            }
        }
    }
    
    public void println(final PrintWriter w) {
        Printer.print(this, w);
    }
    
    public void println(final PrintStream ps) {
        Printer.print(this, new PrintWriter(ps, true));
    }
    
    void shiftPc(final int where, final int gapSize, final boolean exclusive) throws BadBytecode {
        new OffsetShifter(this, where, gapSize).parse();
        new Shifter(this, where, gapSize, exclusive).doit();
    }
    
    void shiftForSwitch(final int where, final int gapSize) throws BadBytecode {
        new SwitchShifter(this, where, gapSize).doit();
    }
    
    public void removeNew(final int where) throws CannotCompileException {
        try {
            final byte[] data = new NewRemover(this.get(), where).doit();
            this.set(data);
        }
        catch (BadBytecode e) {
            throw new CannotCompileException("bad stack map table", e);
        }
    }
    
    public static class RuntimeCopyException extends RuntimeException
    {
        public RuntimeCopyException(final String s) {
            super(s);
        }
    }
    
    public static class Walker
    {
        byte[] info;
        int numOfEntries;
        
        public Walker(final StackMapTable smt) {
            this(smt.get());
        }
        
        public Walker(final byte[] data) {
            this.info = data;
            this.numOfEntries = ByteArray.readU16bit(data, 0);
        }
        
        public final int size() {
            return this.numOfEntries;
        }
        
        public void parse() throws BadBytecode {
            final int n = this.numOfEntries;
            int pos = 2;
            for (int i = 0; i < n; ++i) {
                pos = this.stackMapFrames(pos, i);
            }
        }
        
        int stackMapFrames(int pos, final int nth) throws BadBytecode {
            final int type = this.info[pos] & 0xFF;
            if (type < 64) {
                this.sameFrame(pos, type);
                ++pos;
            }
            else if (type < 128) {
                pos = this.sameLocals(pos, type);
            }
            else {
                if (type < 247) {
                    throw new BadBytecode("bad frame_type in StackMapTable");
                }
                if (type == 247) {
                    pos = this.sameLocals(pos, type);
                }
                else if (type < 251) {
                    final int offset = ByteArray.readU16bit(this.info, pos + 1);
                    this.chopFrame(pos, offset, 251 - type);
                    pos += 3;
                }
                else if (type == 251) {
                    final int offset = ByteArray.readU16bit(this.info, pos + 1);
                    this.sameFrame(pos, offset);
                    pos += 3;
                }
                else if (type < 255) {
                    pos = this.appendFrame(pos, type);
                }
                else {
                    pos = this.fullFrame(pos);
                }
            }
            return pos;
        }
        
        public void sameFrame(final int pos, final int offsetDelta) throws BadBytecode {
        }
        
        private int sameLocals(int pos, final int type) throws BadBytecode {
            final int top = pos;
            int offset;
            if (type < 128) {
                offset = type - 64;
            }
            else {
                offset = ByteArray.readU16bit(this.info, pos + 1);
                pos += 2;
            }
            final int tag = this.info[pos + 1] & 0xFF;
            int data = 0;
            if (tag == 7 || tag == 8) {
                data = ByteArray.readU16bit(this.info, pos + 2);
                this.objectOrUninitialized(tag, data, pos + 2);
                pos += 2;
            }
            this.sameLocals(top, offset, tag, data);
            return pos + 2;
        }
        
        public void sameLocals(final int pos, final int offsetDelta, final int stackTag, final int stackData) throws BadBytecode {
        }
        
        public void chopFrame(final int pos, final int offsetDelta, final int k) throws BadBytecode {
        }
        
        private int appendFrame(final int pos, final int type) throws BadBytecode {
            final int k = type - 251;
            final int offset = ByteArray.readU16bit(this.info, pos + 1);
            final int[] tags = new int[k];
            final int[] data = new int[k];
            int p = pos + 3;
            for (int i = 0; i < k; ++i) {
                final int tag = this.info[p] & 0xFF;
                tags[i] = tag;
                if (tag == 7 || tag == 8) {
                    this.objectOrUninitialized(tag, data[i] = ByteArray.readU16bit(this.info, p + 1), p + 1);
                    p += 3;
                }
                else {
                    data[i] = 0;
                    ++p;
                }
            }
            this.appendFrame(pos, offset, tags, data);
            return p;
        }
        
        public void appendFrame(final int pos, final int offsetDelta, final int[] tags, final int[] data) throws BadBytecode {
        }
        
        private int fullFrame(final int pos) throws BadBytecode {
            final int offset = ByteArray.readU16bit(this.info, pos + 1);
            final int numOfLocals = ByteArray.readU16bit(this.info, pos + 3);
            final int[] localsTags = new int[numOfLocals];
            final int[] localsData = new int[numOfLocals];
            int p = this.verifyTypeInfo(pos + 5, numOfLocals, localsTags, localsData);
            final int numOfItems = ByteArray.readU16bit(this.info, p);
            final int[] itemsTags = new int[numOfItems];
            final int[] itemsData = new int[numOfItems];
            p = this.verifyTypeInfo(p + 2, numOfItems, itemsTags, itemsData);
            this.fullFrame(pos, offset, localsTags, localsData, itemsTags, itemsData);
            return p;
        }
        
        public void fullFrame(final int pos, final int offsetDelta, final int[] localTags, final int[] localData, final int[] stackTags, final int[] stackData) throws BadBytecode {
        }
        
        private int verifyTypeInfo(int pos, final int n, final int[] tags, final int[] data) {
            for (int i = 0; i < n; ++i) {
                final int tag = this.info[pos++] & 0xFF;
                tags[i] = tag;
                if (tag == 7 || tag == 8) {
                    this.objectOrUninitialized(tag, data[i] = ByteArray.readU16bit(this.info, pos), pos);
                    pos += 2;
                }
            }
            return pos;
        }
        
        public void objectOrUninitialized(final int tag, final int data, final int pos) {
        }
    }
    
    static class SimpleCopy extends Walker
    {
        private Writer writer;
        
        public SimpleCopy(final byte[] data) {
            super(data);
            this.writer = new Writer(data.length);
        }
        
        public byte[] doit() throws BadBytecode {
            this.parse();
            return this.writer.toByteArray();
        }
        
        @Override
        public void sameFrame(final int pos, final int offsetDelta) {
            this.writer.sameFrame(offsetDelta);
        }
        
        @Override
        public void sameLocals(final int pos, final int offsetDelta, final int stackTag, final int stackData) {
            this.writer.sameLocals(offsetDelta, stackTag, this.copyData(stackTag, stackData));
        }
        
        @Override
        public void chopFrame(final int pos, final int offsetDelta, final int k) {
            this.writer.chopFrame(offsetDelta, k);
        }
        
        @Override
        public void appendFrame(final int pos, final int offsetDelta, final int[] tags, final int[] data) {
            this.writer.appendFrame(offsetDelta, tags, this.copyData(tags, data));
        }
        
        @Override
        public void fullFrame(final int pos, final int offsetDelta, final int[] localTags, final int[] localData, final int[] stackTags, final int[] stackData) {
            this.writer.fullFrame(offsetDelta, localTags, this.copyData(localTags, localData), stackTags, this.copyData(stackTags, stackData));
        }
        
        protected int copyData(final int tag, final int data) {
            return data;
        }
        
        protected int[] copyData(final int[] tags, final int[] data) {
            return data;
        }
    }
    
    static class Copier extends SimpleCopy
    {
        private ConstPool srcPool;
        private ConstPool destPool;
        private Map classnames;
        
        public Copier(final ConstPool src, final byte[] data, final ConstPool dest, final Map names) {
            super(data);
            this.srcPool = src;
            this.destPool = dest;
            this.classnames = names;
        }
        
        @Override
        protected int copyData(final int tag, final int data) {
            if (tag == 7) {
                return this.srcPool.copy(data, this.destPool, this.classnames);
            }
            return data;
        }
        
        @Override
        protected int[] copyData(final int[] tags, final int[] data) {
            final int[] newData = new int[data.length];
            for (int i = 0; i < data.length; ++i) {
                if (tags[i] == 7) {
                    newData[i] = this.srcPool.copy(data[i], this.destPool, this.classnames);
                }
                else {
                    newData[i] = data[i];
                }
            }
            return newData;
        }
    }
    
    static class InsertLocal extends SimpleCopy
    {
        private int varIndex;
        private int varTag;
        private int varData;
        
        public InsertLocal(final byte[] data, final int varIndex, final int varTag, final int varData) {
            super(data);
            this.varIndex = varIndex;
            this.varTag = varTag;
            this.varData = varData;
        }
        
        @Override
        public void fullFrame(final int pos, final int offsetDelta, final int[] localTags, final int[] localData, final int[] stackTags, final int[] stackData) {
            final int len = localTags.length;
            if (len < this.varIndex) {
                super.fullFrame(pos, offsetDelta, localTags, localData, stackTags, stackData);
                return;
            }
            final int typeSize = (this.varTag == 4 || this.varTag == 3) ? 2 : 1;
            final int[] localTags2 = new int[len + typeSize];
            final int[] localData2 = new int[len + typeSize];
            final int index = this.varIndex;
            int j = 0;
            for (int i = 0; i < len; ++i) {
                if (j == index) {
                    j += typeSize;
                }
                localTags2[j] = localTags[i];
                localData2[j++] = localData[i];
            }
            localTags2[index] = this.varTag;
            localData2[index] = this.varData;
            if (typeSize > 1) {
                localData2[index + 1] = (localTags2[index + 1] = 0);
            }
            super.fullFrame(pos, offsetDelta, localTags2, localData2, stackTags, stackData);
        }
    }
    
    public static class Writer
    {
        ByteArrayOutputStream output;
        int numOfEntries;
        
        public Writer(final int size) {
            this.output = new ByteArrayOutputStream(size);
            this.numOfEntries = 0;
            this.output.write(0);
            this.output.write(0);
        }
        
        public byte[] toByteArray() {
            final byte[] b = this.output.toByteArray();
            ByteArray.write16bit(this.numOfEntries, b, 0);
            return b;
        }
        
        public StackMapTable toStackMapTable(final ConstPool cp) {
            return new StackMapTable(cp, this.toByteArray());
        }
        
        public void sameFrame(final int offsetDelta) {
            ++this.numOfEntries;
            if (offsetDelta < 64) {
                this.output.write(offsetDelta);
            }
            else {
                this.output.write(251);
                this.write16(offsetDelta);
            }
        }
        
        public void sameLocals(final int offsetDelta, final int tag, final int data) {
            ++this.numOfEntries;
            if (offsetDelta < 64) {
                this.output.write(offsetDelta + 64);
            }
            else {
                this.output.write(247);
                this.write16(offsetDelta);
            }
            this.writeTypeInfo(tag, data);
        }
        
        public void chopFrame(final int offsetDelta, final int k) {
            ++this.numOfEntries;
            this.output.write(251 - k);
            this.write16(offsetDelta);
        }
        
        public void appendFrame(final int offsetDelta, final int[] tags, final int[] data) {
            ++this.numOfEntries;
            final int k = tags.length;
            this.output.write(k + 251);
            this.write16(offsetDelta);
            for (int i = 0; i < k; ++i) {
                this.writeTypeInfo(tags[i], data[i]);
            }
        }
        
        public void fullFrame(final int offsetDelta, final int[] localTags, final int[] localData, final int[] stackTags, final int[] stackData) {
            ++this.numOfEntries;
            this.output.write(255);
            this.write16(offsetDelta);
            int n = localTags.length;
            this.write16(n);
            for (int i = 0; i < n; ++i) {
                this.writeTypeInfo(localTags[i], localData[i]);
            }
            n = stackTags.length;
            this.write16(n);
            for (int i = 0; i < n; ++i) {
                this.writeTypeInfo(stackTags[i], stackData[i]);
            }
        }
        
        private void writeTypeInfo(final int tag, final int data) {
            this.output.write(tag);
            if (tag == 7 || tag == 8) {
                this.write16(data);
            }
        }
        
        private void write16(final int value) {
            this.output.write(value >>> 8 & 0xFF);
            this.output.write(value & 0xFF);
        }
    }
    
    static class Printer extends Walker
    {
        private PrintWriter writer;
        private int offset;
        
        public static void print(final StackMapTable smt, final PrintWriter writer) {
            try {
                new Printer(smt.get(), writer).parse();
            }
            catch (BadBytecode e) {
                writer.println(e.getMessage());
            }
        }
        
        Printer(final byte[] data, final PrintWriter pw) {
            super(data);
            this.writer = pw;
            this.offset = -1;
        }
        
        @Override
        public void sameFrame(final int pos, final int offsetDelta) {
            this.offset += offsetDelta + 1;
            this.writer.println(this.offset + " same frame: " + offsetDelta);
        }
        
        @Override
        public void sameLocals(final int pos, final int offsetDelta, final int stackTag, final int stackData) {
            this.offset += offsetDelta + 1;
            this.writer.println(this.offset + " same locals: " + offsetDelta);
            this.printTypeInfo(stackTag, stackData);
        }
        
        @Override
        public void chopFrame(final int pos, final int offsetDelta, final int k) {
            this.offset += offsetDelta + 1;
            this.writer.println(this.offset + " chop frame: " + offsetDelta + ",    " + k + " last locals");
        }
        
        @Override
        public void appendFrame(final int pos, final int offsetDelta, final int[] tags, final int[] data) {
            this.offset += offsetDelta + 1;
            this.writer.println(this.offset + " append frame: " + offsetDelta);
            for (int i = 0; i < tags.length; ++i) {
                this.printTypeInfo(tags[i], data[i]);
            }
        }
        
        @Override
        public void fullFrame(final int pos, final int offsetDelta, final int[] localTags, final int[] localData, final int[] stackTags, final int[] stackData) {
            this.offset += offsetDelta + 1;
            this.writer.println(this.offset + " full frame: " + offsetDelta);
            this.writer.println("[locals]");
            for (int i = 0; i < localTags.length; ++i) {
                this.printTypeInfo(localTags[i], localData[i]);
            }
            this.writer.println("[stack]");
            for (int i = 0; i < stackTags.length; ++i) {
                this.printTypeInfo(stackTags[i], stackData[i]);
            }
        }
        
        private void printTypeInfo(final int tag, final int data) {
            String msg = null;
            switch (tag) {
                case 0: {
                    msg = "top";
                    break;
                }
                case 1: {
                    msg = "integer";
                    break;
                }
                case 2: {
                    msg = "float";
                    break;
                }
                case 3: {
                    msg = "double";
                    break;
                }
                case 4: {
                    msg = "long";
                    break;
                }
                case 5: {
                    msg = "null";
                    break;
                }
                case 6: {
                    msg = "this";
                    break;
                }
                case 7: {
                    msg = "object (cpool_index " + data + ")";
                    break;
                }
                case 8: {
                    msg = "uninitialized (offset " + data + ")";
                    break;
                }
            }
            this.writer.print("    ");
            this.writer.println(msg);
        }
    }
    
    static class OffsetShifter extends Walker
    {
        int where;
        int gap;
        
        public OffsetShifter(final StackMapTable smt, final int where, final int gap) {
            super(smt);
            this.where = where;
            this.gap = gap;
        }
        
        @Override
        public void objectOrUninitialized(final int tag, final int data, final int pos) {
            if (tag == 8 && this.where <= data) {
                ByteArray.write16bit(data + this.gap, this.info, pos);
            }
        }
    }
    
    static class Shifter extends Walker
    {
        private StackMapTable stackMap;
        int where;
        int gap;
        int position;
        byte[] updatedInfo;
        boolean exclusive;
        
        public Shifter(final StackMapTable smt, final int where, final int gap, final boolean exclusive) {
            super(smt);
            this.stackMap = smt;
            this.where = where;
            this.gap = gap;
            this.position = 0;
            this.updatedInfo = null;
            this.exclusive = exclusive;
        }
        
        public void doit() throws BadBytecode {
            this.parse();
            if (this.updatedInfo != null) {
                this.stackMap.set(this.updatedInfo);
            }
        }
        
        @Override
        public void sameFrame(final int pos, final int offsetDelta) {
            this.update(pos, offsetDelta, 0, 251);
        }
        
        @Override
        public void sameLocals(final int pos, final int offsetDelta, final int stackTag, final int stackData) {
            this.update(pos, offsetDelta, 64, 247);
        }
        
        void update(final int pos, final int offsetDelta, final int base, final int entry) {
            final int oldPos = this.position;
            this.position = oldPos + offsetDelta + ((oldPos != 0) ? 1 : 0);
            boolean match;
            if (this.exclusive) {
                match = (oldPos < this.where && this.where <= this.position);
            }
            else {
                match = (oldPos <= this.where && this.where < this.position);
            }
            if (match) {
                final int newDelta = offsetDelta + this.gap;
                this.position += this.gap;
                if (newDelta < 64) {
                    this.info[pos] = (byte)(newDelta + base);
                }
                else if (offsetDelta < 64) {
                    final byte[] newinfo = insertGap(this.info, pos, 2);
                    newinfo[pos] = (byte)entry;
                    ByteArray.write16bit(newDelta, newinfo, pos + 1);
                    this.updatedInfo = newinfo;
                }
                else {
                    ByteArray.write16bit(newDelta, this.info, pos + 1);
                }
            }
        }
        
        static byte[] insertGap(final byte[] info, final int where, final int gap) {
            final int len = info.length;
            final byte[] newinfo = new byte[len + gap];
            for (int i = 0; i < len; ++i) {
                newinfo[i + ((i < where) ? 0 : gap)] = info[i];
            }
            return newinfo;
        }
        
        @Override
        public void chopFrame(final int pos, final int offsetDelta, final int k) {
            this.update(pos, offsetDelta);
        }
        
        @Override
        public void appendFrame(final int pos, final int offsetDelta, final int[] tags, final int[] data) {
            this.update(pos, offsetDelta);
        }
        
        @Override
        public void fullFrame(final int pos, final int offsetDelta, final int[] localTags, final int[] localData, final int[] stackTags, final int[] stackData) {
            this.update(pos, offsetDelta);
        }
        
        void update(final int pos, final int offsetDelta) {
            final int oldPos = this.position;
            this.position = oldPos + offsetDelta + ((oldPos != 0) ? 1 : 0);
            boolean match;
            if (this.exclusive) {
                match = (oldPos < this.where && this.where <= this.position);
            }
            else {
                match = (oldPos <= this.where && this.where < this.position);
            }
            if (match) {
                final int newDelta = offsetDelta + this.gap;
                ByteArray.write16bit(newDelta, this.info, pos + 1);
                this.position += this.gap;
            }
        }
    }
    
    static class SwitchShifter extends Shifter
    {
        SwitchShifter(final StackMapTable smt, final int where, final int gap) {
            super(smt, where, gap, false);
        }
        
        @Override
        void update(final int pos, final int offsetDelta, final int base, final int entry) {
            final int oldPos = this.position;
            this.position = oldPos + offsetDelta + ((oldPos != 0) ? 1 : 0);
            int newDelta = offsetDelta;
            if (this.where == this.position) {
                newDelta = offsetDelta - this.gap;
            }
            else {
                if (this.where != oldPos) {
                    return;
                }
                newDelta = offsetDelta + this.gap;
            }
            if (offsetDelta < 64) {
                if (newDelta < 64) {
                    this.info[pos] = (byte)(newDelta + base);
                }
                else {
                    final byte[] newinfo = Shifter.insertGap(this.info, pos, 2);
                    newinfo[pos] = (byte)entry;
                    ByteArray.write16bit(newDelta, newinfo, pos + 1);
                    this.updatedInfo = newinfo;
                }
            }
            else if (newDelta < 64) {
                final byte[] newinfo = deleteGap(this.info, pos, 2);
                newinfo[pos] = (byte)(newDelta + base);
                this.updatedInfo = newinfo;
            }
            else {
                ByteArray.write16bit(newDelta, this.info, pos + 1);
            }
        }
        
        static byte[] deleteGap(final byte[] info, int where, final int gap) {
            where += gap;
            final int len = info.length;
            final byte[] newinfo = new byte[len - gap];
            for (int i = 0; i < len; ++i) {
                newinfo[i - ((i < where) ? 0 : gap)] = info[i];
            }
            return newinfo;
        }
        
        @Override
        void update(final int pos, final int offsetDelta) {
            final int oldPos = this.position;
            this.position = oldPos + offsetDelta + ((oldPos != 0) ? 1 : 0);
            int newDelta = offsetDelta;
            if (this.where == this.position) {
                newDelta = offsetDelta - this.gap;
            }
            else {
                if (this.where != oldPos) {
                    return;
                }
                newDelta = offsetDelta + this.gap;
            }
            ByteArray.write16bit(newDelta, this.info, pos + 1);
        }
    }
    
    static class NewRemover extends SimpleCopy
    {
        int posOfNew;
        
        public NewRemover(final byte[] data, final int pos) {
            super(data);
            this.posOfNew = pos;
        }
        
        @Override
        public void sameLocals(final int pos, final int offsetDelta, final int stackTag, final int stackData) {
            if (stackTag == 8 && stackData == this.posOfNew) {
                super.sameFrame(pos, offsetDelta);
            }
            else {
                super.sameLocals(pos, offsetDelta, stackTag, stackData);
            }
        }
        
        @Override
        public void fullFrame(final int pos, final int offsetDelta, final int[] localTags, final int[] localData, int[] stackTags, int[] stackData) {
            for (int n = stackTags.length - 1, i = 0; i < n; ++i) {
                if (stackTags[i] == 8 && stackData[i] == this.posOfNew && stackTags[i + 1] == 8 && stackData[i + 1] == this.posOfNew) {
                    final int[] stackTags2 = new int[++n - 2];
                    final int[] stackData2 = new int[n - 2];
                    int k = 0;
                    for (int j = 0; j < n; ++j) {
                        if (j == i) {
                            ++j;
                        }
                        else {
                            stackTags2[k] = stackTags[j];
                            stackData2[k++] = stackData[j];
                        }
                    }
                    stackTags = stackTags2;
                    stackData = stackData2;
                    break;
                }
            }
            super.fullFrame(pos, offsetDelta, localTags, localData, stackTags, stackData);
        }
    }
}

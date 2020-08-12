// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import javassist.CannotCompileException;
import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

public class StackMap extends AttributeInfo
{
    public static final String tag = "StackMap";
    public static final int TOP = 0;
    public static final int INTEGER = 1;
    public static final int FLOAT = 2;
    public static final int DOUBLE = 3;
    public static final int LONG = 4;
    public static final int NULL = 5;
    public static final int THIS = 6;
    public static final int OBJECT = 7;
    public static final int UNINIT = 8;
    
    StackMap(final ConstPool cp, final byte[] newInfo) {
        super(cp, "StackMap", newInfo);
    }
    
    StackMap(final ConstPool cp, final int name_id, final DataInputStream in) throws IOException {
        super(cp, name_id, in);
    }
    
    public int numOfEntries() {
        return ByteArray.readU16bit(this.info, 0);
    }
    
    @Override
    public AttributeInfo copy(final ConstPool newCp, final Map classnames) {
        final Copier copier = new Copier(this, newCp, classnames);
        copier.visit();
        return copier.getStackMap();
    }
    
    public void insertLocal(final int index, final int tag, final int classInfo) throws BadBytecode {
        final byte[] data = new InsertLocal(this, index, tag, classInfo).doit();
        this.set(data);
    }
    
    void shiftPc(final int where, final int gapSize, final boolean exclusive) throws BadBytecode {
        new Shifter(this, where, gapSize, exclusive).visit();
    }
    
    void shiftForSwitch(final int where, final int gapSize) throws BadBytecode {
        new SwitchShifter(this, where, gapSize).visit();
    }
    
    public void removeNew(final int where) throws CannotCompileException {
        final byte[] data = new NewRemover(this, where).doit();
        this.set(data);
    }
    
    public void print(final PrintWriter out) {
        new Printer(this, out).print();
    }
    
    public static class Walker
    {
        byte[] info;
        
        public Walker(final StackMap sm) {
            this.info = sm.get();
        }
        
        public void visit() {
            final int num = ByteArray.readU16bit(this.info, 0);
            int pos = 2;
            for (int i = 0; i < num; ++i) {
                final int offset = ByteArray.readU16bit(this.info, pos);
                final int numLoc = ByteArray.readU16bit(this.info, pos + 2);
                pos = this.locals(pos + 4, offset, numLoc);
                final int numStack = ByteArray.readU16bit(this.info, pos);
                pos = this.stack(pos + 2, offset, numStack);
            }
        }
        
        public int locals(final int pos, final int offset, final int num) {
            return this.typeInfoArray(pos, offset, num, true);
        }
        
        public int stack(final int pos, final int offset, final int num) {
            return this.typeInfoArray(pos, offset, num, false);
        }
        
        public int typeInfoArray(int pos, final int offset, final int num, final boolean isLocals) {
            for (int k = 0; k < num; ++k) {
                pos = this.typeInfoArray2(k, pos);
            }
            return pos;
        }
        
        int typeInfoArray2(final int k, int pos) {
            final byte tag = this.info[pos];
            if (tag == 7) {
                final int clazz = ByteArray.readU16bit(this.info, pos + 1);
                this.objectVariable(pos, clazz);
                pos += 3;
            }
            else if (tag == 8) {
                final int offsetOfNew = ByteArray.readU16bit(this.info, pos + 1);
                this.uninitialized(pos, offsetOfNew);
                pos += 3;
            }
            else {
                this.typeInfo(pos, tag);
                ++pos;
            }
            return pos;
        }
        
        public void typeInfo(final int pos, final byte tag) {
        }
        
        public void objectVariable(final int pos, final int clazz) {
        }
        
        public void uninitialized(final int pos, final int offset) {
        }
    }
    
    static class Copier extends Walker
    {
        byte[] dest;
        ConstPool srcCp;
        ConstPool destCp;
        Map classnames;
        
        Copier(final StackMap map, final ConstPool newCp, final Map classnames) {
            super(map);
            this.srcCp = map.getConstPool();
            this.dest = new byte[this.info.length];
            this.destCp = newCp;
            this.classnames = classnames;
        }
        
        @Override
        public void visit() {
            final int num = ByteArray.readU16bit(this.info, 0);
            ByteArray.write16bit(num, this.dest, 0);
            super.visit();
        }
        
        @Override
        public int locals(final int pos, final int offset, final int num) {
            ByteArray.write16bit(offset, this.dest, pos - 4);
            return super.locals(pos, offset, num);
        }
        
        @Override
        public int typeInfoArray(final int pos, final int offset, final int num, final boolean isLocals) {
            ByteArray.write16bit(num, this.dest, pos - 2);
            return super.typeInfoArray(pos, offset, num, isLocals);
        }
        
        @Override
        public void typeInfo(final int pos, final byte tag) {
            this.dest[pos] = tag;
        }
        
        @Override
        public void objectVariable(final int pos, final int clazz) {
            this.dest[pos] = 7;
            final int newClazz = this.srcCp.copy(clazz, this.destCp, this.classnames);
            ByteArray.write16bit(newClazz, this.dest, pos + 1);
        }
        
        @Override
        public void uninitialized(final int pos, final int offset) {
            this.dest[pos] = 8;
            ByteArray.write16bit(offset, this.dest, pos + 1);
        }
        
        public StackMap getStackMap() {
            return new StackMap(this.destCp, this.dest);
        }
    }
    
    static class SimpleCopy extends Walker
    {
        Writer writer;
        
        SimpleCopy(final StackMap map) {
            super(map);
            this.writer = new Writer();
        }
        
        byte[] doit() {
            this.visit();
            return this.writer.toByteArray();
        }
        
        @Override
        public void visit() {
            final int num = ByteArray.readU16bit(this.info, 0);
            this.writer.write16bit(num);
            super.visit();
        }
        
        @Override
        public int locals(final int pos, final int offset, final int num) {
            this.writer.write16bit(offset);
            return super.locals(pos, offset, num);
        }
        
        @Override
        public int typeInfoArray(final int pos, final int offset, final int num, final boolean isLocals) {
            this.writer.write16bit(num);
            return super.typeInfoArray(pos, offset, num, isLocals);
        }
        
        @Override
        public void typeInfo(final int pos, final byte tag) {
            this.writer.writeVerifyTypeInfo(tag, 0);
        }
        
        @Override
        public void objectVariable(final int pos, final int clazz) {
            this.writer.writeVerifyTypeInfo(7, clazz);
        }
        
        @Override
        public void uninitialized(final int pos, final int offset) {
            this.writer.writeVerifyTypeInfo(8, offset);
        }
    }
    
    static class InsertLocal extends SimpleCopy
    {
        private int varIndex;
        private int varTag;
        private int varData;
        
        InsertLocal(final StackMap map, final int varIndex, final int varTag, final int varData) {
            super(map);
            this.varIndex = varIndex;
            this.varTag = varTag;
            this.varData = varData;
        }
        
        @Override
        public int typeInfoArray(int pos, final int offset, final int num, final boolean isLocals) {
            if (!isLocals || num < this.varIndex) {
                return super.typeInfoArray(pos, offset, num, isLocals);
            }
            this.writer.write16bit(num + 1);
            for (int k = 0; k < num; ++k) {
                if (k == this.varIndex) {
                    this.writeVarTypeInfo();
                }
                pos = this.typeInfoArray2(k, pos);
            }
            if (num == this.varIndex) {
                this.writeVarTypeInfo();
            }
            return pos;
        }
        
        private void writeVarTypeInfo() {
            if (this.varTag == 7) {
                this.writer.writeVerifyTypeInfo(7, this.varData);
            }
            else if (this.varTag == 8) {
                this.writer.writeVerifyTypeInfo(8, this.varData);
            }
            else {
                this.writer.writeVerifyTypeInfo(this.varTag, 0);
            }
        }
    }
    
    static class Shifter extends Walker
    {
        private int where;
        private int gap;
        private boolean exclusive;
        
        public Shifter(final StackMap smt, final int where, final int gap, final boolean exclusive) {
            super(smt);
            this.where = where;
            this.gap = gap;
            this.exclusive = exclusive;
        }
        
        @Override
        public int locals(final int pos, final int offset, final int num) {
            if (this.exclusive) {
                if (this.where > offset) {
                    return super.locals(pos, offset, num);
                }
            }
            else if (this.where >= offset) {
                return super.locals(pos, offset, num);
            }
            ByteArray.write16bit(offset + this.gap, this.info, pos - 4);
            return super.locals(pos, offset, num);
        }
        
        @Override
        public void uninitialized(final int pos, final int offset) {
            if (this.where <= offset) {
                ByteArray.write16bit(offset + this.gap, this.info, pos + 1);
            }
        }
    }
    
    static class SwitchShifter extends Walker
    {
        private int where;
        private int gap;
        
        public SwitchShifter(final StackMap smt, final int where, final int gap) {
            super(smt);
            this.where = where;
            this.gap = gap;
        }
        
        @Override
        public int locals(final int pos, final int offset, final int num) {
            if (this.where == pos + offset) {
                ByteArray.write16bit(offset - this.gap, this.info, pos - 4);
            }
            else if (this.where == pos) {
                ByteArray.write16bit(offset + this.gap, this.info, pos - 4);
            }
            return super.locals(pos, offset, num);
        }
    }
    
    static class NewRemover extends SimpleCopy
    {
        int posOfNew;
        
        NewRemover(final StackMap map, final int where) {
            super(map);
            this.posOfNew = where;
        }
        
        @Override
        public int stack(final int pos, final int offset, final int num) {
            return this.stackTypeInfoArray(pos, offset, num);
        }
        
        private int stackTypeInfoArray(int pos, final int offset, final int num) {
            int p = pos;
            int count = 0;
            for (int k = 0; k < num; ++k) {
                final byte tag = this.info[p];
                if (tag == 7) {
                    p += 3;
                }
                else if (tag == 8) {
                    final int offsetOfNew = ByteArray.readU16bit(this.info, p + 1);
                    if (offsetOfNew == this.posOfNew) {
                        ++count;
                    }
                    p += 3;
                }
                else {
                    ++p;
                }
            }
            this.writer.write16bit(num - count);
            for (int k = 0; k < num; ++k) {
                final byte tag = this.info[pos];
                if (tag == 7) {
                    final int clazz = ByteArray.readU16bit(this.info, pos + 1);
                    this.objectVariable(pos, clazz);
                    pos += 3;
                }
                else if (tag == 8) {
                    final int offsetOfNew = ByteArray.readU16bit(this.info, pos + 1);
                    if (offsetOfNew != this.posOfNew) {
                        this.uninitialized(pos, offsetOfNew);
                    }
                    pos += 3;
                }
                else {
                    this.typeInfo(pos, tag);
                    ++pos;
                }
            }
            return pos;
        }
    }
    
    static class Printer extends Walker
    {
        private PrintWriter writer;
        
        public Printer(final StackMap map, final PrintWriter out) {
            super(map);
            this.writer = out;
        }
        
        public void print() {
            final int num = ByteArray.readU16bit(this.info, 0);
            this.writer.println(num + " entries");
            this.visit();
        }
        
        @Override
        public int locals(final int pos, final int offset, final int num) {
            this.writer.println("  * offset " + offset);
            return super.locals(pos, offset, num);
        }
    }
    
    public static class Writer
    {
        private ByteArrayOutputStream output;
        
        public Writer() {
            this.output = new ByteArrayOutputStream();
        }
        
        public byte[] toByteArray() {
            return this.output.toByteArray();
        }
        
        public StackMap toStackMap(final ConstPool cp) {
            return new StackMap(cp, this.output.toByteArray());
        }
        
        public void writeVerifyTypeInfo(final int tag, final int data) {
            this.output.write(tag);
            if (tag == 7 || tag == 8) {
                this.write16bit(data);
            }
        }
        
        public void write16bit(final int value) {
            this.output.write(value >>> 8 & 0xFF);
            this.output.write(value & 0xFF);
        }
    }
}

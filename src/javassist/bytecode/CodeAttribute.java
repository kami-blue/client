// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class CodeAttribute extends AttributeInfo implements Opcode
{
    public static final String tag = "Code";
    private int maxStack;
    private int maxLocals;
    private ExceptionTable exceptions;
    private ArrayList attributes;
    
    public CodeAttribute(final ConstPool cp, final int stack, final int locals, final byte[] code, final ExceptionTable etable) {
        super(cp, "Code");
        this.maxStack = stack;
        this.maxLocals = locals;
        this.info = code;
        this.exceptions = etable;
        this.attributes = new ArrayList();
    }
    
    private CodeAttribute(final ConstPool cp, final CodeAttribute src, final Map classnames) throws BadBytecode {
        super(cp, "Code");
        this.maxStack = src.getMaxStack();
        this.maxLocals = src.getMaxLocals();
        this.exceptions = src.getExceptionTable().copy(cp, classnames);
        this.attributes = new ArrayList();
        final List src_attr = src.getAttributes();
        for (int num = src_attr.size(), i = 0; i < num; ++i) {
            final AttributeInfo ai = src_attr.get(i);
            this.attributes.add(ai.copy(cp, classnames));
        }
        this.info = src.copyCode(cp, classnames, this.exceptions, this);
    }
    
    CodeAttribute(final ConstPool cp, final int name_id, final DataInputStream in) throws IOException {
        super(cp, name_id, (byte[])null);
        final int attr_len = in.readInt();
        this.maxStack = in.readUnsignedShort();
        this.maxLocals = in.readUnsignedShort();
        final int code_len = in.readInt();
        in.readFully(this.info = new byte[code_len]);
        this.exceptions = new ExceptionTable(cp, in);
        this.attributes = new ArrayList();
        for (int num = in.readUnsignedShort(), i = 0; i < num; ++i) {
            this.attributes.add(AttributeInfo.read(cp, in));
        }
    }
    
    @Override
    public AttributeInfo copy(final ConstPool newCp, final Map classnames) throws RuntimeCopyException {
        try {
            return new CodeAttribute(newCp, this, classnames);
        }
        catch (BadBytecode e) {
            throw new RuntimeCopyException("bad bytecode. fatal?");
        }
    }
    
    @Override
    public int length() {
        return 18 + this.info.length + this.exceptions.size() * 8 + AttributeInfo.getLength(this.attributes);
    }
    
    @Override
    void write(final DataOutputStream out) throws IOException {
        out.writeShort(this.name);
        out.writeInt(this.length() - 6);
        out.writeShort(this.maxStack);
        out.writeShort(this.maxLocals);
        out.writeInt(this.info.length);
        out.write(this.info);
        this.exceptions.write(out);
        out.writeShort(this.attributes.size());
        AttributeInfo.writeAll(this.attributes, out);
    }
    
    @Override
    public byte[] get() {
        throw new UnsupportedOperationException("CodeAttribute.get()");
    }
    
    @Override
    public void set(final byte[] newinfo) {
        throw new UnsupportedOperationException("CodeAttribute.set()");
    }
    
    @Override
    void renameClass(final String oldname, final String newname) {
        AttributeInfo.renameClass(this.attributes, oldname, newname);
    }
    
    @Override
    void renameClass(final Map classnames) {
        AttributeInfo.renameClass(this.attributes, classnames);
    }
    
    @Override
    void getRefClasses(final Map classnames) {
        AttributeInfo.getRefClasses(this.attributes, classnames);
    }
    
    public String getDeclaringClass() {
        final ConstPool cp = this.getConstPool();
        return cp.getClassName();
    }
    
    public int getMaxStack() {
        return this.maxStack;
    }
    
    public void setMaxStack(final int value) {
        this.maxStack = value;
    }
    
    public int computeMaxStack() throws BadBytecode {
        return this.maxStack = new CodeAnalyzer(this).computeMaxStack();
    }
    
    public int getMaxLocals() {
        return this.maxLocals;
    }
    
    public void setMaxLocals(final int value) {
        this.maxLocals = value;
    }
    
    public int getCodeLength() {
        return this.info.length;
    }
    
    public byte[] getCode() {
        return this.info;
    }
    
    void setCode(final byte[] newinfo) {
        super.set(newinfo);
    }
    
    public CodeIterator iterator() {
        return new CodeIterator(this);
    }
    
    public ExceptionTable getExceptionTable() {
        return this.exceptions;
    }
    
    public List getAttributes() {
        return this.attributes;
    }
    
    public AttributeInfo getAttribute(final String name) {
        return AttributeInfo.lookup(this.attributes, name);
    }
    
    public void setAttribute(final StackMapTable smt) {
        AttributeInfo.remove(this.attributes, "StackMapTable");
        if (smt != null) {
            this.attributes.add(smt);
        }
    }
    
    public void setAttribute(final StackMap sm) {
        AttributeInfo.remove(this.attributes, "StackMap");
        if (sm != null) {
            this.attributes.add(sm);
        }
    }
    
    private byte[] copyCode(final ConstPool destCp, final Map classnames, final ExceptionTable etable, final CodeAttribute destCa) throws BadBytecode {
        final int len = this.getCodeLength();
        final byte[] newCode = new byte[len];
        destCa.info = newCode;
        final LdcEntry ldc = copyCode(this.info, 0, len, this.getConstPool(), newCode, destCp, classnames);
        return LdcEntry.doit(newCode, ldc, etable, destCa);
    }
    
    private static LdcEntry copyCode(final byte[] code, final int beginPos, final int endPos, final ConstPool srcCp, final byte[] newcode, final ConstPool destCp, final Map classnameMap) throws BadBytecode {
        LdcEntry ldcEntry = null;
        int i2;
        for (int i = beginPos; i < endPos; i = i2) {
            i2 = CodeIterator.nextOpcode(code, i);
            final byte c = code[i];
            switch ((newcode[i] = c) & 0xFF) {
                case 19:
                case 20:
                case 178:
                case 179:
                case 180:
                case 181:
                case 182:
                case 183:
                case 184:
                case 187:
                case 189:
                case 192:
                case 193: {
                    copyConstPoolInfo(i + 1, code, srcCp, newcode, destCp, classnameMap);
                    break;
                }
                case 18: {
                    int index = code[i + 1] & 0xFF;
                    index = srcCp.copy(index, destCp, classnameMap);
                    if (index < 256) {
                        newcode[i + 1] = (byte)index;
                        break;
                    }
                    newcode[i + 1] = (newcode[i] = 0);
                    final LdcEntry ldc = new LdcEntry();
                    ldc.where = i;
                    ldc.index = index;
                    ldc.next = ldcEntry;
                    ldcEntry = ldc;
                    break;
                }
                case 185: {
                    copyConstPoolInfo(i + 1, code, srcCp, newcode, destCp, classnameMap);
                    newcode[i + 3] = code[i + 3];
                    newcode[i + 4] = code[i + 4];
                    break;
                }
                case 186: {
                    copyConstPoolInfo(i + 1, code, srcCp, newcode, destCp, classnameMap);
                    newcode[i + 4] = (newcode[i + 3] = 0);
                    break;
                }
                case 197: {
                    copyConstPoolInfo(i + 1, code, srcCp, newcode, destCp, classnameMap);
                    newcode[i + 3] = code[i + 3];
                    break;
                }
                default: {
                    while (++i < i2) {
                        newcode[i] = code[i];
                    }
                    break;
                }
            }
        }
        return ldcEntry;
    }
    
    private static void copyConstPoolInfo(final int i, final byte[] code, final ConstPool srcCp, final byte[] newcode, final ConstPool destCp, final Map classnameMap) {
        int index = (code[i] & 0xFF) << 8 | (code[i + 1] & 0xFF);
        index = srcCp.copy(index, destCp, classnameMap);
        newcode[i] = (byte)(index >> 8);
        newcode[i + 1] = (byte)index;
    }
    
    public void insertLocalVar(final int where, final int size) throws BadBytecode {
        final CodeIterator ci = this.iterator();
        while (ci.hasNext()) {
            shiftIndex(ci, where, size);
        }
        this.setMaxLocals(this.getMaxLocals() + size);
    }
    
    private static void shiftIndex(final CodeIterator ci, final int lessThan, final int delta) throws BadBytecode {
        final int index = ci.next();
        final int opcode = ci.byteAt(index);
        if (opcode < 21) {
            return;
        }
        if (opcode < 79) {
            if (opcode < 26) {
                shiftIndex8(ci, index, opcode, lessThan, delta);
            }
            else if (opcode < 46) {
                shiftIndex0(ci, index, opcode, lessThan, delta, 26, 21);
            }
            else {
                if (opcode < 54) {
                    return;
                }
                if (opcode < 59) {
                    shiftIndex8(ci, index, opcode, lessThan, delta);
                }
                else {
                    shiftIndex0(ci, index, opcode, lessThan, delta, 59, 54);
                }
            }
        }
        else if (opcode == 132) {
            int var = ci.byteAt(index + 1);
            if (var < lessThan) {
                return;
            }
            var += delta;
            if (var < 256) {
                ci.writeByte(var, index + 1);
            }
            else {
                final int plus = (byte)ci.byteAt(index + 2);
                final int pos = ci.insertExGap(3);
                ci.writeByte(196, pos - 3);
                ci.writeByte(132, pos - 2);
                ci.write16bit(var, pos - 1);
                ci.write16bit(plus, pos + 1);
            }
        }
        else if (opcode == 169) {
            shiftIndex8(ci, index, opcode, lessThan, delta);
        }
        else if (opcode == 196) {
            int var = ci.u16bitAt(index + 2);
            if (var < lessThan) {
                return;
            }
            var += delta;
            ci.write16bit(var, index + 2);
        }
    }
    
    private static void shiftIndex8(final CodeIterator ci, final int index, final int opcode, final int lessThan, final int delta) throws BadBytecode {
        int var = ci.byteAt(index + 1);
        if (var < lessThan) {
            return;
        }
        var += delta;
        if (var < 256) {
            ci.writeByte(var, index + 1);
        }
        else {
            final int pos = ci.insertExGap(2);
            ci.writeByte(196, pos - 2);
            ci.writeByte(opcode, pos - 1);
            ci.write16bit(var, pos);
        }
    }
    
    private static void shiftIndex0(final CodeIterator ci, final int index, int opcode, final int lessThan, final int delta, final int opcode_i_0, final int opcode_i) throws BadBytecode {
        int var = (opcode - opcode_i_0) % 4;
        if (var < lessThan) {
            return;
        }
        var += delta;
        if (var < 4) {
            ci.writeByte(opcode + delta, index);
        }
        else {
            opcode = (opcode - opcode_i_0) / 4 + opcode_i;
            if (var < 256) {
                final int pos = ci.insertExGap(1);
                ci.writeByte(opcode, pos - 1);
                ci.writeByte(var, pos);
            }
            else {
                final int pos = ci.insertExGap(3);
                ci.writeByte(196, pos - 1);
                ci.writeByte(opcode, pos);
                ci.write16bit(var, pos + 1);
            }
        }
    }
    
    public static class RuntimeCopyException extends RuntimeException
    {
        public RuntimeCopyException(final String s) {
            super(s);
        }
    }
    
    static class LdcEntry
    {
        LdcEntry next;
        int where;
        int index;
        
        static byte[] doit(byte[] code, final LdcEntry ldc, final ExceptionTable etable, final CodeAttribute ca) throws BadBytecode {
            if (ldc != null) {
                code = CodeIterator.changeLdcToLdcW(code, etable, ca, ldc);
            }
            return code;
        }
    }
}

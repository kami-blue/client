// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.stackmap;

import javassist.bytecode.Descriptor;
import javassist.bytecode.Opcode;
import javassist.bytecode.ByteArray;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ConstPool;
import javassist.ClassPool;

public abstract class Tracer implements TypeTag
{
    protected ClassPool classPool;
    protected ConstPool cpool;
    protected String returnType;
    protected int stackTop;
    protected TypeData[] stackTypes;
    protected TypeData[] localsTypes;
    
    public Tracer(final ClassPool classes, final ConstPool cp, final int maxStack, final int maxLocals, final String retType) {
        this.classPool = classes;
        this.cpool = cp;
        this.returnType = retType;
        this.stackTop = 0;
        this.stackTypes = TypeData.make(maxStack);
        this.localsTypes = TypeData.make(maxLocals);
    }
    
    public Tracer(final Tracer t) {
        this.classPool = t.classPool;
        this.cpool = t.cpool;
        this.returnType = t.returnType;
        this.stackTop = t.stackTop;
        this.stackTypes = TypeData.make(t.stackTypes.length);
        this.localsTypes = TypeData.make(t.localsTypes.length);
    }
    
    protected int doOpcode(final int pos, final byte[] code) throws BadBytecode {
        try {
            final int op = code[pos] & 0xFF;
            if (op < 96) {
                if (op < 54) {
                    return this.doOpcode0_53(pos, code, op);
                }
                return this.doOpcode54_95(pos, code, op);
            }
            else {
                if (op < 148) {
                    return this.doOpcode96_147(pos, code, op);
                }
                return this.doOpcode148_201(pos, code, op);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new BadBytecode("inconsistent stack height " + e.getMessage(), e);
        }
    }
    
    protected void visitBranch(final int pos, final byte[] code, final int offset) throws BadBytecode {
    }
    
    protected void visitGoto(final int pos, final byte[] code, final int offset) throws BadBytecode {
    }
    
    protected void visitReturn(final int pos, final byte[] code) throws BadBytecode {
    }
    
    protected void visitThrow(final int pos, final byte[] code) throws BadBytecode {
    }
    
    protected void visitTableSwitch(final int pos, final byte[] code, final int n, final int offsetPos, final int defaultOffset) throws BadBytecode {
    }
    
    protected void visitLookupSwitch(final int pos, final byte[] code, final int n, final int pairsPos, final int defaultOffset) throws BadBytecode {
    }
    
    protected void visitJSR(final int pos, final byte[] code) throws BadBytecode {
    }
    
    protected void visitRET(final int pos, final byte[] code) throws BadBytecode {
    }
    
    private int doOpcode0_53(final int pos, final byte[] code, final int op) throws BadBytecode {
        final TypeData[] stackTypes = this.stackTypes;
        switch (op) {
            case 0: {
                break;
            }
            case 1: {
                stackTypes[this.stackTop++] = new TypeData.NullType();
                break;
            }
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8: {
                stackTypes[this.stackTop++] = Tracer.INTEGER;
                break;
            }
            case 9:
            case 10: {
                stackTypes[this.stackTop++] = Tracer.LONG;
                stackTypes[this.stackTop++] = Tracer.TOP;
                break;
            }
            case 11:
            case 12:
            case 13: {
                stackTypes[this.stackTop++] = Tracer.FLOAT;
                break;
            }
            case 14:
            case 15: {
                stackTypes[this.stackTop++] = Tracer.DOUBLE;
                stackTypes[this.stackTop++] = Tracer.TOP;
                break;
            }
            case 16:
            case 17: {
                stackTypes[this.stackTop++] = Tracer.INTEGER;
                return (op == 17) ? 3 : 2;
            }
            case 18: {
                this.doLDC(code[pos + 1] & 0xFF);
                return 2;
            }
            case 19:
            case 20: {
                this.doLDC(ByteArray.readU16bit(code, pos + 1));
                return 3;
            }
            case 21: {
                return this.doXLOAD(Tracer.INTEGER, code, pos);
            }
            case 22: {
                return this.doXLOAD(Tracer.LONG, code, pos);
            }
            case 23: {
                return this.doXLOAD(Tracer.FLOAT, code, pos);
            }
            case 24: {
                return this.doXLOAD(Tracer.DOUBLE, code, pos);
            }
            case 25: {
                return this.doALOAD(code[pos + 1] & 0xFF);
            }
            case 26:
            case 27:
            case 28:
            case 29: {
                stackTypes[this.stackTop++] = Tracer.INTEGER;
                break;
            }
            case 30:
            case 31:
            case 32:
            case 33: {
                stackTypes[this.stackTop++] = Tracer.LONG;
                stackTypes[this.stackTop++] = Tracer.TOP;
                break;
            }
            case 34:
            case 35:
            case 36:
            case 37: {
                stackTypes[this.stackTop++] = Tracer.FLOAT;
                break;
            }
            case 38:
            case 39:
            case 40:
            case 41: {
                stackTypes[this.stackTop++] = Tracer.DOUBLE;
                stackTypes[this.stackTop++] = Tracer.TOP;
                break;
            }
            case 42:
            case 43:
            case 44:
            case 45: {
                final int reg = op - 42;
                stackTypes[this.stackTop++] = this.localsTypes[reg];
                break;
            }
            case 46: {
                final TypeData[] array = stackTypes;
                final int stackTop = this.stackTop - 1;
                this.stackTop = stackTop;
                array[stackTop - 1] = Tracer.INTEGER;
                break;
            }
            case 47: {
                stackTypes[this.stackTop - 2] = Tracer.LONG;
                stackTypes[this.stackTop - 1] = Tracer.TOP;
                break;
            }
            case 48: {
                final TypeData[] array2 = stackTypes;
                final int stackTop2 = this.stackTop - 1;
                this.stackTop = stackTop2;
                array2[stackTop2 - 1] = Tracer.FLOAT;
                break;
            }
            case 49: {
                stackTypes[this.stackTop - 2] = Tracer.DOUBLE;
                stackTypes[this.stackTop - 1] = Tracer.TOP;
                break;
            }
            case 50: {
                final int stackTop3 = this.stackTop - 1;
                this.stackTop = stackTop3;
                final int s = stackTop3 - 1;
                final TypeData data = stackTypes[s];
                stackTypes[s] = TypeData.ArrayElement.make(data);
                break;
            }
            case 51:
            case 52:
            case 53: {
                final TypeData[] array3 = stackTypes;
                final int stackTop4 = this.stackTop - 1;
                this.stackTop = stackTop4;
                array3[stackTop4 - 1] = Tracer.INTEGER;
                break;
            }
            default: {
                throw new RuntimeException("fatal");
            }
        }
        return 1;
    }
    
    private void doLDC(final int index) {
        final TypeData[] stackTypes = this.stackTypes;
        final int tag = this.cpool.getTag(index);
        if (tag == 8) {
            stackTypes[this.stackTop++] = new TypeData.ClassName("java.lang.String");
        }
        else if (tag == 3) {
            stackTypes[this.stackTop++] = Tracer.INTEGER;
        }
        else if (tag == 4) {
            stackTypes[this.stackTop++] = Tracer.FLOAT;
        }
        else if (tag == 5) {
            stackTypes[this.stackTop++] = Tracer.LONG;
            stackTypes[this.stackTop++] = Tracer.TOP;
        }
        else if (tag == 6) {
            stackTypes[this.stackTop++] = Tracer.DOUBLE;
            stackTypes[this.stackTop++] = Tracer.TOP;
        }
        else {
            if (tag != 7) {
                throw new RuntimeException("bad LDC: " + tag);
            }
            stackTypes[this.stackTop++] = new TypeData.ClassName("java.lang.Class");
        }
    }
    
    private int doXLOAD(final TypeData type, final byte[] code, final int pos) {
        final int localVar = code[pos + 1] & 0xFF;
        return this.doXLOAD(localVar, type);
    }
    
    private int doXLOAD(final int localVar, final TypeData type) {
        this.stackTypes[this.stackTop++] = type;
        if (type.is2WordType()) {
            this.stackTypes[this.stackTop++] = Tracer.TOP;
        }
        return 2;
    }
    
    private int doALOAD(final int localVar) {
        this.stackTypes[this.stackTop++] = this.localsTypes[localVar];
        return 2;
    }
    
    private int doOpcode54_95(final int pos, final byte[] code, final int op) throws BadBytecode {
        switch (op) {
            case 54: {
                return this.doXSTORE(pos, code, Tracer.INTEGER);
            }
            case 55: {
                return this.doXSTORE(pos, code, Tracer.LONG);
            }
            case 56: {
                return this.doXSTORE(pos, code, Tracer.FLOAT);
            }
            case 57: {
                return this.doXSTORE(pos, code, Tracer.DOUBLE);
            }
            case 58: {
                return this.doASTORE(code[pos + 1] & 0xFF);
            }
            case 59:
            case 60:
            case 61:
            case 62: {
                final int var = op - 59;
                this.localsTypes[var] = Tracer.INTEGER;
                --this.stackTop;
                break;
            }
            case 63:
            case 64:
            case 65:
            case 66: {
                final int var = op - 63;
                this.localsTypes[var] = Tracer.LONG;
                this.localsTypes[var + 1] = Tracer.TOP;
                this.stackTop -= 2;
                break;
            }
            case 67:
            case 68:
            case 69:
            case 70: {
                final int var = op - 67;
                this.localsTypes[var] = Tracer.FLOAT;
                --this.stackTop;
                break;
            }
            case 71:
            case 72:
            case 73:
            case 74: {
                final int var = op - 71;
                this.localsTypes[var] = Tracer.DOUBLE;
                this.localsTypes[var + 1] = Tracer.TOP;
                this.stackTop -= 2;
                break;
            }
            case 75:
            case 76:
            case 77:
            case 78: {
                final int var = op - 75;
                this.doASTORE(var);
                break;
            }
            case 79:
            case 80:
            case 81:
            case 82: {
                this.stackTop -= ((op == 80 || op == 82) ? 4 : 3);
                break;
            }
            case 83: {
                TypeData.aastore(this.stackTypes[this.stackTop - 3], this.stackTypes[this.stackTop - 1], this.classPool);
                this.stackTop -= 3;
                break;
            }
            case 84:
            case 85:
            case 86: {
                this.stackTop -= 3;
                break;
            }
            case 87: {
                --this.stackTop;
                break;
            }
            case 88: {
                this.stackTop -= 2;
                break;
            }
            case 89: {
                final int sp = this.stackTop;
                this.stackTypes[sp] = this.stackTypes[sp - 1];
                this.stackTop = sp + 1;
                break;
            }
            case 90:
            case 91: {
                final int len = op - 90 + 2;
                this.doDUP_XX(1, len);
                final int sp2 = this.stackTop;
                this.stackTypes[sp2 - len] = this.stackTypes[sp2];
                this.stackTop = sp2 + 1;
                break;
            }
            case 92: {
                this.doDUP_XX(2, 2);
                this.stackTop += 2;
                break;
            }
            case 93:
            case 94: {
                final int len = op - 93 + 3;
                this.doDUP_XX(2, len);
                final int sp2 = this.stackTop;
                this.stackTypes[sp2 - len] = this.stackTypes[sp2];
                this.stackTypes[sp2 - len + 1] = this.stackTypes[sp2 + 1];
                this.stackTop = sp2 + 2;
                break;
            }
            case 95: {
                final int sp = this.stackTop - 1;
                final TypeData t = this.stackTypes[sp];
                this.stackTypes[sp] = this.stackTypes[sp - 1];
                this.stackTypes[sp - 1] = t;
                break;
            }
            default: {
                throw new RuntimeException("fatal");
            }
        }
        return 1;
    }
    
    private int doXSTORE(final int pos, final byte[] code, final TypeData type) {
        final int index = code[pos + 1] & 0xFF;
        return this.doXSTORE(index, type);
    }
    
    private int doXSTORE(final int index, final TypeData type) {
        --this.stackTop;
        this.localsTypes[index] = type;
        if (type.is2WordType()) {
            --this.stackTop;
            this.localsTypes[index + 1] = Tracer.TOP;
        }
        return 2;
    }
    
    private int doASTORE(final int index) {
        --this.stackTop;
        this.localsTypes[index] = this.stackTypes[this.stackTop];
        return 2;
    }
    
    private void doDUP_XX(final int delta, final int len) {
        final TypeData[] types = this.stackTypes;
        for (int sp = this.stackTop - 1, end = sp - len; sp > end; --sp) {
            types[sp + delta] = types[sp];
        }
    }
    
    private int doOpcode96_147(final int pos, final byte[] code, final int op) {
        if (op <= 131) {
            this.stackTop += Opcode.STACK_GROW[op];
            return 1;
        }
        switch (op) {
            case 132: {
                return 3;
            }
            case 133: {
                this.stackTypes[this.stackTop - 1] = Tracer.LONG;
                this.stackTypes[this.stackTop] = Tracer.TOP;
                ++this.stackTop;
                break;
            }
            case 134: {
                this.stackTypes[this.stackTop - 1] = Tracer.FLOAT;
                break;
            }
            case 135: {
                this.stackTypes[this.stackTop - 1] = Tracer.DOUBLE;
                this.stackTypes[this.stackTop] = Tracer.TOP;
                ++this.stackTop;
                break;
            }
            case 136: {
                final TypeData[] stackTypes = this.stackTypes;
                final int stackTop = this.stackTop - 1;
                this.stackTop = stackTop;
                stackTypes[stackTop - 1] = Tracer.INTEGER;
                break;
            }
            case 137: {
                final TypeData[] stackTypes2 = this.stackTypes;
                final int stackTop2 = this.stackTop - 1;
                this.stackTop = stackTop2;
                stackTypes2[stackTop2 - 1] = Tracer.FLOAT;
                break;
            }
            case 138: {
                this.stackTypes[this.stackTop - 2] = Tracer.DOUBLE;
                break;
            }
            case 139: {
                this.stackTypes[this.stackTop - 1] = Tracer.INTEGER;
                break;
            }
            case 140: {
                this.stackTypes[this.stackTop - 1] = Tracer.LONG;
                this.stackTypes[this.stackTop] = Tracer.TOP;
                ++this.stackTop;
                break;
            }
            case 141: {
                this.stackTypes[this.stackTop - 1] = Tracer.DOUBLE;
                this.stackTypes[this.stackTop] = Tracer.TOP;
                ++this.stackTop;
                break;
            }
            case 142: {
                final TypeData[] stackTypes3 = this.stackTypes;
                final int stackTop3 = this.stackTop - 1;
                this.stackTop = stackTop3;
                stackTypes3[stackTop3 - 1] = Tracer.INTEGER;
                break;
            }
            case 143: {
                this.stackTypes[this.stackTop - 2] = Tracer.LONG;
                break;
            }
            case 144: {
                final TypeData[] stackTypes4 = this.stackTypes;
                final int stackTop4 = this.stackTop - 1;
                this.stackTop = stackTop4;
                stackTypes4[stackTop4 - 1] = Tracer.FLOAT;
                break;
            }
            case 145:
            case 146:
            case 147: {
                break;
            }
            default: {
                throw new RuntimeException("fatal");
            }
        }
        return 1;
    }
    
    private int doOpcode148_201(final int pos, final byte[] code, final int op) throws BadBytecode {
        switch (op) {
            case 148: {
                this.stackTypes[this.stackTop - 4] = Tracer.INTEGER;
                this.stackTop -= 3;
                break;
            }
            case 149:
            case 150: {
                final TypeData[] stackTypes = this.stackTypes;
                final int stackTop = this.stackTop - 1;
                this.stackTop = stackTop;
                stackTypes[stackTop - 1] = Tracer.INTEGER;
                break;
            }
            case 151:
            case 152: {
                this.stackTypes[this.stackTop - 4] = Tracer.INTEGER;
                this.stackTop -= 3;
                break;
            }
            case 153:
            case 154:
            case 155:
            case 156:
            case 157:
            case 158: {
                --this.stackTop;
                this.visitBranch(pos, code, ByteArray.readS16bit(code, pos + 1));
                return 3;
            }
            case 159:
            case 160:
            case 161:
            case 162:
            case 163:
            case 164:
            case 165:
            case 166: {
                this.stackTop -= 2;
                this.visitBranch(pos, code, ByteArray.readS16bit(code, pos + 1));
                return 3;
            }
            case 167: {
                this.visitGoto(pos, code, ByteArray.readS16bit(code, pos + 1));
                return 3;
            }
            case 168: {
                this.visitJSR(pos, code);
                return 3;
            }
            case 169: {
                this.visitRET(pos, code);
                return 2;
            }
            case 170: {
                --this.stackTop;
                final int pos2 = (pos & 0xFFFFFFFC) + 8;
                final int low = ByteArray.read32bit(code, pos2);
                final int high = ByteArray.read32bit(code, pos2 + 4);
                final int n = high - low + 1;
                this.visitTableSwitch(pos, code, n, pos2 + 8, ByteArray.read32bit(code, pos2 - 4));
                return n * 4 + 16 - (pos & 0x3);
            }
            case 171: {
                --this.stackTop;
                final int pos2 = (pos & 0xFFFFFFFC) + 8;
                final int n2 = ByteArray.read32bit(code, pos2);
                this.visitLookupSwitch(pos, code, n2, pos2 + 4, ByteArray.read32bit(code, pos2 - 4));
                return n2 * 8 + 12 - (pos & 0x3);
            }
            case 172: {
                --this.stackTop;
                this.visitReturn(pos, code);
                break;
            }
            case 173: {
                this.stackTop -= 2;
                this.visitReturn(pos, code);
                break;
            }
            case 174: {
                --this.stackTop;
                this.visitReturn(pos, code);
                break;
            }
            case 175: {
                this.stackTop -= 2;
                this.visitReturn(pos, code);
                break;
            }
            case 176: {
                final TypeData[] stackTypes2 = this.stackTypes;
                final int stackTop2 = this.stackTop - 1;
                this.stackTop = stackTop2;
                stackTypes2[stackTop2].setType(this.returnType, this.classPool);
                this.visitReturn(pos, code);
                break;
            }
            case 177: {
                this.visitReturn(pos, code);
                break;
            }
            case 178: {
                return this.doGetField(pos, code, false);
            }
            case 179: {
                return this.doPutField(pos, code, false);
            }
            case 180: {
                return this.doGetField(pos, code, true);
            }
            case 181: {
                return this.doPutField(pos, code, true);
            }
            case 182:
            case 183: {
                return this.doInvokeMethod(pos, code, true);
            }
            case 184: {
                return this.doInvokeMethod(pos, code, false);
            }
            case 185: {
                return this.doInvokeIntfMethod(pos, code);
            }
            case 186: {
                return this.doInvokeDynamic(pos, code);
            }
            case 187: {
                final int i = ByteArray.readU16bit(code, pos + 1);
                this.stackTypes[this.stackTop++] = new TypeData.UninitData(pos, this.cpool.getClassInfo(i));
                return 3;
            }
            case 188: {
                return this.doNEWARRAY(pos, code);
            }
            case 189: {
                final int i = ByteArray.readU16bit(code, pos + 1);
                String type = this.cpool.getClassInfo(i).replace('.', '/');
                if (type.charAt(0) == '[') {
                    type = "[" + type;
                }
                else {
                    type = "[L" + type + ";";
                }
                this.stackTypes[this.stackTop - 1] = new TypeData.ClassName(type);
                return 3;
            }
            case 190: {
                this.stackTypes[this.stackTop - 1].setType("[Ljava.lang.Object;", this.classPool);
                this.stackTypes[this.stackTop - 1] = Tracer.INTEGER;
                break;
            }
            case 191: {
                final TypeData[] stackTypes3 = this.stackTypes;
                final int stackTop3 = this.stackTop - 1;
                this.stackTop = stackTop3;
                stackTypes3[stackTop3].setType("java.lang.Throwable", this.classPool);
                this.visitThrow(pos, code);
                break;
            }
            case 192: {
                final int i = ByteArray.readU16bit(code, pos + 1);
                String type = this.cpool.getClassInfo(i);
                if (type.charAt(0) == '[') {
                    type = type.replace('.', '/');
                }
                this.stackTypes[this.stackTop - 1] = new TypeData.ClassName(type);
                return 3;
            }
            case 193: {
                this.stackTypes[this.stackTop - 1] = Tracer.INTEGER;
                return 3;
            }
            case 194:
            case 195: {
                --this.stackTop;
                break;
            }
            case 196: {
                return this.doWIDE(pos, code);
            }
            case 197: {
                return this.doMultiANewArray(pos, code);
            }
            case 198:
            case 199: {
                --this.stackTop;
                this.visitBranch(pos, code, ByteArray.readS16bit(code, pos + 1));
                return 3;
            }
            case 200: {
                this.visitGoto(pos, code, ByteArray.read32bit(code, pos + 1));
                return 5;
            }
            case 201: {
                this.visitJSR(pos, code);
                return 5;
            }
        }
        return 1;
    }
    
    private int doWIDE(final int pos, final byte[] code) throws BadBytecode {
        final int op = code[pos + 1] & 0xFF;
        switch (op) {
            case 21: {
                this.doWIDE_XLOAD(pos, code, Tracer.INTEGER);
                break;
            }
            case 22: {
                this.doWIDE_XLOAD(pos, code, Tracer.LONG);
                break;
            }
            case 23: {
                this.doWIDE_XLOAD(pos, code, Tracer.FLOAT);
                break;
            }
            case 24: {
                this.doWIDE_XLOAD(pos, code, Tracer.DOUBLE);
                break;
            }
            case 25: {
                final int index = ByteArray.readU16bit(code, pos + 2);
                this.doALOAD(index);
                break;
            }
            case 54: {
                this.doWIDE_STORE(pos, code, Tracer.INTEGER);
                break;
            }
            case 55: {
                this.doWIDE_STORE(pos, code, Tracer.LONG);
                break;
            }
            case 56: {
                this.doWIDE_STORE(pos, code, Tracer.FLOAT);
                break;
            }
            case 57: {
                this.doWIDE_STORE(pos, code, Tracer.DOUBLE);
                break;
            }
            case 58: {
                final int index = ByteArray.readU16bit(code, pos + 2);
                this.doASTORE(index);
                break;
            }
            case 132: {
                return 6;
            }
            case 169: {
                this.visitRET(pos, code);
                break;
            }
            default: {
                throw new RuntimeException("bad WIDE instruction: " + op);
            }
        }
        return 4;
    }
    
    private void doWIDE_XLOAD(final int pos, final byte[] code, final TypeData type) {
        final int index = ByteArray.readU16bit(code, pos + 2);
        this.doXLOAD(index, type);
    }
    
    private void doWIDE_STORE(final int pos, final byte[] code, final TypeData type) {
        final int index = ByteArray.readU16bit(code, pos + 2);
        this.doXSTORE(index, type);
    }
    
    private int doPutField(final int pos, final byte[] code, final boolean notStatic) throws BadBytecode {
        final int index = ByteArray.readU16bit(code, pos + 1);
        final String desc = this.cpool.getFieldrefType(index);
        this.stackTop -= Descriptor.dataSize(desc);
        final char c = desc.charAt(0);
        if (c == 'L') {
            this.stackTypes[this.stackTop].setType(getFieldClassName(desc, 0), this.classPool);
        }
        else if (c == '[') {
            this.stackTypes[this.stackTop].setType(desc, this.classPool);
        }
        this.setFieldTarget(notStatic, index);
        return 3;
    }
    
    private int doGetField(final int pos, final byte[] code, final boolean notStatic) throws BadBytecode {
        final int index = ByteArray.readU16bit(code, pos + 1);
        this.setFieldTarget(notStatic, index);
        final String desc = this.cpool.getFieldrefType(index);
        this.pushMemberType(desc);
        return 3;
    }
    
    private void setFieldTarget(final boolean notStatic, final int index) throws BadBytecode {
        if (notStatic) {
            final String className = this.cpool.getFieldrefClassName(index);
            final TypeData[] stackTypes = this.stackTypes;
            final int stackTop = this.stackTop - 1;
            this.stackTop = stackTop;
            stackTypes[stackTop].setType(className, this.classPool);
        }
    }
    
    private int doNEWARRAY(final int pos, final byte[] code) {
        final int s = this.stackTop - 1;
        String type = null;
        switch (code[pos + 1] & 0xFF) {
            case 4: {
                type = "[Z";
                break;
            }
            case 5: {
                type = "[C";
                break;
            }
            case 6: {
                type = "[F";
                break;
            }
            case 7: {
                type = "[D";
                break;
            }
            case 8: {
                type = "[B";
                break;
            }
            case 9: {
                type = "[S";
                break;
            }
            case 10: {
                type = "[I";
                break;
            }
            case 11: {
                type = "[J";
                break;
            }
            default: {
                throw new RuntimeException("bad newarray");
            }
        }
        this.stackTypes[s] = new TypeData.ClassName(type);
        return 2;
    }
    
    private int doMultiANewArray(final int pos, final byte[] code) {
        final int i = ByteArray.readU16bit(code, pos + 1);
        final int dim = code[pos + 3] & 0xFF;
        this.stackTop -= dim - 1;
        final String type = this.cpool.getClassInfo(i).replace('.', '/');
        this.stackTypes[this.stackTop - 1] = new TypeData.ClassName(type);
        return 4;
    }
    
    private int doInvokeMethod(final int pos, final byte[] code, final boolean notStatic) throws BadBytecode {
        final int i = ByteArray.readU16bit(code, pos + 1);
        final String desc = this.cpool.getMethodrefType(i);
        this.checkParamTypes(desc, 1);
        if (notStatic) {
            final String className = this.cpool.getMethodrefClassName(i);
            final TypeData[] stackTypes = this.stackTypes;
            final int stackTop = this.stackTop - 1;
            this.stackTop = stackTop;
            final TypeData target = stackTypes[stackTop];
            if (target instanceof TypeData.UninitTypeVar && target.isUninit()) {
                this.constructorCalled(target, ((TypeData.UninitTypeVar)target).offset());
            }
            else if (target instanceof TypeData.UninitData) {
                this.constructorCalled(target, ((TypeData.UninitData)target).offset());
            }
            target.setType(className, this.classPool);
        }
        this.pushMemberType(desc);
        return 3;
    }
    
    private void constructorCalled(final TypeData target, final int offset) {
        target.constructorCalled(offset);
        for (int i = 0; i < this.stackTop; ++i) {
            this.stackTypes[i].constructorCalled(offset);
        }
        for (int i = 0; i < this.localsTypes.length; ++i) {
            this.localsTypes[i].constructorCalled(offset);
        }
    }
    
    private int doInvokeIntfMethod(final int pos, final byte[] code) throws BadBytecode {
        final int i = ByteArray.readU16bit(code, pos + 1);
        final String desc = this.cpool.getInterfaceMethodrefType(i);
        this.checkParamTypes(desc, 1);
        final String className = this.cpool.getInterfaceMethodrefClassName(i);
        final TypeData[] stackTypes = this.stackTypes;
        final int stackTop = this.stackTop - 1;
        this.stackTop = stackTop;
        stackTypes[stackTop].setType(className, this.classPool);
        this.pushMemberType(desc);
        return 5;
    }
    
    private int doInvokeDynamic(final int pos, final byte[] code) throws BadBytecode {
        final int i = ByteArray.readU16bit(code, pos + 1);
        final String desc = this.cpool.getInvokeDynamicType(i);
        this.checkParamTypes(desc, 1);
        this.pushMemberType(desc);
        return 5;
    }
    
    private void pushMemberType(final String descriptor) {
        int top = 0;
        if (descriptor.charAt(0) == '(') {
            top = descriptor.indexOf(41) + 1;
            if (top < 1) {
                throw new IndexOutOfBoundsException("bad descriptor: " + descriptor);
            }
        }
        final TypeData[] types = this.stackTypes;
        final int index = this.stackTop;
        switch (descriptor.charAt(top)) {
            case '[': {
                types[index] = new TypeData.ClassName(descriptor.substring(top));
                break;
            }
            case 'L': {
                types[index] = new TypeData.ClassName(getFieldClassName(descriptor, top));
                break;
            }
            case 'J': {
                types[index] = Tracer.LONG;
                types[index + 1] = Tracer.TOP;
                this.stackTop += 2;
                return;
            }
            case 'F': {
                types[index] = Tracer.FLOAT;
                break;
            }
            case 'D': {
                types[index] = Tracer.DOUBLE;
                types[index + 1] = Tracer.TOP;
                this.stackTop += 2;
                return;
            }
            case 'V': {
                return;
            }
            default: {
                types[index] = Tracer.INTEGER;
                break;
            }
        }
        ++this.stackTop;
    }
    
    private static String getFieldClassName(final String desc, final int index) {
        return desc.substring(index + 1, desc.length() - 1).replace('/', '.');
    }
    
    private void checkParamTypes(final String desc, final int i) throws BadBytecode {
        char c = desc.charAt(i);
        if (c == ')') {
            return;
        }
        int k = i;
        boolean array = false;
        while (c == '[') {
            array = true;
            c = desc.charAt(++k);
        }
        if (c == 'L') {
            k = desc.indexOf(59, k) + 1;
            if (k <= 0) {
                throw new IndexOutOfBoundsException("bad descriptor");
            }
        }
        else {
            ++k;
        }
        this.checkParamTypes(desc, k);
        if (!array && (c == 'J' || c == 'D')) {
            this.stackTop -= 2;
        }
        else {
            --this.stackTop;
        }
        if (array) {
            this.stackTypes[this.stackTop].setType(desc.substring(i, k), this.classPool);
        }
        else if (c == 'L') {
            this.stackTypes[this.stackTop].setType(desc.substring(i + 1, k - 1).replace('/', '.'), this.classPool);
        }
    }
}

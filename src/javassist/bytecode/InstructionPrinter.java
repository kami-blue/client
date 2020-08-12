// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import javassist.CtMethod;
import java.io.PrintStream;

public class InstructionPrinter implements Opcode
{
    private static final String[] opcodes;
    private final PrintStream stream;
    
    public InstructionPrinter(final PrintStream stream) {
        this.stream = stream;
    }
    
    public static void print(final CtMethod method, final PrintStream stream) {
        new InstructionPrinter(stream).print(method);
    }
    
    public void print(final CtMethod method) {
        final MethodInfo info = method.getMethodInfo2();
        final ConstPool pool = info.getConstPool();
        final CodeAttribute code = info.getCodeAttribute();
        if (code == null) {
            return;
        }
        final CodeIterator iterator = code.iterator();
        while (iterator.hasNext()) {
            int pos;
            try {
                pos = iterator.next();
            }
            catch (BadBytecode e) {
                throw new RuntimeException(e);
            }
            this.stream.println(pos + ": " + instructionString(iterator, pos, pool));
        }
    }
    
    public static String instructionString(final CodeIterator iter, final int pos, final ConstPool pool) {
        final int opcode = iter.byteAt(pos);
        if (opcode > InstructionPrinter.opcodes.length || opcode < 0) {
            throw new IllegalArgumentException("Invalid opcode, opcode: " + opcode + " pos: " + pos);
        }
        final String opstring = InstructionPrinter.opcodes[opcode];
        switch (opcode) {
            case 16: {
                return opstring + " " + iter.byteAt(pos + 1);
            }
            case 17: {
                return opstring + " " + iter.s16bitAt(pos + 1);
            }
            case 18: {
                return opstring + " " + ldc(pool, iter.byteAt(pos + 1));
            }
            case 19:
            case 20: {
                return opstring + " " + ldc(pool, iter.u16bitAt(pos + 1));
            }
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58: {
                return opstring + " " + iter.byteAt(pos + 1);
            }
            case 153:
            case 154:
            case 155:
            case 156:
            case 157:
            case 158:
            case 159:
            case 160:
            case 161:
            case 162:
            case 163:
            case 164:
            case 165:
            case 166:
            case 198:
            case 199: {
                return opstring + " " + (iter.s16bitAt(pos + 1) + pos);
            }
            case 132: {
                return opstring + " " + iter.byteAt(pos + 1) + ", " + iter.signedByteAt(pos + 2);
            }
            case 167:
            case 168: {
                return opstring + " " + (iter.s16bitAt(pos + 1) + pos);
            }
            case 169: {
                return opstring + " " + iter.byteAt(pos + 1);
            }
            case 170: {
                return tableSwitch(iter, pos);
            }
            case 171: {
                return lookupSwitch(iter, pos);
            }
            case 178:
            case 179:
            case 180:
            case 181: {
                return opstring + " " + fieldInfo(pool, iter.u16bitAt(pos + 1));
            }
            case 182:
            case 183:
            case 184: {
                return opstring + " " + methodInfo(pool, iter.u16bitAt(pos + 1));
            }
            case 185: {
                return opstring + " " + interfaceMethodInfo(pool, iter.u16bitAt(pos + 1));
            }
            case 186: {
                return opstring + " " + iter.u16bitAt(pos + 1);
            }
            case 187: {
                return opstring + " " + classInfo(pool, iter.u16bitAt(pos + 1));
            }
            case 188: {
                return opstring + " " + arrayInfo(iter.byteAt(pos + 1));
            }
            case 189:
            case 192: {
                return opstring + " " + classInfo(pool, iter.u16bitAt(pos + 1));
            }
            case 196: {
                return wide(iter, pos);
            }
            case 197: {
                return opstring + " " + classInfo(pool, iter.u16bitAt(pos + 1));
            }
            case 200:
            case 201: {
                return opstring + " " + (iter.s32bitAt(pos + 1) + pos);
            }
            default: {
                return opstring;
            }
        }
    }
    
    private static String wide(final CodeIterator iter, final int pos) {
        final int opcode = iter.byteAt(pos + 1);
        final int index = iter.u16bitAt(pos + 2);
        switch (opcode) {
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 132:
            case 169: {
                return InstructionPrinter.opcodes[opcode] + " " + index;
            }
            default: {
                throw new RuntimeException("Invalid WIDE operand");
            }
        }
    }
    
    private static String arrayInfo(final int type) {
        switch (type) {
            case 4: {
                return "boolean";
            }
            case 5: {
                return "char";
            }
            case 8: {
                return "byte";
            }
            case 9: {
                return "short";
            }
            case 10: {
                return "int";
            }
            case 11: {
                return "long";
            }
            case 6: {
                return "float";
            }
            case 7: {
                return "double";
            }
            default: {
                throw new RuntimeException("Invalid array type");
            }
        }
    }
    
    private static String classInfo(final ConstPool pool, final int index) {
        return "#" + index + " = Class " + pool.getClassInfo(index);
    }
    
    private static String interfaceMethodInfo(final ConstPool pool, final int index) {
        return "#" + index + " = Method " + pool.getInterfaceMethodrefClassName(index) + "." + pool.getInterfaceMethodrefName(index) + "(" + pool.getInterfaceMethodrefType(index) + ")";
    }
    
    private static String methodInfo(final ConstPool pool, final int index) {
        return "#" + index + " = Method " + pool.getMethodrefClassName(index) + "." + pool.getMethodrefName(index) + "(" + pool.getMethodrefType(index) + ")";
    }
    
    private static String fieldInfo(final ConstPool pool, final int index) {
        return "#" + index + " = Field " + pool.getFieldrefClassName(index) + "." + pool.getFieldrefName(index) + "(" + pool.getFieldrefType(index) + ")";
    }
    
    private static String lookupSwitch(final CodeIterator iter, final int pos) {
        final StringBuffer buffer = new StringBuffer("lookupswitch {\n");
        int index = (pos & 0xFFFFFFFC) + 4;
        buffer.append("\t\tdefault: ").append(pos + iter.s32bitAt(index)).append("\n");
        index += 4;
        final int npairs = iter.s32bitAt(index);
        final int n = npairs * 8;
        index += 4;
        for (int end = n + index; index < end; index += 8) {
            final int match = iter.s32bitAt(index);
            final int target = iter.s32bitAt(index + 4) + pos;
            buffer.append("\t\t").append(match).append(": ").append(target).append("\n");
        }
        buffer.setCharAt(buffer.length() - 1, '}');
        return buffer.toString();
    }
    
    private static String tableSwitch(final CodeIterator iter, final int pos) {
        final StringBuffer buffer = new StringBuffer("tableswitch {\n");
        int index = (pos & 0xFFFFFFFC) + 4;
        buffer.append("\t\tdefault: ").append(pos + iter.s32bitAt(index)).append("\n");
        index += 4;
        final int low = iter.s32bitAt(index);
        index += 4;
        final int high = iter.s32bitAt(index);
        final int n = (high - low + 1) * 4;
        index += 4;
        for (int end = n + index, key = low; index < end; index += 4, ++key) {
            final int target = iter.s32bitAt(index) + pos;
            buffer.append("\t\t").append(key).append(": ").append(target).append("\n");
        }
        buffer.setCharAt(buffer.length() - 1, '}');
        return buffer.toString();
    }
    
    private static String ldc(final ConstPool pool, final int index) {
        final int tag = pool.getTag(index);
        switch (tag) {
            case 8: {
                return "#" + index + " = \"" + pool.getStringInfo(index) + "\"";
            }
            case 3: {
                return "#" + index + " = int " + pool.getIntegerInfo(index);
            }
            case 4: {
                return "#" + index + " = float " + pool.getFloatInfo(index);
            }
            case 5: {
                return "#" + index + " = long " + pool.getLongInfo(index);
            }
            case 6: {
                return "#" + index + " = int " + pool.getDoubleInfo(index);
            }
            case 7: {
                return classInfo(pool, index);
            }
            default: {
                throw new RuntimeException("bad LDC: " + tag);
            }
        }
    }
    
    static {
        opcodes = Mnemonic.OPCODE;
    }
}

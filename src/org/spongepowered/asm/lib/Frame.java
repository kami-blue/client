// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib;

class Frame
{
    static final int DIM = -268435456;
    static final int ARRAY_OF = 268435456;
    static final int ELEMENT_OF = -268435456;
    static final int KIND = 251658240;
    static final int TOP_IF_LONG_OR_DOUBLE = 8388608;
    static final int VALUE = 8388607;
    static final int BASE_KIND = 267386880;
    static final int BASE_VALUE = 1048575;
    static final int BASE = 16777216;
    static final int OBJECT = 24117248;
    static final int UNINITIALIZED = 25165824;
    private static final int LOCAL = 33554432;
    private static final int STACK = 50331648;
    static final int TOP = 16777216;
    static final int BOOLEAN = 16777225;
    static final int BYTE = 16777226;
    static final int CHAR = 16777227;
    static final int SHORT = 16777228;
    static final int INTEGER = 16777217;
    static final int FLOAT = 16777218;
    static final int DOUBLE = 16777219;
    static final int LONG = 16777220;
    static final int NULL = 16777221;
    static final int UNINITIALIZED_THIS = 16777222;
    static final int[] SIZE;
    Label owner;
    int[] inputLocals;
    int[] inputStack;
    private int[] outputLocals;
    private int[] outputStack;
    int outputStackTop;
    private int initializationCount;
    private int[] initializations;
    
    final void set(final ClassWriter cw, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
        for (int i = convert(cw, nLocal, local, this.inputLocals); i < local.length; this.inputLocals[i++] = 16777216) {}
        int nStackTop = 0;
        for (int j = 0; j < nStack; ++j) {
            if (stack[j] == Opcodes.LONG || stack[j] == Opcodes.DOUBLE) {
                ++nStackTop;
            }
        }
        convert(cw, nStack, stack, this.inputStack = new int[nStack + nStackTop]);
        this.outputStackTop = 0;
        this.initializationCount = 0;
    }
    
    private static int convert(final ClassWriter cw, final int nInput, final Object[] input, final int[] output) {
        int i = 0;
        for (int j = 0; j < nInput; ++j) {
            if (input[j] instanceof Integer) {
                output[i++] = (0x1000000 | (int)input[j]);
                if (input[j] == Opcodes.LONG || input[j] == Opcodes.DOUBLE) {
                    output[i++] = 16777216;
                }
            }
            else if (input[j] instanceof String) {
                output[i++] = type(cw, Type.getObjectType((String)input[j]).getDescriptor());
            }
            else {
                output[i++] = (0x1800000 | cw.addUninitializedType("", ((Label)input[j]).position));
            }
        }
        return i;
    }
    
    final void set(final Frame f) {
        this.inputLocals = f.inputLocals;
        this.inputStack = f.inputStack;
        this.outputLocals = f.outputLocals;
        this.outputStack = f.outputStack;
        this.outputStackTop = f.outputStackTop;
        this.initializationCount = f.initializationCount;
        this.initializations = f.initializations;
    }
    
    private int get(final int local) {
        if (this.outputLocals == null || local >= this.outputLocals.length) {
            return 0x2000000 | local;
        }
        int type = this.outputLocals[local];
        if (type == 0) {
            final int[] outputLocals = this.outputLocals;
            final int n = 0x2000000 | local;
            outputLocals[local] = n;
            type = n;
        }
        return type;
    }
    
    private void set(final int local, final int type) {
        if (this.outputLocals == null) {
            this.outputLocals = new int[10];
        }
        final int n = this.outputLocals.length;
        if (local >= n) {
            final int[] t = new int[Math.max(local + 1, 2 * n)];
            System.arraycopy(this.outputLocals, 0, t, 0, n);
            this.outputLocals = t;
        }
        this.outputLocals[local] = type;
    }
    
    private void push(final int type) {
        if (this.outputStack == null) {
            this.outputStack = new int[10];
        }
        final int n = this.outputStack.length;
        if (this.outputStackTop >= n) {
            final int[] t = new int[Math.max(this.outputStackTop + 1, 2 * n)];
            System.arraycopy(this.outputStack, 0, t, 0, n);
            this.outputStack = t;
        }
        this.outputStack[this.outputStackTop++] = type;
        final int top = this.owner.inputStackTop + this.outputStackTop;
        if (top > this.owner.outputStackMax) {
            this.owner.outputStackMax = top;
        }
    }
    
    private void push(final ClassWriter cw, final String desc) {
        final int type = type(cw, desc);
        if (type != 0) {
            this.push(type);
            if (type == 16777220 || type == 16777219) {
                this.push(16777216);
            }
        }
    }
    
    private static int type(final ClassWriter cw, final String desc) {
        final int index = (desc.charAt(0) == '(') ? (desc.indexOf(41) + 1) : 0;
        switch (desc.charAt(index)) {
            case 'V': {
                return 0;
            }
            case 'B':
            case 'C':
            case 'I':
            case 'S':
            case 'Z': {
                return 16777217;
            }
            case 'F': {
                return 16777218;
            }
            case 'J': {
                return 16777220;
            }
            case 'D': {
                return 16777219;
            }
            case 'L': {
                final String t = desc.substring(index + 1, desc.length() - 1);
                return 0x1700000 | cw.addType(t);
            }
            default: {
                int dims;
                for (dims = index + 1; desc.charAt(dims) == '['; ++dims) {}
                int data = 0;
                switch (desc.charAt(dims)) {
                    case 'Z': {
                        data = 16777225;
                        break;
                    }
                    case 'C': {
                        data = 16777227;
                        break;
                    }
                    case 'B': {
                        data = 16777226;
                        break;
                    }
                    case 'S': {
                        data = 16777228;
                        break;
                    }
                    case 'I': {
                        data = 16777217;
                        break;
                    }
                    case 'F': {
                        data = 16777218;
                        break;
                    }
                    case 'J': {
                        data = 16777220;
                        break;
                    }
                    case 'D': {
                        data = 16777219;
                        break;
                    }
                    default: {
                        final String t = desc.substring(dims + 1, desc.length() - 1);
                        data = (0x1700000 | cw.addType(t));
                        break;
                    }
                }
                return dims - index << 28 | data;
            }
        }
    }
    
    private int pop() {
        if (this.outputStackTop > 0) {
            final int[] outputStack = this.outputStack;
            final int outputStackTop = this.outputStackTop - 1;
            this.outputStackTop = outputStackTop;
            return outputStack[outputStackTop];
        }
        final int n = 50331648;
        final Label owner = this.owner;
        return n | -(--owner.inputStackTop);
    }
    
    private void pop(final int elements) {
        if (this.outputStackTop >= elements) {
            this.outputStackTop -= elements;
        }
        else {
            final Label owner = this.owner;
            owner.inputStackTop -= elements - this.outputStackTop;
            this.outputStackTop = 0;
        }
    }
    
    private void pop(final String desc) {
        final char c = desc.charAt(0);
        if (c == '(') {
            this.pop((Type.getArgumentsAndReturnSizes(desc) >> 2) - 1);
        }
        else if (c == 'J' || c == 'D') {
            this.pop(2);
        }
        else {
            this.pop(1);
        }
    }
    
    private void init(final int var) {
        if (this.initializations == null) {
            this.initializations = new int[2];
        }
        final int n = this.initializations.length;
        if (this.initializationCount >= n) {
            final int[] t = new int[Math.max(this.initializationCount + 1, 2 * n)];
            System.arraycopy(this.initializations, 0, t, 0, n);
            this.initializations = t;
        }
        this.initializations[this.initializationCount++] = var;
    }
    
    private int init(final ClassWriter cw, final int t) {
        int s;
        if (t == 16777222) {
            s = (0x1700000 | cw.addType(cw.thisName));
        }
        else {
            if ((t & 0xFFF00000) != 0x1800000) {
                return t;
            }
            final String type = cw.typeTable[t & 0xFFFFF].strVal1;
            s = (0x1700000 | cw.addType(type));
        }
        for (int j = 0; j < this.initializationCount; ++j) {
            int u = this.initializations[j];
            final int dim = u & 0xF0000000;
            final int kind = u & 0xF000000;
            if (kind == 33554432) {
                u = dim + this.inputLocals[u & 0x7FFFFF];
            }
            else if (kind == 50331648) {
                u = dim + this.inputStack[this.inputStack.length - (u & 0x7FFFFF)];
            }
            if (t == u) {
                return s;
            }
        }
        return t;
    }
    
    final void initInputFrame(final ClassWriter cw, final int access, final Type[] args, final int maxLocals) {
        this.inputLocals = new int[maxLocals];
        this.inputStack = new int[0];
        int i = 0;
        if ((access & 0x8) == 0x0) {
            if ((access & 0x80000) == 0x0) {
                this.inputLocals[i++] = (0x1700000 | cw.addType(cw.thisName));
            }
            else {
                this.inputLocals[i++] = 16777222;
            }
        }
        for (int j = 0; j < args.length; ++j) {
            final int t = type(cw, args[j].getDescriptor());
            this.inputLocals[i++] = t;
            if (t == 16777220 || t == 16777219) {
                this.inputLocals[i++] = 16777216;
            }
        }
        while (i < maxLocals) {
            this.inputLocals[i++] = 16777216;
        }
    }
    
    void execute(final int opcode, final int arg, final ClassWriter cw, final Item item) {
        Label_2260: {
            switch (opcode) {
                case 0:
                case 116:
                case 117:
                case 118:
                case 119:
                case 145:
                case 146:
                case 147:
                case 167:
                case 177: {
                    break;
                }
                case 1: {
                    this.push(16777221);
                    break;
                }
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 16:
                case 17:
                case 21: {
                    this.push(16777217);
                    break;
                }
                case 9:
                case 10:
                case 22: {
                    this.push(16777220);
                    this.push(16777216);
                    break;
                }
                case 11:
                case 12:
                case 13:
                case 23: {
                    this.push(16777218);
                    break;
                }
                case 14:
                case 15:
                case 24: {
                    this.push(16777219);
                    this.push(16777216);
                    break;
                }
                case 18: {
                    switch (item.type) {
                        case 3: {
                            this.push(16777217);
                            break Label_2260;
                        }
                        case 5: {
                            this.push(16777220);
                            this.push(16777216);
                            break Label_2260;
                        }
                        case 4: {
                            this.push(16777218);
                            break Label_2260;
                        }
                        case 6: {
                            this.push(16777219);
                            this.push(16777216);
                            break Label_2260;
                        }
                        case 7: {
                            this.push(0x1700000 | cw.addType("java/lang/Class"));
                            break Label_2260;
                        }
                        case 8: {
                            this.push(0x1700000 | cw.addType("java/lang/String"));
                            break Label_2260;
                        }
                        case 16: {
                            this.push(0x1700000 | cw.addType("java/lang/invoke/MethodType"));
                            break Label_2260;
                        }
                        default: {
                            this.push(0x1700000 | cw.addType("java/lang/invoke/MethodHandle"));
                            break Label_2260;
                        }
                    }
                    break;
                }
                case 25: {
                    this.push(this.get(arg));
                    break;
                }
                case 46:
                case 51:
                case 52:
                case 53: {
                    this.pop(2);
                    this.push(16777217);
                    break;
                }
                case 47:
                case 143: {
                    this.pop(2);
                    this.push(16777220);
                    this.push(16777216);
                    break;
                }
                case 48: {
                    this.pop(2);
                    this.push(16777218);
                    break;
                }
                case 49:
                case 138: {
                    this.pop(2);
                    this.push(16777219);
                    this.push(16777216);
                    break;
                }
                case 50: {
                    this.pop(1);
                    final int t1 = this.pop();
                    this.push(-268435456 + t1);
                    break;
                }
                case 54:
                case 56:
                case 58: {
                    final int t1 = this.pop();
                    this.set(arg, t1);
                    if (arg <= 0) {
                        break;
                    }
                    final int t2 = this.get(arg - 1);
                    if (t2 == 16777220 || t2 == 16777219) {
                        this.set(arg - 1, 16777216);
                        break;
                    }
                    if ((t2 & 0xF000000) != 0x1000000) {
                        this.set(arg - 1, t2 | 0x800000);
                        break;
                    }
                    break;
                }
                case 55:
                case 57: {
                    this.pop(1);
                    final int t1 = this.pop();
                    this.set(arg, t1);
                    this.set(arg + 1, 16777216);
                    if (arg <= 0) {
                        break;
                    }
                    final int t2 = this.get(arg - 1);
                    if (t2 == 16777220 || t2 == 16777219) {
                        this.set(arg - 1, 16777216);
                        break;
                    }
                    if ((t2 & 0xF000000) != 0x1000000) {
                        this.set(arg - 1, t2 | 0x800000);
                        break;
                    }
                    break;
                }
                case 79:
                case 81:
                case 83:
                case 84:
                case 85:
                case 86: {
                    this.pop(3);
                    break;
                }
                case 80:
                case 82: {
                    this.pop(4);
                    break;
                }
                case 87:
                case 153:
                case 154:
                case 155:
                case 156:
                case 157:
                case 158:
                case 170:
                case 171:
                case 172:
                case 174:
                case 176:
                case 191:
                case 194:
                case 195:
                case 198:
                case 199: {
                    this.pop(1);
                    break;
                }
                case 88:
                case 159:
                case 160:
                case 161:
                case 162:
                case 163:
                case 164:
                case 165:
                case 166:
                case 173:
                case 175: {
                    this.pop(2);
                    break;
                }
                case 89: {
                    final int t1 = this.pop();
                    this.push(t1);
                    this.push(t1);
                    break;
                }
                case 90: {
                    final int t1 = this.pop();
                    final int t2 = this.pop();
                    this.push(t1);
                    this.push(t2);
                    this.push(t1);
                    break;
                }
                case 91: {
                    final int t1 = this.pop();
                    final int t2 = this.pop();
                    final int t3 = this.pop();
                    this.push(t1);
                    this.push(t3);
                    this.push(t2);
                    this.push(t1);
                    break;
                }
                case 92: {
                    final int t1 = this.pop();
                    final int t2 = this.pop();
                    this.push(t2);
                    this.push(t1);
                    this.push(t2);
                    this.push(t1);
                    break;
                }
                case 93: {
                    final int t1 = this.pop();
                    final int t2 = this.pop();
                    final int t3 = this.pop();
                    this.push(t2);
                    this.push(t1);
                    this.push(t3);
                    this.push(t2);
                    this.push(t1);
                    break;
                }
                case 94: {
                    final int t1 = this.pop();
                    final int t2 = this.pop();
                    final int t3 = this.pop();
                    final int t4 = this.pop();
                    this.push(t2);
                    this.push(t1);
                    this.push(t4);
                    this.push(t3);
                    this.push(t2);
                    this.push(t1);
                    break;
                }
                case 95: {
                    final int t1 = this.pop();
                    final int t2 = this.pop();
                    this.push(t1);
                    this.push(t2);
                    break;
                }
                case 96:
                case 100:
                case 104:
                case 108:
                case 112:
                case 120:
                case 122:
                case 124:
                case 126:
                case 128:
                case 130:
                case 136:
                case 142:
                case 149:
                case 150: {
                    this.pop(2);
                    this.push(16777217);
                    break;
                }
                case 97:
                case 101:
                case 105:
                case 109:
                case 113:
                case 127:
                case 129:
                case 131: {
                    this.pop(4);
                    this.push(16777220);
                    this.push(16777216);
                    break;
                }
                case 98:
                case 102:
                case 106:
                case 110:
                case 114:
                case 137:
                case 144: {
                    this.pop(2);
                    this.push(16777218);
                    break;
                }
                case 99:
                case 103:
                case 107:
                case 111:
                case 115: {
                    this.pop(4);
                    this.push(16777219);
                    this.push(16777216);
                    break;
                }
                case 121:
                case 123:
                case 125: {
                    this.pop(3);
                    this.push(16777220);
                    this.push(16777216);
                    break;
                }
                case 132: {
                    this.set(arg, 16777217);
                    break;
                }
                case 133:
                case 140: {
                    this.pop(1);
                    this.push(16777220);
                    this.push(16777216);
                    break;
                }
                case 134: {
                    this.pop(1);
                    this.push(16777218);
                    break;
                }
                case 135:
                case 141: {
                    this.pop(1);
                    this.push(16777219);
                    this.push(16777216);
                    break;
                }
                case 139:
                case 190:
                case 193: {
                    this.pop(1);
                    this.push(16777217);
                    break;
                }
                case 148:
                case 151:
                case 152: {
                    this.pop(4);
                    this.push(16777217);
                    break;
                }
                case 168:
                case 169: {
                    throw new RuntimeException("JSR/RET are not supported with computeFrames option");
                }
                case 178: {
                    this.push(cw, item.strVal3);
                    break;
                }
                case 179: {
                    this.pop(item.strVal3);
                    break;
                }
                case 180: {
                    this.pop(1);
                    this.push(cw, item.strVal3);
                    break;
                }
                case 181: {
                    this.pop(item.strVal3);
                    this.pop();
                    break;
                }
                case 182:
                case 183:
                case 184:
                case 185: {
                    this.pop(item.strVal3);
                    if (opcode != 184) {
                        final int t1 = this.pop();
                        if (opcode == 183 && item.strVal2.charAt(0) == '<') {
                            this.init(t1);
                        }
                    }
                    this.push(cw, item.strVal3);
                    break;
                }
                case 186: {
                    this.pop(item.strVal2);
                    this.push(cw, item.strVal2);
                    break;
                }
                case 187: {
                    this.push(0x1800000 | cw.addUninitializedType(item.strVal1, arg));
                    break;
                }
                case 188: {
                    this.pop();
                    switch (arg) {
                        case 4: {
                            this.push(285212681);
                            break Label_2260;
                        }
                        case 5: {
                            this.push(285212683);
                            break Label_2260;
                        }
                        case 8: {
                            this.push(285212682);
                            break Label_2260;
                        }
                        case 9: {
                            this.push(285212684);
                            break Label_2260;
                        }
                        case 10: {
                            this.push(285212673);
                            break Label_2260;
                        }
                        case 6: {
                            this.push(285212674);
                            break Label_2260;
                        }
                        case 7: {
                            this.push(285212675);
                            break Label_2260;
                        }
                        default: {
                            this.push(285212676);
                            break Label_2260;
                        }
                    }
                    break;
                }
                case 189: {
                    final String s = item.strVal1;
                    this.pop();
                    if (s.charAt(0) == '[') {
                        this.push(cw, '[' + s);
                        break;
                    }
                    this.push(0x11700000 | cw.addType(s));
                    break;
                }
                case 192: {
                    final String s = item.strVal1;
                    this.pop();
                    if (s.charAt(0) == '[') {
                        this.push(cw, s);
                        break;
                    }
                    this.push(0x1700000 | cw.addType(s));
                    break;
                }
                default: {
                    this.pop(arg);
                    this.push(cw, item.strVal1);
                    break;
                }
            }
        }
    }
    
    final boolean merge(final ClassWriter cw, final Frame frame, final int edge) {
        boolean changed = false;
        final int nLocal = this.inputLocals.length;
        final int nStack = this.inputStack.length;
        if (frame.inputLocals == null) {
            frame.inputLocals = new int[nLocal];
            changed = true;
        }
        for (int i = 0; i < nLocal; ++i) {
            int t;
            if (this.outputLocals != null && i < this.outputLocals.length) {
                final int s = this.outputLocals[i];
                if (s == 0) {
                    t = this.inputLocals[i];
                }
                else {
                    final int dim = s & 0xF0000000;
                    final int kind = s & 0xF000000;
                    if (kind == 16777216) {
                        t = s;
                    }
                    else {
                        if (kind == 33554432) {
                            t = dim + this.inputLocals[s & 0x7FFFFF];
                        }
                        else {
                            t = dim + this.inputStack[nStack - (s & 0x7FFFFF)];
                        }
                        if ((s & 0x800000) != 0x0 && (t == 16777220 || t == 16777219)) {
                            t = 16777216;
                        }
                    }
                }
            }
            else {
                t = this.inputLocals[i];
            }
            if (this.initializations != null) {
                t = this.init(cw, t);
            }
            changed |= merge(cw, t, frame.inputLocals, i);
        }
        if (edge > 0) {
            for (int i = 0; i < nLocal; ++i) {
                final int t = this.inputLocals[i];
                changed |= merge(cw, t, frame.inputLocals, i);
            }
            if (frame.inputStack == null) {
                frame.inputStack = new int[1];
                changed = true;
            }
            changed |= merge(cw, edge, frame.inputStack, 0);
            return changed;
        }
        final int nInputStack = this.inputStack.length + this.owner.inputStackTop;
        if (frame.inputStack == null) {
            frame.inputStack = new int[nInputStack + this.outputStackTop];
            changed = true;
        }
        for (int i = 0; i < nInputStack; ++i) {
            int t = this.inputStack[i];
            if (this.initializations != null) {
                t = this.init(cw, t);
            }
            changed |= merge(cw, t, frame.inputStack, i);
        }
        for (int i = 0; i < this.outputStackTop; ++i) {
            final int s = this.outputStack[i];
            final int dim = s & 0xF0000000;
            final int kind = s & 0xF000000;
            int t;
            if (kind == 16777216) {
                t = s;
            }
            else {
                if (kind == 33554432) {
                    t = dim + this.inputLocals[s & 0x7FFFFF];
                }
                else {
                    t = dim + this.inputStack[nStack - (s & 0x7FFFFF)];
                }
                if ((s & 0x800000) != 0x0 && (t == 16777220 || t == 16777219)) {
                    t = 16777216;
                }
            }
            if (this.initializations != null) {
                t = this.init(cw, t);
            }
            changed |= merge(cw, t, frame.inputStack, nInputStack + i);
        }
        return changed;
    }
    
    private static boolean merge(final ClassWriter cw, int t, final int[] types, final int index) {
        final int u = types[index];
        if (u == t) {
            return false;
        }
        if ((t & 0xFFFFFFF) == 0x1000005) {
            if (u == 16777221) {
                return false;
            }
            t = 16777221;
        }
        if (u == 0) {
            types[index] = t;
            return true;
        }
        int v;
        if ((u & 0xFF00000) == 0x1700000 || (u & 0xF0000000) != 0x0) {
            if (t == 16777221) {
                return false;
            }
            if ((t & 0xFFF00000) == (u & 0xFFF00000)) {
                if ((u & 0xFF00000) == 0x1700000) {
                    v = ((t & 0xF0000000) | 0x1700000 | cw.getMergedType(t & 0xFFFFF, u & 0xFFFFF));
                }
                else {
                    final int vdim = -268435456 + (u & 0xF0000000);
                    v = (vdim | 0x1700000 | cw.addType("java/lang/Object"));
                }
            }
            else if ((t & 0xFF00000) == 0x1700000 || (t & 0xF0000000) != 0x0) {
                final int tdim = (((t & 0xF0000000) == 0x0 || (t & 0xFF00000) == 0x1700000) ? 0 : -268435456) + (t & 0xF0000000);
                final int udim = (((u & 0xF0000000) == 0x0 || (u & 0xFF00000) == 0x1700000) ? 0 : -268435456) + (u & 0xF0000000);
                v = (Math.min(tdim, udim) | 0x1700000 | cw.addType("java/lang/Object"));
            }
            else {
                v = 16777216;
            }
        }
        else if (u == 16777221) {
            v = (((t & 0xFF00000) == 0x1700000 || (t & 0xF0000000) != 0x0) ? t : 16777216);
        }
        else {
            v = 16777216;
        }
        if (u != v) {
            types[index] = v;
            return true;
        }
        return false;
    }
    
    static {
        final int[] b = new int[202];
        final String s = "EFFFFFFFFGGFFFGGFFFEEFGFGFEEEEEEEEEEEEEEEEEEEEDEDEDDDDDCDCDEEEEEEEEEEEEEEEEEEEEBABABBBBDCFFFGGGEDCDCDCDCDCDCDCDCDCDCEEEEDDDDDDDCDCDCEFEFDDEEFFDEDEEEBDDBBDDDDDDCCCCCCCCEFEDDDCDCDEEEEEEEEEEFEEEEEEDDEEDDEE";
        for (int i = 0; i < b.length; ++i) {
            b[i] = s.charAt(i) - 'E';
        }
        SIZE = b;
    }
}

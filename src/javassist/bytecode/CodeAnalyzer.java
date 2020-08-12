// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

class CodeAnalyzer implements Opcode
{
    private ConstPool constPool;
    private CodeAttribute codeAttr;
    
    public CodeAnalyzer(final CodeAttribute ca) {
        this.codeAttr = ca;
        this.constPool = ca.getConstPool();
    }
    
    public int computeMaxStack() throws BadBytecode {
        final CodeIterator ci = this.codeAttr.iterator();
        final int length = ci.getCodeLength();
        final int[] stack = new int[length];
        this.constPool = this.codeAttr.getConstPool();
        this.initStack(stack, this.codeAttr);
        boolean repeat;
        do {
            repeat = false;
            for (int i = 0; i < length; ++i) {
                if (stack[i] < 0) {
                    repeat = true;
                    this.visitBytecode(ci, stack, i);
                }
            }
        } while (repeat);
        int maxStack = 1;
        for (int j = 0; j < length; ++j) {
            if (stack[j] > maxStack) {
                maxStack = stack[j];
            }
        }
        return maxStack - 1;
    }
    
    private void initStack(final int[] stack, final CodeAttribute ca) {
        stack[0] = -1;
        final ExceptionTable et = ca.getExceptionTable();
        if (et != null) {
            for (int size = et.size(), i = 0; i < size; ++i) {
                stack[et.handlerPc(i)] = -2;
            }
        }
    }
    
    private void visitBytecode(final CodeIterator ci, final int[] stack, int index) throws BadBytecode {
        final int codeLength = stack.length;
        ci.move(index);
        int stackDepth = -stack[index];
        final int[] jsrDepth = { -1 };
        while (ci.hasNext()) {
            index = ci.next();
            stack[index] = stackDepth;
            final int op = ci.byteAt(index);
            stackDepth = this.visitInst(op, ci, index, stackDepth);
            if (stackDepth < 1) {
                throw new BadBytecode("stack underflow at " + index);
            }
            if (this.processBranch(op, ci, index, codeLength, stack, stackDepth, jsrDepth)) {
                break;
            }
            if (isEnd(op)) {
                break;
            }
            if (op != 168 && op != 201) {
                continue;
            }
            --stackDepth;
        }
    }
    
    private boolean processBranch(final int opcode, final CodeIterator ci, final int index, final int codeLength, final int[] stack, final int stackDepth, final int[] jsrDepth) throws BadBytecode {
        if ((153 <= opcode && opcode <= 166) || opcode == 198 || opcode == 199) {
            final int target = index + ci.s16bitAt(index + 1);
            this.checkTarget(index, target, codeLength, stack, stackDepth);
        }
        else {
            switch (opcode) {
                case 167: {
                    final int target = index + ci.s16bitAt(index + 1);
                    this.checkTarget(index, target, codeLength, stack, stackDepth);
                    return true;
                }
                case 200: {
                    final int target = index + ci.s32bitAt(index + 1);
                    this.checkTarget(index, target, codeLength, stack, stackDepth);
                    return true;
                }
                case 168:
                case 201: {
                    int target;
                    if (opcode == 168) {
                        target = index + ci.s16bitAt(index + 1);
                    }
                    else {
                        target = index + ci.s32bitAt(index + 1);
                    }
                    this.checkTarget(index, target, codeLength, stack, stackDepth);
                    if (jsrDepth[0] < 0) {
                        jsrDepth[0] = stackDepth;
                        return false;
                    }
                    if (stackDepth == jsrDepth[0]) {
                        return false;
                    }
                    throw new BadBytecode("sorry, cannot compute this data flow due to JSR: " + stackDepth + "," + jsrDepth[0]);
                }
                case 169: {
                    if (jsrDepth[0] < 0) {
                        jsrDepth[0] = stackDepth + 1;
                        return false;
                    }
                    if (stackDepth + 1 == jsrDepth[0]) {
                        return true;
                    }
                    throw new BadBytecode("sorry, cannot compute this data flow due to RET: " + stackDepth + "," + jsrDepth[0]);
                }
                case 170:
                case 171: {
                    int index2 = (index & 0xFFFFFFFC) + 4;
                    int target = index + ci.s32bitAt(index2);
                    this.checkTarget(index, target, codeLength, stack, stackDepth);
                    if (opcode == 171) {
                        final int npairs = ci.s32bitAt(index2 + 4);
                        index2 += 12;
                        for (int i = 0; i < npairs; ++i) {
                            target = index + ci.s32bitAt(index2);
                            this.checkTarget(index, target, codeLength, stack, stackDepth);
                            index2 += 8;
                        }
                    }
                    else {
                        final int low = ci.s32bitAt(index2 + 4);
                        final int high = ci.s32bitAt(index2 + 8);
                        final int n = high - low + 1;
                        index2 += 12;
                        for (int j = 0; j < n; ++j) {
                            target = index + ci.s32bitAt(index2);
                            this.checkTarget(index, target, codeLength, stack, stackDepth);
                            index2 += 4;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    private void checkTarget(final int opIndex, final int target, final int codeLength, final int[] stack, final int stackDepth) throws BadBytecode {
        if (target < 0 || codeLength <= target) {
            throw new BadBytecode("bad branch offset at " + opIndex);
        }
        final int d = stack[target];
        if (d == 0) {
            stack[target] = -stackDepth;
        }
        else if (d != stackDepth && d != -stackDepth) {
            throw new BadBytecode("verification error (" + stackDepth + "," + d + ") at " + opIndex);
        }
    }
    
    private static boolean isEnd(final int opcode) {
        return (172 <= opcode && opcode <= 177) || opcode == 191;
    }
    
    private int visitInst(int op, final CodeIterator ci, final int index, int stack) throws BadBytecode {
        switch (op) {
            case 180: {
                stack += this.getFieldSize(ci, index) - 1;
                return stack;
            }
            case 181: {
                stack -= this.getFieldSize(ci, index) + 1;
                return stack;
            }
            case 178: {
                stack += this.getFieldSize(ci, index);
                return stack;
            }
            case 179: {
                stack -= this.getFieldSize(ci, index);
                return stack;
            }
            case 182:
            case 183: {
                final String desc = this.constPool.getMethodrefType(ci.u16bitAt(index + 1));
                stack += Descriptor.dataSize(desc) - 1;
                return stack;
            }
            case 184: {
                final String desc = this.constPool.getMethodrefType(ci.u16bitAt(index + 1));
                stack += Descriptor.dataSize(desc);
                return stack;
            }
            case 185: {
                final String desc = this.constPool.getInterfaceMethodrefType(ci.u16bitAt(index + 1));
                stack += Descriptor.dataSize(desc) - 1;
                return stack;
            }
            case 186: {
                final String desc = this.constPool.getInvokeDynamicType(ci.u16bitAt(index + 1));
                stack += Descriptor.dataSize(desc);
                return stack;
            }
            case 191: {
                stack = 1;
                return stack;
            }
            case 197: {
                stack += 1 - ci.byteAt(index + 3);
                return stack;
            }
            case 196: {
                op = ci.byteAt(index + 1);
                break;
            }
        }
        stack += CodeAnalyzer.STACK_GROW[op];
        return stack;
    }
    
    private int getFieldSize(final CodeIterator ci, final int index) {
        final String desc = this.constPool.getFieldrefType(ci.u16bitAt(index + 1));
        return Descriptor.dataSize(desc);
    }
}

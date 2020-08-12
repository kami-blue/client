// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.tree.analysis;

import org.spongepowered.asm.lib.tree.MultiANewArrayInsnNode;
import org.spongepowered.asm.lib.tree.InvokeDynamicInsnNode;
import java.util.List;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import java.util.ArrayList;
import org.spongepowered.asm.lib.tree.IincInsnNode;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;

public class Frame<V extends Value>
{
    private V returnValue;
    private V[] values;
    private int locals;
    private int top;
    
    public Frame(final int nLocals, final int nStack) {
        this.values = (V[])new Value[nLocals + nStack];
        this.locals = nLocals;
    }
    
    public Frame(final Frame<? extends V> src) {
        this(src.locals, src.values.length - src.locals);
        this.init(src);
    }
    
    public Frame<V> init(final Frame<? extends V> src) {
        this.returnValue = (V)src.returnValue;
        System.arraycopy(src.values, 0, this.values, 0, this.values.length);
        this.top = src.top;
        return this;
    }
    
    public void setReturn(final V v) {
        this.returnValue = v;
    }
    
    public int getLocals() {
        return this.locals;
    }
    
    public int getMaxStackSize() {
        return this.values.length - this.locals;
    }
    
    public V getLocal(final int i) throws IndexOutOfBoundsException {
        if (i >= this.locals) {
            throw new IndexOutOfBoundsException("Trying to access an inexistant local variable");
        }
        return this.values[i];
    }
    
    public void setLocal(final int i, final V value) throws IndexOutOfBoundsException {
        if (i >= this.locals) {
            throw new IndexOutOfBoundsException("Trying to access an inexistant local variable " + i);
        }
        this.values[i] = value;
    }
    
    public int getStackSize() {
        return this.top;
    }
    
    public V getStack(final int i) throws IndexOutOfBoundsException {
        return this.values[i + this.locals];
    }
    
    public void clearStack() {
        this.top = 0;
    }
    
    public V pop() throws IndexOutOfBoundsException {
        if (this.top == 0) {
            throw new IndexOutOfBoundsException("Cannot pop operand off an empty stack.");
        }
        final V[] values = this.values;
        final int top = this.top - 1;
        this.top = top;
        return values[top + this.locals];
    }
    
    public void push(final V value) throws IndexOutOfBoundsException {
        if (this.top + this.locals >= this.values.length) {
            throw new IndexOutOfBoundsException("Insufficient maximum stack size.");
        }
        this.values[this.top++ + this.locals] = value;
    }
    
    public void execute(final AbstractInsnNode insn, final Interpreter<V> interpreter) throws AnalyzerException {
        switch (insn.getOpcode()) {
            case 0: {
                break;
            }
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: {
                this.push(interpreter.newOperation(insn));
                break;
            }
            case 21:
            case 22:
            case 23:
            case 24:
            case 25: {
                this.push(interpreter.copyOperation(insn, this.getLocal(((VarInsnNode)insn).var)));
                break;
            }
            case 46:
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53: {
                final V value2 = this.pop();
                final V value3 = this.pop();
                this.push(interpreter.binaryOperation(insn, value3, value2));
                break;
            }
            case 54:
            case 55:
            case 56:
            case 57:
            case 58: {
                final V value3 = interpreter.copyOperation(insn, this.pop());
                final int var = ((VarInsnNode)insn).var;
                this.setLocal(var, value3);
                if (value3.getSize() == 2) {
                    this.setLocal(var + 1, interpreter.newValue(null));
                }
                if (var > 0) {
                    final Value local = this.getLocal(var - 1);
                    if (local != null && local.getSize() == 2) {
                        this.setLocal(var - 1, interpreter.newValue(null));
                    }
                    break;
                }
                break;
            }
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86: {
                final V value4 = this.pop();
                final V value2 = this.pop();
                final V value3 = this.pop();
                interpreter.ternaryOperation(insn, value3, value2, value4);
                break;
            }
            case 87: {
                if (this.pop().getSize() == 2) {
                    throw new AnalyzerException(insn, "Illegal use of POP");
                }
                break;
            }
            case 88: {
                if (this.pop().getSize() == 1 && this.pop().getSize() != 1) {
                    throw new AnalyzerException(insn, "Illegal use of POP2");
                }
                break;
            }
            case 89: {
                final V value3 = this.pop();
                if (value3.getSize() != 1) {
                    throw new AnalyzerException(insn, "Illegal use of DUP");
                }
                this.push(value3);
                this.push(interpreter.copyOperation(insn, value3));
                break;
            }
            case 90: {
                final V value3 = this.pop();
                final V value2 = this.pop();
                if (value3.getSize() != 1 || value2.getSize() != 1) {
                    throw new AnalyzerException(insn, "Illegal use of DUP_X1");
                }
                this.push(interpreter.copyOperation(insn, value3));
                this.push(value2);
                this.push(value3);
                break;
            }
            case 91: {
                final V value3 = this.pop();
                if (value3.getSize() == 1) {
                    final V value2 = this.pop();
                    if (value2.getSize() != 1) {
                        this.push(interpreter.copyOperation(insn, value3));
                        this.push(value2);
                        this.push(value3);
                        break;
                    }
                    final V value4 = this.pop();
                    if (value4.getSize() == 1) {
                        this.push(interpreter.copyOperation(insn, value3));
                        this.push(value4);
                        this.push(value2);
                        this.push(value3);
                        break;
                    }
                }
                throw new AnalyzerException(insn, "Illegal use of DUP_X2");
            }
            case 92: {
                final V value3 = this.pop();
                if (value3.getSize() != 1) {
                    this.push(value3);
                    this.push(interpreter.copyOperation(insn, value3));
                    break;
                }
                final V value2 = this.pop();
                if (value2.getSize() == 1) {
                    this.push(value2);
                    this.push(value3);
                    this.push(interpreter.copyOperation(insn, value2));
                    this.push(interpreter.copyOperation(insn, value3));
                    break;
                }
                throw new AnalyzerException(insn, "Illegal use of DUP2");
            }
            case 93: {
                final V value3 = this.pop();
                if (value3.getSize() == 1) {
                    final V value2 = this.pop();
                    if (value2.getSize() == 1) {
                        final V value4 = this.pop();
                        if (value4.getSize() == 1) {
                            this.push(interpreter.copyOperation(insn, value2));
                            this.push(interpreter.copyOperation(insn, value3));
                            this.push(value4);
                            this.push(value2);
                            this.push(value3);
                            break;
                        }
                    }
                }
                else {
                    final V value2 = this.pop();
                    if (value2.getSize() == 1) {
                        this.push(interpreter.copyOperation(insn, value3));
                        this.push(value2);
                        this.push(value3);
                        break;
                    }
                }
                throw new AnalyzerException(insn, "Illegal use of DUP2_X1");
            }
            case 94: {
                final V value3 = this.pop();
                if (value3.getSize() == 1) {
                    final V value2 = this.pop();
                    if (value2.getSize() == 1) {
                        final V value4 = this.pop();
                        if (value4.getSize() != 1) {
                            this.push(interpreter.copyOperation(insn, value2));
                            this.push(interpreter.copyOperation(insn, value3));
                            this.push(value4);
                            this.push(value2);
                            this.push(value3);
                            break;
                        }
                        final V value5 = this.pop();
                        if (value5.getSize() == 1) {
                            this.push(interpreter.copyOperation(insn, value2));
                            this.push(interpreter.copyOperation(insn, value3));
                            this.push(value5);
                            this.push(value4);
                            this.push(value2);
                            this.push(value3);
                            break;
                        }
                    }
                }
                else {
                    final V value2 = this.pop();
                    if (value2.getSize() != 1) {
                        this.push(interpreter.copyOperation(insn, value3));
                        this.push(value2);
                        this.push(value3);
                        break;
                    }
                    final V value4 = this.pop();
                    if (value4.getSize() == 1) {
                        this.push(interpreter.copyOperation(insn, value3));
                        this.push(value4);
                        this.push(value2);
                        this.push(value3);
                        break;
                    }
                }
                throw new AnalyzerException(insn, "Illegal use of DUP2_X2");
            }
            case 95: {
                final V value2 = this.pop();
                final V value3 = this.pop();
                if (value3.getSize() != 1 || value2.getSize() != 1) {
                    throw new AnalyzerException(insn, "Illegal use of SWAP");
                }
                this.push(interpreter.copyOperation(insn, value2));
                this.push(interpreter.copyOperation(insn, value3));
                break;
            }
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 115: {
                final V value2 = this.pop();
                final V value3 = this.pop();
                this.push(interpreter.binaryOperation(insn, value3, value2));
                break;
            }
            case 116:
            case 117:
            case 118:
            case 119: {
                this.push(interpreter.unaryOperation(insn, this.pop()));
                break;
            }
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 129:
            case 130:
            case 131: {
                final V value2 = this.pop();
                final V value3 = this.pop();
                this.push(interpreter.binaryOperation(insn, value3, value2));
                break;
            }
            case 132: {
                final int var = ((IincInsnNode)insn).var;
                this.setLocal(var, interpreter.unaryOperation(insn, this.getLocal(var)));
                break;
            }
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 140:
            case 141:
            case 142:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147: {
                this.push(interpreter.unaryOperation(insn, this.pop()));
                break;
            }
            case 148:
            case 149:
            case 150:
            case 151:
            case 152: {
                final V value2 = this.pop();
                final V value3 = this.pop();
                this.push(interpreter.binaryOperation(insn, value3, value2));
                break;
            }
            case 153:
            case 154:
            case 155:
            case 156:
            case 157:
            case 158: {
                interpreter.unaryOperation(insn, this.pop());
                break;
            }
            case 159:
            case 160:
            case 161:
            case 162:
            case 163:
            case 164:
            case 165:
            case 166: {
                final V value2 = this.pop();
                final V value3 = this.pop();
                interpreter.binaryOperation(insn, value3, value2);
                break;
            }
            case 167: {
                break;
            }
            case 168: {
                this.push(interpreter.newOperation(insn));
                break;
            }
            case 169: {
                break;
            }
            case 170:
            case 171: {
                interpreter.unaryOperation(insn, this.pop());
                break;
            }
            case 172:
            case 173:
            case 174:
            case 175:
            case 176: {
                final V value3 = this.pop();
                interpreter.unaryOperation(insn, value3);
                interpreter.returnOperation(insn, value3, this.returnValue);
                break;
            }
            case 177: {
                if (this.returnValue != null) {
                    throw new AnalyzerException(insn, "Incompatible return type");
                }
                break;
            }
            case 178: {
                this.push(interpreter.newOperation(insn));
                break;
            }
            case 179: {
                interpreter.unaryOperation(insn, this.pop());
                break;
            }
            case 180: {
                this.push(interpreter.unaryOperation(insn, this.pop()));
                break;
            }
            case 181: {
                final V value2 = this.pop();
                final V value3 = this.pop();
                interpreter.binaryOperation(insn, value3, value2);
                break;
            }
            case 182:
            case 183:
            case 184:
            case 185: {
                final List<V> values = new ArrayList<V>();
                final String desc = ((MethodInsnNode)insn).desc;
                for (int i = Type.getArgumentTypes(desc).length; i > 0; --i) {
                    values.add(0, this.pop());
                }
                if (insn.getOpcode() != 184) {
                    values.add(0, this.pop());
                }
                if (Type.getReturnType(desc) == Type.VOID_TYPE) {
                    interpreter.naryOperation(insn, (List<? extends V>)values);
                    break;
                }
                this.push(interpreter.naryOperation(insn, (List<? extends V>)values));
                break;
            }
            case 186: {
                final List<V> values = new ArrayList<V>();
                final String desc = ((InvokeDynamicInsnNode)insn).desc;
                for (int i = Type.getArgumentTypes(desc).length; i > 0; --i) {
                    values.add(0, this.pop());
                }
                if (Type.getReturnType(desc) == Type.VOID_TYPE) {
                    interpreter.naryOperation(insn, (List<? extends V>)values);
                    break;
                }
                this.push(interpreter.naryOperation(insn, (List<? extends V>)values));
                break;
            }
            case 187: {
                this.push(interpreter.newOperation(insn));
                break;
            }
            case 188:
            case 189:
            case 190: {
                this.push(interpreter.unaryOperation(insn, this.pop()));
                break;
            }
            case 191: {
                interpreter.unaryOperation(insn, this.pop());
                break;
            }
            case 192:
            case 193: {
                this.push(interpreter.unaryOperation(insn, this.pop()));
                break;
            }
            case 194:
            case 195: {
                interpreter.unaryOperation(insn, this.pop());
                break;
            }
            case 197: {
                final List<V> values = new ArrayList<V>();
                for (int j = ((MultiANewArrayInsnNode)insn).dims; j > 0; --j) {
                    values.add(0, this.pop());
                }
                this.push(interpreter.naryOperation(insn, (List<? extends V>)values));
                break;
            }
            case 198:
            case 199: {
                interpreter.unaryOperation(insn, this.pop());
                break;
            }
            default: {
                throw new RuntimeException("Illegal opcode " + insn.getOpcode());
            }
        }
    }
    
    public boolean merge(final Frame<? extends V> frame, final Interpreter<V> interpreter) throws AnalyzerException {
        if (this.top != frame.top) {
            throw new AnalyzerException(null, "Incompatible stack heights");
        }
        boolean changes = false;
        for (int i = 0; i < this.locals + this.top; ++i) {
            final V v = interpreter.merge(this.values[i], (V)frame.values[i]);
            if (!v.equals(this.values[i])) {
                this.values[i] = v;
                changes = true;
            }
        }
        return changes;
    }
    
    public boolean merge(final Frame<? extends V> frame, final boolean[] access) {
        boolean changes = false;
        for (int i = 0; i < this.locals; ++i) {
            if (!access[i] && !this.values[i].equals(frame.values[i])) {
                this.values[i] = (V)frame.values[i];
                changes = true;
            }
        }
        return changes;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.getLocals(); ++i) {
            sb.append(this.getLocal(i));
        }
        sb.append(' ');
        for (int i = 0; i < this.getStackSize(); ++i) {
            sb.append(this.getStack(i).toString());
        }
        return sb.toString();
    }
}

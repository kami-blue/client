// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.tree.analysis;

import org.spongepowered.asm.lib.tree.InvokeDynamicInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import java.util.List;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;

public class BasicVerifier extends BasicInterpreter
{
    public BasicVerifier() {
        super(327680);
    }
    
    protected BasicVerifier(final int api) {
        super(api);
    }
    
    @Override
    public BasicValue copyOperation(final AbstractInsnNode insn, final BasicValue value) throws AnalyzerException {
        Value expected = null;
        switch (insn.getOpcode()) {
            case 21:
            case 54: {
                expected = BasicValue.INT_VALUE;
                break;
            }
            case 23:
            case 56: {
                expected = BasicValue.FLOAT_VALUE;
                break;
            }
            case 22:
            case 55: {
                expected = BasicValue.LONG_VALUE;
                break;
            }
            case 24:
            case 57: {
                expected = BasicValue.DOUBLE_VALUE;
                break;
            }
            case 25: {
                if (!value.isReference()) {
                    throw new AnalyzerException(insn, null, "an object reference", value);
                }
                return value;
            }
            case 58: {
                if (!value.isReference() && !BasicValue.RETURNADDRESS_VALUE.equals(value)) {
                    throw new AnalyzerException(insn, null, "an object reference or a return address", value);
                }
                return value;
            }
            default: {
                return value;
            }
        }
        if (!expected.equals(value)) {
            throw new AnalyzerException(insn, null, expected, value);
        }
        return value;
    }
    
    @Override
    public BasicValue unaryOperation(final AbstractInsnNode insn, final BasicValue value) throws AnalyzerException {
        BasicValue expected = null;
        switch (insn.getOpcode()) {
            case 116:
            case 132:
            case 133:
            case 134:
            case 135:
            case 145:
            case 146:
            case 147:
            case 153:
            case 154:
            case 155:
            case 156:
            case 157:
            case 158:
            case 170:
            case 171:
            case 172:
            case 188:
            case 189: {
                expected = BasicValue.INT_VALUE;
                break;
            }
            case 118:
            case 139:
            case 140:
            case 141:
            case 174: {
                expected = BasicValue.FLOAT_VALUE;
                break;
            }
            case 117:
            case 136:
            case 137:
            case 138:
            case 173: {
                expected = BasicValue.LONG_VALUE;
                break;
            }
            case 119:
            case 142:
            case 143:
            case 144:
            case 175: {
                expected = BasicValue.DOUBLE_VALUE;
                break;
            }
            case 180: {
                expected = this.newValue(Type.getObjectType(((FieldInsnNode)insn).owner));
                break;
            }
            case 192: {
                if (!value.isReference()) {
                    throw new AnalyzerException(insn, null, "an object reference", value);
                }
                return super.unaryOperation(insn, value);
            }
            case 190: {
                if (!this.isArrayValue(value)) {
                    throw new AnalyzerException(insn, null, "an array reference", value);
                }
                return super.unaryOperation(insn, value);
            }
            case 176:
            case 191:
            case 193:
            case 194:
            case 195:
            case 198:
            case 199: {
                if (!value.isReference()) {
                    throw new AnalyzerException(insn, null, "an object reference", value);
                }
                return super.unaryOperation(insn, value);
            }
            case 179: {
                expected = this.newValue(Type.getType(((FieldInsnNode)insn).desc));
                break;
            }
            default: {
                throw new Error("Internal error.");
            }
        }
        if (!this.isSubTypeOf(value, expected)) {
            throw new AnalyzerException(insn, null, expected, value);
        }
        return super.unaryOperation(insn, value);
    }
    
    @Override
    public BasicValue binaryOperation(final AbstractInsnNode insn, final BasicValue value1, final BasicValue value2) throws AnalyzerException {
        BasicValue expected1 = null;
        BasicValue expected2 = null;
        switch (insn.getOpcode()) {
            case 46: {
                expected1 = this.newValue(Type.getType("[I"));
                expected2 = BasicValue.INT_VALUE;
                break;
            }
            case 51: {
                if (this.isSubTypeOf(value1, this.newValue(Type.getType("[Z")))) {
                    expected1 = this.newValue(Type.getType("[Z"));
                }
                else {
                    expected1 = this.newValue(Type.getType("[B"));
                }
                expected2 = BasicValue.INT_VALUE;
                break;
            }
            case 52: {
                expected1 = this.newValue(Type.getType("[C"));
                expected2 = BasicValue.INT_VALUE;
                break;
            }
            case 53: {
                expected1 = this.newValue(Type.getType("[S"));
                expected2 = BasicValue.INT_VALUE;
                break;
            }
            case 47: {
                expected1 = this.newValue(Type.getType("[J"));
                expected2 = BasicValue.INT_VALUE;
                break;
            }
            case 48: {
                expected1 = this.newValue(Type.getType("[F"));
                expected2 = BasicValue.INT_VALUE;
                break;
            }
            case 49: {
                expected1 = this.newValue(Type.getType("[D"));
                expected2 = BasicValue.INT_VALUE;
                break;
            }
            case 50: {
                expected1 = this.newValue(Type.getType("[Ljava/lang/Object;"));
                expected2 = BasicValue.INT_VALUE;
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
            case 159:
            case 160:
            case 161:
            case 162:
            case 163:
            case 164: {
                expected1 = BasicValue.INT_VALUE;
                expected2 = BasicValue.INT_VALUE;
                break;
            }
            case 98:
            case 102:
            case 106:
            case 110:
            case 114:
            case 149:
            case 150: {
                expected1 = BasicValue.FLOAT_VALUE;
                expected2 = BasicValue.FLOAT_VALUE;
                break;
            }
            case 97:
            case 101:
            case 105:
            case 109:
            case 113:
            case 127:
            case 129:
            case 131:
            case 148: {
                expected1 = BasicValue.LONG_VALUE;
                expected2 = BasicValue.LONG_VALUE;
                break;
            }
            case 121:
            case 123:
            case 125: {
                expected1 = BasicValue.LONG_VALUE;
                expected2 = BasicValue.INT_VALUE;
                break;
            }
            case 99:
            case 103:
            case 107:
            case 111:
            case 115:
            case 151:
            case 152: {
                expected1 = BasicValue.DOUBLE_VALUE;
                expected2 = BasicValue.DOUBLE_VALUE;
                break;
            }
            case 165:
            case 166: {
                expected1 = BasicValue.REFERENCE_VALUE;
                expected2 = BasicValue.REFERENCE_VALUE;
                break;
            }
            case 181: {
                final FieldInsnNode fin = (FieldInsnNode)insn;
                expected1 = this.newValue(Type.getObjectType(fin.owner));
                expected2 = this.newValue(Type.getType(fin.desc));
                break;
            }
            default: {
                throw new Error("Internal error.");
            }
        }
        if (!this.isSubTypeOf(value1, expected1)) {
            throw new AnalyzerException(insn, "First argument", expected1, value1);
        }
        if (!this.isSubTypeOf(value2, expected2)) {
            throw new AnalyzerException(insn, "Second argument", expected2, value2);
        }
        if (insn.getOpcode() == 50) {
            return this.getElementValue(value1);
        }
        return super.binaryOperation(insn, value1, value2);
    }
    
    @Override
    public BasicValue ternaryOperation(final AbstractInsnNode insn, final BasicValue value1, final BasicValue value2, final BasicValue value3) throws AnalyzerException {
        BasicValue expected1 = null;
        BasicValue expected2 = null;
        switch (insn.getOpcode()) {
            case 79: {
                expected1 = this.newValue(Type.getType("[I"));
                expected2 = BasicValue.INT_VALUE;
                break;
            }
            case 84: {
                if (this.isSubTypeOf(value1, this.newValue(Type.getType("[Z")))) {
                    expected1 = this.newValue(Type.getType("[Z"));
                }
                else {
                    expected1 = this.newValue(Type.getType("[B"));
                }
                expected2 = BasicValue.INT_VALUE;
                break;
            }
            case 85: {
                expected1 = this.newValue(Type.getType("[C"));
                expected2 = BasicValue.INT_VALUE;
                break;
            }
            case 86: {
                expected1 = this.newValue(Type.getType("[S"));
                expected2 = BasicValue.INT_VALUE;
                break;
            }
            case 80: {
                expected1 = this.newValue(Type.getType("[J"));
                expected2 = BasicValue.LONG_VALUE;
                break;
            }
            case 81: {
                expected1 = this.newValue(Type.getType("[F"));
                expected2 = BasicValue.FLOAT_VALUE;
                break;
            }
            case 82: {
                expected1 = this.newValue(Type.getType("[D"));
                expected2 = BasicValue.DOUBLE_VALUE;
                break;
            }
            case 83: {
                expected1 = value1;
                expected2 = BasicValue.REFERENCE_VALUE;
                break;
            }
            default: {
                throw new Error("Internal error.");
            }
        }
        if (!this.isSubTypeOf(value1, expected1)) {
            throw new AnalyzerException(insn, "First argument", "a " + expected1 + " array reference", value1);
        }
        if (!BasicValue.INT_VALUE.equals(value2)) {
            throw new AnalyzerException(insn, "Second argument", BasicValue.INT_VALUE, value2);
        }
        if (!this.isSubTypeOf(value3, expected2)) {
            throw new AnalyzerException(insn, "Third argument", expected2, value3);
        }
        return null;
    }
    
    @Override
    public BasicValue naryOperation(final AbstractInsnNode insn, final List<? extends BasicValue> values) throws AnalyzerException {
        final int opcode = insn.getOpcode();
        if (opcode == 197) {
            for (int i = 0; i < values.size(); ++i) {
                if (!BasicValue.INT_VALUE.equals(values.get(i))) {
                    throw new AnalyzerException(insn, null, BasicValue.INT_VALUE, (Value)values.get(i));
                }
            }
        }
        else {
            int i = 0;
            int j = 0;
            if (opcode != 184 && opcode != 186) {
                final Type owner = Type.getObjectType(((MethodInsnNode)insn).owner);
                if (!this.isSubTypeOf((BasicValue)values.get(i++), this.newValue(owner))) {
                    throw new AnalyzerException(insn, "Method owner", this.newValue(owner), (Value)values.get(0));
                }
            }
            final String desc = (opcode == 186) ? ((InvokeDynamicInsnNode)insn).desc : ((MethodInsnNode)insn).desc;
            final Type[] args = Type.getArgumentTypes(desc);
            while (i < values.size()) {
                final BasicValue expected = this.newValue(args[j++]);
                final BasicValue encountered = (BasicValue)values.get(i++);
                if (!this.isSubTypeOf(encountered, expected)) {
                    throw new AnalyzerException(insn, "Argument " + j, expected, encountered);
                }
            }
        }
        return super.naryOperation(insn, values);
    }
    
    @Override
    public void returnOperation(final AbstractInsnNode insn, final BasicValue value, final BasicValue expected) throws AnalyzerException {
        if (!this.isSubTypeOf(value, expected)) {
            throw new AnalyzerException(insn, "Incompatible return type", expected, value);
        }
    }
    
    protected boolean isArrayValue(final BasicValue value) {
        return value.isReference();
    }
    
    protected BasicValue getElementValue(final BasicValue objectArrayValue) throws AnalyzerException {
        return BasicValue.REFERENCE_VALUE;
    }
    
    protected boolean isSubTypeOf(final BasicValue value, final BasicValue expected) {
        return value.equals(expected);
    }
}

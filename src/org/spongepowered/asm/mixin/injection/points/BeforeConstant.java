// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.points;

import org.apache.logging.log4j.LogManager;
import java.util.ListIterator;
import org.spongepowered.asm.lib.tree.FrameNode;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import java.util.Iterator;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.mixin.refmap.IMixinContext;
import org.spongepowered.asm.lib.Type;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.injection.InjectionPoint;

@AtCode("CONSTANT")
public class BeforeConstant extends InjectionPoint
{
    private static final Logger logger;
    private final int ordinal;
    private final boolean nullValue;
    private final Integer intValue;
    private final Float floatValue;
    private final Long longValue;
    private final Double doubleValue;
    private final String stringValue;
    private final Type typeValue;
    private final int[] expandOpcodes;
    private final boolean expand;
    private final String matchByType;
    private final boolean log;
    
    public BeforeConstant(final IMixinContext context, final AnnotationNode node, final String returnType) {
        super(Annotations.getValue(node, "slice", ""), Selector.DEFAULT, null);
        final Boolean empty = Annotations.getValue(node, "nullValue", (Boolean)null);
        this.ordinal = Annotations.getValue(node, "ordinal", -1);
        this.nullValue = (empty != null && empty);
        this.intValue = Annotations.getValue(node, "intValue", (Integer)null);
        this.floatValue = Annotations.getValue(node, "floatValue", (Float)null);
        this.longValue = Annotations.getValue(node, "longValue", (Long)null);
        this.doubleValue = Annotations.getValue(node, "doubleValue", (Double)null);
        this.stringValue = Annotations.getValue(node, "stringValue", (String)null);
        this.typeValue = Annotations.getValue(node, "classValue", (Type)null);
        this.matchByType = this.validateDiscriminator(context, returnType, empty, "on @Constant annotation");
        this.expandOpcodes = this.parseExpandOpcodes(Annotations.getValue(node, "expandZeroConditions", true, Constant.Condition.class));
        this.expand = (this.expandOpcodes.length > 0);
        this.log = Annotations.getValue(node, "log", Boolean.FALSE);
    }
    
    public BeforeConstant(final InjectionPointData data) {
        super(data);
        final String strNullValue = data.get("nullValue", null);
        final Boolean empty = (strNullValue != null) ? Boolean.valueOf(Boolean.parseBoolean(strNullValue)) : null;
        this.ordinal = data.getOrdinal();
        this.nullValue = (empty != null && empty);
        this.intValue = Ints.tryParse(data.get("intValue", ""));
        this.floatValue = Floats.tryParse(data.get("floatValue", ""));
        this.longValue = Longs.tryParse(data.get("longValue", ""));
        this.doubleValue = Doubles.tryParse(data.get("doubleValue", ""));
        this.stringValue = data.get("stringValue", null);
        final String strClassValue = data.get("classValue", null);
        this.typeValue = ((strClassValue != null) ? Type.getObjectType(strClassValue.replace('.', '/')) : null);
        this.matchByType = this.validateDiscriminator(data.getContext(), "V", empty, "in @At(\"CONSTANT\") args");
        if ("V".equals(this.matchByType)) {
            throw new InvalidInjectionException(data.getContext(), "No constant discriminator could be parsed in @At(\"CONSTANT\") args");
        }
        final List<Constant.Condition> conditions = new ArrayList<Constant.Condition>();
        final String strConditions = data.get("expandZeroConditions", "").toLowerCase();
        for (final Constant.Condition condition : Constant.Condition.values()) {
            if (strConditions.contains(condition.name().toLowerCase())) {
                conditions.add(condition);
            }
        }
        this.expandOpcodes = this.parseExpandOpcodes(conditions);
        this.expand = (this.expandOpcodes.length > 0);
        this.log = data.get("log", false);
    }
    
    private String validateDiscriminator(final IMixinContext context, String returnType, final Boolean empty, final String type) {
        final int c = count(empty, this.intValue, this.floatValue, this.longValue, this.doubleValue, this.stringValue, this.typeValue);
        if (c == 1) {
            returnType = null;
        }
        else if (c > 1) {
            throw new InvalidInjectionException(context, "Conflicting constant discriminators specified " + type + " for " + context);
        }
        return returnType;
    }
    
    private int[] parseExpandOpcodes(final List<Constant.Condition> conditions) {
        final Set<Integer> opcodes = new HashSet<Integer>();
        for (final Constant.Condition condition : conditions) {
            final Constant.Condition actual = condition.getEquivalentCondition();
            for (final int opcode : actual.getOpcodes()) {
                opcodes.add(opcode);
            }
        }
        return Ints.toArray((Collection)opcodes);
    }
    
    @Override
    public boolean find(final String desc, final InsnList insns, final Collection<AbstractInsnNode> nodes) {
        boolean found = false;
        this.log("BeforeConstant is searching for constants in method with descriptor {}", desc);
        final ListIterator<AbstractInsnNode> iter = insns.iterator();
        int ordinal = 0;
        int last = 0;
        while (iter.hasNext()) {
            final AbstractInsnNode insn = iter.next();
            final boolean matchesInsn = this.expand ? this.matchesConditionalInsn(last, insn) : this.matchesConstantInsn(insn);
            if (matchesInsn) {
                this.log("    BeforeConstant found a matching constant{} at ordinal {}", (this.matchByType != null) ? " TYPE" : " value", ordinal);
                if (this.ordinal == -1 || this.ordinal == ordinal) {
                    this.log("      BeforeConstant found {}", Bytecode.describeNode(insn).trim());
                    nodes.add(insn);
                    found = true;
                }
                ++ordinal;
            }
            if (!(insn instanceof LabelNode) && !(insn instanceof FrameNode)) {
                last = insn.getOpcode();
            }
        }
        return found;
    }
    
    private boolean matchesConditionalInsn(final int last, final AbstractInsnNode insn) {
        final int[] expandOpcodes = this.expandOpcodes;
        final int length = expandOpcodes.length;
        int i = 0;
        while (i < length) {
            final int conditionalOpcode = expandOpcodes[i];
            final int opcode = insn.getOpcode();
            if (opcode == conditionalOpcode) {
                if (last == 148 || last == 149 || last == 150 || last == 151 || last == 152) {
                    this.log("  BeforeConstant is ignoring {} following {}", Bytecode.getOpcodeName(opcode), Bytecode.getOpcodeName(last));
                    return false;
                }
                this.log("  BeforeConstant found {} instruction", Bytecode.getOpcodeName(opcode));
                return true;
            }
            else {
                ++i;
            }
        }
        if (this.intValue != null && this.intValue == 0 && Bytecode.isConstant(insn)) {
            final Object value = Bytecode.getConstant(insn);
            this.log("  BeforeConstant found INTEGER constant: value = {}", value);
            return value instanceof Integer && (int)value == 0;
        }
        return false;
    }
    
    private boolean matchesConstantInsn(final AbstractInsnNode insn) {
        if (!Bytecode.isConstant(insn)) {
            return false;
        }
        final Object value = Bytecode.getConstant(insn);
        if (value == null) {
            this.log("  BeforeConstant found NULL constant: nullValue = {}", this.nullValue);
            return this.nullValue || "Ljava/lang/Object;".equals(this.matchByType);
        }
        if (value instanceof Integer) {
            this.log("  BeforeConstant found INTEGER constant: value = {}, intValue = {}", value, this.intValue);
            return value.equals(this.intValue) || "I".equals(this.matchByType);
        }
        if (value instanceof Float) {
            this.log("  BeforeConstant found FLOAT constant: value = {}, floatValue = {}", value, this.floatValue);
            return value.equals(this.floatValue) || "F".equals(this.matchByType);
        }
        if (value instanceof Long) {
            this.log("  BeforeConstant found LONG constant: value = {}, longValue = {}", value, this.longValue);
            return value.equals(this.longValue) || "J".equals(this.matchByType);
        }
        if (value instanceof Double) {
            this.log("  BeforeConstant found DOUBLE constant: value = {}, doubleValue = {}", value, this.doubleValue);
            return value.equals(this.doubleValue) || "D".equals(this.matchByType);
        }
        if (value instanceof String) {
            this.log("  BeforeConstant found STRING constant: value = {}, stringValue = {}", value, this.stringValue);
            return value.equals(this.stringValue) || "Ljava/lang/String;".equals(this.matchByType);
        }
        if (value instanceof Type) {
            this.log("  BeforeConstant found CLASS constant: value = {}, typeValue = {}", value, this.typeValue);
            return value.equals(this.typeValue) || "Ljava/lang/Class;".equals(this.matchByType);
        }
        return false;
    }
    
    protected void log(final String message, final Object... params) {
        if (this.log) {
            BeforeConstant.logger.info(message, params);
        }
    }
    
    private static int count(final Object... values) {
        int counter = 0;
        for (final Object value : values) {
            if (value != null) {
                ++counter;
            }
        }
        return counter;
    }
    
    static {
        logger = LogManager.getLogger("mixin");
    }
}

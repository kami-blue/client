// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.points;

import java.util.Iterator;
import org.spongepowered.asm.util.Bytecode;
import java.util.Collection;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;
import org.spongepowered.asm.mixin.injection.InjectionPoint;

@AtCode("FIELD")
public class BeforeFieldAccess extends BeforeInvoke
{
    private static final String ARRAY_GET = "get";
    private static final String ARRAY_SET = "set";
    private static final String ARRAY_LENGTH = "length";
    public static final int ARRAY_SEARCH_FUZZ_DEFAULT = 8;
    private final int opcode;
    private final int arrOpcode;
    private final int fuzzFactor;
    
    public BeforeFieldAccess(final InjectionPointData data) {
        super(data);
        this.opcode = data.getOpcode(-1, 180, 181, 178, 179, -1);
        final String array = data.get("array", "");
        this.arrOpcode = ("get".equalsIgnoreCase(array) ? 46 : ("set".equalsIgnoreCase(array) ? 79 : ("length".equalsIgnoreCase(array) ? 190 : 0)));
        this.fuzzFactor = Math.min(Math.max(data.get("fuzz", 8), 1), 32);
    }
    
    public int getFuzzFactor() {
        return this.fuzzFactor;
    }
    
    public int getArrayOpcode() {
        return this.arrOpcode;
    }
    
    private int getArrayOpcode(final String desc) {
        if (this.arrOpcode != 190) {
            return Type.getType(desc).getElementType().getOpcode(this.arrOpcode);
        }
        return this.arrOpcode;
    }
    
    @Override
    protected boolean matchesInsn(final AbstractInsnNode insn) {
        return insn instanceof FieldInsnNode && (((FieldInsnNode)insn).getOpcode() == this.opcode || this.opcode == -1) && (this.arrOpcode == 0 || ((insn.getOpcode() == 178 || insn.getOpcode() == 180) && Type.getType(((FieldInsnNode)insn).desc).getSort() == 9));
    }
    
    @Override
    protected boolean addInsn(final InsnList insns, final Collection<AbstractInsnNode> nodes, final AbstractInsnNode insn) {
        if (this.arrOpcode > 0) {
            final FieldInsnNode fieldInsn = (FieldInsnNode)insn;
            final int accOpcode = this.getArrayOpcode(fieldInsn.desc);
            this.log("{} > > > > searching for array access opcode {} fuzz={}", this.className, Bytecode.getOpcodeName(accOpcode), this.fuzzFactor);
            if (findArrayNode(insns, fieldInsn, accOpcode, this.fuzzFactor) == null) {
                this.log("{} > > > > > failed to locate matching insn", this.className);
                return false;
            }
        }
        this.log("{} > > > > > adding matching insn", this.className);
        return super.addInsn(insns, nodes, insn);
    }
    
    public static AbstractInsnNode findArrayNode(final InsnList insns, final FieldInsnNode fieldNode, final int opcode, final int searchRange) {
        int pos = 0;
        final Iterator<AbstractInsnNode> iter = insns.iterator(insns.indexOf(fieldNode) + 1);
        while (iter.hasNext()) {
            final AbstractInsnNode insn = iter.next();
            if (insn.getOpcode() == opcode) {
                return insn;
            }
            if (insn.getOpcode() == 190 && pos == 0) {
                return null;
            }
            if (insn instanceof FieldInsnNode) {
                final FieldInsnNode field = (FieldInsnNode)insn;
                if (field.desc.equals(fieldNode.desc) && field.name.equals(fieldNode.name) && field.owner.equals(fieldNode.owner)) {
                    return null;
                }
            }
            if (pos++ > searchRange) {
                return null;
            }
        }
        return null;
    }
}

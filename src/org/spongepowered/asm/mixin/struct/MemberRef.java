// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.struct;

import org.spongepowered.asm.mixin.transformer.throwables.MixinTransformerError;
import org.spongepowered.asm.lib.Handle;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.util.Bytecode;

public abstract class MemberRef
{
    private static final int[] H_OPCODES;
    
    public abstract boolean isField();
    
    public abstract int getOpcode();
    
    public abstract void setOpcode(final int p0);
    
    public abstract String getOwner();
    
    public abstract void setOwner(final String p0);
    
    public abstract String getName();
    
    public abstract void setName(final String p0);
    
    public abstract String getDesc();
    
    public abstract void setDesc(final String p0);
    
    @Override
    public String toString() {
        final String name = Bytecode.getOpcodeName(this.getOpcode());
        return String.format("%s for %s.%s%s%s", name, this.getOwner(), this.getName(), this.isField() ? ":" : "", this.getDesc());
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof MemberRef)) {
            return false;
        }
        final MemberRef other = (MemberRef)obj;
        return this.getOpcode() == other.getOpcode() && this.getOwner().equals(other.getOwner()) && this.getName().equals(other.getName()) && this.getDesc().equals(other.getDesc());
    }
    
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
    
    static int opcodeFromTag(final int tag) {
        return (tag >= 0 && tag < MemberRef.H_OPCODES.length) ? MemberRef.H_OPCODES[tag] : 0;
    }
    
    static int tagFromOpcode(final int opcode) {
        for (int tag = 1; tag < MemberRef.H_OPCODES.length; ++tag) {
            if (MemberRef.H_OPCODES[tag] == opcode) {
                return tag;
            }
        }
        return 0;
    }
    
    static {
        H_OPCODES = new int[] { 0, 180, 178, 181, 179, 182, 184, 183, 183, 185 };
    }
    
    public static final class Method extends MemberRef
    {
        private static final int OPCODES = 191;
        public final MethodInsnNode insn;
        
        public Method(final MethodInsnNode insn) {
            this.insn = insn;
        }
        
        @Override
        public boolean isField() {
            return false;
        }
        
        @Override
        public int getOpcode() {
            return this.insn.getOpcode();
        }
        
        @Override
        public void setOpcode(final int opcode) {
            if ((opcode & 0xBF) == 0x0) {
                throw new IllegalArgumentException("Invalid opcode for method instruction: 0x" + Integer.toHexString(opcode));
            }
            this.insn.setOpcode(opcode);
        }
        
        @Override
        public String getOwner() {
            return this.insn.owner;
        }
        
        @Override
        public void setOwner(final String owner) {
            this.insn.owner = owner;
        }
        
        @Override
        public String getName() {
            return this.insn.name;
        }
        
        @Override
        public void setName(final String name) {
            this.insn.name = name;
        }
        
        @Override
        public String getDesc() {
            return this.insn.desc;
        }
        
        @Override
        public void setDesc(final String desc) {
            this.insn.desc = desc;
        }
    }
    
    public static final class Field extends MemberRef
    {
        private static final int OPCODES = 183;
        public final FieldInsnNode insn;
        
        public Field(final FieldInsnNode insn) {
            this.insn = insn;
        }
        
        @Override
        public boolean isField() {
            return true;
        }
        
        @Override
        public int getOpcode() {
            return this.insn.getOpcode();
        }
        
        @Override
        public void setOpcode(final int opcode) {
            if ((opcode & 0xB7) == 0x0) {
                throw new IllegalArgumentException("Invalid opcode for field instruction: 0x" + Integer.toHexString(opcode));
            }
            this.insn.setOpcode(opcode);
        }
        
        @Override
        public String getOwner() {
            return this.insn.owner;
        }
        
        @Override
        public void setOwner(final String owner) {
            this.insn.owner = owner;
        }
        
        @Override
        public String getName() {
            return this.insn.name;
        }
        
        @Override
        public void setName(final String name) {
            this.insn.name = name;
        }
        
        @Override
        public String getDesc() {
            return this.insn.desc;
        }
        
        @Override
        public void setDesc(final String desc) {
            this.insn.desc = desc;
        }
    }
    
    public static final class Handle extends MemberRef
    {
        private org.spongepowered.asm.lib.Handle handle;
        
        public Handle(final org.spongepowered.asm.lib.Handle handle) {
            this.handle = handle;
        }
        
        public org.spongepowered.asm.lib.Handle getMethodHandle() {
            return this.handle;
        }
        
        @Override
        public boolean isField() {
            switch (this.handle.getTag()) {
                case 5:
                case 6:
                case 7:
                case 8:
                case 9: {
                    return false;
                }
                case 1:
                case 2:
                case 3:
                case 4: {
                    return true;
                }
                default: {
                    throw new MixinTransformerError("Invalid tag " + this.handle.getTag() + " for method handle " + this.handle + ".");
                }
            }
        }
        
        @Override
        public int getOpcode() {
            final int opcode = MemberRef.opcodeFromTag(this.handle.getTag());
            if (opcode == 0) {
                throw new MixinTransformerError("Invalid tag " + this.handle.getTag() + " for method handle " + this.handle + ".");
            }
            return opcode;
        }
        
        @Override
        public void setOpcode(final int opcode) {
            final int tag = MemberRef.tagFromOpcode(opcode);
            if (tag == 0) {
                throw new MixinTransformerError("Invalid opcode " + Bytecode.getOpcodeName(opcode) + " for method handle " + this.handle + ".");
            }
            final boolean itf = tag == 9;
            this.handle = new org.spongepowered.asm.lib.Handle(tag, this.handle.getOwner(), this.handle.getName(), this.handle.getDesc(), itf);
        }
        
        @Override
        public String getOwner() {
            return this.handle.getOwner();
        }
        
        @Override
        public void setOwner(final String owner) {
            final boolean itf = this.handle.getTag() == 9;
            this.handle = new org.spongepowered.asm.lib.Handle(this.handle.getTag(), owner, this.handle.getName(), this.handle.getDesc(), itf);
        }
        
        @Override
        public String getName() {
            return this.handle.getName();
        }
        
        @Override
        public void setName(final String name) {
            final boolean itf = this.handle.getTag() == 9;
            this.handle = new org.spongepowered.asm.lib.Handle(this.handle.getTag(), this.handle.getOwner(), name, this.handle.getDesc(), itf);
        }
        
        @Override
        public String getDesc() {
            return this.handle.getDesc();
        }
        
        @Override
        public void setDesc(final String desc) {
            final boolean itf = this.handle.getTag() == 9;
            this.handle = new org.spongepowered.asm.lib.Handle(this.handle.getTag(), this.handle.getOwner(), this.handle.getName(), desc, itf);
        }
    }
}

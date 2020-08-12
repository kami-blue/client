// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.util.asm;

import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.lib.MethodVisitor;

public class MethodVisitorEx extends MethodVisitor
{
    public MethodVisitorEx(final MethodVisitor mv) {
        super(327680, mv);
    }
    
    public void visitConstant(final byte constant) {
        if (constant > -2 && constant < 6) {
            this.visitInsn(Bytecode.CONSTANTS_INT[constant + 1]);
            return;
        }
        this.visitIntInsn(16, constant);
    }
}

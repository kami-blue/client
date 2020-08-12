// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib;

class CurrentFrame extends Frame
{
    @Override
    void execute(final int opcode, final int arg, final ClassWriter cw, final Item item) {
        super.execute(opcode, arg, cw, item);
        final Frame successor = new Frame();
        this.merge(cw, successor, 0);
        this.set(successor);
        this.owner.inputStackTop = 0;
    }
}

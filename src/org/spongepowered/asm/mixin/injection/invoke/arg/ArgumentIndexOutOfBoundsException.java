// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.invoke.arg;

public class ArgumentIndexOutOfBoundsException extends IndexOutOfBoundsException
{
    private static final long serialVersionUID = 1L;
    
    public ArgumentIndexOutOfBoundsException(final int index) {
        super("Argument index is out of bounds: " + index);
    }
}

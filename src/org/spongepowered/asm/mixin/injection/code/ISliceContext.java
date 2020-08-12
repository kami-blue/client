// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.code;

import org.spongepowered.asm.mixin.injection.IInjectionPointContext;

public interface ISliceContext extends IInjectionPointContext
{
    MethodSlice getSlice(final String p0);
}

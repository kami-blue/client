// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.modify;

import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;
import org.spongepowered.asm.mixin.injection.InjectionPoint;

@AtCode("STORE")
public class AfterStoreLocal extends BeforeLoadLocal
{
    public AfterStoreLocal(final InjectionPointData data) {
        super(data, 54, true);
    }
}

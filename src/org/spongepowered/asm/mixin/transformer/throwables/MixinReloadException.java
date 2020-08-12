// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer.throwables;

import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.throwables.MixinException;

public class MixinReloadException extends MixinException
{
    private static final long serialVersionUID = 2L;
    private final IMixinInfo mixinInfo;
    
    public MixinReloadException(final IMixinInfo mixinInfo, final String message) {
        super(message);
        this.mixinInfo = mixinInfo;
    }
    
    public IMixinInfo getMixinInfo() {
        return this.mixinInfo;
    }
}

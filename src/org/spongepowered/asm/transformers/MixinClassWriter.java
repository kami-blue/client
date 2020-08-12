// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.transformers;

import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.ClassWriter;

public class MixinClassWriter extends ClassWriter
{
    public MixinClassWriter(final int flags) {
        super(flags);
    }
    
    public MixinClassWriter(final ClassReader classReader, final int flags) {
        super(classReader, flags);
    }
    
    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        return ClassInfo.getCommonSuperClass(type1, type2).getName();
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation.mapping;

import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.tools.obfuscation.ObfuscationType;

public interface IMappingWriter
{
    void write(final String p0, final ObfuscationType p1, final IMappingConsumer.MappingSet<MappingField> p2, final IMappingConsumer.MappingSet<MappingMethod> p3);
}

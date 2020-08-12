// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation.mapping;

import com.google.common.base.Objects;
import java.util.LinkedHashSet;
import org.spongepowered.asm.obfuscation.mapping.IMapping;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.tools.obfuscation.ObfuscationType;

public interface IMappingConsumer
{
    void clear();
    
    void addFieldMapping(final ObfuscationType p0, final MappingField p1, final MappingField p2);
    
    void addMethodMapping(final ObfuscationType p0, final MappingMethod p1, final MappingMethod p2);
    
    MappingSet<MappingField> getFieldMappings(final ObfuscationType p0);
    
    MappingSet<MappingMethod> getMethodMappings(final ObfuscationType p0);
    
    public static class MappingSet<TMapping extends IMapping<TMapping>> extends LinkedHashSet<Pair<TMapping>>
    {
        private static final long serialVersionUID = 1L;
        
        public static class Pair<TMapping extends IMapping<TMapping>>
        {
            public final TMapping from;
            public final TMapping to;
            
            public Pair(final TMapping from, final TMapping to) {
                this.from = from;
                this.to = to;
            }
            
            @Override
            public boolean equals(final Object obj) {
                if (!(obj instanceof Pair)) {
                    return false;
                }
                final Pair<TMapping> other = (Pair<TMapping>)obj;
                return Objects.equal((Object)this.from, (Object)other.from) && Objects.equal((Object)this.to, (Object)other.to);
            }
            
            @Override
            public int hashCode() {
                return Objects.hashCode(new Object[] { this.from, this.to });
            }
            
            @Override
            public String toString() {
                return String.format("%s -> %s", this.from, this.to);
            }
        }
    }
}

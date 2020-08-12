// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation.mapping;

import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import java.io.IOException;
import java.io.File;

public interface IMappingProvider
{
    void clear();
    
    boolean isEmpty();
    
    void read(final File p0) throws IOException;
    
    MappingMethod getMethodMapping(final MappingMethod p0);
    
    MappingField getFieldMapping(final MappingField p0);
    
    String getClassMapping(final String p0);
    
    String getPackageMapping(final String p0);
}

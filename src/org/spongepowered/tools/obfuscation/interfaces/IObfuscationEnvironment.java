// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation.interfaces;

import org.spongepowered.tools.obfuscation.mapping.IMappingConsumer;
import java.util.Collection;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;

public interface IObfuscationEnvironment
{
    MappingMethod getObfMethod(final MemberInfo p0);
    
    MappingMethod getObfMethod(final MappingMethod p0);
    
    MappingMethod getObfMethod(final MappingMethod p0, final boolean p1);
    
    MappingField getObfField(final MemberInfo p0);
    
    MappingField getObfField(final MappingField p0);
    
    MappingField getObfField(final MappingField p0, final boolean p1);
    
    String getObfClass(final String p0);
    
    MemberInfo remapDescriptor(final MemberInfo p0);
    
    String remapDescriptor(final String p0);
    
    void writeMappings(final Collection<IMappingConsumer> p0);
}

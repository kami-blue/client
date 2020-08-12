// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.obfuscation.mapping.mcp;

import org.spongepowered.asm.obfuscation.mapping.common.MappingField;

public class MappingFieldSrg extends MappingField
{
    private final String srg;
    
    public MappingFieldSrg(final String srg) {
        super(getOwnerFromSrg(srg), getNameFromSrg(srg), null);
        this.srg = srg;
    }
    
    public MappingFieldSrg(final MappingField field) {
        super(field.getOwner(), field.getName(), null);
        this.srg = field.getOwner() + "/" + field.getName();
    }
    
    @Override
    public String serialise() {
        return this.srg;
    }
    
    private static String getNameFromSrg(final String srg) {
        if (srg == null) {
            return null;
        }
        final int pos = srg.lastIndexOf(47);
        return (pos > -1) ? srg.substring(pos + 1) : srg;
    }
    
    private static String getOwnerFromSrg(final String srg) {
        if (srg == null) {
            return null;
        }
        final int pos = srg.lastIndexOf(47);
        return (pos > -1) ? srg.substring(0, pos) : null;
    }
}

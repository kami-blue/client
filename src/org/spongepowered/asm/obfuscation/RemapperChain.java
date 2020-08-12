// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.obfuscation;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.extensibility.IRemapper;

public class RemapperChain implements IRemapper
{
    private final List<IRemapper> remappers;
    
    public RemapperChain() {
        this.remappers = new ArrayList<IRemapper>();
    }
    
    @Override
    public String toString() {
        return String.format("RemapperChain[%d]", this.remappers.size());
    }
    
    public RemapperChain add(final IRemapper remapper) {
        this.remappers.add(remapper);
        return this;
    }
    
    @Override
    public String mapMethodName(final String owner, String name, final String desc) {
        for (final IRemapper remapper : this.remappers) {
            final String newName = remapper.mapMethodName(owner, name, desc);
            if (newName != null && !newName.equals(name)) {
                name = newName;
            }
        }
        return name;
    }
    
    @Override
    public String mapFieldName(final String owner, String name, final String desc) {
        for (final IRemapper remapper : this.remappers) {
            final String newName = remapper.mapFieldName(owner, name, desc);
            if (newName != null && !newName.equals(name)) {
                name = newName;
            }
        }
        return name;
    }
    
    @Override
    public String map(String typeName) {
        for (final IRemapper remapper : this.remappers) {
            final String newName = remapper.map(typeName);
            if (newName != null && !newName.equals(typeName)) {
                typeName = newName;
            }
        }
        return typeName;
    }
    
    @Override
    public String unmap(String typeName) {
        for (final IRemapper remapper : this.remappers) {
            final String newName = remapper.unmap(typeName);
            if (newName != null && !newName.equals(typeName)) {
                typeName = newName;
            }
        }
        return typeName;
    }
    
    @Override
    public String mapDesc(String desc) {
        for (final IRemapper remapper : this.remappers) {
            final String newDesc = remapper.mapDesc(desc);
            if (newDesc != null && !newDesc.equals(desc)) {
                desc = newDesc;
            }
        }
        return desc;
    }
    
    @Override
    public String unmapDesc(String desc) {
        for (final IRemapper remapper : this.remappers) {
            final String newDesc = remapper.unmapDesc(desc);
            if (newDesc != null && !newDesc.equals(desc)) {
                desc = newDesc;
            }
        }
        return desc;
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.obfuscation.mapping.common;

import com.google.common.base.Strings;
import com.google.common.base.Objects;
import org.spongepowered.asm.obfuscation.mapping.IMapping;

public class MappingField implements IMapping<MappingField>
{
    private final String owner;
    private final String name;
    private final String desc;
    
    public MappingField(final String owner, final String name) {
        this(owner, name, null);
    }
    
    public MappingField(final String owner, final String name, final String desc) {
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }
    
    @Override
    public Type getType() {
        return Type.FIELD;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public final String getSimpleName() {
        return this.name;
    }
    
    @Override
    public final String getOwner() {
        return this.owner;
    }
    
    @Override
    public final String getDesc() {
        return this.desc;
    }
    
    @Override
    public MappingField getSuper() {
        return null;
    }
    
    @Override
    public MappingField move(final String newOwner) {
        return new MappingField(newOwner, this.getName(), this.getDesc());
    }
    
    @Override
    public MappingField remap(final String newName) {
        return new MappingField(this.getOwner(), newName, this.getDesc());
    }
    
    @Override
    public MappingField transform(final String newDesc) {
        return new MappingField(this.getOwner(), this.getName(), newDesc);
    }
    
    @Override
    public MappingField copy() {
        return new MappingField(this.getOwner(), this.getName(), this.getDesc());
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(new Object[] { this.toString() });
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || (obj instanceof MappingField && Objects.equal((Object)this.toString(), (Object)((MappingField)obj).toString()));
    }
    
    @Override
    public String serialise() {
        return this.toString();
    }
    
    @Override
    public String toString() {
        return String.format("L%s;%s:%s", this.getOwner(), this.getName(), Strings.nullToEmpty(this.getDesc()));
    }
}

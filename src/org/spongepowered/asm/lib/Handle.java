// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib;

public final class Handle
{
    final int tag;
    final String owner;
    final String name;
    final String desc;
    final boolean itf;
    
    @Deprecated
    public Handle(final int tag, final String owner, final String name, final String desc) {
        this(tag, owner, name, desc, tag == 9);
    }
    
    public Handle(final int tag, final String owner, final String name, final String desc, final boolean itf) {
        this.tag = tag;
        this.owner = owner;
        this.name = name;
        this.desc = desc;
        this.itf = itf;
    }
    
    public int getTag() {
        return this.tag;
    }
    
    public String getOwner() {
        return this.owner;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getDesc() {
        return this.desc;
    }
    
    public boolean isInterface() {
        return this.itf;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Handle)) {
            return false;
        }
        final Handle h = (Handle)obj;
        return this.tag == h.tag && this.itf == h.itf && this.owner.equals(h.owner) && this.name.equals(h.name) && this.desc.equals(h.desc);
    }
    
    @Override
    public int hashCode() {
        return this.tag + (this.itf ? 64 : 0) + this.owner.hashCode() * this.name.hashCode() * this.desc.hashCode();
    }
    
    @Override
    public String toString() {
        return this.owner + '.' + this.name + this.desc + " (" + this.tag + (this.itf ? " itf" : "") + ')';
    }
}

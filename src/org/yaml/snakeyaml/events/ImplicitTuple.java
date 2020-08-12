// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.events;

public class ImplicitTuple
{
    private final boolean plain;
    private final boolean nonPlain;
    
    public ImplicitTuple(final boolean plain, final boolean nonplain) {
        this.plain = plain;
        this.nonPlain = nonplain;
    }
    
    public boolean canOmitTagInPlainScalar() {
        return this.plain;
    }
    
    public boolean canOmitTagInNonPlainScalar() {
        return this.nonPlain;
    }
    
    public boolean bothFalse() {
        return !this.plain && !this.nonPlain;
    }
    
    @Override
    public String toString() {
        return "implicit=[" + this.plain + ", " + this.nonPlain + "]";
    }
}

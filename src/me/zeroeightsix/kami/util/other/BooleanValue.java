// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util.other;

public class BooleanValue extends Value
{
    public BooleanValue() {
    }
    
    public BooleanValue(final String displayName, final String[] alias, final Object value) {
        super(displayName, alias, value);
    }
    
    public boolean getBoolean() {
        return this.getValue();
    }
    
    public void setBoolean(final boolean val) {
        this.setValue(val);
    }
}

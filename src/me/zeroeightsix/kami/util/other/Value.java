// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util.other;

public class Value<T>
{
    private String displayName;
    private String[] alias;
    private T value;
    private T type;
    private Value parent;
    
    public Value() {
    }
    
    public Value(final String displayName, final String[] alias, final T value) {
        this.displayName = displayName;
        this.alias = alias;
        this.value = value;
    }
    
    public Value(final String displayName, final String[] alias, final T value, final T type) {
        this.displayName = displayName;
        this.alias = alias;
        this.value = value;
        this.type = type;
    }
    
    public String getDisplayName() {
        return this.displayName;
    }
    
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }
    
    public String[] getAlias() {
        return this.alias;
    }
    
    public void setAlias(final String[] alias) {
        this.alias = alias;
    }
    
    public T getValue() {
        return this.value;
    }
    
    public void setValue(final T value) {
        this.value = value;
    }
    
    public T getType() {
        return this.type;
    }
    
    public void setType(final T type) {
        this.type = type;
    }
    
    public Value getParent() {
        return this.parent;
    }
    
    public void setParent(final Value parent) {
        this.parent = parent;
    }
}

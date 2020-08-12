// 
// Decompiled by Procyon v0.5.36
// 

package com.github.mrebhan.ingameaccountswitcher.tools;

import java.io.Serializable;

public class Pair<V1, V2> implements Serializable
{
    private static final long serialVersionUID = 2586850598481149380L;
    private V1 obj1;
    private V2 obj2;
    
    public Pair(final V1 obj1, final V2 obj2) {
        this.obj1 = obj1;
        this.obj2 = obj2;
    }
    
    public V1 getValue1() {
        return this.obj1;
    }
    
    public V2 getValue2() {
        return this.obj2;
    }
    
    @Override
    public String toString() {
        return Pair.class.getName() + "@" + Integer.toHexString(this.hashCode()) + " [" + this.obj1.toString() + ", " + this.obj2.toString() + "]";
    }
}

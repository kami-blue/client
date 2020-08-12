// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.analysis;

import java.util.Collection;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;

public class Subroutine
{
    private List callers;
    private Set access;
    private int start;
    
    public Subroutine(final int start, final int caller) {
        this.callers = new ArrayList();
        this.access = new HashSet();
        this.start = start;
        this.callers.add(new Integer(caller));
    }
    
    public void addCaller(final int caller) {
        this.callers.add(new Integer(caller));
    }
    
    public int start() {
        return this.start;
    }
    
    public void access(final int index) {
        this.access.add(new Integer(index));
    }
    
    public boolean isAccessed(final int index) {
        return this.access.contains(new Integer(index));
    }
    
    public Collection accessed() {
        return this.access;
    }
    
    public Collection callers() {
        return this.callers;
    }
    
    @Override
    public String toString() {
        return "start = " + this.start + " callers = " + this.callers.toString();
    }
}

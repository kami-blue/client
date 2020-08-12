// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.tree.analysis;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Iterator;
import java.util.AbstractSet;

class SmallSet<E> extends AbstractSet<E> implements Iterator<E>
{
    E e1;
    E e2;
    
    static final <T> Set<T> emptySet() {
        return new SmallSet<T>(null, null);
    }
    
    SmallSet(final E e1, final E e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    
    @Override
    public Iterator<E> iterator() {
        return new SmallSet(this.e1, this.e2);
    }
    
    @Override
    public int size() {
        return (this.e1 == null) ? 0 : ((this.e2 == null) ? 1 : 2);
    }
    
    public boolean hasNext() {
        return this.e1 != null;
    }
    
    public E next() {
        if (this.e1 == null) {
            throw new NoSuchElementException();
        }
        final E e = this.e1;
        this.e1 = this.e2;
        this.e2 = null;
        return e;
    }
    
    public void remove() {
    }
    
    Set<E> union(final SmallSet<E> s) {
        if ((s.e1 == this.e1 && s.e2 == this.e2) || (s.e1 == this.e2 && s.e2 == this.e1)) {
            return this;
        }
        if (s.e1 == null) {
            return this;
        }
        if (this.e1 == null) {
            return s;
        }
        if (s.e2 == null) {
            if (this.e2 == null) {
                return new SmallSet(this.e1, s.e1);
            }
            if (s.e1 == this.e1 || s.e1 == this.e2) {
                return this;
            }
        }
        if (this.e2 == null && (this.e1 == s.e1 || this.e1 == s.e2)) {
            return s;
        }
        final HashSet<E> r = new HashSet<E>(4);
        r.add(this.e1);
        if (this.e2 != null) {
            r.add(this.e2);
        }
        r.add(s.e1);
        if (s.e2 != null) {
            r.add(s.e2);
        }
        return r;
    }
}

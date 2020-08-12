// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.analysis;

import java.util.NoSuchElementException;

class IntQueue
{
    private Entry head;
    private Entry tail;
    
    void add(final int value) {
        final Entry entry = new Entry(value);
        if (this.tail != null) {
            this.tail.next = entry;
        }
        this.tail = entry;
        if (this.head == null) {
            this.head = entry;
        }
    }
    
    boolean isEmpty() {
        return this.head == null;
    }
    
    int take() {
        if (this.head == null) {
            throw new NoSuchElementException();
        }
        final int value = this.head.value;
        this.head = this.head.next;
        if (this.head == null) {
            this.tail = null;
        }
        return value;
    }
    
    private static class Entry
    {
        private Entry next;
        private int value;
        
        private Entry(final int value) {
            this.value = value;
        }
    }
}

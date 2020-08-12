// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.tree;

import java.util.NoSuchElementException;
import java.util.ListIterator;
import org.spongepowered.asm.lib.MethodVisitor;

public class InsnList
{
    private int size;
    private AbstractInsnNode first;
    private AbstractInsnNode last;
    AbstractInsnNode[] cache;
    
    public int size() {
        return this.size;
    }
    
    public AbstractInsnNode getFirst() {
        return this.first;
    }
    
    public AbstractInsnNode getLast() {
        return this.last;
    }
    
    public AbstractInsnNode get(final int index) {
        if (index < 0 || index >= this.size) {
            throw new IndexOutOfBoundsException();
        }
        if (this.cache == null) {
            this.cache = this.toArray();
        }
        return this.cache[index];
    }
    
    public boolean contains(final AbstractInsnNode insn) {
        AbstractInsnNode i;
        for (i = this.first; i != null && i != insn; i = i.next) {}
        return i != null;
    }
    
    public int indexOf(final AbstractInsnNode insn) {
        if (this.cache == null) {
            this.cache = this.toArray();
        }
        return insn.index;
    }
    
    public void accept(final MethodVisitor mv) {
        for (AbstractInsnNode insn = this.first; insn != null; insn = insn.next) {
            insn.accept(mv);
        }
    }
    
    public ListIterator<AbstractInsnNode> iterator() {
        return this.iterator(0);
    }
    
    public ListIterator<AbstractInsnNode> iterator(final int index) {
        return (ListIterator<AbstractInsnNode>)new InsnListIterator(index);
    }
    
    public AbstractInsnNode[] toArray() {
        int i = 0;
        AbstractInsnNode elem = this.first;
        final AbstractInsnNode[] insns = new AbstractInsnNode[this.size];
        while (elem != null) {
            insns[i] = elem;
            elem.index = i++;
            elem = elem.next;
        }
        return insns;
    }
    
    public void set(final AbstractInsnNode location, final AbstractInsnNode insn) {
        final AbstractInsnNode next = location.next;
        insn.next = next;
        if (next != null) {
            next.prev = insn;
        }
        else {
            this.last = insn;
        }
        final AbstractInsnNode prev = location.prev;
        insn.prev = prev;
        if (prev != null) {
            prev.next = insn;
        }
        else {
            this.first = insn;
        }
        if (this.cache != null) {
            final int index = location.index;
            this.cache[index] = insn;
            insn.index = index;
        }
        else {
            insn.index = 0;
        }
        location.index = -1;
        location.prev = null;
        location.next = null;
    }
    
    public void add(final AbstractInsnNode insn) {
        ++this.size;
        if (this.last == null) {
            this.first = insn;
            this.last = insn;
        }
        else {
            this.last.next = insn;
            insn.prev = this.last;
        }
        this.last = insn;
        this.cache = null;
        insn.index = 0;
    }
    
    public void add(final InsnList insns) {
        if (insns.size == 0) {
            return;
        }
        this.size += insns.size;
        if (this.last == null) {
            this.first = insns.first;
            this.last = insns.last;
        }
        else {
            final AbstractInsnNode elem = insns.first;
            this.last.next = elem;
            elem.prev = this.last;
            this.last = insns.last;
        }
        this.cache = null;
        insns.removeAll(false);
    }
    
    public void insert(final AbstractInsnNode insn) {
        ++this.size;
        if (this.first == null) {
            this.first = insn;
            this.last = insn;
        }
        else {
            this.first.prev = insn;
            insn.next = this.first;
        }
        this.first = insn;
        this.cache = null;
        insn.index = 0;
    }
    
    public void insert(final InsnList insns) {
        if (insns.size == 0) {
            return;
        }
        this.size += insns.size;
        if (this.first == null) {
            this.first = insns.first;
            this.last = insns.last;
        }
        else {
            final AbstractInsnNode elem = insns.last;
            this.first.prev = elem;
            elem.next = this.first;
            this.first = insns.first;
        }
        this.cache = null;
        insns.removeAll(false);
    }
    
    public void insert(final AbstractInsnNode location, final AbstractInsnNode insn) {
        ++this.size;
        final AbstractInsnNode next = location.next;
        if (next == null) {
            this.last = insn;
        }
        else {
            next.prev = insn;
        }
        location.next = insn;
        insn.next = next;
        insn.prev = location;
        this.cache = null;
        insn.index = 0;
    }
    
    public void insert(final AbstractInsnNode location, final InsnList insns) {
        if (insns.size == 0) {
            return;
        }
        this.size += insns.size;
        final AbstractInsnNode ifirst = insns.first;
        final AbstractInsnNode ilast = insns.last;
        final AbstractInsnNode next = location.next;
        if (next == null) {
            this.last = ilast;
        }
        else {
            next.prev = ilast;
        }
        location.next = ifirst;
        ilast.next = next;
        ifirst.prev = location;
        this.cache = null;
        insns.removeAll(false);
    }
    
    public void insertBefore(final AbstractInsnNode location, final AbstractInsnNode insn) {
        ++this.size;
        final AbstractInsnNode prev = location.prev;
        if (prev == null) {
            this.first = insn;
        }
        else {
            prev.next = insn;
        }
        location.prev = insn;
        insn.next = location;
        insn.prev = prev;
        this.cache = null;
        insn.index = 0;
    }
    
    public void insertBefore(final AbstractInsnNode location, final InsnList insns) {
        if (insns.size == 0) {
            return;
        }
        this.size += insns.size;
        final AbstractInsnNode ifirst = insns.first;
        final AbstractInsnNode ilast = insns.last;
        final AbstractInsnNode prev = location.prev;
        if (prev == null) {
            this.first = ifirst;
        }
        else {
            prev.next = ifirst;
        }
        location.prev = ilast;
        ilast.next = location;
        ifirst.prev = prev;
        this.cache = null;
        insns.removeAll(false);
    }
    
    public void remove(final AbstractInsnNode insn) {
        --this.size;
        final AbstractInsnNode next = insn.next;
        final AbstractInsnNode prev = insn.prev;
        if (next == null) {
            if (prev == null) {
                this.first = null;
                this.last = null;
            }
            else {
                prev.next = null;
                this.last = prev;
            }
        }
        else if (prev == null) {
            this.first = next;
            next.prev = null;
        }
        else {
            prev.next = next;
            next.prev = prev;
        }
        this.cache = null;
        insn.index = -1;
        insn.prev = null;
        insn.next = null;
    }
    
    void removeAll(final boolean mark) {
        if (mark) {
            AbstractInsnNode next;
            for (AbstractInsnNode insn = this.first; insn != null; insn = next) {
                next = insn.next;
                insn.index = -1;
                insn.prev = null;
                insn.next = null;
            }
        }
        this.size = 0;
        this.first = null;
        this.last = null;
        this.cache = null;
    }
    
    public void clear() {
        this.removeAll(false);
    }
    
    public void resetLabels() {
        for (AbstractInsnNode insn = this.first; insn != null; insn = insn.next) {
            if (insn instanceof LabelNode) {
                ((LabelNode)insn).resetLabel();
            }
        }
    }
    
    private final class InsnListIterator implements ListIterator
    {
        AbstractInsnNode next;
        AbstractInsnNode prev;
        AbstractInsnNode remove;
        
        InsnListIterator(final int index) {
            if (index == InsnList.this.size()) {
                this.next = null;
                this.prev = InsnList.this.getLast();
            }
            else {
                this.next = InsnList.this.get(index);
                this.prev = this.next.prev;
            }
        }
        
        public boolean hasNext() {
            return this.next != null;
        }
        
        public Object next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            final AbstractInsnNode result = this.next;
            this.prev = result;
            this.next = result.next;
            return this.remove = result;
        }
        
        public void remove() {
            if (this.remove != null) {
                if (this.remove == this.next) {
                    this.next = this.next.next;
                }
                else {
                    this.prev = this.prev.prev;
                }
                InsnList.this.remove(this.remove);
                this.remove = null;
                return;
            }
            throw new IllegalStateException();
        }
        
        public boolean hasPrevious() {
            return this.prev != null;
        }
        
        public Object previous() {
            final AbstractInsnNode result = this.prev;
            this.next = result;
            this.prev = result.prev;
            return this.remove = result;
        }
        
        public int nextIndex() {
            if (this.next == null) {
                return InsnList.this.size();
            }
            if (InsnList.this.cache == null) {
                InsnList.this.cache = InsnList.this.toArray();
            }
            return this.next.index;
        }
        
        public int previousIndex() {
            if (this.prev == null) {
                return -1;
            }
            if (InsnList.this.cache == null) {
                InsnList.this.cache = InsnList.this.toArray();
            }
            return this.prev.index;
        }
        
        public void add(final Object o) {
            if (this.next != null) {
                InsnList.this.insertBefore(this.next, (AbstractInsnNode)o);
            }
            else if (this.prev != null) {
                InsnList.this.insert(this.prev, (AbstractInsnNode)o);
            }
            else {
                InsnList.this.add((AbstractInsnNode)o);
            }
            this.prev = (AbstractInsnNode)o;
            this.remove = null;
        }
        
        public void set(final Object o) {
            if (this.remove != null) {
                InsnList.this.set(this.remove, (AbstractInsnNode)o);
                if (this.remove == this.prev) {
                    this.prev = (AbstractInsnNode)o;
                }
                else {
                    this.next = (AbstractInsnNode)o;
                }
                return;
            }
            throw new IllegalStateException();
        }
    }
}

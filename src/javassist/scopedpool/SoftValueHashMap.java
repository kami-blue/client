// 
// Decompiled by Procyon v0.5.36
// 

package javassist.scopedpool;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Set;
import java.lang.ref.ReferenceQueue;
import java.util.Map;
import java.util.AbstractMap;

public class SoftValueHashMap extends AbstractMap implements Map
{
    private Map hash;
    private ReferenceQueue queue;
    
    @Override
    public Set entrySet() {
        this.processQueue();
        return this.hash.entrySet();
    }
    
    private void processQueue() {
        SoftValueRef ref;
        while ((ref = (SoftValueRef)this.queue.poll()) != null) {
            if (ref == this.hash.get(ref.key)) {
                this.hash.remove(ref.key);
            }
        }
    }
    
    public SoftValueHashMap(final int initialCapacity, final float loadFactor) {
        this.queue = new ReferenceQueue();
        this.hash = new HashMap(initialCapacity, loadFactor);
    }
    
    public SoftValueHashMap(final int initialCapacity) {
        this.queue = new ReferenceQueue();
        this.hash = new HashMap(initialCapacity);
    }
    
    public SoftValueHashMap() {
        this.queue = new ReferenceQueue();
        this.hash = new HashMap();
    }
    
    public SoftValueHashMap(final Map t) {
        this(Math.max(2 * t.size(), 11), 0.75f);
        this.putAll(t);
    }
    
    @Override
    public int size() {
        this.processQueue();
        return this.hash.size();
    }
    
    @Override
    public boolean isEmpty() {
        this.processQueue();
        return this.hash.isEmpty();
    }
    
    @Override
    public boolean containsKey(final Object key) {
        this.processQueue();
        return this.hash.containsKey(key);
    }
    
    @Override
    public Object get(final Object key) {
        this.processQueue();
        final SoftReference ref = this.hash.get(key);
        if (ref != null) {
            return ref.get();
        }
        return null;
    }
    
    @Override
    public Object put(final Object key, final Object value) {
        this.processQueue();
        Object rtn = this.hash.put(key, create(key, value, this.queue));
        if (rtn != null) {
            rtn = ((SoftReference)rtn).get();
        }
        return rtn;
    }
    
    @Override
    public Object remove(final Object key) {
        this.processQueue();
        return this.hash.remove(key);
    }
    
    @Override
    public void clear() {
        this.processQueue();
        this.hash.clear();
    }
    
    private static class SoftValueRef extends SoftReference
    {
        public Object key;
        
        private SoftValueRef(final Object key, final Object val, final ReferenceQueue q) {
            super(val, q);
            this.key = key;
        }
        
        private static SoftValueRef create(final Object key, final Object val, final ReferenceQueue q) {
            if (val == null) {
                return null;
            }
            return new SoftValueRef(key, val, q);
        }
    }
}

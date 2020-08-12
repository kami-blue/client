// 
// Decompiled by Procyon v0.5.36
// 

package javassist.runtime;

public class Cflow extends ThreadLocal
{
    @Override
    protected synchronized Object initialValue() {
        return new Depth();
    }
    
    public void enter() {
        this.get().inc();
    }
    
    public void exit() {
        this.get().dec();
    }
    
    public int value() {
        return this.get().get();
    }
    
    private static class Depth
    {
        private int depth;
        
        Depth() {
            this.depth = 0;
        }
        
        int get() {
            return this.depth;
        }
        
        void inc() {
            ++this.depth;
        }
        
        void dec() {
            --this.depth;
        }
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util;

public final class Timer
{
    private long time;
    
    public Timer() {
        this.time = -1L;
    }
    
    public boolean passed(final double ms) {
        return System.currentTimeMillis() - this.time >= ms;
    }
    
    public void reset() {
        this.time = System.currentTimeMillis();
    }
    
    public long getTime() {
        return this.time;
    }
    
    public void setTime(final long time) {
        this.time = time;
    }
}

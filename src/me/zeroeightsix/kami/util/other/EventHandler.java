// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util.other;

public interface EventHandler
{
     <E> void handle(final E p0);
    
     <T> T getListener();
    
    Iterable<EventFilter> getFilters();
}

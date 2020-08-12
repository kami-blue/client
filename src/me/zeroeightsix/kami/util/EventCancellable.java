// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util;

public class EventCancellable extends EventStageable
{
    private boolean canceled;
    
    public EventCancellable() {
    }
    
    public EventCancellable(final EventStage stage) {
        super(stage);
    }
    
    public EventCancellable(final EventStage stage, final boolean canceled) {
        super(stage);
        this.canceled = canceled;
    }
    
    public boolean isCanceled() {
        return this.canceled;
    }
    
    public void setCanceled(final boolean canceled) {
        this.canceled = canceled;
    }
}

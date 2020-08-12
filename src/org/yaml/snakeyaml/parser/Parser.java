// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.parser;

import org.yaml.snakeyaml.events.Event;

public interface Parser
{
    boolean checkEvent(final Event.ID p0);
    
    Event peekEvent();
    
    Event getEvent();
}

// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.emitter;

import java.io.IOException;
import org.yaml.snakeyaml.events.Event;

public interface Emitable
{
    void emit(final Event p0) throws IOException;
}

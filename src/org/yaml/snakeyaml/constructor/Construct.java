// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.constructor;

import org.yaml.snakeyaml.nodes.Node;

public interface Construct
{
    Object construct(final Node p0);
    
    void construct2ndStep(final Node p0, final Object p1);
}

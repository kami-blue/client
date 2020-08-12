// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.serializer;

import org.yaml.snakeyaml.nodes.Node;

public interface AnchorGenerator
{
    String nextAnchor(final Node p0);
}

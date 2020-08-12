// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.emitter;

import java.io.IOException;

interface EmitterState
{
    void expect() throws IOException;
}

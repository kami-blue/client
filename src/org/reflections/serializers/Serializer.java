// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.serializers;

import java.io.File;
import org.reflections.Reflections;
import java.io.InputStream;

public interface Serializer
{
    Reflections read(final InputStream p0);
    
    File save(final Reflections p0, final String p1);
    
    String toString(final Reflections p0);
}

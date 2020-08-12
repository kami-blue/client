// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.scanners;

import javax.annotation.Nullable;
import org.reflections.vfs.Vfs;
import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;
import org.reflections.Configuration;

public interface Scanner
{
    void setConfiguration(final Configuration p0);
    
    Multimap<String, String> getStore();
    
    void setStore(final Multimap<String, String> p0);
    
    Scanner filterResultsBy(final Predicate<String> p0);
    
    boolean acceptsInput(final String p0);
    
    Object scan(final Vfs.File p0, @Nullable final Object p1);
    
    boolean acceptResult(final String p0);
}

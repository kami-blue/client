// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections;

import org.reflections.serializers.Serializer;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nullable;
import com.google.common.base.Predicate;
import org.reflections.adapters.MetadataAdapter;
import java.net.URL;
import org.reflections.scanners.Scanner;
import java.util.Set;

public interface Configuration
{
    Set<Scanner> getScanners();
    
    Set<URL> getUrls();
    
    MetadataAdapter getMetadataAdapter();
    
    @Nullable
    Predicate<String> getInputsFilter();
    
    ExecutorService getExecutorService();
    
    Serializer getSerializer();
    
    @Nullable
    ClassLoader[] getClassLoaders();
    
    boolean shouldExpandSuperTypes();
}

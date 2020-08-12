// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.util;

import com.google.common.collect.ObjectArrays;
import org.reflections.serializers.XmlSerializer;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.Executors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.reflections.adapters.JavaReflectionAdapter;
import org.reflections.adapters.JavassistAdapter;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import org.reflections.ReflectionsException;
import org.reflections.Reflections;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import java.util.concurrent.ExecutorService;
import org.reflections.serializers.Serializer;
import javax.annotation.Nullable;
import com.google.common.base.Predicate;
import org.reflections.adapters.MetadataAdapter;
import java.net.URL;
import javax.annotation.Nonnull;
import org.reflections.scanners.Scanner;
import java.util.Set;
import org.reflections.Configuration;

public class ConfigurationBuilder implements Configuration
{
    @Nonnull
    private Set<Scanner> scanners;
    @Nonnull
    private Set<URL> urls;
    protected MetadataAdapter metadataAdapter;
    @Nullable
    private Predicate<String> inputsFilter;
    private Serializer serializer;
    @Nullable
    private ExecutorService executorService;
    @Nullable
    private ClassLoader[] classLoaders;
    private boolean expandSuperTypes;
    
    public ConfigurationBuilder() {
        this.expandSuperTypes = true;
        this.scanners = (Set<Scanner>)Sets.newHashSet((Object[])new Scanner[] { new TypeAnnotationsScanner(), new SubTypesScanner() });
        this.urls = (Set<URL>)Sets.newHashSet();
    }
    
    public static ConfigurationBuilder build(@Nullable final Object... params) {
        final ConfigurationBuilder builder = new ConfigurationBuilder();
        final List<Object> parameters = (List<Object>)Lists.newArrayList();
        if (params != null) {
            for (final Object param : params) {
                if (param != null) {
                    if (param.getClass().isArray()) {
                        for (final Object p : (Object[])param) {
                            if (p != null) {
                                parameters.add(p);
                            }
                        }
                    }
                    else if (param instanceof Iterable) {
                        for (final Object p2 : (Iterable)param) {
                            if (p2 != null) {
                                parameters.add(p2);
                            }
                        }
                    }
                    else {
                        parameters.add(param);
                    }
                }
            }
        }
        final List<ClassLoader> loaders = (List<ClassLoader>)Lists.newArrayList();
        for (final Object param2 : parameters) {
            if (param2 instanceof ClassLoader) {
                loaders.add((ClassLoader)param2);
            }
        }
        final ClassLoader[] classLoaders = (ClassLoader[])(loaders.isEmpty() ? null : ((ClassLoader[])loaders.toArray(new ClassLoader[loaders.size()])));
        final FilterBuilder filter = new FilterBuilder();
        final List<Scanner> scanners = (List<Scanner>)Lists.newArrayList();
        for (final Object param3 : parameters) {
            if (param3 instanceof String) {
                builder.addUrls(ClasspathHelper.forPackage((String)param3, classLoaders));
                filter.includePackage((String)param3);
            }
            else if (param3 instanceof Class) {
                if (Scanner.class.isAssignableFrom((Class<?>)param3)) {
                    try {
                        builder.addScanners(((Class)param3).newInstance());
                    }
                    catch (Exception ex) {}
                }
                builder.addUrls(ClasspathHelper.forClass((Class<?>)param3, classLoaders));
                filter.includePackage((Class<?>)param3);
            }
            else if (param3 instanceof Scanner) {
                scanners.add((Scanner)param3);
            }
            else if (param3 instanceof URL) {
                builder.addUrls((URL)param3);
            }
            else {
                if (param3 instanceof ClassLoader) {
                    continue;
                }
                if (param3 instanceof Predicate) {
                    filter.add((Predicate<String>)param3);
                }
                else if (param3 instanceof ExecutorService) {
                    builder.setExecutorService((ExecutorService)param3);
                }
                else {
                    if (Reflections.log != null) {
                        throw new ReflectionsException("could not use param " + param3);
                    }
                    continue;
                }
            }
        }
        if (builder.getUrls().isEmpty()) {
            if (classLoaders != null) {
                builder.addUrls(ClasspathHelper.forClassLoader(classLoaders));
            }
            else {
                builder.addUrls(ClasspathHelper.forClassLoader());
            }
        }
        builder.filterInputsBy((Predicate<String>)filter);
        if (!scanners.isEmpty()) {
            builder.setScanners((Scanner[])scanners.toArray(new Scanner[scanners.size()]));
        }
        if (!loaders.isEmpty()) {
            builder.addClassLoaders(loaders);
        }
        return builder;
    }
    
    public ConfigurationBuilder forPackages(final String... packages) {
        for (final String pkg : packages) {
            this.addUrls(ClasspathHelper.forPackage(pkg, new ClassLoader[0]));
        }
        return this;
    }
    
    @Nonnull
    @Override
    public Set<Scanner> getScanners() {
        return this.scanners;
    }
    
    public ConfigurationBuilder setScanners(@Nonnull final Scanner... scanners) {
        this.scanners.clear();
        return this.addScanners(scanners);
    }
    
    public ConfigurationBuilder addScanners(final Scanner... scanners) {
        this.scanners.addAll(Sets.newHashSet((Object[])scanners));
        return this;
    }
    
    @Nonnull
    @Override
    public Set<URL> getUrls() {
        return this.urls;
    }
    
    public ConfigurationBuilder setUrls(@Nonnull final Collection<URL> urls) {
        this.urls = (Set<URL>)Sets.newHashSet((Iterable)urls);
        return this;
    }
    
    public ConfigurationBuilder setUrls(final URL... urls) {
        this.urls = (Set<URL>)Sets.newHashSet((Object[])urls);
        return this;
    }
    
    public ConfigurationBuilder addUrls(final Collection<URL> urls) {
        this.urls.addAll(urls);
        return this;
    }
    
    public ConfigurationBuilder addUrls(final URL... urls) {
        this.urls.addAll(Sets.newHashSet((Object[])urls));
        return this;
    }
    
    @Override
    public MetadataAdapter getMetadataAdapter() {
        if (this.metadataAdapter != null) {
            return this.metadataAdapter;
        }
        try {
            return this.metadataAdapter = new JavassistAdapter();
        }
        catch (Throwable e) {
            if (Reflections.log != null) {
                Reflections.log.warn("could not create JavassistAdapter, using JavaReflectionAdapter", e);
            }
            return this.metadataAdapter = new JavaReflectionAdapter();
        }
    }
    
    public ConfigurationBuilder setMetadataAdapter(final MetadataAdapter metadataAdapter) {
        this.metadataAdapter = metadataAdapter;
        return this;
    }
    
    @Nullable
    @Override
    public Predicate<String> getInputsFilter() {
        return this.inputsFilter;
    }
    
    public void setInputsFilter(@Nullable final Predicate<String> inputsFilter) {
        this.inputsFilter = inputsFilter;
    }
    
    public ConfigurationBuilder filterInputsBy(final Predicate<String> inputsFilter) {
        this.inputsFilter = inputsFilter;
        return this;
    }
    
    @Nullable
    @Override
    public ExecutorService getExecutorService() {
        return this.executorService;
    }
    
    public ConfigurationBuilder setExecutorService(@Nullable final ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }
    
    public ConfigurationBuilder useParallelExecutor() {
        return this.useParallelExecutor(Runtime.getRuntime().availableProcessors());
    }
    
    public ConfigurationBuilder useParallelExecutor(final int availableProcessors) {
        final ThreadFactory factory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("org.reflections-scanner-%d").build();
        this.setExecutorService(Executors.newFixedThreadPool(availableProcessors, factory));
        return this;
    }
    
    @Override
    public Serializer getSerializer() {
        return (this.serializer != null) ? this.serializer : (this.serializer = new XmlSerializer());
    }
    
    public ConfigurationBuilder setSerializer(final Serializer serializer) {
        this.serializer = serializer;
        return this;
    }
    
    @Nullable
    @Override
    public ClassLoader[] getClassLoaders() {
        return this.classLoaders;
    }
    
    @Override
    public boolean shouldExpandSuperTypes() {
        return this.expandSuperTypes;
    }
    
    public ConfigurationBuilder setExpandSuperTypes(final boolean expandSuperTypes) {
        this.expandSuperTypes = expandSuperTypes;
        return this;
    }
    
    public void setClassLoaders(@Nullable final ClassLoader[] classLoaders) {
        this.classLoaders = classLoaders;
    }
    
    public ConfigurationBuilder addClassLoader(final ClassLoader classLoader) {
        return this.addClassLoaders(classLoader);
    }
    
    public ConfigurationBuilder addClassLoaders(final ClassLoader... classLoaders) {
        this.classLoaders = (ClassLoader[])((this.classLoaders == null) ? classLoaders : ObjectArrays.concat((Object[])this.classLoaders, (Object[])classLoaders, (Class)ClassLoader.class));
        return this;
    }
    
    public ConfigurationBuilder addClassLoaders(final Collection<ClassLoader> classLoaders) {
        return this.addClassLoaders((ClassLoader[])classLoaders.toArray(new ClassLoader[classLoaders.size()]));
    }
}

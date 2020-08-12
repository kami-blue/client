// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer.ext;

import java.util.Iterator;
import com.google.common.collect.ImmutableList;
import org.spongepowered.asm.mixin.MixinEnvironment;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import org.spongepowered.asm.mixin.transformer.MixinTransformer;

public final class Extensions
{
    private final MixinTransformer transformer;
    private final List<IExtension> extensions;
    private final Map<Class<? extends IExtension>, IExtension> extensionMap;
    private final List<IClassGenerator> generators;
    private final List<IClassGenerator> generatorsView;
    private final Map<Class<? extends IClassGenerator>, IClassGenerator> generatorMap;
    private List<IExtension> activeExtensions;
    
    public Extensions(final MixinTransformer transformer) {
        this.extensions = new ArrayList<IExtension>();
        this.extensionMap = new HashMap<Class<? extends IExtension>, IExtension>();
        this.generators = new ArrayList<IClassGenerator>();
        this.generatorsView = Collections.unmodifiableList((List<? extends IClassGenerator>)this.generators);
        this.generatorMap = new HashMap<Class<? extends IClassGenerator>, IClassGenerator>();
        this.activeExtensions = Collections.emptyList();
        this.transformer = transformer;
    }
    
    public MixinTransformer getTransformer() {
        return this.transformer;
    }
    
    public void add(final IExtension extension) {
        this.extensions.add(extension);
        this.extensionMap.put(extension.getClass(), extension);
    }
    
    public List<IExtension> getExtensions() {
        return Collections.unmodifiableList((List<? extends IExtension>)this.extensions);
    }
    
    public List<IExtension> getActiveExtensions() {
        return this.activeExtensions;
    }
    
    public <T extends IExtension> T getExtension(final Class<? extends IExtension> extensionClass) {
        return lookup((Class<? extends T>)extensionClass, (Map<Class<? extends T>, T>)this.extensionMap, (List<T>)this.extensions);
    }
    
    public void select(final MixinEnvironment environment) {
        final ImmutableList.Builder<IExtension> activeExtensions = (ImmutableList.Builder<IExtension>)ImmutableList.builder();
        for (final IExtension extension : this.extensions) {
            if (extension.checkActive(environment)) {
                activeExtensions.add((Object)extension);
            }
        }
        this.activeExtensions = (List<IExtension>)activeExtensions.build();
    }
    
    public void preApply(final ITargetClassContext context) {
        for (final IExtension extension : this.activeExtensions) {
            extension.preApply(context);
        }
    }
    
    public void postApply(final ITargetClassContext context) {
        for (final IExtension extension : this.activeExtensions) {
            extension.postApply(context);
        }
    }
    
    public void export(final MixinEnvironment env, final String name, final boolean force, final byte[] bytes) {
        for (final IExtension extension : this.activeExtensions) {
            extension.export(env, name, force, bytes);
        }
    }
    
    public void add(final IClassGenerator generator) {
        this.generators.add(generator);
        this.generatorMap.put(generator.getClass(), generator);
    }
    
    public List<IClassGenerator> getGenerators() {
        return this.generatorsView;
    }
    
    public <T extends IClassGenerator> T getGenerator(final Class<? extends IClassGenerator> generatorClass) {
        return lookup((Class<? extends T>)generatorClass, (Map<Class<? extends T>, T>)this.generatorMap, (List<T>)this.generators);
    }
    
    private static <T> T lookup(final Class<? extends T> extensionClass, final Map<Class<? extends T>, T> map, final List<T> list) {
        T extension = map.get(extensionClass);
        if (extension == null) {
            for (final T classGenerator : list) {
                if (extensionClass.isAssignableFrom(classGenerator.getClass())) {
                    extension = classGenerator;
                    break;
                }
            }
            if (extension == null) {
                throw new IllegalArgumentException("Extension for <" + extensionClass.getName() + "> could not be found");
            }
            map.put(extensionClass, extension);
        }
        return extension;
    }
}

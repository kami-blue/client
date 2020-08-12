// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation;

import java.util.LinkedHashMap;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.lang.reflect.Constructor;
import org.spongepowered.tools.obfuscation.interfaces.IOptionProvider;
import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;
import org.spongepowered.tools.obfuscation.service.ObfuscationTypeDescriptor;
import java.util.Map;

public final class ObfuscationType
{
    private static final Map<String, ObfuscationType> types;
    private final String key;
    private final ObfuscationTypeDescriptor descriptor;
    private final IMixinAnnotationProcessor ap;
    private final IOptionProvider options;
    
    private ObfuscationType(final ObfuscationTypeDescriptor descriptor, final IMixinAnnotationProcessor ap) {
        this.key = descriptor.getKey();
        this.descriptor = descriptor;
        this.ap = ap;
        this.options = ap;
    }
    
    public final ObfuscationEnvironment createEnvironment() {
        try {
            final Class<? extends ObfuscationEnvironment> cls = this.descriptor.getEnvironmentType();
            final Constructor<? extends ObfuscationEnvironment> ctor = cls.getDeclaredConstructor(ObfuscationType.class);
            ctor.setAccessible(true);
            return (ObfuscationEnvironment)ctor.newInstance(this);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public String toString() {
        return this.key;
    }
    
    public String getKey() {
        return this.key;
    }
    
    public ObfuscationTypeDescriptor getConfig() {
        return this.descriptor;
    }
    
    public IMixinAnnotationProcessor getAnnotationProcessor() {
        return this.ap;
    }
    
    public boolean isDefault() {
        final String defaultEnv = this.options.getOption("defaultObfuscationEnv");
        return (defaultEnv == null && this.key.equals("searge")) || (defaultEnv != null && this.key.equals(defaultEnv.toLowerCase()));
    }
    
    public boolean isSupported() {
        return this.getInputFileNames().size() > 0;
    }
    
    public List<String> getInputFileNames() {
        final ImmutableList.Builder<String> builder = (ImmutableList.Builder<String>)ImmutableList.builder();
        final String inputFile = this.options.getOption(this.descriptor.getInputFileOption());
        if (inputFile != null) {
            builder.add((Object)inputFile);
        }
        final String extraInputFiles = this.options.getOption(this.descriptor.getExtraInputFilesOption());
        if (extraInputFiles != null) {
            for (final String extraInputFile : extraInputFiles.split(";")) {
                builder.add((Object)extraInputFile.trim());
            }
        }
        return (List<String>)builder.build();
    }
    
    public String getOutputFileName() {
        return this.options.getOption(this.descriptor.getOutputFileOption());
    }
    
    public static Iterable<ObfuscationType> types() {
        return ObfuscationType.types.values();
    }
    
    public static ObfuscationType create(final ObfuscationTypeDescriptor descriptor, final IMixinAnnotationProcessor ap) {
        final String key = descriptor.getKey();
        if (ObfuscationType.types.containsKey(key)) {
            throw new IllegalArgumentException("Obfuscation type with key " + key + " was already registered");
        }
        final ObfuscationType type = new ObfuscationType(descriptor, ap);
        ObfuscationType.types.put(key, type);
        return type;
    }
    
    public static ObfuscationType get(final String key) {
        final ObfuscationType type = ObfuscationType.types.get(key);
        if (type == null) {
            throw new IllegalArgumentException("Obfuscation type with key " + key + " was not registered");
        }
        return type;
    }
    
    static {
        types = new LinkedHashMap<String, ObfuscationType>();
    }
}

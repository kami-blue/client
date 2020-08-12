// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.refmap;

import org.apache.logging.log4j.LogManager;
import com.google.common.io.Files;
import java.io.IOException;
import com.google.common.base.Strings;
import com.google.common.io.LineProcessor;
import com.google.common.base.Charsets;
import java.io.File;
import java.util.Iterator;
import java.util.HashMap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import java.util.Map;
import org.apache.logging.log4j.Logger;

public final class RemappingReferenceMapper implements IReferenceMapper
{
    private static final String DEFAULT_RESOURCE_PATH_PROPERTY = "net.minecraftforge.gradle.GradleStart.srg.srg-mcp";
    private static final String DEFAULT_MAPPING_ENV = "searge";
    private static final Logger logger;
    private static final Map<String, Map<String, String>> srgs;
    private final IReferenceMapper refMap;
    private final Map<String, String> mappings;
    private final Map<String, Map<String, String>> cache;
    
    private RemappingReferenceMapper(final MixinEnvironment env, final IReferenceMapper refMap) {
        this.cache = new HashMap<String, Map<String, String>>();
        (this.refMap = refMap).setContext(getMappingEnv(env));
        final String resource = getResource(env);
        this.mappings = loadSrgs(resource);
        RemappingReferenceMapper.logger.info("Remapping refMap {} using {}", new Object[] { refMap.getResourceName(), resource });
    }
    
    @Override
    public boolean isDefault() {
        return this.refMap.isDefault();
    }
    
    @Override
    public String getResourceName() {
        return this.refMap.getResourceName();
    }
    
    @Override
    public String getStatus() {
        return this.refMap.getStatus();
    }
    
    @Override
    public String getContext() {
        return this.refMap.getContext();
    }
    
    @Override
    public void setContext(final String context) {
    }
    
    @Override
    public String remap(final String className, final String reference) {
        final Map<String, String> classCache = this.getCache(className);
        String remapped = classCache.get(reference);
        if (remapped == null) {
            remapped = this.refMap.remap(className, reference);
            for (final Map.Entry<String, String> entry : this.mappings.entrySet()) {
                remapped = remapped.replace(entry.getKey(), entry.getValue());
            }
            classCache.put(reference, remapped);
        }
        return remapped;
    }
    
    private Map<String, String> getCache(final String className) {
        Map<String, String> classCache = this.cache.get(className);
        if (classCache == null) {
            classCache = new HashMap<String, String>();
            this.cache.put(className, classCache);
        }
        return classCache;
    }
    
    @Override
    public String remapWithContext(final String context, final String className, final String reference) {
        return this.refMap.remapWithContext(context, className, reference);
    }
    
    private static Map<String, String> loadSrgs(final String fileName) {
        if (RemappingReferenceMapper.srgs.containsKey(fileName)) {
            return RemappingReferenceMapper.srgs.get(fileName);
        }
        final Map<String, String> map = new HashMap<String, String>();
        RemappingReferenceMapper.srgs.put(fileName, map);
        final File file = new File(fileName);
        if (!file.isFile()) {
            return map;
        }
        try {
            Files.readLines(file, Charsets.UTF_8, (LineProcessor)new LineProcessor<Object>() {
                public Object getResult() {
                    return null;
                }
                
                public boolean processLine(final String line) throws IOException {
                    if (Strings.isNullOrEmpty(line) || line.startsWith("#")) {
                        return true;
                    }
                    final int fromPos = 0;
                    int toPos = 0;
                    int n2;
                    final int n = line.startsWith("MD: ") ? (n2 = 2) : (line.startsWith("FD: ") ? (n2 = 1) : (n2 = 0));
                    toPos = n2;
                    if (n > 0) {
                        final String[] entries = line.substring(4).split(" ", 4);
                        map.put(entries[fromPos].substring(entries[fromPos].lastIndexOf(47) + 1), entries[toPos].substring(entries[toPos].lastIndexOf(47) + 1));
                    }
                    return true;
                }
            });
        }
        catch (IOException ex) {
            RemappingReferenceMapper.logger.warn("Could not read input SRG file: {}", new Object[] { fileName });
            RemappingReferenceMapper.logger.catching((Throwable)ex);
        }
        return map;
    }
    
    public static IReferenceMapper of(final MixinEnvironment env, final IReferenceMapper refMap) {
        if (!refMap.isDefault() && hasData(env)) {
            return new RemappingReferenceMapper(env, refMap);
        }
        return refMap;
    }
    
    private static boolean hasData(final MixinEnvironment env) {
        final String fileName = getResource(env);
        return fileName != null && new File(fileName).exists();
    }
    
    private static String getResource(final MixinEnvironment env) {
        final String resource = env.getOptionValue(MixinEnvironment.Option.REFMAP_REMAP_RESOURCE);
        return Strings.isNullOrEmpty(resource) ? System.getProperty("net.minecraftforge.gradle.GradleStart.srg.srg-mcp") : resource;
    }
    
    private static String getMappingEnv(final MixinEnvironment env) {
        final String resource = env.getOptionValue(MixinEnvironment.Option.REFMAP_REMAP_SOURCE_ENV);
        return Strings.isNullOrEmpty(resource) ? "searge" : resource;
    }
    
    static {
        logger = LogManager.getLogger("mixin");
        srgs = new HashMap<String, Map<String, String>>();
    }
}

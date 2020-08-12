// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin;

import org.apache.logging.log4j.LogManager;
import java.util.Collections;
import java.util.LinkedHashSet;
import org.spongepowered.asm.launch.GlobalProperties;
import java.util.Iterator;
import org.spongepowered.asm.mixin.transformer.Config;
import java.util.Set;
import org.apache.logging.log4j.Logger;

public final class Mixins
{
    private static final Logger logger;
    private static final String CONFIGS_KEY = "mixin.configs.queue";
    private static final Set<String> errorHandlers;
    
    private Mixins() {
    }
    
    public static void addConfigurations(final String... configFiles) {
        final MixinEnvironment fallback = MixinEnvironment.getDefaultEnvironment();
        for (final String configFile : configFiles) {
            createConfiguration(configFile, fallback);
        }
    }
    
    public static void addConfiguration(final String configFile) {
        createConfiguration(configFile, MixinEnvironment.getDefaultEnvironment());
    }
    
    @Deprecated
    static void addConfiguration(final String configFile, final MixinEnvironment fallback) {
        createConfiguration(configFile, fallback);
    }
    
    private static void createConfiguration(final String configFile, final MixinEnvironment fallback) {
        Config config = null;
        try {
            config = Config.create(configFile, fallback);
        }
        catch (Exception ex) {
            Mixins.logger.error("Error encountered reading mixin config " + configFile + ": " + ex.getClass().getName() + " " + ex.getMessage(), (Throwable)ex);
        }
        registerConfiguration(config);
    }
    
    private static void registerConfiguration(final Config config) {
        if (config == null) {
            return;
        }
        final MixinEnvironment env = config.getEnvironment();
        if (env != null) {
            env.registerConfig(config.getName());
        }
        getConfigs().add(config);
    }
    
    public static int getUnvisitedCount() {
        int count = 0;
        for (final Config config : getConfigs()) {
            if (!config.isVisited()) {
                ++count;
            }
        }
        return count;
    }
    
    public static Set<Config> getConfigs() {
        Set<Config> mixinConfigs = GlobalProperties.get("mixin.configs.queue");
        if (mixinConfigs == null) {
            mixinConfigs = new LinkedHashSet<Config>();
            GlobalProperties.put("mixin.configs.queue", mixinConfigs);
        }
        return mixinConfigs;
    }
    
    public static void registerErrorHandlerClass(final String handlerName) {
        if (handlerName != null) {
            Mixins.errorHandlers.add(handlerName);
        }
    }
    
    public static Set<String> getErrorHandlerClasses() {
        return Collections.unmodifiableSet((Set<? extends String>)Mixins.errorHandlers);
    }
    
    static {
        logger = LogManager.getLogger("mixin");
        errorHandlers = new LinkedHashSet<String>();
    }
}

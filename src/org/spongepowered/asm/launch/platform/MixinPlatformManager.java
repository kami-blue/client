// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.launch.platform;

import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.MixinEnvironment;
import java.net.URL;
import java.io.File;
import org.spongepowered.asm.service.MixinService;
import java.util.Iterator;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.net.URI;
import java.util.Map;
import org.apache.logging.log4j.Logger;

public class MixinPlatformManager
{
    private static final String DEFAULT_MAIN_CLASS = "net.minecraft.client.main.Main";
    private static final String MIXIN_TWEAKER_CLASS = "org.spongepowered.asm.launch.MixinTweaker";
    private static final Logger logger;
    private final Map<URI, MixinContainer> containers;
    private MixinContainer primaryContainer;
    private boolean prepared;
    private boolean injected;
    
    public MixinPlatformManager() {
        this.containers = new LinkedHashMap<URI, MixinContainer>();
        this.prepared = false;
    }
    
    public void init() {
        MixinPlatformManager.logger.debug("Initialising Mixin Platform Manager");
        URI uri = null;
        try {
            uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            if (uri != null) {
                MixinPlatformManager.logger.debug("Mixin platform: primary container is {}", new Object[] { uri });
                this.primaryContainer = this.addContainer(uri);
            }
        }
        catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        this.scanClasspath();
    }
    
    public Collection<String> getPhaseProviderClasses() {
        final Collection<String> phaseProviders = this.primaryContainer.getPhaseProviders();
        if (phaseProviders != null) {
            return Collections.unmodifiableCollection((Collection<? extends String>)phaseProviders);
        }
        return (Collection<String>)Collections.emptyList();
    }
    
    public final MixinContainer addContainer(final URI uri) {
        final MixinContainer existingContainer = this.containers.get(uri);
        if (existingContainer != null) {
            return existingContainer;
        }
        MixinPlatformManager.logger.debug("Adding mixin platform agents for container {}", new Object[] { uri });
        final MixinContainer container = new MixinContainer(this, uri);
        this.containers.put(uri, container);
        if (this.prepared) {
            container.prepare();
        }
        return container;
    }
    
    public final void prepare(final List<String> args) {
        this.prepared = true;
        for (final MixinContainer container : this.containers.values()) {
            container.prepare();
        }
        if (args != null) {
            this.parseArgs(args);
        }
        else {
            final String argv = System.getProperty("sun.java.command");
            if (argv != null) {
                this.parseArgs(Arrays.asList(argv.split(" ")));
            }
        }
    }
    
    private void parseArgs(final List<String> args) {
        boolean captureNext = false;
        for (final String arg : args) {
            if (captureNext) {
                this.addConfig(arg);
            }
            captureNext = "--mixin".equals(arg);
        }
    }
    
    public final void inject() {
        if (this.injected) {
            return;
        }
        this.injected = true;
        if (this.primaryContainer != null) {
            this.primaryContainer.initPrimaryContainer();
        }
        this.scanClasspath();
        MixinPlatformManager.logger.debug("inject() running with {} agents", new Object[] { this.containers.size() });
        for (final MixinContainer container : this.containers.values()) {
            try {
                container.inject();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void scanClasspath() {
        final URL[] classPath;
        final URL[] sources = classPath = MixinService.getService().getClassProvider().getClassPath();
        for (final URL url : classPath) {
            try {
                final URI uri = url.toURI();
                if (!this.containers.containsKey(uri)) {
                    MixinPlatformManager.logger.debug("Scanning {} for mixin tweaker", new Object[] { uri });
                    if ("file".equals(uri.getScheme()) && new File(uri).exists()) {
                        final MainAttributes attributes = MainAttributes.of(uri);
                        final String tweaker = attributes.get("TweakClass");
                        if ("org.spongepowered.asm.launch.MixinTweaker".equals(tweaker)) {
                            MixinPlatformManager.logger.debug("{} contains a mixin tweaker, adding agents", new Object[] { uri });
                            this.addContainer(uri);
                        }
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public String getLaunchTarget() {
        for (final MixinContainer container : this.containers.values()) {
            final String mainClass = container.getLaunchTarget();
            if (mainClass != null) {
                return mainClass;
            }
        }
        return "net.minecraft.client.main.Main";
    }
    
    final void setCompatibilityLevel(final String level) {
        try {
            final MixinEnvironment.CompatibilityLevel value = MixinEnvironment.CompatibilityLevel.valueOf(level.toUpperCase());
            MixinPlatformManager.logger.debug("Setting mixin compatibility level: {}", new Object[] { value });
            MixinEnvironment.setCompatibilityLevel(value);
        }
        catch (IllegalArgumentException ex) {
            MixinPlatformManager.logger.warn("Invalid compatibility level specified: {}", new Object[] { level });
        }
    }
    
    final void addConfig(String config) {
        if (config.endsWith(".json")) {
            MixinPlatformManager.logger.debug("Registering mixin config: {}", new Object[] { config });
            Mixins.addConfiguration(config);
        }
        else if (config.contains(".json@")) {
            final int pos = config.indexOf(".json@");
            final String phaseName = config.substring(pos + 6);
            config = config.substring(0, pos + 5);
            final MixinEnvironment.Phase phase = MixinEnvironment.Phase.forName(phaseName);
            if (phase != null) {
                MixinPlatformManager.logger.warn("Setting config phase via manifest is deprecated: {}. Specify target in config instead", new Object[] { config });
                MixinPlatformManager.logger.debug("Registering mixin config: {}", new Object[] { config });
                MixinEnvironment.getEnvironment(phase).addConfiguration(config);
            }
        }
    }
    
    final void addTokenProvider(final String provider) {
        if (provider.contains("@")) {
            final String[] parts = provider.split("@", 2);
            final MixinEnvironment.Phase phase = MixinEnvironment.Phase.forName(parts[1]);
            if (phase != null) {
                MixinPlatformManager.logger.debug("Registering token provider class: {}", new Object[] { parts[0] });
                MixinEnvironment.getEnvironment(phase).registerTokenProviderClass(parts[0]);
            }
            return;
        }
        MixinEnvironment.getDefaultEnvironment().registerTokenProviderClass(provider);
    }
    
    static {
        logger = LogManager.getLogger("mixin");
    }
}

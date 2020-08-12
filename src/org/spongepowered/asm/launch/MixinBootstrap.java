// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.launch;

import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.MixinEnvironment;
import java.util.List;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.launch.platform.MixinPlatformManager;
import org.apache.logging.log4j.Logger;

public abstract class MixinBootstrap
{
    public static final String VERSION = "0.7.4";
    private static final Logger logger;
    private static boolean initialised;
    private static boolean initState;
    private static MixinPlatformManager platform;
    
    private MixinBootstrap() {
    }
    
    @Deprecated
    public static void addProxy() {
        MixinService.getService().beginPhase();
    }
    
    public static MixinPlatformManager getPlatform() {
        if (MixinBootstrap.platform == null) {
            final Object globalPlatformManager = GlobalProperties.get("mixin.platform");
            if (globalPlatformManager instanceof MixinPlatformManager) {
                MixinBootstrap.platform = (MixinPlatformManager)globalPlatformManager;
            }
            else {
                GlobalProperties.put("mixin.platform", MixinBootstrap.platform = new MixinPlatformManager());
                MixinBootstrap.platform.init();
            }
        }
        return MixinBootstrap.platform;
    }
    
    public static void init() {
        if (!start()) {
            return;
        }
        doInit(null);
    }
    
    static boolean start() {
        if (!isSubsystemRegistered()) {
            registerSubsystem("0.7.4");
            if (!MixinBootstrap.initialised) {
                MixinBootstrap.initialised = true;
                final String command = System.getProperty("sun.java.command");
                if (command != null && command.contains("GradleStart")) {
                    System.setProperty("mixin.env.remapRefMap", "true");
                }
                final MixinEnvironment.Phase initialPhase = MixinService.getService().getInitialPhase();
                if (initialPhase == MixinEnvironment.Phase.DEFAULT) {
                    MixinBootstrap.logger.error("Initialising mixin subsystem after game pre-init phase! Some mixins may be skipped.");
                    MixinEnvironment.init(initialPhase);
                    getPlatform().prepare(null);
                    MixinBootstrap.initState = false;
                }
                else {
                    MixinEnvironment.init(initialPhase);
                }
                MixinService.getService().beginPhase();
            }
            getPlatform();
            return true;
        }
        if (!checkSubsystemVersion()) {
            throw new MixinInitialisationError("Mixin subsystem version " + getActiveSubsystemVersion() + " was already initialised. Cannot bootstrap version " + "0.7.4");
        }
        return false;
    }
    
    static void doInit(final List<String> args) {
        if (MixinBootstrap.initialised) {
            getPlatform().getPhaseProviderClasses();
            if (MixinBootstrap.initState) {
                getPlatform().prepare(args);
                MixinService.getService().init();
            }
            return;
        }
        if (isSubsystemRegistered()) {
            MixinBootstrap.logger.warn("Multiple Mixin containers present, init suppressed for 0.7.4");
            return;
        }
        throw new IllegalStateException("MixinBootstrap.doInit() called before MixinBootstrap.start()");
    }
    
    static void inject() {
        getPlatform().inject();
    }
    
    private static boolean isSubsystemRegistered() {
        return GlobalProperties.get("mixin.initialised") != null;
    }
    
    private static boolean checkSubsystemVersion() {
        return "0.7.4".equals(getActiveSubsystemVersion());
    }
    
    private static Object getActiveSubsystemVersion() {
        final Object version = GlobalProperties.get("mixin.initialised");
        return (version != null) ? version : "";
    }
    
    private static void registerSubsystem(final String version) {
        GlobalProperties.put("mixin.initialised", version);
    }
    
    static {
        logger = LogManager.getLogger("mixin");
        MixinBootstrap.initialised = false;
        MixinBootstrap.initState = true;
        MixinService.boot();
        MixinService.getService().prepare();
    }
}

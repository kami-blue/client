// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.launch.platform;

import java.util.HashSet;
import java.util.Collections;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IRemapper;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.launchwrapper.Launch;
import java.io.File;
import net.minecraft.launchwrapper.LaunchClassLoader;
import java.lang.reflect.Method;
import java.util.List;
import org.spongepowered.asm.launch.GlobalProperties;
import java.net.URI;
import net.minecraft.launchwrapper.ITweaker;
import java.util.Set;

public class MixinPlatformAgentFML extends MixinPlatformAgentAbstract
{
    private static final String LOAD_CORE_MOD_METHOD = "loadCoreMod";
    private static final String GET_REPARSEABLE_COREMODS_METHOD = "getReparseableCoremods";
    private static final String CORE_MOD_MANAGER_CLASS = "net.minecraftforge.fml.relauncher.CoreModManager";
    private static final String CORE_MOD_MANAGER_CLASS_LEGACY = "cpw.mods.fml.relauncher.CoreModManager";
    private static final String GET_IGNORED_MODS_METHOD = "getIgnoredMods";
    private static final String GET_IGNORED_MODS_METHOD_LEGACY = "getLoadedCoremods";
    private static final String FML_REMAPPER_ADAPTER_CLASS = "org.spongepowered.asm.bridge.RemapperAdapterFML";
    private static final String FML_CMDLINE_COREMODS = "fml.coreMods.load";
    private static final String FML_PLUGIN_WRAPPER_CLASS = "FMLPluginWrapper";
    private static final String FML_CORE_MOD_INSTANCE_FIELD = "coreModInstance";
    private static final String MFATT_FORCELOADASMOD = "ForceLoadAsMod";
    private static final String MFATT_FMLCOREPLUGIN = "FMLCorePlugin";
    private static final String MFATT_COREMODCONTAINSMOD = "FMLCorePluginContainsFMLMod";
    private static final String FML_TWEAKER_DEOBF = "FMLDeobfTweaker";
    private static final String FML_TWEAKER_INJECTION = "FMLInjectionAndSortingTweaker";
    private static final String FML_TWEAKER_TERMINAL = "TerminalTweaker";
    private static final Set<String> loadedCoreMods;
    private final ITweaker coreModWrapper;
    private final String fileName;
    private Class<?> clCoreModManager;
    private boolean initInjectionState;
    
    public MixinPlatformAgentFML(final MixinPlatformManager manager, final URI uri) {
        super(manager, uri);
        this.fileName = this.container.getName();
        this.coreModWrapper = this.initFMLCoreMod();
    }
    
    private ITweaker initFMLCoreMod() {
        try {
            try {
                this.clCoreModManager = getCoreModManagerClass();
            }
            catch (ClassNotFoundException ex) {
                MixinPlatformAgentAbstract.logger.info("FML platform manager could not load class {}. Proceeding without FML support.", new Object[] { ex.getMessage() });
                return null;
            }
            if ("true".equalsIgnoreCase(this.attributes.get("ForceLoadAsMod"))) {
                MixinPlatformAgentAbstract.logger.debug("ForceLoadAsMod was specified for {}, attempting force-load", new Object[] { this.fileName });
                this.loadAsMod();
            }
            return this.injectCorePlugin();
        }
        catch (Exception ex2) {
            MixinPlatformAgentAbstract.logger.catching((Throwable)ex2);
            return null;
        }
    }
    
    private void loadAsMod() {
        try {
            getIgnoredMods(this.clCoreModManager).remove(this.fileName);
        }
        catch (Exception ex) {
            MixinPlatformAgentAbstract.logger.catching((Throwable)ex);
        }
        if (this.attributes.get("FMLCorePluginContainsFMLMod") != null) {
            if (this.isIgnoredReparseable()) {
                MixinPlatformAgentAbstract.logger.debug("Ignoring request to add {} to reparseable coremod collection - it is a deobfuscated dependency", new Object[] { this.fileName });
                return;
            }
            this.addReparseableJar();
        }
    }
    
    private boolean isIgnoredReparseable() {
        return this.container.toString().contains("deobfedDeps");
    }
    
    private void addReparseableJar() {
        try {
            final Method mdGetReparsedCoremods = this.clCoreModManager.getDeclaredMethod(GlobalProperties.getString("mixin.launch.fml.reparseablecoremodsmethod", "getReparseableCoremods"), (Class<?>[])new Class[0]);
            final List<String> reparsedCoremods = (List<String>)mdGetReparsedCoremods.invoke(null, new Object[0]);
            if (!reparsedCoremods.contains(this.fileName)) {
                MixinPlatformAgentAbstract.logger.debug("Adding {} to reparseable coremod collection", new Object[] { this.fileName });
                reparsedCoremods.add(this.fileName);
            }
        }
        catch (Exception ex) {
            MixinPlatformAgentAbstract.logger.catching((Throwable)ex);
        }
    }
    
    private ITweaker injectCorePlugin() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final String coreModName = this.attributes.get("FMLCorePlugin");
        if (coreModName == null) {
            return null;
        }
        if (this.isAlreadyInjected(coreModName)) {
            MixinPlatformAgentAbstract.logger.debug("{} has core plugin {}. Skipping because it was already injected.", new Object[] { this.fileName, coreModName });
            return null;
        }
        MixinPlatformAgentAbstract.logger.debug("{} has core plugin {}. Injecting it into FML for co-initialisation:", new Object[] { this.fileName, coreModName });
        final Method mdLoadCoreMod = this.clCoreModManager.getDeclaredMethod(GlobalProperties.getString("mixin.launch.fml.loadcoremodmethod", "loadCoreMod"), LaunchClassLoader.class, String.class, File.class);
        mdLoadCoreMod.setAccessible(true);
        final ITweaker wrapper = (ITweaker)mdLoadCoreMod.invoke(null, Launch.classLoader, coreModName, this.container);
        if (wrapper == null) {
            MixinPlatformAgentAbstract.logger.debug("Core plugin {} could not be loaded.", new Object[] { coreModName });
            return null;
        }
        this.initInjectionState = isTweakerQueued("FMLInjectionAndSortingTweaker");
        MixinPlatformAgentFML.loadedCoreMods.add(coreModName);
        return wrapper;
    }
    
    private boolean isAlreadyInjected(final String coreModName) {
        if (MixinPlatformAgentFML.loadedCoreMods.contains(coreModName)) {
            return true;
        }
        try {
            final List<ITweaker> tweakers = GlobalProperties.get("Tweaks");
            if (tweakers == null) {
                return false;
            }
            for (final ITweaker tweaker : tweakers) {
                final Class<? extends ITweaker> tweakClass = tweaker.getClass();
                if ("FMLPluginWrapper".equals(tweakClass.getSimpleName())) {
                    final Field fdCoreModInstance = tweakClass.getField("coreModInstance");
                    fdCoreModInstance.setAccessible(true);
                    final Object coreMod = fdCoreModInstance.get(tweaker);
                    if (coreModName.equals(coreMod.getClass().getName())) {
                        return true;
                    }
                    continue;
                }
            }
        }
        catch (Exception ex) {}
        return false;
    }
    
    @Override
    public String getPhaseProvider() {
        return MixinPlatformAgentFML.class.getName() + "$PhaseProvider";
    }
    
    @Override
    public void prepare() {
        this.initInjectionState |= isTweakerQueued("FMLInjectionAndSortingTweaker");
    }
    
    @Override
    public void initPrimaryContainer() {
        if (this.clCoreModManager != null) {
            this.injectRemapper();
        }
    }
    
    private void injectRemapper() {
        try {
            MixinPlatformAgentAbstract.logger.debug("Creating FML remapper adapter: {}", new Object[] { "org.spongepowered.asm.bridge.RemapperAdapterFML" });
            final Class<?> clFmlRemapperAdapter = Class.forName("org.spongepowered.asm.bridge.RemapperAdapterFML", true, (ClassLoader)Launch.classLoader);
            final Method mdCreate = clFmlRemapperAdapter.getDeclaredMethod("create", (Class<?>[])new Class[0]);
            final IRemapper remapper = (IRemapper)mdCreate.invoke(null, new Object[0]);
            MixinEnvironment.getDefaultEnvironment().getRemappers().add(remapper);
        }
        catch (Exception ex) {
            MixinPlatformAgentAbstract.logger.debug("Failed instancing FML remapper adapter, things will probably go horribly for notch-obf'd mods!");
        }
    }
    
    @Override
    public void inject() {
        if (this.coreModWrapper != null && this.checkForCoInitialisation()) {
            MixinPlatformAgentAbstract.logger.debug("FML agent is co-initiralising coremod instance {} for {}", new Object[] { this.coreModWrapper, this.uri });
            this.coreModWrapper.injectIntoClassLoader(Launch.classLoader);
        }
    }
    
    @Override
    public String getLaunchTarget() {
        return null;
    }
    
    protected final boolean checkForCoInitialisation() {
        final boolean injectionTweaker = isTweakerQueued("FMLInjectionAndSortingTweaker");
        final boolean terminalTweaker = isTweakerQueued("TerminalTweaker");
        if ((this.initInjectionState && terminalTweaker) || injectionTweaker) {
            MixinPlatformAgentAbstract.logger.debug("FML agent is skipping co-init for {} because FML will inject it normally", new Object[] { this.coreModWrapper });
            return false;
        }
        return !isTweakerQueued("FMLDeobfTweaker");
    }
    
    private static boolean isTweakerQueued(final String tweakerName) {
        for (final String tweaker : GlobalProperties.get("TweakClasses")) {
            if (tweaker.endsWith(tweakerName)) {
                return true;
            }
        }
        return false;
    }
    
    private static Class<?> getCoreModManagerClass() throws ClassNotFoundException {
        try {
            return Class.forName(GlobalProperties.getString("mixin.launch.fml.coremodmanagerclass", "net.minecraftforge.fml.relauncher.CoreModManager"));
        }
        catch (ClassNotFoundException ex) {
            return Class.forName("cpw.mods.fml.relauncher.CoreModManager");
        }
    }
    
    private static List<String> getIgnoredMods(final Class<?> clCoreModManager) throws IllegalAccessException, InvocationTargetException {
        Method mdGetIgnoredMods = null;
        try {
            mdGetIgnoredMods = clCoreModManager.getDeclaredMethod(GlobalProperties.getString("mixin.launch.fml.ignoredmodsmethod", "getIgnoredMods"), (Class<?>[])new Class[0]);
        }
        catch (NoSuchMethodException ex3) {
            try {
                mdGetIgnoredMods = clCoreModManager.getDeclaredMethod("getLoadedCoremods", (Class<?>[])new Class[0]);
            }
            catch (NoSuchMethodException ex2) {
                MixinPlatformAgentAbstract.logger.catching(Level.DEBUG, (Throwable)ex2);
                return Collections.emptyList();
            }
        }
        return (List<String>)mdGetIgnoredMods.invoke(null, new Object[0]);
    }
    
    static {
        loadedCoreMods = new HashSet<String>();
        for (final String cmdLineCoreMod : System.getProperty("fml.coreMods.load", "").split(",")) {
            if (!cmdLineCoreMod.isEmpty()) {
                MixinPlatformAgentAbstract.logger.debug("FML platform agent will ignore coremod {} specified on the command line", new Object[] { cmdLineCoreMod });
                MixinPlatformAgentFML.loadedCoreMods.add(cmdLineCoreMod);
            }
        }
    }
}

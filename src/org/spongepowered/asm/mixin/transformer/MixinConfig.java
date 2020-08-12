// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer;

import java.io.Reader;
import java.io.InputStreamReader;
import com.google.gson.Gson;
import org.spongepowered.asm.service.MixinService;
import org.apache.logging.log4j.Level;
import java.util.Collections;
import java.util.Collection;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;
import org.spongepowered.asm.mixin.refmap.RemappingReferenceMapper;
import org.spongepowered.asm.mixin.refmap.ReferenceMapper;
import org.spongepowered.asm.util.VersionNumber;
import java.util.Iterator;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.spongepowered.asm.launch.MixinInitialisationError;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.refmap.IReferenceMapper;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.service.IMixinService;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import java.util.Set;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;

final class MixinConfig implements Comparable<MixinConfig>, IMixinConfig
{
    private static int configOrder;
    private static final Set<String> globalMixinList;
    private final Logger logger;
    private final transient Map<String, List<MixinInfo>> mixinMapping;
    private final transient Set<String> unhandledTargets;
    private final transient List<MixinInfo> mixins;
    private transient Config handle;
    @SerializedName("target")
    private String selector;
    @SerializedName("minVersion")
    private String version;
    @SerializedName("compatibilityLevel")
    private String compatibility;
    @SerializedName("required")
    private boolean required;
    @SerializedName("priority")
    private int priority;
    @SerializedName("mixinPriority")
    private int mixinPriority;
    @SerializedName("package")
    private String mixinPackage;
    @SerializedName("mixins")
    private List<String> mixinClasses;
    @SerializedName("client")
    private List<String> mixinClassesClient;
    @SerializedName("server")
    private List<String> mixinClassesServer;
    @SerializedName("setSourceFile")
    private boolean setSourceFile;
    @SerializedName("refmap")
    private String refMapperConfig;
    @SerializedName("verbose")
    private boolean verboseLogging;
    private final transient int order;
    private final transient List<IListener> listeners;
    private transient IMixinService service;
    private transient MixinEnvironment env;
    private transient String name;
    @SerializedName("plugin")
    private String pluginClassName;
    @SerializedName("injectors")
    private InjectorOptions injectorOptions;
    @SerializedName("overwrites")
    private OverwriteOptions overwriteOptions;
    private transient IMixinConfigPlugin plugin;
    private transient IReferenceMapper refMapper;
    private transient boolean prepared;
    private transient boolean visited;
    
    private MixinConfig() {
        this.logger = LogManager.getLogger("mixin");
        this.mixinMapping = new HashMap<String, List<MixinInfo>>();
        this.unhandledTargets = new HashSet<String>();
        this.mixins = new ArrayList<MixinInfo>();
        this.priority = 1000;
        this.mixinPriority = 1000;
        this.setSourceFile = false;
        this.order = MixinConfig.configOrder++;
        this.listeners = new ArrayList<IListener>();
        this.injectorOptions = new InjectorOptions();
        this.overwriteOptions = new OverwriteOptions();
        this.prepared = false;
        this.visited = false;
    }
    
    private boolean onLoad(final IMixinService service, final String name, final MixinEnvironment fallbackEnvironment) {
        this.service = service;
        this.name = name;
        this.env = this.parseSelector(this.selector, fallbackEnvironment);
        this.required &= !this.env.getOption(MixinEnvironment.Option.IGNORE_REQUIRED);
        this.initCompatibilityLevel();
        this.initInjectionPoints();
        return this.checkVersion();
    }
    
    private void initCompatibilityLevel() {
        if (this.compatibility == null) {
            return;
        }
        final MixinEnvironment.CompatibilityLevel level = MixinEnvironment.CompatibilityLevel.valueOf(this.compatibility.trim().toUpperCase());
        final MixinEnvironment.CompatibilityLevel current = MixinEnvironment.getCompatibilityLevel();
        if (level == current) {
            return;
        }
        if (current.isAtLeast(level) && !current.canSupport(level)) {
            throw new MixinInitialisationError("Mixin config " + this.name + " requires compatibility level " + level + " which is too old");
        }
        if (!current.canElevateTo(level)) {
            throw new MixinInitialisationError("Mixin config " + this.name + " requires compatibility level " + level + " which is prohibited by " + current);
        }
        MixinEnvironment.setCompatibilityLevel(level);
    }
    
    private MixinEnvironment parseSelector(final String target, final MixinEnvironment fallbackEnvironment) {
        if (target != null) {
            final String[] split;
            final String[] selectors = split = target.split("[&\\| ]");
            for (String sel : split) {
                sel = sel.trim();
                final Pattern environmentSelector = Pattern.compile("^@env(?:ironment)?\\(([A-Z]+)\\)$");
                final Matcher environmentSelectorMatcher = environmentSelector.matcher(sel);
                if (environmentSelectorMatcher.matches()) {
                    return MixinEnvironment.getEnvironment(MixinEnvironment.Phase.forName(environmentSelectorMatcher.group(1)));
                }
            }
            final MixinEnvironment.Phase phase = MixinEnvironment.Phase.forName(target);
            if (phase != null) {
                return MixinEnvironment.getEnvironment(phase);
            }
        }
        return fallbackEnvironment;
    }
    
    private void initInjectionPoints() {
        if (this.injectorOptions.injectionPoints == null) {
            return;
        }
        for (final String injectionPoint : this.injectorOptions.injectionPoints) {
            try {
                final Class<?> injectionPointClass = this.service.getClassProvider().findClass(injectionPoint, true);
                if (InjectionPoint.class.isAssignableFrom(injectionPointClass)) {
                    InjectionPoint.register((Class<? extends InjectionPoint>)injectionPointClass);
                }
                else {
                    this.logger.error("Unable to register injection point {} for {}, class must extend InjectionPoint", new Object[] { injectionPointClass, this });
                }
            }
            catch (Throwable th) {
                this.logger.catching(th);
            }
        }
    }
    
    private boolean checkVersion() throws MixinInitialisationError {
        if (this.version == null) {
            this.logger.error("Mixin config {} does not specify \"minVersion\" property", new Object[] { this.name });
        }
        final VersionNumber minVersion = VersionNumber.parse(this.version);
        final VersionNumber curVersion = VersionNumber.parse(this.env.getVersion());
        if (minVersion.compareTo(curVersion) <= 0) {
            return true;
        }
        this.logger.warn("Mixin config {} requires mixin subsystem version {} but {} was found. The mixin config will not be applied.", new Object[] { this.name, minVersion, curVersion });
        if (this.required) {
            throw new MixinInitialisationError("Required mixin config " + this.name + " requires mixin subsystem version " + minVersion);
        }
        return false;
    }
    
    void addListener(final IListener listener) {
        this.listeners.add(listener);
    }
    
    void onSelect() {
        if (this.pluginClassName != null) {
            try {
                final Class<?> pluginClass = this.service.getClassProvider().findClass(this.pluginClassName, true);
                this.plugin = (IMixinConfigPlugin)pluginClass.newInstance();
                if (this.plugin != null) {
                    this.plugin.onLoad(this.mixinPackage);
                }
            }
            catch (Throwable th) {
                th.printStackTrace();
                this.plugin = null;
            }
        }
        if (!this.mixinPackage.endsWith(".")) {
            this.mixinPackage += ".";
        }
        boolean suppressRefMapWarning = false;
        if (this.refMapperConfig == null) {
            if (this.plugin != null) {
                this.refMapperConfig = this.plugin.getRefMapperConfig();
            }
            if (this.refMapperConfig == null) {
                suppressRefMapWarning = true;
                this.refMapperConfig = "mixin.refmap.json";
            }
        }
        this.refMapper = ReferenceMapper.read(this.refMapperConfig);
        this.verboseLogging |= this.env.getOption(MixinEnvironment.Option.DEBUG_VERBOSE);
        if (!suppressRefMapWarning && this.refMapper.isDefault() && !this.env.getOption(MixinEnvironment.Option.DISABLE_REFMAP)) {
            this.logger.warn("Reference map '{}' for {} could not be read. If this is a development environment you can ignore this message", new Object[] { this.refMapperConfig, this });
        }
        if (this.env.getOption(MixinEnvironment.Option.REFMAP_REMAP)) {
            this.refMapper = RemappingReferenceMapper.of(this.env, this.refMapper);
        }
    }
    
    void prepare() {
        if (this.prepared) {
            return;
        }
        this.prepared = true;
        this.prepareMixins(this.mixinClasses, false);
        switch (this.env.getSide()) {
            case CLIENT: {
                this.prepareMixins(this.mixinClassesClient, false);
                break;
            }
            case SERVER: {
                this.prepareMixins(this.mixinClassesServer, false);
                break;
            }
            default: {
                this.logger.warn("Mixin environment was unable to detect the current side, sided mixins will not be applied");
                break;
            }
        }
    }
    
    void postInitialise() {
        if (this.plugin != null) {
            final List<String> pluginMixins = this.plugin.getMixins();
            this.prepareMixins(pluginMixins, true);
        }
        final Iterator<MixinInfo> iter = this.mixins.iterator();
        while (iter.hasNext()) {
            final MixinInfo mixin = iter.next();
            try {
                mixin.validate();
                for (final IListener listener : this.listeners) {
                    listener.onInit(mixin);
                }
            }
            catch (InvalidMixinException ex) {
                this.logger.error(ex.getMixin() + ": " + ex.getMessage(), (Throwable)ex);
                this.removeMixin(mixin);
                iter.remove();
            }
            catch (Exception ex2) {
                this.logger.error(ex2.getMessage(), (Throwable)ex2);
                this.removeMixin(mixin);
                iter.remove();
            }
        }
    }
    
    private void removeMixin(final MixinInfo remove) {
        for (final List<MixinInfo> mixinsFor : this.mixinMapping.values()) {
            final Iterator<MixinInfo> iter = mixinsFor.iterator();
            while (iter.hasNext()) {
                if (remove == iter.next()) {
                    iter.remove();
                }
            }
        }
    }
    
    private void prepareMixins(final List<String> mixinClasses, final boolean suppressPlugin) {
        if (mixinClasses == null) {
            return;
        }
        for (final String mixinClass : mixinClasses) {
            final String fqMixinClass = this.mixinPackage + mixinClass;
            if (mixinClass != null) {
                if (MixinConfig.globalMixinList.contains(fqMixinClass)) {
                    continue;
                }
                MixinInfo mixin = null;
                try {
                    mixin = new MixinInfo(this.service, this, mixinClass, true, this.plugin, suppressPlugin);
                    if (mixin.getTargetClasses().size() <= 0) {
                        continue;
                    }
                    MixinConfig.globalMixinList.add(fqMixinClass);
                    for (final String targetClass : mixin.getTargetClasses()) {
                        final String targetClassName = targetClass.replace('/', '.');
                        this.mixinsFor(targetClassName).add(mixin);
                        this.unhandledTargets.add(targetClassName);
                    }
                    for (final IListener listener : this.listeners) {
                        listener.onPrepare(mixin);
                    }
                    this.mixins.add(mixin);
                }
                catch (InvalidMixinException ex) {
                    if (this.required) {
                        throw ex;
                    }
                    this.logger.error(ex.getMessage(), (Throwable)ex);
                }
                catch (Exception ex2) {
                    if (this.required) {
                        throw new InvalidMixinException(mixin, "Error initialising mixin " + mixin + " - " + ex2.getClass() + ": " + ex2.getMessage(), ex2);
                    }
                    this.logger.error(ex2.getMessage(), (Throwable)ex2);
                }
            }
        }
    }
    
    void postApply(final String transformedName, final ClassNode targetClass) {
        this.unhandledTargets.remove(transformedName);
    }
    
    public Config getHandle() {
        if (this.handle == null) {
            this.handle = new Config(this);
        }
        return this.handle;
    }
    
    @Override
    public boolean isRequired() {
        return this.required;
    }
    
    @Override
    public MixinEnvironment getEnvironment() {
        return this.env;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public String getMixinPackage() {
        return this.mixinPackage;
    }
    
    @Override
    public int getPriority() {
        return this.priority;
    }
    
    public int getDefaultMixinPriority() {
        return this.mixinPriority;
    }
    
    public int getDefaultRequiredInjections() {
        return this.injectorOptions.defaultRequireValue;
    }
    
    public String getDefaultInjectorGroup() {
        final String defaultGroup = this.injectorOptions.defaultGroup;
        return (defaultGroup != null && !defaultGroup.isEmpty()) ? defaultGroup : "default";
    }
    
    public boolean conformOverwriteVisibility() {
        return this.overwriteOptions.conformAccessModifiers;
    }
    
    public boolean requireOverwriteAnnotations() {
        return this.overwriteOptions.requireOverwriteAnnotations;
    }
    
    public int getMaxShiftByValue() {
        return Math.min(Math.max(this.injectorOptions.maxShiftBy, 0), 0);
    }
    
    public boolean select(final MixinEnvironment environment) {
        this.visited = true;
        return this.env == environment;
    }
    
    boolean isVisited() {
        return this.visited;
    }
    
    int getDeclaredMixinCount() {
        return getCollectionSize(this.mixinClasses, this.mixinClassesClient, this.mixinClassesServer);
    }
    
    int getMixinCount() {
        return this.mixins.size();
    }
    
    public List<String> getClasses() {
        return Collections.unmodifiableList((List<? extends String>)this.mixinClasses);
    }
    
    public boolean shouldSetSourceFile() {
        return this.setSourceFile;
    }
    
    public IReferenceMapper getReferenceMapper() {
        if (this.env.getOption(MixinEnvironment.Option.DISABLE_REFMAP)) {
            return ReferenceMapper.DEFAULT_MAPPER;
        }
        this.refMapper.setContext(this.env.getRefmapObfuscationContext());
        return this.refMapper;
    }
    
    String remapClassName(final String className, final String reference) {
        return this.getReferenceMapper().remap(className, reference);
    }
    
    @Override
    public IMixinConfigPlugin getPlugin() {
        return this.plugin;
    }
    
    @Override
    public Set<String> getTargets() {
        return Collections.unmodifiableSet((Set<? extends String>)this.mixinMapping.keySet());
    }
    
    public Set<String> getUnhandledTargets() {
        return Collections.unmodifiableSet((Set<? extends String>)this.unhandledTargets);
    }
    
    public Level getLoggingLevel() {
        return this.verboseLogging ? Level.INFO : Level.DEBUG;
    }
    
    public boolean packageMatch(final String className) {
        return className.startsWith(this.mixinPackage);
    }
    
    public boolean hasMixinsFor(final String targetClass) {
        return this.mixinMapping.containsKey(targetClass);
    }
    
    public List<MixinInfo> getMixinsFor(final String targetClass) {
        return this.mixinsFor(targetClass);
    }
    
    private List<MixinInfo> mixinsFor(final String targetClass) {
        List<MixinInfo> mixins = this.mixinMapping.get(targetClass);
        if (mixins == null) {
            mixins = new ArrayList<MixinInfo>();
            this.mixinMapping.put(targetClass, mixins);
        }
        return mixins;
    }
    
    public List<String> reloadMixin(final String mixinClass, final byte[] bytes) {
        for (final MixinInfo mixin : this.mixins) {
            if (mixin.getClassName().equals(mixinClass)) {
                mixin.reloadMixin(bytes);
                return mixin.getTargetClasses();
            }
        }
        return Collections.emptyList();
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    @Override
    public int compareTo(final MixinConfig other) {
        if (other == null) {
            return 0;
        }
        if (other.priority == this.priority) {
            return this.order - other.order;
        }
        return this.priority - other.priority;
    }
    
    static Config create(final String configFile, final MixinEnvironment outer) {
        try {
            final IMixinService service = MixinService.getService();
            final MixinConfig config = (MixinConfig)new Gson().fromJson((Reader)new InputStreamReader(service.getResourceAsStream(configFile)), (Class)MixinConfig.class);
            if (config.onLoad(service, configFile, outer)) {
                return config.getHandle();
            }
            return null;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(String.format("The specified resource '%s' was invalid or could not be read", configFile), ex);
        }
    }
    
    private static int getCollectionSize(final Collection<?>... collections) {
        int total = 0;
        for (final Collection<?> collection : collections) {
            if (collection != null) {
                total += collection.size();
            }
        }
        return total;
    }
    
    static {
        MixinConfig.configOrder = 0;
        globalMixinList = new HashSet<String>();
    }
    
    static class InjectorOptions
    {
        @SerializedName("defaultRequire")
        int defaultRequireValue;
        @SerializedName("defaultGroup")
        String defaultGroup;
        @SerializedName("injectionPoints")
        List<String> injectionPoints;
        @SerializedName("maxShiftBy")
        int maxShiftBy;
        
        InjectorOptions() {
            this.defaultRequireValue = 0;
            this.defaultGroup = "default";
            this.maxShiftBy = 0;
        }
    }
    
    static class OverwriteOptions
    {
        @SerializedName("conformVisibility")
        boolean conformAccessModifiers;
        @SerializedName("requireAnnotations")
        boolean requireOverwriteAnnotations;
    }
    
    interface IListener
    {
        void onPrepare(final MixinInfo p0);
        
        void onInit(final MixinInfo p0);
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Filter;
import org.spongepowered.asm.util.JavaVersion;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.transformer.MixinTransformer;
import org.spongepowered.asm.service.ITransformer;
import java.util.Iterator;
import java.util.Collections;
import org.spongepowered.asm.mixin.extensibility.IEnvironmentTokenProvider;
import org.spongepowered.asm.launch.GlobalProperties;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.asm.mixin.throwables.MixinException;
import org.spongepowered.asm.service.MixinService;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import org.spongepowered.asm.service.ILegacyClassTransformer;
import org.spongepowered.asm.obfuscation.RemapperChain;
import java.util.Map;
import java.util.List;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.util.perf.Profiler;
import org.apache.logging.log4j.Logger;
import java.util.Set;
import org.spongepowered.asm.util.ITokenProvider;

public final class MixinEnvironment implements ITokenProvider
{
    private static final Set<String> excludeTransformers;
    private static MixinEnvironment currentEnvironment;
    private static Phase currentPhase;
    private static CompatibilityLevel compatibility;
    private static boolean showHeader;
    private static final Logger logger;
    private static final Profiler profiler;
    private final IMixinService service;
    private final Phase phase;
    private final String configsKey;
    private final boolean[] options;
    private final Set<String> tokenProviderClasses;
    private final List<TokenProviderWrapper> tokenProviders;
    private final Map<String, Integer> internalTokens;
    private final RemapperChain remappers;
    private Side side;
    private List<ILegacyClassTransformer> transformers;
    private String obfuscationContext;
    
    MixinEnvironment(final Phase phase) {
        this.tokenProviderClasses = new HashSet<String>();
        this.tokenProviders = new ArrayList<TokenProviderWrapper>();
        this.internalTokens = new HashMap<String, Integer>();
        this.remappers = new RemapperChain();
        this.obfuscationContext = null;
        this.service = MixinService.getService();
        this.phase = phase;
        this.configsKey = "mixin.configs." + this.phase.name.toLowerCase();
        final Object version = this.getVersion();
        if (version == null || !"0.7.4".equals(version)) {
            throw new MixinException("Environment conflict, mismatched versions or you didn't call MixinBootstrap.init()");
        }
        this.service.checkEnv(this);
        this.options = new boolean[Option.values().length];
        for (final Option option : Option.values()) {
            this.options[option.ordinal()] = option.getBooleanValue();
        }
        if (MixinEnvironment.showHeader) {
            MixinEnvironment.showHeader = false;
            this.printHeader(version);
        }
    }
    
    private void printHeader(final Object version) {
        final String codeSource = this.getCodeSource();
        final String serviceName = this.service.getName();
        final Side side = this.getSide();
        MixinEnvironment.logger.info("SpongePowered MIXIN Subsystem Version={} Source={} Service={} Env={}", new Object[] { version, codeSource, serviceName, side });
        final boolean verbose = this.getOption(Option.DEBUG_VERBOSE);
        if (verbose || this.getOption(Option.DEBUG_EXPORT) || this.getOption(Option.DEBUG_PROFILER)) {
            final PrettyPrinter printer = new PrettyPrinter(32);
            printer.add("SpongePowered MIXIN%s", verbose ? " (Verbose debugging enabled)" : "").centre().hr();
            printer.kv("Code source", (Object)codeSource);
            printer.kv("Internal Version", version);
            printer.kv("Java 8 Supported", CompatibilityLevel.JAVA_8.isSupported()).hr();
            printer.kv("Service Name", (Object)serviceName);
            printer.kv("Service Class", (Object)this.service.getClass().getName()).hr();
            for (final Option option : Option.values()) {
                final StringBuilder indent = new StringBuilder();
                for (int i = 0; i < option.depth; ++i) {
                    indent.append("- ");
                }
                printer.kv(option.property, "%s<%s>", indent, option);
            }
            printer.hr().kv("Detected Side", side);
            printer.print(System.err);
        }
    }
    
    private String getCodeSource() {
        try {
            return this.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
        }
        catch (Throwable th) {
            return "Unknown";
        }
    }
    
    public Phase getPhase() {
        return this.phase;
    }
    
    @Deprecated
    public List<String> getMixinConfigs() {
        List<String> mixinConfigs = GlobalProperties.get(this.configsKey);
        if (mixinConfigs == null) {
            mixinConfigs = new ArrayList<String>();
            GlobalProperties.put(this.configsKey, mixinConfigs);
        }
        return mixinConfigs;
    }
    
    @Deprecated
    public MixinEnvironment addConfiguration(final String config) {
        MixinEnvironment.logger.warn("MixinEnvironment::addConfiguration is deprecated and will be removed. Use Mixins::addConfiguration instead!");
        Mixins.addConfiguration(config, this);
        return this;
    }
    
    void registerConfig(final String config) {
        final List<String> configs = this.getMixinConfigs();
        if (!configs.contains(config)) {
            configs.add(config);
        }
    }
    
    @Deprecated
    public MixinEnvironment registerErrorHandlerClass(final String handlerName) {
        Mixins.registerErrorHandlerClass(handlerName);
        return this;
    }
    
    public MixinEnvironment registerTokenProviderClass(final String providerName) {
        if (!this.tokenProviderClasses.contains(providerName)) {
            try {
                final Class<? extends IEnvironmentTokenProvider> providerClass = (Class<? extends IEnvironmentTokenProvider>)this.service.getClassProvider().findClass(providerName, true);
                final IEnvironmentTokenProvider provider = (IEnvironmentTokenProvider)providerClass.newInstance();
                this.registerTokenProvider(provider);
            }
            catch (Throwable th) {
                MixinEnvironment.logger.error("Error instantiating " + providerName, th);
            }
        }
        return this;
    }
    
    public MixinEnvironment registerTokenProvider(final IEnvironmentTokenProvider provider) {
        if (provider != null && !this.tokenProviderClasses.contains(provider.getClass().getName())) {
            final String providerName = provider.getClass().getName();
            final TokenProviderWrapper wrapper = new TokenProviderWrapper(provider, this);
            MixinEnvironment.logger.info("Adding new token provider {} to {}", new Object[] { providerName, this });
            this.tokenProviders.add(wrapper);
            this.tokenProviderClasses.add(providerName);
            Collections.sort(this.tokenProviders);
        }
        return this;
    }
    
    @Override
    public Integer getToken(String token) {
        token = token.toUpperCase();
        for (final TokenProviderWrapper provider : this.tokenProviders) {
            final Integer value = provider.getToken(token);
            if (value != null) {
                return value;
            }
        }
        return this.internalTokens.get(token);
    }
    
    @Deprecated
    public Set<String> getErrorHandlerClasses() {
        return Mixins.getErrorHandlerClasses();
    }
    
    public Object getActiveTransformer() {
        return GlobalProperties.get("mixin.transformer");
    }
    
    public void setActiveTransformer(final ITransformer transformer) {
        if (transformer != null) {
            GlobalProperties.put("mixin.transformer", transformer);
        }
    }
    
    public MixinEnvironment setSide(final Side side) {
        if (side != null && this.getSide() == Side.UNKNOWN && side != Side.UNKNOWN) {
            this.side = side;
        }
        return this;
    }
    
    public Side getSide() {
        if (this.side == null) {
            for (final Side side : Side.values()) {
                if (side.detect()) {
                    this.side = side;
                    break;
                }
            }
        }
        return (this.side != null) ? this.side : Side.UNKNOWN;
    }
    
    public String getVersion() {
        return GlobalProperties.get("mixin.initialised");
    }
    
    public boolean getOption(final Option option) {
        return this.options[option.ordinal()];
    }
    
    public void setOption(final Option option, final boolean value) {
        this.options[option.ordinal()] = value;
    }
    
    public String getOptionValue(final Option option) {
        return option.getStringValue();
    }
    
    public <E extends Enum<E>> E getOption(final Option option, final E defaultValue) {
        return option.getEnumValue(defaultValue);
    }
    
    public void setObfuscationContext(final String context) {
        this.obfuscationContext = context;
    }
    
    public String getObfuscationContext() {
        return this.obfuscationContext;
    }
    
    public String getRefmapObfuscationContext() {
        final String overrideObfuscationType = Option.OBFUSCATION_TYPE.getStringValue();
        if (overrideObfuscationType != null) {
            return overrideObfuscationType;
        }
        return this.obfuscationContext;
    }
    
    public RemapperChain getRemappers() {
        return this.remappers;
    }
    
    public void audit() {
        final Object activeTransformer = this.getActiveTransformer();
        if (activeTransformer instanceof MixinTransformer) {
            final MixinTransformer transformer = (MixinTransformer)activeTransformer;
            transformer.audit(this);
        }
    }
    
    public List<ILegacyClassTransformer> getTransformers() {
        if (this.transformers == null) {
            this.buildTransformerDelegationList();
        }
        return Collections.unmodifiableList((List<? extends ILegacyClassTransformer>)this.transformers);
    }
    
    public void addTransformerExclusion(final String name) {
        MixinEnvironment.excludeTransformers.add(name);
        this.transformers = null;
    }
    
    private void buildTransformerDelegationList() {
        MixinEnvironment.logger.debug("Rebuilding transformer delegation list:");
        this.transformers = new ArrayList<ILegacyClassTransformer>();
        for (final ITransformer transformer : this.service.getTransformers()) {
            if (!(transformer instanceof ILegacyClassTransformer)) {
                continue;
            }
            final ILegacyClassTransformer legacyTransformer = (ILegacyClassTransformer)transformer;
            final String transformerName = legacyTransformer.getName();
            boolean include = true;
            for (final String excludeClass : MixinEnvironment.excludeTransformers) {
                if (transformerName.contains(excludeClass)) {
                    include = false;
                    break;
                }
            }
            if (include && !legacyTransformer.isDelegationExcluded()) {
                MixinEnvironment.logger.debug("  Adding:    {}", new Object[] { transformerName });
                this.transformers.add(legacyTransformer);
            }
            else {
                MixinEnvironment.logger.debug("  Excluding: {}", new Object[] { transformerName });
            }
        }
        MixinEnvironment.logger.debug("Transformer delegation list created with {} entries", new Object[] { this.transformers.size() });
    }
    
    @Override
    public String toString() {
        return String.format("%s[%s]", this.getClass().getSimpleName(), this.phase);
    }
    
    private static Phase getCurrentPhase() {
        if (MixinEnvironment.currentPhase == Phase.NOT_INITIALISED) {
            init(Phase.PREINIT);
        }
        return MixinEnvironment.currentPhase;
    }
    
    public static void init(final Phase phase) {
        if (MixinEnvironment.currentPhase == Phase.NOT_INITIALISED) {
            MixinEnvironment.currentPhase = phase;
            final MixinEnvironment env = getEnvironment(phase);
            getProfiler().setActive(env.getOption(Option.DEBUG_PROFILER));
            final MixinLogger mixinLogger = new MixinLogger();
        }
    }
    
    public static MixinEnvironment getEnvironment(final Phase phase) {
        if (phase == null) {
            return Phase.DEFAULT.getEnvironment();
        }
        return phase.getEnvironment();
    }
    
    public static MixinEnvironment getDefaultEnvironment() {
        return getEnvironment(Phase.DEFAULT);
    }
    
    public static MixinEnvironment getCurrentEnvironment() {
        if (MixinEnvironment.currentEnvironment == null) {
            MixinEnvironment.currentEnvironment = getEnvironment(getCurrentPhase());
        }
        return MixinEnvironment.currentEnvironment;
    }
    
    public static CompatibilityLevel getCompatibilityLevel() {
        return MixinEnvironment.compatibility;
    }
    
    @Deprecated
    public static void setCompatibilityLevel(final CompatibilityLevel level) throws IllegalArgumentException {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (!"org.spongepowered.asm.mixin.transformer.MixinConfig".equals(stackTrace[2].getClassName())) {
            MixinEnvironment.logger.warn("MixinEnvironment::setCompatibilityLevel is deprecated and will be removed. Set level via config instead!");
        }
        if (level != MixinEnvironment.compatibility && level.isAtLeast(MixinEnvironment.compatibility)) {
            if (!level.isSupported()) {
                throw new IllegalArgumentException("The requested compatibility level " + level + " could not be set. Level is not supported");
            }
            MixinEnvironment.compatibility = level;
            MixinEnvironment.logger.info("Compatibility level set to {}", new Object[] { level });
        }
    }
    
    public static Profiler getProfiler() {
        return MixinEnvironment.profiler;
    }
    
    static void gotoPhase(final Phase phase) {
        if (phase == null || phase.ordinal < 0) {
            throw new IllegalArgumentException("Cannot go to the specified phase, phase is null or invalid");
        }
        if (phase.ordinal > getCurrentPhase().ordinal) {
            MixinService.getService().beginPhase();
        }
        if (phase == Phase.DEFAULT) {
            final org.apache.logging.log4j.core.Logger log = (org.apache.logging.log4j.core.Logger)LogManager.getLogger("FML");
            log.removeAppender((Appender)MixinLogger.appender);
        }
        MixinEnvironment.currentPhase = phase;
        MixinEnvironment.currentEnvironment = getEnvironment(getCurrentPhase());
    }
    
    static {
        excludeTransformers = Sets.newHashSet((Object[])new String[] { "net.minecraftforge.fml.common.asm.transformers.EventSubscriptionTransformer", "cpw.mods.fml.common.asm.transformers.EventSubscriptionTransformer", "net.minecraftforge.fml.common.asm.transformers.TerminalTransformer", "cpw.mods.fml.common.asm.transformers.TerminalTransformer" });
        MixinEnvironment.currentPhase = Phase.NOT_INITIALISED;
        MixinEnvironment.compatibility = Option.DEFAULT_COMPATIBILITY_LEVEL.getEnumValue(CompatibilityLevel.JAVA_6);
        MixinEnvironment.showHeader = true;
        logger = LogManager.getLogger("mixin");
        profiler = new Profiler();
    }
    
    public static final class Phase
    {
        static final Phase NOT_INITIALISED;
        public static final Phase PREINIT;
        public static final Phase INIT;
        public static final Phase DEFAULT;
        static final List<Phase> phases;
        final int ordinal;
        final String name;
        private MixinEnvironment environment;
        
        private Phase(final int ordinal, final String name) {
            this.ordinal = ordinal;
            this.name = name;
        }
        
        @Override
        public String toString() {
            return this.name;
        }
        
        public static Phase forName(final String name) {
            for (final Phase phase : Phase.phases) {
                if (phase.name.equals(name)) {
                    return phase;
                }
            }
            return null;
        }
        
        MixinEnvironment getEnvironment() {
            if (this.ordinal < 0) {
                throw new IllegalArgumentException("Cannot access the NOT_INITIALISED environment");
            }
            if (this.environment == null) {
                this.environment = new MixinEnvironment(this);
            }
            return this.environment;
        }
        
        static {
            NOT_INITIALISED = new Phase(-1, "NOT_INITIALISED");
            PREINIT = new Phase(0, "PREINIT");
            INIT = new Phase(1, "INIT");
            DEFAULT = new Phase(2, "DEFAULT");
            phases = (List)ImmutableList.of((Object)Phase.PREINIT, (Object)Phase.INIT, (Object)Phase.DEFAULT);
        }
    }
    
    public enum Side
    {
        UNKNOWN {
            @Override
            protected boolean detect() {
                return false;
            }
        }, 
        CLIENT {
            @Override
            protected boolean detect() {
                final String sideName = MixinService.getService().getSideName();
                return "CLIENT".equals(sideName);
            }
        }, 
        SERVER {
            @Override
            protected boolean detect() {
                final String sideName = MixinService.getService().getSideName();
                return "SERVER".equals(sideName) || "DEDICATEDSERVER".equals(sideName);
            }
        };
        
        protected abstract boolean detect();
    }
    
    public enum Option
    {
        DEBUG_ALL("debug"), 
        DEBUG_EXPORT(Option.DEBUG_ALL, "export"), 
        DEBUG_EXPORT_FILTER(Option.DEBUG_EXPORT, "filter", false), 
        DEBUG_EXPORT_DECOMPILE(Option.DEBUG_EXPORT, Inherit.ALLOW_OVERRIDE, "decompile"), 
        DEBUG_EXPORT_DECOMPILE_THREADED(Option.DEBUG_EXPORT_DECOMPILE, Inherit.ALLOW_OVERRIDE, "async"), 
        DEBUG_VERIFY(Option.DEBUG_ALL, "verify"), 
        DEBUG_VERBOSE(Option.DEBUG_ALL, "verbose"), 
        DEBUG_INJECTORS(Option.DEBUG_ALL, "countInjections"), 
        DEBUG_STRICT(Option.DEBUG_ALL, Inherit.INDEPENDENT, "strict"), 
        DEBUG_UNIQUE(Option.DEBUG_STRICT, "unique"), 
        DEBUG_TARGETS(Option.DEBUG_STRICT, "targets"), 
        DEBUG_PROFILER(Option.DEBUG_ALL, Inherit.ALLOW_OVERRIDE, "profiler"), 
        DUMP_TARGET_ON_FAILURE("dumpTargetOnFailure"), 
        CHECK_ALL("checks"), 
        CHECK_IMPLEMENTS(Option.CHECK_ALL, "interfaces"), 
        CHECK_IMPLEMENTS_STRICT(Option.CHECK_IMPLEMENTS, Inherit.ALLOW_OVERRIDE, "strict"), 
        IGNORE_CONSTRAINTS("ignoreConstraints"), 
        HOT_SWAP("hotSwap"), 
        ENVIRONMENT(Inherit.ALWAYS_FALSE, "env"), 
        OBFUSCATION_TYPE(Option.ENVIRONMENT, Inherit.ALWAYS_FALSE, "obf"), 
        DISABLE_REFMAP(Option.ENVIRONMENT, Inherit.INDEPENDENT, "disableRefMap"), 
        REFMAP_REMAP(Option.ENVIRONMENT, Inherit.INDEPENDENT, "remapRefMap"), 
        REFMAP_REMAP_RESOURCE(Option.ENVIRONMENT, Inherit.INDEPENDENT, "refMapRemappingFile", ""), 
        REFMAP_REMAP_SOURCE_ENV(Option.ENVIRONMENT, Inherit.INDEPENDENT, "refMapRemappingEnv", "searge"), 
        IGNORE_REQUIRED(Option.ENVIRONMENT, Inherit.INDEPENDENT, "ignoreRequired"), 
        DEFAULT_COMPATIBILITY_LEVEL(Option.ENVIRONMENT, Inherit.INDEPENDENT, "compatLevel"), 
        SHIFT_BY_VIOLATION_BEHAVIOUR(Option.ENVIRONMENT, Inherit.INDEPENDENT, "shiftByViolation", "warn"), 
        INITIALISER_INJECTION_MODE("initialiserInjectionMode", "default");
        
        private static final String PREFIX = "mixin";
        final Option parent;
        final Inherit inheritance;
        final String property;
        final String defaultValue;
        final boolean isFlag;
        final int depth;
        
        private Option(final String property) {
            this(null, property, true);
        }
        
        private Option(final Inherit inheritance, final String property) {
            this(null, inheritance, property, true);
        }
        
        private Option(final String property, final boolean flag) {
            this(null, property, flag);
        }
        
        private Option(final String property, final String defaultStringValue) {
            this(null, Inherit.INDEPENDENT, property, false, defaultStringValue);
        }
        
        private Option(final Option parent, final String property) {
            this(parent, Inherit.INHERIT, property, true);
        }
        
        private Option(final Option parent, final Inherit inheritance, final String property) {
            this(parent, inheritance, property, true);
        }
        
        private Option(final Option parent, final String property, final boolean isFlag) {
            this(parent, Inherit.INHERIT, property, isFlag, null);
        }
        
        private Option(final Option parent, final Inherit inheritance, final String property, final boolean isFlag) {
            this(parent, inheritance, property, isFlag, null);
        }
        
        private Option(final Option parent, final String property, final String defaultStringValue) {
            this(parent, Inherit.INHERIT, property, false, defaultStringValue);
        }
        
        private Option(final Option parent, final Inherit inheritance, final String property, final String defaultStringValue) {
            this(parent, inheritance, property, false, defaultStringValue);
        }
        
        private Option(Option parent, final Inherit inheritance, final String property, final boolean isFlag, final String defaultStringValue) {
            this.parent = parent;
            this.inheritance = inheritance;
            this.property = ((parent != null) ? parent.property : "mixin") + "." + property;
            this.defaultValue = defaultStringValue;
            this.isFlag = isFlag;
            int depth;
            for (depth = 0; parent != null; parent = parent.parent, ++depth) {}
            this.depth = depth;
        }
        
        Option getParent() {
            return this.parent;
        }
        
        String getProperty() {
            return this.property;
        }
        
        @Override
        public String toString() {
            return this.isFlag ? String.valueOf(this.getBooleanValue()) : this.getStringValue();
        }
        
        private boolean getLocalBooleanValue(final boolean defaultValue) {
            return Boolean.parseBoolean(System.getProperty(this.property, Boolean.toString(defaultValue)));
        }
        
        private boolean getInheritedBooleanValue() {
            return this.parent != null && this.parent.getBooleanValue();
        }
        
        final boolean getBooleanValue() {
            if (this.inheritance == Inherit.ALWAYS_FALSE) {
                return false;
            }
            final boolean local = this.getLocalBooleanValue(false);
            if (this.inheritance == Inherit.INDEPENDENT) {
                return local;
            }
            final boolean inherited = local || this.getInheritedBooleanValue();
            return (this.inheritance == Inherit.INHERIT) ? inherited : this.getLocalBooleanValue(inherited);
        }
        
        final String getStringValue() {
            return (this.parent == null || this.parent.getBooleanValue()) ? System.getProperty(this.property, this.defaultValue) : this.defaultValue;
        }
        
         <E extends Enum<E>> E getEnumValue(final E defaultValue) {
            final String value = System.getProperty(this.property, defaultValue.name());
            try {
                return Enum.valueOf(defaultValue.getClass(), value.toUpperCase());
            }
            catch (IllegalArgumentException ex) {
                return defaultValue;
            }
        }
        
        private enum Inherit
        {
            INHERIT, 
            ALLOW_OVERRIDE, 
            INDEPENDENT, 
            ALWAYS_FALSE;
        }
    }
    
    public enum CompatibilityLevel
    {
        JAVA_6(6, 50, false), 
        JAVA_7(7, 51, false) {
            @Override
            boolean isSupported() {
                return JavaVersion.current() >= 1.7;
            }
        }, 
        JAVA_8(8, 52, true) {
            @Override
            boolean isSupported() {
                return JavaVersion.current() >= 1.8;
            }
        }, 
        JAVA_9(9, 53, true) {
            @Override
            boolean isSupported() {
                return false;
            }
        };
        
        private static final int CLASS_V1_9 = 53;
        private final int ver;
        private final int classVersion;
        private final boolean supportsMethodsInInterfaces;
        private CompatibilityLevel maxCompatibleLevel;
        
        private CompatibilityLevel(final int ver, final int classVersion, final boolean resolveMethodsInInterfaces) {
            this.ver = ver;
            this.classVersion = classVersion;
            this.supportsMethodsInInterfaces = resolveMethodsInInterfaces;
        }
        
        private void setMaxCompatibleLevel(final CompatibilityLevel maxCompatibleLevel) {
            this.maxCompatibleLevel = maxCompatibleLevel;
        }
        
        boolean isSupported() {
            return true;
        }
        
        public int classVersion() {
            return this.classVersion;
        }
        
        public boolean supportsMethodsInInterfaces() {
            return this.supportsMethodsInInterfaces;
        }
        
        public boolean isAtLeast(final CompatibilityLevel level) {
            return level == null || this.ver >= level.ver;
        }
        
        public boolean canElevateTo(final CompatibilityLevel level) {
            return level == null || this.maxCompatibleLevel == null || level.ver <= this.maxCompatibleLevel.ver;
        }
        
        public boolean canSupport(final CompatibilityLevel level) {
            return level == null || level.canElevateTo(this);
        }
    }
    
    static class TokenProviderWrapper implements Comparable<TokenProviderWrapper>
    {
        private static int nextOrder;
        private final int priority;
        private final int order;
        private final IEnvironmentTokenProvider provider;
        private final MixinEnvironment environment;
        
        public TokenProviderWrapper(final IEnvironmentTokenProvider provider, final MixinEnvironment environment) {
            this.provider = provider;
            this.environment = environment;
            this.order = TokenProviderWrapper.nextOrder++;
            this.priority = provider.getPriority();
        }
        
        @Override
        public int compareTo(final TokenProviderWrapper other) {
            if (other == null) {
                return 0;
            }
            if (other.priority == this.priority) {
                return other.order - this.order;
            }
            return other.priority - this.priority;
        }
        
        public IEnvironmentTokenProvider getProvider() {
            return this.provider;
        }
        
        Integer getToken(final String token) {
            return this.provider.getToken(token, this.environment);
        }
        
        static {
            TokenProviderWrapper.nextOrder = 0;
        }
    }
    
    static class MixinLogger
    {
        static MixinAppender appender;
        
        public MixinLogger() {
            final org.apache.logging.log4j.core.Logger log = (org.apache.logging.log4j.core.Logger)LogManager.getLogger("FML");
            MixinLogger.appender.start();
            log.addAppender((Appender)MixinLogger.appender);
        }
        
        static {
            MixinLogger.appender = new MixinAppender("MixinLogger", null, null);
        }
        
        static class MixinAppender extends AbstractAppender
        {
            protected MixinAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout) {
                super(name, filter, (Layout)layout);
            }
            
            public void append(final LogEvent event) {
                if (event.getLevel() == Level.DEBUG && "Validating minecraft".equals(event.getMessage().getFormat())) {
                    MixinEnvironment.gotoPhase(Phase.INIT);
                }
            }
        }
    }
}

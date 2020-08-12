// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer;

import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.throwables.MixinPrepareError;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import java.util.Collections;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.lib.tree.ClassNode;
import java.util.SortedSet;
import org.spongepowered.asm.mixin.transformer.throwables.MixinTransformerError;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;
import org.spongepowered.asm.mixin.throwables.MixinApplyError;
import java.util.TreeSet;
import java.lang.reflect.Method;
import org.spongepowered.asm.util.PrettyPrinter;
import java.util.Map;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Set;
import org.spongepowered.asm.mixin.throwables.ClassAlreadyLoadedException;
import org.apache.logging.log4j.LogManager;
import java.util.Collection;
import java.util.HashSet;
import java.lang.reflect.Constructor;
import org.spongepowered.asm.mixin.transformer.ext.extensions.ExtensionCheckInterfaces;
import org.spongepowered.asm.mixin.transformer.ext.extensions.ExtensionCheckClass;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.extensions.ExtensionClassExporter;
import org.spongepowered.asm.mixin.transformer.ext.IClassGenerator;
import org.spongepowered.asm.mixin.injection.invoke.arg.ArgsClassGenerator;
import org.spongepowered.asm.mixin.throwables.MixinException;
import org.spongepowered.asm.service.ITransformer;
import java.util.UUID;
import java.util.ArrayList;
import org.spongepowered.asm.service.MixinService;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.util.perf.Profiler;
import org.spongepowered.asm.mixin.transformer.ext.IHotSwap;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.util.ReEntranceLock;
import java.util.List;
import org.spongepowered.asm.service.IMixinService;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.transformers.TreeTransformer;

public class MixinTransformer extends TreeTransformer
{
    private static final String MIXIN_AGENT_CLASS = "org.spongepowered.tools.agent.MixinAgent";
    private static final String METRONOME_AGENT_CLASS = "org.spongepowered.metronome.Agent";
    static final Logger logger;
    private final IMixinService service;
    private final List<MixinConfig> configs;
    private final List<MixinConfig> pendingConfigs;
    private final ReEntranceLock lock;
    private final String sessionId;
    private final Extensions extensions;
    private final IHotSwap hotSwapper;
    private final MixinPostProcessor postProcessor;
    private final Profiler profiler;
    private MixinEnvironment currentEnvironment;
    private Level verboseLoggingLevel;
    private boolean errorState;
    private int transformedCount;
    
    MixinTransformer() {
        this.service = MixinService.getService();
        this.configs = new ArrayList<MixinConfig>();
        this.pendingConfigs = new ArrayList<MixinConfig>();
        this.sessionId = UUID.randomUUID().toString();
        this.verboseLoggingLevel = Level.DEBUG;
        this.errorState = false;
        this.transformedCount = 0;
        final MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();
        final Object globalMixinTransformer = environment.getActiveTransformer();
        if (globalMixinTransformer instanceof ITransformer) {
            throw new MixinException("Terminating MixinTransformer instance " + this);
        }
        environment.setActiveTransformer(this);
        this.lock = this.service.getReEntranceLock();
        this.extensions = new Extensions(this);
        this.hotSwapper = this.initHotSwapper(environment);
        this.postProcessor = new MixinPostProcessor();
        this.extensions.add(new ArgsClassGenerator());
        this.extensions.add(new InnerClassGenerator());
        this.extensions.add(new ExtensionClassExporter(environment));
        this.extensions.add(new ExtensionCheckClass());
        this.extensions.add(new ExtensionCheckInterfaces());
        this.profiler = MixinEnvironment.getProfiler();
    }
    
    private IHotSwap initHotSwapper(final MixinEnvironment environment) {
        if (!environment.getOption(MixinEnvironment.Option.HOT_SWAP)) {
            return null;
        }
        try {
            MixinTransformer.logger.info("Attempting to load Hot-Swap agent");
            final Class<? extends IHotSwap> clazz = (Class<? extends IHotSwap>)Class.forName("org.spongepowered.tools.agent.MixinAgent");
            final Constructor<? extends IHotSwap> ctor = clazz.getDeclaredConstructor(MixinTransformer.class);
            return (IHotSwap)ctor.newInstance(this);
        }
        catch (Throwable th) {
            MixinTransformer.logger.info("Hot-swap agent could not be loaded, hot swapping of mixins won't work. {}: {}", new Object[] { th.getClass().getSimpleName(), th.getMessage() });
            return null;
        }
    }
    
    public void audit(final MixinEnvironment environment) {
        final Set<String> unhandled = new HashSet<String>();
        for (final MixinConfig config : this.configs) {
            unhandled.addAll(config.getUnhandledTargets());
        }
        final Logger auditLogger = LogManager.getLogger("mixin/audit");
        for (final String target : unhandled) {
            try {
                auditLogger.info("Force-loading class {}", new Object[] { target });
                this.service.getClassProvider().findClass(target, true);
            }
            catch (ClassNotFoundException ex) {
                auditLogger.error("Could not force-load " + target, (Throwable)ex);
            }
        }
        for (final MixinConfig config2 : this.configs) {
            for (final String target2 : config2.getUnhandledTargets()) {
                final ClassAlreadyLoadedException ex2 = new ClassAlreadyLoadedException(target2 + " was already classloaded");
                auditLogger.error("Could not force-load " + target2, (Throwable)ex2);
            }
        }
        if (environment.getOption(MixinEnvironment.Option.DEBUG_PROFILER)) {
            this.printProfilerSummary();
        }
    }
    
    private void printProfilerSummary() {
        final DecimalFormat threedp = new DecimalFormat("(###0.000");
        final DecimalFormat onedp = new DecimalFormat("(###0.0");
        final PrettyPrinter printer = this.profiler.printer(false, false);
        final long prepareTime = this.profiler.get("mixin.prepare").getTotalTime();
        final long readTime = this.profiler.get("mixin.read").getTotalTime();
        final long applyTime = this.profiler.get("mixin.apply").getTotalTime();
        final long writeTime = this.profiler.get("mixin.write").getTotalTime();
        final long totalMixinTime = this.profiler.get("mixin").getTotalTime();
        final long loadTime = this.profiler.get("class.load").getTotalTime();
        final long transformTime = this.profiler.get("class.transform").getTotalTime();
        final long exportTime = this.profiler.get("mixin.debug.export").getTotalTime();
        final long actualTime = totalMixinTime - loadTime - transformTime - exportTime;
        final double timeSliceMixin = actualTime / (double)totalMixinTime * 100.0;
        final double timeSliceLoad = loadTime / (double)totalMixinTime * 100.0;
        final double timeSliceTransform = transformTime / (double)totalMixinTime * 100.0;
        final double timeSliceExport = exportTime / (double)totalMixinTime * 100.0;
        long worstTransformerTime = 0L;
        Profiler.Section worstTransformer = null;
        for (final Profiler.Section section : this.profiler.getSections()) {
            final long transformerTime = section.getName().startsWith("class.transform.") ? section.getTotalTime() : 0L;
            if (transformerTime > worstTransformerTime) {
                worstTransformerTime = transformerTime;
                worstTransformer = section;
            }
        }
        printer.hr().add("Summary").hr().add();
        final String format = "%9d ms %12s seconds)";
        printer.kv("Total mixin time", format, totalMixinTime, threedp.format(totalMixinTime * 0.001)).add();
        printer.kv("Preparing mixins", format, prepareTime, threedp.format(prepareTime * 0.001));
        printer.kv("Reading input", format, readTime, threedp.format(readTime * 0.001));
        printer.kv("Applying mixins", format, applyTime, threedp.format(applyTime * 0.001));
        printer.kv("Writing output", format, writeTime, threedp.format(writeTime * 0.001)).add();
        printer.kv("of which", (Object)"");
        printer.kv("Time spent loading from disk", format, loadTime, threedp.format(loadTime * 0.001));
        printer.kv("Time spent transforming classes", format, transformTime, threedp.format(transformTime * 0.001)).add();
        if (worstTransformer != null) {
            printer.kv("Worst transformer", (Object)worstTransformer.getName());
            printer.kv("Class", (Object)worstTransformer.getInfo());
            printer.kv("Time spent", "%s seconds", worstTransformer.getTotalSeconds());
            printer.kv("called", "%d times", worstTransformer.getTotalCount()).add();
        }
        printer.kv("   Time allocation:     Processing mixins", "%9d ms %10s%% of total)", actualTime, onedp.format(timeSliceMixin));
        printer.kv("Loading classes", "%9d ms %10s%% of total)", loadTime, onedp.format(timeSliceLoad));
        printer.kv("Running transformers", "%9d ms %10s%% of total)", transformTime, onedp.format(timeSliceTransform));
        if (exportTime > 0L) {
            printer.kv("Exporting classes (debug)", "%9d ms %10s%% of total)", exportTime, onedp.format(timeSliceExport));
        }
        printer.add();
        try {
            final Class<?> agent = this.service.getClassProvider().findAgentClass("org.spongepowered.metronome.Agent", false);
            final Method mdGetTimes = agent.getDeclaredMethod("getTimes", (Class<?>[])new Class[0]);
            final Map<String, Long> times = (Map<String, Long>)mdGetTimes.invoke(null, new Object[0]);
            printer.hr().add("Transformer Times").hr().add();
            int longest = 10;
            for (final Map.Entry<String, Long> entry : times.entrySet()) {
                longest = Math.max(longest, entry.getKey().length());
            }
            for (final Map.Entry<String, Long> entry : times.entrySet()) {
                final String name = entry.getKey();
                long mixinTime = 0L;
                for (final Profiler.Section section2 : this.profiler.getSections()) {
                    if (name.equals(section2.getInfo())) {
                        mixinTime = section2.getTotalTime();
                        break;
                    }
                }
                if (mixinTime > 0L) {
                    printer.add("%-" + longest + "s %8s ms %8s ms in mixin)", name, entry.getValue() + mixinTime, "(" + mixinTime);
                }
                else {
                    printer.add("%-" + longest + "s %8s ms", name, entry.getValue());
                }
            }
            printer.add();
        }
        catch (Throwable t) {}
        printer.print();
    }
    
    @Override
    public String getName() {
        return this.getClass().getName();
    }
    
    @Override
    public boolean isDelegationExcluded() {
        return true;
    }
    
    @Override
    public synchronized byte[] transformClassBytes(final String name, final String transformedName, byte[] basicClass) {
        if (transformedName == null || this.errorState) {
            return basicClass;
        }
        final MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();
        if (basicClass == null) {
            for (final IClassGenerator generator : this.extensions.getGenerators()) {
                final Profiler.Section genTimer = this.profiler.begin("generator", generator.getClass().getSimpleName().toLowerCase());
                basicClass = generator.generate(transformedName);
                genTimer.end();
                if (basicClass != null) {
                    this.extensions.export(environment, transformedName.replace('.', '/'), false, basicClass);
                    return basicClass;
                }
            }
            return basicClass;
        }
        final boolean locked = this.lock.push().check();
        final Profiler.Section mixinTimer = this.profiler.begin("mixin");
        if (!locked) {
            try {
                this.checkSelect(environment);
            }
            catch (Exception ex) {
                this.lock.pop();
                mixinTimer.end();
                throw new MixinException(ex);
            }
        }
        try {
            if (this.postProcessor.canTransform(transformedName)) {
                final Profiler.Section postTimer = this.profiler.begin("postprocessor");
                final byte[] bytes = this.postProcessor.transformClassBytes(name, transformedName, basicClass);
                postTimer.end();
                this.extensions.export(environment, transformedName, false, bytes);
                return bytes;
            }
            SortedSet<MixinInfo> mixins = null;
            boolean invalidRef = false;
            for (final MixinConfig config : this.configs) {
                if (config.packageMatch(transformedName)) {
                    invalidRef = true;
                }
                else {
                    if (!config.hasMixinsFor(transformedName)) {
                        continue;
                    }
                    if (mixins == null) {
                        mixins = new TreeSet<MixinInfo>();
                    }
                    mixins.addAll((Collection<?>)config.getMixinsFor(transformedName));
                }
            }
            if (invalidRef) {
                throw new NoClassDefFoundError(String.format("%s is a mixin class and cannot be referenced directly", transformedName));
            }
            if (mixins != null) {
                if (locked) {
                    MixinTransformer.logger.warn("Re-entrance detected, this will cause serious problems.", (Throwable)new MixinException());
                    throw new MixinApplyError("Re-entrance error.");
                }
                if (this.hotSwapper != null) {
                    this.hotSwapper.registerTargetClass(transformedName, basicClass);
                }
                try {
                    final Profiler.Section timer = this.profiler.begin("read");
                    final ClassNode targetClassNode = this.readClass(basicClass, true);
                    final TargetClassContext context = new TargetClassContext(environment, this.extensions, this.sessionId, transformedName, targetClassNode, mixins);
                    timer.end();
                    basicClass = this.applyMixins(environment, context);
                    ++this.transformedCount;
                }
                catch (InvalidMixinException th) {
                    this.dumpClassOnFailure(transformedName, basicClass, environment);
                    this.handleMixinApplyError(transformedName, th, environment);
                }
            }
            return basicClass;
        }
        catch (Throwable th2) {
            th2.printStackTrace();
            this.dumpClassOnFailure(transformedName, basicClass, environment);
            throw new MixinTransformerError("An unexpected critical error was encountered", th2);
        }
        finally {
            this.lock.pop();
            mixinTimer.end();
        }
    }
    
    public List<String> reload(final String mixinClass, final byte[] bytes) {
        if (this.lock.getDepth() > 0) {
            throw new MixinApplyError("Cannot reload mixin if re-entrant lock entered");
        }
        final List<String> targets = new ArrayList<String>();
        for (final MixinConfig config : this.configs) {
            targets.addAll(config.reloadMixin(mixinClass, bytes));
        }
        return targets;
    }
    
    private void checkSelect(final MixinEnvironment environment) {
        if (this.currentEnvironment != environment) {
            this.select(environment);
            return;
        }
        final int unvisitedCount = Mixins.getUnvisitedCount();
        if (unvisitedCount > 0 && this.transformedCount == 0) {
            this.select(environment);
        }
    }
    
    private void select(final MixinEnvironment environment) {
        this.verboseLoggingLevel = (environment.getOption(MixinEnvironment.Option.DEBUG_VERBOSE) ? Level.INFO : Level.DEBUG);
        if (this.transformedCount > 0) {
            MixinTransformer.logger.log(this.verboseLoggingLevel, "Ending {}, applied {} mixins", new Object[] { this.currentEnvironment, this.transformedCount });
        }
        final String action = (this.currentEnvironment == environment) ? "Checking for additional" : "Preparing";
        MixinTransformer.logger.log(this.verboseLoggingLevel, "{} mixins for {}", new Object[] { action, environment });
        this.profiler.setActive(true);
        this.profiler.mark(environment.getPhase().toString() + ":prepare");
        final Profiler.Section prepareTimer = this.profiler.begin("prepare");
        this.selectConfigs(environment);
        this.extensions.select(environment);
        final int totalMixins = this.prepareConfigs(environment);
        this.currentEnvironment = environment;
        this.transformedCount = 0;
        prepareTimer.end();
        final long elapsedMs = prepareTimer.getTime();
        final double elapsedTime = prepareTimer.getSeconds();
        if (elapsedTime > 0.25) {
            final long loadTime = this.profiler.get("class.load").getTime();
            final long transformTime = this.profiler.get("class.transform").getTime();
            final long pluginTime = this.profiler.get("mixin.plugin").getTime();
            final String elapsed = new DecimalFormat("###0.000").format(elapsedTime);
            final String perMixinTime = new DecimalFormat("###0.0").format(elapsedMs / (double)totalMixins);
            MixinTransformer.logger.log(this.verboseLoggingLevel, "Prepared {} mixins in {} sec ({}ms avg) ({}ms load, {}ms transform, {}ms plugin)", new Object[] { totalMixins, elapsed, perMixinTime, loadTime, transformTime, pluginTime });
        }
        this.profiler.mark(environment.getPhase().toString() + ":apply");
        this.profiler.setActive(environment.getOption(MixinEnvironment.Option.DEBUG_PROFILER));
    }
    
    private void selectConfigs(final MixinEnvironment environment) {
        final Iterator<Config> iter = Mixins.getConfigs().iterator();
        while (iter.hasNext()) {
            final Config handle = iter.next();
            try {
                final MixinConfig config = handle.get();
                if (!config.select(environment)) {
                    continue;
                }
                iter.remove();
                MixinTransformer.logger.log(this.verboseLoggingLevel, "Selecting config {}", new Object[] { config });
                config.onSelect();
                this.pendingConfigs.add(config);
            }
            catch (Exception ex) {
                MixinTransformer.logger.warn(String.format("Failed to select mixin config: %s", handle), (Throwable)ex);
            }
        }
        Collections.sort(this.pendingConfigs);
    }
    
    private int prepareConfigs(final MixinEnvironment environment) {
        int totalMixins = 0;
        final IHotSwap hotSwapper = this.hotSwapper;
        for (final MixinConfig config : this.pendingConfigs) {
            config.addListener(this.postProcessor);
            if (hotSwapper != null) {
                config.addListener(new MixinConfig.IListener() {
                    @Override
                    public void onPrepare(final MixinInfo mixin) {
                        hotSwapper.registerMixinClass(mixin.getClassName());
                    }
                    
                    @Override
                    public void onInit(final MixinInfo mixin) {
                    }
                });
            }
        }
        for (final MixinConfig config : this.pendingConfigs) {
            try {
                MixinTransformer.logger.log(this.verboseLoggingLevel, "Preparing {} ({})", new Object[] { config, config.getDeclaredMixinCount() });
                config.prepare();
                totalMixins += config.getMixinCount();
            }
            catch (InvalidMixinException ex) {
                this.handleMixinPrepareError(config, ex, environment);
            }
            catch (Exception ex2) {
                final String message = ex2.getMessage();
                MixinTransformer.logger.error("Error encountered whilst initialising mixin config '" + config.getName() + "': " + message, (Throwable)ex2);
            }
        }
        for (final MixinConfig config : this.pendingConfigs) {
            final IMixinConfigPlugin plugin = config.getPlugin();
            if (plugin == null) {
                continue;
            }
            final Set<String> otherTargets = new HashSet<String>();
            for (final MixinConfig otherConfig : this.pendingConfigs) {
                if (!otherConfig.equals(config)) {
                    otherTargets.addAll(otherConfig.getTargets());
                }
            }
            plugin.acceptTargets(config.getTargets(), Collections.unmodifiableSet((Set<? extends String>)otherTargets));
        }
        for (final MixinConfig config : this.pendingConfigs) {
            try {
                config.postInitialise();
            }
            catch (InvalidMixinException ex) {
                this.handleMixinPrepareError(config, ex, environment);
            }
            catch (Exception ex2) {
                final String message = ex2.getMessage();
                MixinTransformer.logger.error("Error encountered during mixin config postInit step'" + config.getName() + "': " + message, (Throwable)ex2);
            }
        }
        this.configs.addAll(this.pendingConfigs);
        Collections.sort(this.configs);
        this.pendingConfigs.clear();
        return totalMixins;
    }
    
    private byte[] applyMixins(final MixinEnvironment environment, final TargetClassContext context) {
        Profiler.Section timer = this.profiler.begin("preapply");
        this.extensions.preApply(context);
        timer = timer.next("apply");
        this.apply(context);
        timer = timer.next("postapply");
        try {
            this.extensions.postApply(context);
        }
        catch (ExtensionCheckClass.ValidationFailedException ex) {
            MixinTransformer.logger.info(ex.getMessage());
            if (context.isExportForced() || environment.getOption(MixinEnvironment.Option.DEBUG_EXPORT)) {
                this.writeClass(context);
            }
        }
        timer.end();
        return this.writeClass(context);
    }
    
    private void apply(final TargetClassContext context) {
        context.applyMixins();
    }
    
    private void handleMixinPrepareError(final MixinConfig config, final InvalidMixinException ex, final MixinEnvironment environment) throws MixinPrepareError {
        this.handleMixinError(config.getName(), ex, environment, ErrorPhase.PREPARE);
    }
    
    private void handleMixinApplyError(final String targetClass, final InvalidMixinException ex, final MixinEnvironment environment) throws MixinApplyError {
        this.handleMixinError(targetClass, ex, environment, ErrorPhase.APPLY);
    }
    
    private void handleMixinError(final String context, final InvalidMixinException ex, final MixinEnvironment environment, final ErrorPhase errorPhase) throws Error {
        this.errorState = true;
        final IMixinInfo mixin = ex.getMixin();
        if (mixin == null) {
            MixinTransformer.logger.error("InvalidMixinException has no mixin!", (Throwable)ex);
            throw ex;
        }
        final IMixinConfig config = mixin.getConfig();
        final MixinEnvironment.Phase phase = mixin.getPhase();
        IMixinErrorHandler.ErrorAction action = config.isRequired() ? IMixinErrorHandler.ErrorAction.ERROR : IMixinErrorHandler.ErrorAction.WARN;
        if (environment.getOption(MixinEnvironment.Option.DEBUG_VERBOSE)) {
            new PrettyPrinter().add("Invalid Mixin").centre().hr('-').kvWidth(10).kv("Action", (Object)errorPhase.name()).kv("Mixin", (Object)mixin.getClassName()).kv("Config", (Object)config.getName()).kv("Phase", phase).hr('-').add("    %s", ex.getClass().getName()).hr('-').addWrapped("    %s", ex.getMessage()).hr('-').add(ex, 8).trace(action.logLevel);
        }
        for (final IMixinErrorHandler handler : this.getErrorHandlers(mixin.getPhase())) {
            final IMixinErrorHandler.ErrorAction newAction = errorPhase.onError(handler, context, ex, mixin, action);
            if (newAction != null) {
                action = newAction;
            }
        }
        MixinTransformer.logger.log(action.logLevel, errorPhase.getLogMessage(context, ex, mixin), (Throwable)ex);
        this.errorState = false;
        if (action == IMixinErrorHandler.ErrorAction.ERROR) {
            throw new MixinApplyError(errorPhase.getErrorMessage(mixin, config, phase), ex);
        }
    }
    
    private List<IMixinErrorHandler> getErrorHandlers(final MixinEnvironment.Phase phase) {
        final List<IMixinErrorHandler> handlers = new ArrayList<IMixinErrorHandler>();
        for (final String handlerClassName : Mixins.getErrorHandlerClasses()) {
            try {
                MixinTransformer.logger.info("Instancing error handler class {}", new Object[] { handlerClassName });
                final Class<?> handlerClass = this.service.getClassProvider().findClass(handlerClassName, true);
                final IMixinErrorHandler handler = (IMixinErrorHandler)handlerClass.newInstance();
                if (handler == null) {
                    continue;
                }
                handlers.add(handler);
            }
            catch (Throwable t) {}
        }
        return handlers;
    }
    
    private byte[] writeClass(final TargetClassContext context) {
        return this.writeClass(context.getClassName(), context.getClassNode(), context.isExportForced());
    }
    
    private byte[] writeClass(final String transformedName, final ClassNode targetClass, final boolean forceExport) {
        final Profiler.Section writeTimer = this.profiler.begin("write");
        final byte[] bytes = this.writeClass(targetClass);
        writeTimer.end();
        this.extensions.export(this.currentEnvironment, transformedName, forceExport, bytes);
        return bytes;
    }
    
    private void dumpClassOnFailure(final String className, final byte[] bytes, final MixinEnvironment env) {
        if (env.getOption(MixinEnvironment.Option.DUMP_TARGET_ON_FAILURE)) {
            final ExtensionClassExporter exporter = this.extensions.getExtension(ExtensionClassExporter.class);
            exporter.dumpClass(className.replace('.', '/') + ".target", bytes);
        }
    }
    
    static {
        logger = LogManager.getLogger("mixin");
    }
    
    enum ErrorPhase
    {
        PREPARE {
            @Override
            IMixinErrorHandler.ErrorAction onError(final IMixinErrorHandler handler, final String context, final InvalidMixinException ex, final IMixinInfo mixin, final IMixinErrorHandler.ErrorAction action) {
                try {
                    return handler.onPrepareError(mixin.getConfig(), ex, mixin, action);
                }
                catch (AbstractMethodError ame) {
                    return action;
                }
            }
            
            @Override
            protected String getContext(final IMixinInfo mixin, final String context) {
                return String.format("preparing %s in %s", mixin.getName(), context);
            }
        }, 
        APPLY {
            @Override
            IMixinErrorHandler.ErrorAction onError(final IMixinErrorHandler handler, final String context, final InvalidMixinException ex, final IMixinInfo mixin, final IMixinErrorHandler.ErrorAction action) {
                try {
                    return handler.onApplyError(context, ex, mixin, action);
                }
                catch (AbstractMethodError ame) {
                    return action;
                }
            }
            
            @Override
            protected String getContext(final IMixinInfo mixin, final String context) {
                return String.format("%s -> %s", mixin, context);
            }
        };
        
        private final String text;
        
        private ErrorPhase() {
            this.text = this.name().toLowerCase();
        }
        
        abstract IMixinErrorHandler.ErrorAction onError(final IMixinErrorHandler p0, final String p1, final InvalidMixinException p2, final IMixinInfo p3, final IMixinErrorHandler.ErrorAction p4);
        
        protected abstract String getContext(final IMixinInfo p0, final String p1);
        
        public String getLogMessage(final String context, final InvalidMixinException ex, final IMixinInfo mixin) {
            return String.format("Mixin %s failed %s: %s %s", this.text, this.getContext(mixin, context), ex.getClass().getName(), ex.getMessage());
        }
        
        public String getErrorMessage(final IMixinInfo mixin, final IMixinConfig config, final MixinEnvironment.Phase phase) {
            return String.format("Mixin [%s] from phase [%s] in config [%s] FAILED during %s", mixin, phase, config, this.name());
        }
    }
}

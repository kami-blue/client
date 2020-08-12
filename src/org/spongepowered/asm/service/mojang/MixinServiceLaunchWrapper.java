// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.service.mojang;

import org.apache.logging.log4j.LogManager;
import java.lang.reflect.Method;
import net.minecraft.launchwrapper.ITweaker;
import org.spongepowered.asm.lib.ClassVisitor;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.service.ILegacyClassTransformer;
import org.spongepowered.asm.util.perf.Profiler;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import java.net.URLClassLoader;
import java.util.Iterator;
import net.minecraft.launchwrapper.IClassTransformer;
import java.util.ArrayList;
import org.spongepowered.asm.service.ITransformer;
import java.net.URL;
import java.io.InputStream;
import org.spongepowered.asm.mixin.throwables.MixinException;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.spongepowered.asm.launch.GlobalProperties;
import java.util.List;
import org.spongepowered.asm.mixin.MixinEnvironment;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.IClassNameTransformer;
import org.spongepowered.asm.util.ReEntranceLock;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;
import org.spongepowered.asm.service.IMixinService;

public class MixinServiceLaunchWrapper implements IMixinService, IClassProvider, IClassBytecodeProvider
{
    public static final String BLACKBOARD_KEY_TWEAKCLASSES = "TweakClasses";
    public static final String BLACKBOARD_KEY_TWEAKS = "Tweaks";
    private static final String LAUNCH_PACKAGE = "org.spongepowered.asm.launch.";
    private static final String MIXIN_PACKAGE = "org.spongepowered.asm.mixin.";
    private static final String STATE_TWEAKER = "org.spongepowered.asm.mixin.EnvironmentStateTweaker";
    private static final String TRANSFORMER_PROXY_CLASS = "org.spongepowered.asm.mixin.transformer.Proxy";
    private static final Logger logger;
    private final LaunchClassLoaderUtil classLoaderUtil;
    private final ReEntranceLock lock;
    private IClassNameTransformer nameTransformer;
    
    public MixinServiceLaunchWrapper() {
        this.classLoaderUtil = new LaunchClassLoaderUtil(Launch.classLoader);
        this.lock = new ReEntranceLock(1);
    }
    
    @Override
    public String getName() {
        return "LaunchWrapper";
    }
    
    @Override
    public boolean isValid() {
        try {
            Launch.classLoader.hashCode();
        }
        catch (Throwable ex) {
            return false;
        }
        return true;
    }
    
    @Override
    public void prepare() {
        Launch.classLoader.addClassLoaderExclusion("org.spongepowered.asm.launch.");
    }
    
    @Override
    public MixinEnvironment.Phase getInitialPhase() {
        if (findInStackTrace("net.minecraft.launchwrapper.Launch", "launch") > 132) {
            return MixinEnvironment.Phase.DEFAULT;
        }
        return MixinEnvironment.Phase.PREINIT;
    }
    
    @Override
    public void init() {
        if (findInStackTrace("net.minecraft.launchwrapper.Launch", "launch") < 4) {
            MixinServiceLaunchWrapper.logger.error("MixinBootstrap.doInit() called during a tweak constructor!");
        }
        final List<String> tweakClasses = GlobalProperties.get("TweakClasses");
        if (tweakClasses != null) {
            tweakClasses.add("org.spongepowered.asm.mixin.EnvironmentStateTweaker");
        }
    }
    
    @Override
    public ReEntranceLock getReEntranceLock() {
        return this.lock;
    }
    
    @Override
    public Collection<String> getPlatformAgents() {
        return (Collection<String>)ImmutableList.of((Object)"org.spongepowered.asm.launch.platform.MixinPlatformAgentFML");
    }
    
    @Override
    public IClassProvider getClassProvider() {
        return this;
    }
    
    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return this;
    }
    
    @Override
    public Class<?> findClass(final String name) throws ClassNotFoundException {
        return (Class<?>)Launch.classLoader.findClass(name);
    }
    
    @Override
    public Class<?> findClass(final String name, final boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, (ClassLoader)Launch.classLoader);
    }
    
    @Override
    public Class<?> findAgentClass(final String name, final boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, Launch.class.getClassLoader());
    }
    
    @Override
    public void beginPhase() {
        Launch.classLoader.registerTransformer("org.spongepowered.asm.mixin.transformer.Proxy");
    }
    
    @Override
    public void checkEnv(final Object bootSource) {
        if (bootSource.getClass().getClassLoader() != Launch.class.getClassLoader()) {
            throw new MixinException("Attempted to init the mixin environment in the wrong classloader");
        }
    }
    
    @Override
    public InputStream getResourceAsStream(final String name) {
        return Launch.classLoader.getResourceAsStream(name);
    }
    
    @Override
    public void registerInvalidClass(final String className) {
        this.classLoaderUtil.registerInvalidClass(className);
    }
    
    @Override
    public boolean isClassLoaded(final String className) {
        return this.classLoaderUtil.isClassLoaded(className);
    }
    
    @Override
    public URL[] getClassPath() {
        return Launch.classLoader.getSources().toArray(new URL[0]);
    }
    
    @Override
    public Collection<ITransformer> getTransformers() {
        final List<IClassTransformer> transformers = (List<IClassTransformer>)Launch.classLoader.getTransformers();
        final List<ITransformer> wrapped = new ArrayList<ITransformer>(transformers.size());
        for (final IClassTransformer transformer : transformers) {
            if (transformer instanceof ITransformer) {
                wrapped.add((ITransformer)transformer);
            }
            else {
                wrapped.add(new LegacyTransformerHandle(transformer));
            }
            if (transformer instanceof IClassNameTransformer) {
                MixinServiceLaunchWrapper.logger.debug("Found name transformer: {}", new Object[] { transformer.getClass().getName() });
                this.nameTransformer = (IClassNameTransformer)transformer;
            }
        }
        return wrapped;
    }
    
    @Override
    public byte[] getClassBytes(final String name, final String transformedName) throws IOException {
        final byte[] classBytes = Launch.classLoader.getClassBytes(name);
        if (classBytes != null) {
            return classBytes;
        }
        final URLClassLoader appClassLoader = (URLClassLoader)Launch.class.getClassLoader();
        InputStream classStream = null;
        try {
            final String resourcePath = transformedName.replace('.', '/').concat(".class");
            classStream = appClassLoader.getResourceAsStream(resourcePath);
            return IOUtils.toByteArray(classStream);
        }
        catch (Exception ex) {
            return null;
        }
        finally {
            IOUtils.closeQuietly(classStream);
        }
    }
    
    @Override
    public byte[] getClassBytes(final String className, final boolean runTransformers) throws ClassNotFoundException, IOException {
        final String transformedName = className.replace('/', '.');
        final String name = this.unmapClassName(transformedName);
        final Profiler profiler = MixinEnvironment.getProfiler();
        final Profiler.Section loadTime = profiler.begin(1, "class.load");
        byte[] classBytes = this.getClassBytes(name, transformedName);
        loadTime.end();
        if (runTransformers) {
            final Profiler.Section transformTime = profiler.begin(1, "class.transform");
            classBytes = this.applyTransformers(name, transformedName, classBytes, profiler);
            transformTime.end();
        }
        if (classBytes == null) {
            throw new ClassNotFoundException(String.format("The specified class '%s' was not found", transformedName));
        }
        return classBytes;
    }
    
    private byte[] applyTransformers(final String name, final String transformedName, byte[] basicClass, final Profiler profiler) {
        if (this.classLoaderUtil.isClassExcluded(name, transformedName)) {
            return basicClass;
        }
        final MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();
        for (final ILegacyClassTransformer transformer : environment.getTransformers()) {
            this.lock.clear();
            final int pos = transformer.getName().lastIndexOf(46);
            final String simpleName = transformer.getName().substring(pos + 1);
            final Profiler.Section transformTime = profiler.begin(2, simpleName.toLowerCase());
            transformTime.setInfo(transformer.getName());
            basicClass = transformer.transformClassBytes(name, transformedName, basicClass);
            transformTime.end();
            if (this.lock.isSet()) {
                environment.addTransformerExclusion(transformer.getName());
                this.lock.clear();
                MixinServiceLaunchWrapper.logger.info("A re-entrant transformer '{}' was detected and will no longer process meta class data", new Object[] { transformer.getName() });
            }
        }
        return basicClass;
    }
    
    private String unmapClassName(final String className) {
        if (this.nameTransformer == null) {
            this.findNameTransformer();
        }
        if (this.nameTransformer != null) {
            return this.nameTransformer.unmapClassName(className);
        }
        return className;
    }
    
    private void findNameTransformer() {
        final List<IClassTransformer> transformers = (List<IClassTransformer>)Launch.classLoader.getTransformers();
        for (final IClassTransformer transformer : transformers) {
            if (transformer instanceof IClassNameTransformer) {
                MixinServiceLaunchWrapper.logger.debug("Found name transformer: {}", new Object[] { transformer.getClass().getName() });
                this.nameTransformer = (IClassNameTransformer)transformer;
            }
        }
    }
    
    @Override
    public ClassNode getClassNode(final String className) throws ClassNotFoundException, IOException {
        return this.getClassNode(this.getClassBytes(className, true), 0);
    }
    
    private ClassNode getClassNode(final byte[] classBytes, final int flags) {
        final ClassNode classNode = new ClassNode();
        final ClassReader classReader = new ClassReader(classBytes);
        classReader.accept(classNode, flags);
        return classNode;
    }
    
    @Override
    public final String getSideName() {
        for (final ITweaker tweaker : GlobalProperties.get("Tweaks")) {
            if (tweaker.getClass().getName().endsWith(".common.launcher.FMLServerTweaker")) {
                return "SERVER";
            }
            if (tweaker.getClass().getName().endsWith(".common.launcher.FMLTweaker")) {
                return "CLIENT";
            }
        }
        String name = this.getSideName("net.minecraftforge.fml.relauncher.FMLLaunchHandler", "side");
        if (name != null) {
            return name;
        }
        name = this.getSideName("cpw.mods.fml.relauncher.FMLLaunchHandler", "side");
        if (name != null) {
            return name;
        }
        name = this.getSideName("com.mumfrey.liteloader.launch.LiteLoaderTweaker", "getEnvironmentType");
        if (name != null) {
            return name;
        }
        return "UNKNOWN";
    }
    
    private String getSideName(final String className, final String methodName) {
        try {
            final Class<?> clazz = Class.forName(className, false, (ClassLoader)Launch.classLoader);
            final Method method = clazz.getDeclaredMethod(methodName, (Class<?>[])new Class[0]);
            return ((Enum)method.invoke(null, new Object[0])).name();
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    private static int findInStackTrace(final String className, final String methodName) {
        final Thread currentThread = Thread.currentThread();
        if (!"main".equals(currentThread.getName())) {
            return 0;
        }
        final StackTraceElement[] stackTrace2;
        final StackTraceElement[] stackTrace = stackTrace2 = currentThread.getStackTrace();
        for (final StackTraceElement s : stackTrace2) {
            if (className.equals(s.getClassName()) && methodName.equals(s.getMethodName())) {
                return s.getLineNumber();
            }
        }
        return 0;
    }
    
    static {
        logger = LogManager.getLogger("mixin");
    }
}

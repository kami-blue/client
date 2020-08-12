// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer.ext.extensions;

import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.util.perf.Profiler;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;
import java.util.regex.Pattern;
import java.lang.reflect.Constructor;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.spongepowered.asm.util.Constants;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IDecompiler;
import java.io.File;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;

public class ExtensionClassExporter implements IExtension
{
    private static final String DECOMPILER_CLASS = "org.spongepowered.asm.mixin.transformer.debug.RuntimeDecompiler";
    private static final String EXPORT_CLASS_DIR = "class";
    private static final String EXPORT_JAVA_DIR = "java";
    private static final Logger logger;
    private final File classExportDir;
    private final IDecompiler decompiler;
    
    public ExtensionClassExporter(final MixinEnvironment env) {
        this.classExportDir = new File(Constants.DEBUG_OUTPUT_DIR, "class");
        this.decompiler = this.initDecompiler(env, new File(Constants.DEBUG_OUTPUT_DIR, "java"));
        try {
            FileUtils.deleteDirectory(this.classExportDir);
        }
        catch (IOException ex) {
            ExtensionClassExporter.logger.warn("Error cleaning class output directory: {}", new Object[] { ex.getMessage() });
        }
    }
    
    private IDecompiler initDecompiler(final MixinEnvironment env, final File outputPath) {
        if (!env.getOption(MixinEnvironment.Option.DEBUG_EXPORT_DECOMPILE)) {
            return null;
        }
        try {
            final boolean as = env.getOption(MixinEnvironment.Option.DEBUG_EXPORT_DECOMPILE_THREADED);
            ExtensionClassExporter.logger.info("Attempting to load Fernflower decompiler{}", new Object[] { as ? " (Threaded mode)" : "" });
            final String className = "org.spongepowered.asm.mixin.transformer.debug.RuntimeDecompiler" + (as ? "Async" : "");
            final Class<? extends IDecompiler> clazz = (Class<? extends IDecompiler>)Class.forName(className);
            final Constructor<? extends IDecompiler> ctor = clazz.getDeclaredConstructor(File.class);
            final IDecompiler decompiler = (IDecompiler)ctor.newInstance(outputPath);
            ExtensionClassExporter.logger.info("Fernflower decompiler was successfully initialised, exported classes will be decompiled{}", new Object[] { as ? " in a separate thread" : "" });
            return decompiler;
        }
        catch (Throwable th) {
            ExtensionClassExporter.logger.info("Fernflower could not be loaded, exported classes will not be decompiled. {}: {}", new Object[] { th.getClass().getSimpleName(), th.getMessage() });
            return null;
        }
    }
    
    private String prepareFilter(String filter) {
        filter = "^\\Q" + filter.replace("**", "\u0081").replace("*", "\u0082").replace("?", "\u0083") + "\\E$";
        return filter.replace("\u0081", "\\E.*\\Q").replace("\u0082", "\\E[^\\.]+\\Q").replace("\u0083", "\\E.\\Q").replace("\\Q\\E", "");
    }
    
    private boolean applyFilter(final String filter, final String subject) {
        return Pattern.compile(this.prepareFilter(filter), 2).matcher(subject).matches();
    }
    
    @Override
    public boolean checkActive(final MixinEnvironment environment) {
        return true;
    }
    
    @Override
    public void preApply(final ITargetClassContext context) {
    }
    
    @Override
    public void postApply(final ITargetClassContext context) {
    }
    
    @Override
    public void export(final MixinEnvironment env, final String name, final boolean force, final byte[] bytes) {
        if (force || env.getOption(MixinEnvironment.Option.DEBUG_EXPORT)) {
            final String filter = env.getOptionValue(MixinEnvironment.Option.DEBUG_EXPORT_FILTER);
            if (force || filter == null || this.applyFilter(filter, name)) {
                final Profiler.Section exportTimer = MixinEnvironment.getProfiler().begin("debug.export");
                final File outputFile = this.dumpClass(name.replace('.', '/'), bytes);
                if (this.decompiler != null) {
                    this.decompiler.decompile(outputFile);
                }
                exportTimer.end();
            }
        }
    }
    
    public File dumpClass(final String fileName, final byte[] bytes) {
        final File outputFile = new File(this.classExportDir, fileName + ".class");
        try {
            FileUtils.writeByteArrayToFile(outputFile, bytes);
        }
        catch (IOException ex) {}
        return outputFile;
    }
    
    static {
        logger = LogManager.getLogger("mixin");
    }
}

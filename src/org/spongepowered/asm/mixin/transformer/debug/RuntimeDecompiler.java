// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer.debug;

import java.util.jar.Manifest;
import com.google.common.io.Files;
import com.google.common.base.Charsets;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.spongepowered.asm.mixin.transformer.ext.IDecompiler;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

public class RuntimeDecompiler extends IFernflowerLogger implements IDecompiler, IResultSaver
{
    private static final Level[] SEVERITY_LEVELS;
    private final Map<String, Object> options;
    private final File outputPath;
    protected final Logger logger;
    
    public RuntimeDecompiler(final File outputPath) {
        this.options = (Map<String, Object>)ImmutableMap.builder().put((Object)"din", (Object)"0").put((Object)"rbr", (Object)"0").put((Object)"dgs", (Object)"1").put((Object)"asc", (Object)"1").put((Object)"den", (Object)"1").put((Object)"hdc", (Object)"1").put((Object)"ind", (Object)"    ").build();
        this.logger = LogManager.getLogger("fernflower");
        this.outputPath = outputPath;
        if (this.outputPath.exists()) {
            try {
                FileUtils.deleteDirectory(this.outputPath);
            }
            catch (IOException ex) {
                this.logger.warn("Error cleaning output directory: {}", new Object[] { ex.getMessage() });
            }
        }
    }
    
    public void decompile(final File file) {
        try {
            final Fernflower fernflower = new Fernflower((IBytecodeProvider)new IBytecodeProvider() {
                private byte[] byteCode;
                
                public byte[] getBytecode(final String externalPath, final String internalPath) throws IOException {
                    if (this.byteCode == null) {
                        this.byteCode = InterpreterUtil.getBytes(new File(externalPath));
                    }
                    return this.byteCode;
                }
            }, (IResultSaver)this, (Map)this.options, (IFernflowerLogger)this);
            fernflower.getStructContext().addSpace(file, true);
            fernflower.decompileContext();
        }
        catch (Throwable ex) {
            this.logger.warn("Decompilation error while processing {}", new Object[] { file.getName() });
        }
    }
    
    public void saveFolder(final String path) {
    }
    
    public void saveClassFile(final String path, final String qualifiedName, final String entryName, final String content, final int[] mapping) {
        final File file = new File(this.outputPath, qualifiedName + ".java");
        file.getParentFile().mkdirs();
        try {
            this.logger.info("Writing {}", new Object[] { file.getAbsolutePath() });
            Files.write((CharSequence)content, file, Charsets.UTF_8);
        }
        catch (IOException ex) {
            this.writeMessage("Cannot write source file " + file, ex);
        }
    }
    
    public void startReadingClass(final String className) {
        this.logger.info("Decompiling {}", new Object[] { className });
    }
    
    public void writeMessage(final String message, final IFernflowerLogger.Severity severity) {
        this.logger.log(RuntimeDecompiler.SEVERITY_LEVELS[severity.ordinal()], message);
    }
    
    public void writeMessage(final String message, final Throwable t) {
        this.logger.warn("{} {}: {}", new Object[] { message, t.getClass().getSimpleName(), t.getMessage() });
    }
    
    public void writeMessage(final String message, final IFernflowerLogger.Severity severity, final Throwable t) {
        this.logger.log(RuntimeDecompiler.SEVERITY_LEVELS[severity.ordinal()], message, t);
    }
    
    public void copyFile(final String source, final String path, final String entryName) {
    }
    
    public void createArchive(final String path, final String archiveName, final Manifest manifest) {
    }
    
    public void saveDirEntry(final String path, final String archiveName, final String entryName) {
    }
    
    public void copyEntry(final String source, final String path, final String archiveName, final String entry) {
    }
    
    public void saveClassEntry(final String path, final String archiveName, final String qualifiedName, final String entryName, final String content) {
    }
    
    public void closeArchive(final String path, final String archiveName) {
    }
    
    static {
        SEVERITY_LEVELS = new Level[] { Level.TRACE, Level.INFO, Level.WARN, Level.ERROR };
    }
}

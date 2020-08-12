// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.struct;

import org.spongepowered.asm.lib.tree.LineNumberNode;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import java.util.Iterator;
import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.lib.tree.ClassNode;
import java.util.LinkedHashMap;
import java.util.Map;

public class SourceMap
{
    private static final String DEFAULT_STRATUM = "Mixin";
    private static final String NEWLINE = "\n";
    private final String sourceFile;
    private final Map<String, Stratum> strata;
    private int nextLineOffset;
    private String defaultStratum;
    
    public SourceMap(final String sourceFile) {
        this.strata = new LinkedHashMap<String, Stratum>();
        this.nextLineOffset = 1;
        this.defaultStratum = "Mixin";
        this.sourceFile = sourceFile;
    }
    
    public String getSourceFile() {
        return this.sourceFile;
    }
    
    public String getPseudoGeneratedSourceFile() {
        return this.sourceFile.replace(".java", "$mixin.java");
    }
    
    public File addFile(final ClassNode classNode) {
        return this.addFile(this.defaultStratum, classNode);
    }
    
    public File addFile(final String stratumName, final ClassNode classNode) {
        return this.addFile(stratumName, classNode.sourceFile, classNode.name + ".java", Bytecode.getMaxLineNumber(classNode, 500, 50));
    }
    
    public File addFile(final String sourceFileName, final String sourceFilePath, final int size) {
        return this.addFile(this.defaultStratum, sourceFileName, sourceFilePath, size);
    }
    
    public File addFile(final String stratumName, final String sourceFileName, final String sourceFilePath, final int size) {
        Stratum stratum = this.strata.get(stratumName);
        if (stratum == null) {
            stratum = new Stratum(stratumName);
            this.strata.put(stratumName, stratum);
        }
        final File file = stratum.addFile(this.nextLineOffset, size, sourceFileName, sourceFilePath);
        this.nextLineOffset += size;
        return file;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        this.appendTo(sb);
        return sb.toString();
    }
    
    private void appendTo(final StringBuilder sb) {
        sb.append("SMAP").append("\n");
        sb.append(this.getSourceFile()).append("\n");
        sb.append(this.defaultStratum).append("\n");
        for (final Stratum stratum : this.strata.values()) {
            stratum.appendTo(sb);
        }
        sb.append("*E").append("\n");
    }
    
    public static class File
    {
        public final int id;
        public final int lineOffset;
        public final int size;
        public final String sourceFileName;
        public final String sourceFilePath;
        
        public File(final int id, final int lineOffset, final int size, final String sourceFileName) {
            this(id, lineOffset, size, sourceFileName, null);
        }
        
        public File(final int id, final int lineOffset, final int size, final String sourceFileName, final String sourceFilePath) {
            this.id = id;
            this.lineOffset = lineOffset;
            this.size = size;
            this.sourceFileName = sourceFileName;
            this.sourceFilePath = sourceFilePath;
        }
        
        public void applyOffset(final ClassNode classNode) {
            for (final MethodNode method : classNode.methods) {
                this.applyOffset(method);
            }
        }
        
        public void applyOffset(final MethodNode method) {
            for (final AbstractInsnNode node : method.instructions) {
                if (node instanceof LineNumberNode) {
                    final LineNumberNode lineNumberNode = (LineNumberNode)node;
                    lineNumberNode.line += this.lineOffset - 1;
                }
            }
        }
        
        void appendFile(final StringBuilder sb) {
            if (this.sourceFilePath != null) {
                sb.append("+ ").append(this.id).append(" ").append(this.sourceFileName).append("\n");
                sb.append(this.sourceFilePath).append("\n");
            }
            else {
                sb.append(this.id).append(" ").append(this.sourceFileName).append("\n");
            }
        }
        
        public void appendLines(final StringBuilder sb) {
            sb.append("1#").append(this.id).append(",").append(this.size).append(":").append(this.lineOffset).append("\n");
        }
    }
    
    static class Stratum
    {
        private static final String STRATUM_MARK = "*S";
        private static final String FILE_MARK = "*F";
        private static final String LINES_MARK = "*L";
        public final String name;
        private final Map<String, File> files;
        
        public Stratum(final String name) {
            this.files = new LinkedHashMap<String, File>();
            this.name = name;
        }
        
        public File addFile(final int lineOffset, final int size, final String sourceFileName, final String sourceFilePath) {
            File file = this.files.get(sourceFilePath);
            if (file == null) {
                file = new File(this.files.size() + 1, lineOffset, size, sourceFileName, sourceFilePath);
                this.files.put(sourceFilePath, file);
            }
            return file;
        }
        
        void appendTo(final StringBuilder sb) {
            sb.append("*S").append(" ").append(this.name).append("\n");
            sb.append("*F").append("\n");
            for (final File file : this.files.values()) {
                file.appendFile(sb);
            }
            sb.append("*L").append("\n");
            for (final File file : this.files.values()) {
                file.appendLines(sb);
            }
        }
    }
}

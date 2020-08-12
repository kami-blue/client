// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

public class JarInputFile implements Vfs.File
{
    private final ZipEntry entry;
    private final JarInputDir jarInputDir;
    private final long fromIndex;
    private final long endIndex;
    
    public JarInputFile(final ZipEntry entry, final JarInputDir jarInputDir, final long cursor, final long nextCursor) {
        this.entry = entry;
        this.jarInputDir = jarInputDir;
        this.fromIndex = cursor;
        this.endIndex = nextCursor;
    }
    
    @Override
    public String getName() {
        final String name = this.entry.getName();
        return name.substring(name.lastIndexOf("/") + 1);
    }
    
    @Override
    public String getRelativePath() {
        return this.entry.getName();
    }
    
    @Override
    public InputStream openInputStream() throws IOException {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                if (JarInputFile.this.jarInputDir.cursor >= JarInputFile.this.fromIndex && JarInputFile.this.jarInputDir.cursor <= JarInputFile.this.endIndex) {
                    final int read = JarInputFile.this.jarInputDir.jarInputStream.read();
                    final JarInputDir access$000 = JarInputFile.this.jarInputDir;
                    ++access$000.cursor;
                    return read;
                }
                return -1;
            }
        };
    }
}

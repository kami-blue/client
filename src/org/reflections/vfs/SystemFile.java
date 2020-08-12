// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.vfs;

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;

public class SystemFile implements Vfs.File
{
    private final SystemDir root;
    private final java.io.File file;
    
    public SystemFile(final SystemDir root, final java.io.File file) {
        this.root = root;
        this.file = file;
    }
    
    @Override
    public String getName() {
        return this.file.getName();
    }
    
    @Override
    public String getRelativePath() {
        final String filepath = this.file.getPath().replace("\\", "/");
        if (filepath.startsWith(this.root.getPath())) {
            return filepath.substring(this.root.getPath().length() + 1);
        }
        return null;
    }
    
    @Override
    public InputStream openInputStream() {
        try {
            return new FileInputStream(this.file);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public String toString() {
        return this.file.toString();
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.vfs;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Collection;
import java.util.Stack;
import com.google.common.collect.AbstractIterator;
import java.util.Iterator;
import java.util.Collections;
import java.io.File;

public class SystemDir implements Vfs.Dir
{
    private final java.io.File file;
    
    public SystemDir(final java.io.File file) {
        if (file != null && (!file.isDirectory() || !file.canRead())) {
            throw new RuntimeException("cannot use dir " + file);
        }
        this.file = file;
    }
    
    @Override
    public String getPath() {
        if (this.file == null) {
            return "/NO-SUCH-DIRECTORY/";
        }
        return this.file.getPath().replace("\\", "/");
    }
    
    @Override
    public Iterable<Vfs.File> getFiles() {
        if (this.file == null || !this.file.exists()) {
            return (Iterable<Vfs.File>)Collections.emptyList();
        }
        return new Iterable<Vfs.File>() {
            @Override
            public Iterator<Vfs.File> iterator() {
                return (Iterator<Vfs.File>)new AbstractIterator<Vfs.File>() {
                    final Stack<java.io.File> stack;
                    
                    {
                        (this.stack = new Stack<java.io.File>()).addAll((Collection<?>)listFiles(SystemDir.this.file));
                    }
                    
                    protected Vfs.File computeNext() {
                        while (!this.stack.isEmpty()) {
                            final java.io.File file = this.stack.pop();
                            if (!file.isDirectory()) {
                                return new SystemFile(SystemDir.this, file);
                            }
                            this.stack.addAll((Collection<?>)listFiles(file));
                        }
                        return (Vfs.File)this.endOfData();
                    }
                };
            }
        };
    }
    
    private static List<java.io.File> listFiles(final java.io.File file) {
        final java.io.File[] files = file.listFiles();
        if (files != null) {
            return (List<java.io.File>)Lists.newArrayList((Object[])files);
        }
        return (List<java.io.File>)Lists.newArrayList();
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public String toString() {
        return this.getPath();
    }
}

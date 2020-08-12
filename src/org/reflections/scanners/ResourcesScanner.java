// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.scanners;

import org.reflections.vfs.Vfs;

public class ResourcesScanner extends AbstractScanner
{
    @Override
    public boolean acceptsInput(final String file) {
        return !file.endsWith(".class");
    }
    
    @Override
    public Object scan(final Vfs.File file, final Object classObject) {
        this.getStore().put((Object)file.getName(), (Object)file.getRelativePath());
        return classObject;
    }
    
    @Override
    public void scan(final Object cls) {
        throw new UnsupportedOperationException();
    }
}

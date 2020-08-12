// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.launch.platform;

import java.util.HashMap;
import java.util.jar.Manifest;
import java.io.IOException;
import java.util.jar.JarFile;
import java.io.File;
import java.util.jar.Attributes;
import java.net.URI;
import java.util.Map;

final class MainAttributes
{
    private static final Map<URI, MainAttributes> instances;
    protected final Attributes attributes;
    
    private MainAttributes() {
        this.attributes = new Attributes();
    }
    
    private MainAttributes(final File jar) {
        this.attributes = getAttributes(jar);
    }
    
    public final String get(final String name) {
        if (this.attributes != null) {
            return this.attributes.getValue(name);
        }
        return null;
    }
    
    private static Attributes getAttributes(final File jar) {
        if (jar == null) {
            return null;
        }
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(jar);
            final Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                return manifest.getMainAttributes();
            }
        }
        catch (IOException ex) {}
        finally {
            try {
                if (jarFile != null) {
                    jarFile.close();
                }
            }
            catch (IOException ex2) {}
        }
        return new Attributes();
    }
    
    public static MainAttributes of(final File jar) {
        return of(jar.toURI());
    }
    
    public static MainAttributes of(final URI uri) {
        MainAttributes attributes = MainAttributes.instances.get(uri);
        if (attributes == null) {
            attributes = new MainAttributes(new File(uri));
            MainAttributes.instances.put(uri, attributes);
        }
        return attributes;
    }
    
    static {
        instances = new HashMap<URI, MainAttributes>();
    }
}

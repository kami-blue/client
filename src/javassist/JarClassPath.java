// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.util.jar.JarFile;

final class JarClassPath implements ClassPath
{
    JarFile jarfile;
    String jarfileURL;
    
    JarClassPath(final String pathname) throws NotFoundException {
        try {
            this.jarfile = new JarFile(pathname);
            this.jarfileURL = new File(pathname).getCanonicalFile().toURI().toURL().toString();
        }
        catch (IOException ex) {
            throw new NotFoundException(pathname);
        }
    }
    
    @Override
    public InputStream openClassfile(final String classname) throws NotFoundException {
        try {
            final String jarname = classname.replace('.', '/') + ".class";
            final JarEntry je = this.jarfile.getJarEntry(jarname);
            if (je != null) {
                return this.jarfile.getInputStream(je);
            }
            return null;
        }
        catch (IOException ex) {
            throw new NotFoundException("broken jar file?: " + this.jarfile.getName());
        }
    }
    
    @Override
    public URL find(final String classname) {
        final String jarname = classname.replace('.', '/') + ".class";
        final JarEntry je = this.jarfile.getJarEntry(jarname);
        if (je != null) {
            try {
                return new URL("jar:" + this.jarfileURL + "!/" + jarname);
            }
            catch (MalformedURLException ex) {}
        }
        return null;
    }
    
    @Override
    public void close() {
        try {
            this.jarfile.close();
            this.jarfile = null;
        }
        catch (IOException ex) {}
    }
    
    @Override
    public String toString() {
        return (this.jarfile == null) ? "<null>" : this.jarfile.toString();
    }
}

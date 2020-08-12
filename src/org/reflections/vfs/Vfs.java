// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.vfs;

import java.io.IOException;
import java.io.InputStream;
import org.reflections.util.ClasspathHelper;
import java.net.URLConnection;
import java.net.JarURLConnection;
import java.util.jar.JarFile;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URISyntaxException;
import java.io.File;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import org.reflections.util.Utils;
import com.google.common.base.Predicate;
import java.util.Collection;
import com.google.common.collect.Lists;
import java.util.Iterator;
import org.reflections.ReflectionsException;
import org.reflections.Reflections;
import java.net.URL;
import java.util.List;

public abstract class Vfs
{
    private static List<UrlType> defaultUrlTypes;
    
    public static List<UrlType> getDefaultUrlTypes() {
        return Vfs.defaultUrlTypes;
    }
    
    public static void setDefaultURLTypes(final List<UrlType> urlTypes) {
        Vfs.defaultUrlTypes = urlTypes;
    }
    
    public static void addDefaultURLTypes(final UrlType urlType) {
        Vfs.defaultUrlTypes.add(0, urlType);
    }
    
    public static Dir fromURL(final URL url) {
        return fromURL(url, Vfs.defaultUrlTypes);
    }
    
    public static Dir fromURL(final URL url, final List<UrlType> urlTypes) {
        for (final UrlType type : urlTypes) {
            try {
                if (!type.matches(url)) {
                    continue;
                }
                final Dir dir = type.createDir(url);
                if (dir != null) {
                    return dir;
                }
                continue;
            }
            catch (Throwable e) {
                if (Reflections.log == null) {
                    continue;
                }
                Reflections.log.warn("could not create Dir using " + type + " from url " + url.toExternalForm() + ". skipping.", e);
            }
        }
        throw new ReflectionsException("could not create Vfs.Dir from url, no matching UrlType was found [" + url.toExternalForm() + "]\neither use fromURL(final URL url, final List<UrlType> urlTypes) or use the static setDefaultURLTypes(final List<UrlType> urlTypes) or addDefaultURLTypes(UrlType urlType) with your specialized UrlType.");
    }
    
    public static Dir fromURL(final URL url, final UrlType... urlTypes) {
        return fromURL(url, Lists.newArrayList((Object[])urlTypes));
    }
    
    public static Iterable<File> findFiles(final Collection<URL> inUrls, final String packagePrefix, final Predicate<String> nameFilter) {
        final Predicate<File> fileNamePredicate = (Predicate<File>)new Predicate<File>() {
            public boolean apply(final File file) {
                final String path = file.getRelativePath();
                if (path.startsWith(packagePrefix)) {
                    final String filename = path.substring(path.indexOf(packagePrefix) + packagePrefix.length());
                    return !Utils.isEmpty(filename) && nameFilter.apply((Object)filename.substring(1));
                }
                return false;
            }
        };
        return findFiles(inUrls, fileNamePredicate);
    }
    
    public static Iterable<File> findFiles(final Collection<URL> inUrls, final Predicate<File> filePredicate) {
        Iterable<File> result = new ArrayList<File>();
        for (final URL url : inUrls) {
            try {
                result = (Iterable<File>)Iterables.concat((Iterable)result, Iterables.filter((Iterable)new Iterable<File>() {
                    @Override
                    public Iterator<File> iterator() {
                        return Vfs.fromURL(url).getFiles().iterator();
                    }
                }, (Predicate)filePredicate));
            }
            catch (Throwable e) {
                if (Reflections.log == null) {
                    continue;
                }
                Reflections.log.error("could not findFiles for url. continuing. [" + url + "]", e);
            }
        }
        return result;
    }
    
    @Nullable
    public static java.io.File getFile(final URL url) {
        try {
            final String path = url.toURI().getSchemeSpecificPart();
            final java.io.File file;
            if ((file = new java.io.File(path)).exists()) {
                return file;
            }
        }
        catch (URISyntaxException ex) {}
        try {
            String path = URLDecoder.decode(url.getPath(), "UTF-8");
            if (path.contains(".jar!")) {
                path = path.substring(0, path.lastIndexOf(".jar!") + ".jar".length());
            }
            final java.io.File file;
            if ((file = new java.io.File(path)).exists()) {
                return file;
            }
        }
        catch (UnsupportedEncodingException ex2) {}
        try {
            String path = url.toExternalForm();
            if (path.startsWith("jar:")) {
                path = path.substring("jar:".length());
            }
            if (path.startsWith("wsjar:")) {
                path = path.substring("wsjar:".length());
            }
            if (path.startsWith("file:")) {
                path = path.substring("file:".length());
            }
            if (path.contains(".jar!")) {
                path = path.substring(0, path.indexOf(".jar!") + ".jar".length());
            }
            java.io.File file;
            if ((file = new java.io.File(path)).exists()) {
                return file;
            }
            path = path.replace("%20", " ");
            if ((file = new java.io.File(path)).exists()) {
                return file;
            }
        }
        catch (Exception ex3) {}
        return null;
    }
    
    private static boolean hasJarFileInPath(final URL url) {
        return url.toExternalForm().matches(".*\\.jar(\\!.*|$)");
    }
    
    static {
        Vfs.defaultUrlTypes = (List<UrlType>)Lists.newArrayList((Object[])DefaultUrlTypes.values());
    }
    
    public enum DefaultUrlTypes implements UrlType
    {
        jarFile {
            @Override
            public boolean matches(final URL url) {
                return url.getProtocol().equals("file") && hasJarFileInPath(url);
            }
            
            @Override
            public Dir createDir(final URL url) throws Exception {
                return new ZipDir(new JarFile(Vfs.getFile(url)));
            }
        }, 
        jarUrl {
            @Override
            public boolean matches(final URL url) {
                return "jar".equals(url.getProtocol()) || "zip".equals(url.getProtocol()) || "wsjar".equals(url.getProtocol());
            }
            
            @Override
            public Dir createDir(final URL url) throws Exception {
                try {
                    final URLConnection urlConnection = url.openConnection();
                    if (urlConnection instanceof JarURLConnection) {
                        return new ZipDir(((JarURLConnection)urlConnection).getJarFile());
                    }
                }
                catch (Throwable t) {}
                final java.io.File file = Vfs.getFile(url);
                if (file != null) {
                    return new ZipDir(new JarFile(file));
                }
                return null;
            }
        }, 
        directory {
            @Override
            public boolean matches(final URL url) {
                if (url.getProtocol().equals("file") && !hasJarFileInPath(url)) {
                    final java.io.File file = Vfs.getFile(url);
                    return file != null && file.isDirectory();
                }
                return false;
            }
            
            @Override
            public Dir createDir(final URL url) throws Exception {
                return new SystemDir(Vfs.getFile(url));
            }
        }, 
        jboss_vfs {
            @Override
            public boolean matches(final URL url) {
                return url.getProtocol().equals("vfs");
            }
            
            @Override
            public Dir createDir(final URL url) throws Exception {
                final Object content = url.openConnection().getContent();
                final Class<?> virtualFile = ClasspathHelper.contextClassLoader().loadClass("org.jboss.vfs.VirtualFile");
                final java.io.File physicalFile = (java.io.File)virtualFile.getMethod("getPhysicalFile", (Class<?>[])new Class[0]).invoke(content, new Object[0]);
                final String name = (String)virtualFile.getMethod("getName", (Class<?>[])new Class[0]).invoke(content, new Object[0]);
                java.io.File file = new java.io.File(physicalFile.getParentFile(), name);
                if (!file.exists() || !file.canRead()) {
                    file = physicalFile;
                }
                return file.isDirectory() ? new SystemDir(file) : new ZipDir(new JarFile(file));
            }
        }, 
        jboss_vfsfile {
            @Override
            public boolean matches(final URL url) throws Exception {
                return "vfszip".equals(url.getProtocol()) || "vfsfile".equals(url.getProtocol());
            }
            
            @Override
            public Dir createDir(final URL url) throws Exception {
                return new UrlTypeVFS().createDir(url);
            }
        }, 
        bundle {
            @Override
            public boolean matches(final URL url) throws Exception {
                return url.getProtocol().startsWith("bundle");
            }
            
            @Override
            public Dir createDir(final URL url) throws Exception {
                return Vfs.fromURL((URL)ClasspathHelper.contextClassLoader().loadClass("org.eclipse.core.runtime.FileLocator").getMethod("resolve", URL.class).invoke(null, url));
            }
        }, 
        jarInputStream {
            @Override
            public boolean matches(final URL url) throws Exception {
                return url.toExternalForm().contains(".jar");
            }
            
            @Override
            public Dir createDir(final URL url) throws Exception {
                return new JarInputDir(url);
            }
        };
    }
    
    public interface UrlType
    {
        boolean matches(final URL p0) throws Exception;
        
        Dir createDir(final URL p0) throws Exception;
    }
    
    public interface File
    {
        String getName();
        
        String getRelativePath();
        
        InputStream openInputStream() throws IOException;
    }
    
    public interface Dir
    {
        String getPath();
        
        Iterable<File> getFiles();
        
        void close();
    }
}

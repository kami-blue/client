// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.util;

import java.util.Map;
import java.util.LinkedHashMap;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.ServletContext;
import java.io.File;
import java.util.Arrays;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.net.URL;
import java.util.Collection;
import org.reflections.Reflections;

public abstract class ClasspathHelper
{
    public static ClassLoader contextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
    
    public static ClassLoader staticClassLoader() {
        return Reflections.class.getClassLoader();
    }
    
    public static ClassLoader[] classLoaders(final ClassLoader... classLoaders) {
        if (classLoaders != null && classLoaders.length != 0) {
            return classLoaders;
        }
        final ClassLoader contextClassLoader = contextClassLoader();
        final ClassLoader staticClassLoader = staticClassLoader();
        return (contextClassLoader != null) ? ((staticClassLoader != null && contextClassLoader != staticClassLoader) ? new ClassLoader[] { contextClassLoader, staticClassLoader } : new ClassLoader[] { contextClassLoader }) : new ClassLoader[0];
    }
    
    public static Collection<URL> forPackage(final String name, final ClassLoader... classLoaders) {
        return forResource(resourceName(name), classLoaders);
    }
    
    public static Collection<URL> forResource(final String resourceName, final ClassLoader... classLoaders) {
        final List<URL> result = new ArrayList<URL>();
        final ClassLoader[] classLoaders2;
        final ClassLoader[] loaders = classLoaders2 = classLoaders(classLoaders);
        for (final ClassLoader classLoader : classLoaders2) {
            try {
                final Enumeration<URL> urls = classLoader.getResources(resourceName);
                while (urls.hasMoreElements()) {
                    final URL url = urls.nextElement();
                    final int index = url.toExternalForm().lastIndexOf(resourceName);
                    if (index != -1) {
                        result.add(new URL(url, url.toExternalForm().substring(0, index)));
                    }
                    else {
                        result.add(url);
                    }
                }
            }
            catch (IOException e) {
                if (Reflections.log != null) {
                    Reflections.log.error("error getting resources for " + resourceName, (Throwable)e);
                }
            }
        }
        return distinctUrls(result);
    }
    
    public static URL forClass(final Class<?> aClass, final ClassLoader... classLoaders) {
        final ClassLoader[] loaders = classLoaders(classLoaders);
        final String resourceName = aClass.getName().replace(".", "/") + ".class";
        for (final ClassLoader classLoader : loaders) {
            try {
                final URL url = classLoader.getResource(resourceName);
                if (url != null) {
                    final String normalizedUrl = url.toExternalForm().substring(0, url.toExternalForm().lastIndexOf(aClass.getPackage().getName().replace(".", "/")));
                    return new URL(normalizedUrl);
                }
            }
            catch (MalformedURLException e) {
                if (Reflections.log != null) {
                    Reflections.log.warn("Could not get URL", (Throwable)e);
                }
            }
        }
        return null;
    }
    
    public static Collection<URL> forClassLoader() {
        return forClassLoader(classLoaders(new ClassLoader[0]));
    }
    
    public static Collection<URL> forClassLoader(final ClassLoader... classLoaders) {
        final Collection<URL> result = new ArrayList<URL>();
        final ClassLoader[] classLoaders2;
        final ClassLoader[] loaders = classLoaders2 = classLoaders(classLoaders);
        for (ClassLoader classLoader : classLoaders2) {
            while (classLoader != null) {
                if (classLoader instanceof URLClassLoader) {
                    final URL[] urls = ((URLClassLoader)classLoader).getURLs();
                    if (urls != null) {
                        result.addAll(Arrays.asList(urls));
                    }
                }
                classLoader = classLoader.getParent();
            }
        }
        return distinctUrls(result);
    }
    
    public static Collection<URL> forJavaClassPath() {
        final Collection<URL> urls = new ArrayList<URL>();
        final String javaClassPath = System.getProperty("java.class.path");
        if (javaClassPath != null) {
            for (final String path : javaClassPath.split(File.pathSeparator)) {
                try {
                    urls.add(new File(path).toURI().toURL());
                }
                catch (Exception e) {
                    if (Reflections.log != null) {
                        Reflections.log.warn("Could not get URL", (Throwable)e);
                    }
                }
            }
        }
        return distinctUrls(urls);
    }
    
    public static Collection<URL> forWebInfLib(final ServletContext servletContext) {
        final Collection<URL> urls = new ArrayList<URL>();
        final Set<?> resourcePaths = (Set<?>)servletContext.getResourcePaths("/WEB-INF/lib");
        if (resourcePaths == null) {
            return urls;
        }
        for (final Object urlString : resourcePaths) {
            try {
                urls.add(servletContext.getResource((String)urlString));
            }
            catch (MalformedURLException ex) {}
        }
        return distinctUrls(urls);
    }
    
    public static URL forWebInfClasses(final ServletContext servletContext) {
        try {
            final String path = servletContext.getRealPath("/WEB-INF/classes");
            if (path == null) {
                return servletContext.getResource("/WEB-INF/classes");
            }
            final File file = new File(path);
            if (file.exists()) {
                return file.toURL();
            }
        }
        catch (MalformedURLException ex) {}
        return null;
    }
    
    public static Collection<URL> forManifest() {
        return forManifest(forClassLoader());
    }
    
    public static Collection<URL> forManifest(final URL url) {
        final Collection<URL> result = new ArrayList<URL>();
        result.add(url);
        try {
            final String part = cleanPath(url);
            final File jarFile = new File(part);
            final JarFile myJar = new JarFile(part);
            URL validUrl = tryToGetValidUrl(jarFile.getPath(), new File(part).getParent(), part);
            if (validUrl != null) {
                result.add(validUrl);
            }
            final Manifest manifest = myJar.getManifest();
            if (manifest != null) {
                final String classPath = manifest.getMainAttributes().getValue(new Attributes.Name("Class-Path"));
                if (classPath != null) {
                    for (final String jar : classPath.split(" ")) {
                        validUrl = tryToGetValidUrl(jarFile.getPath(), new File(part).getParent(), jar);
                        if (validUrl != null) {
                            result.add(validUrl);
                        }
                    }
                }
            }
        }
        catch (IOException ex) {}
        return distinctUrls(result);
    }
    
    public static Collection<URL> forManifest(final Iterable<URL> urls) {
        final Collection<URL> result = new ArrayList<URL>();
        for (final URL url : urls) {
            result.addAll(forManifest(url));
        }
        return distinctUrls(result);
    }
    
    static URL tryToGetValidUrl(final String workingDir, final String path, final String filename) {
        try {
            if (new File(filename).exists()) {
                return new File(filename).toURI().toURL();
            }
            if (new File(path + File.separator + filename).exists()) {
                return new File(path + File.separator + filename).toURI().toURL();
            }
            if (new File(workingDir + File.separator + filename).exists()) {
                return new File(workingDir + File.separator + filename).toURI().toURL();
            }
            if (new File(new URL(filename).getFile()).exists()) {
                return new File(new URL(filename).getFile()).toURI().toURL();
            }
        }
        catch (MalformedURLException ex) {}
        return null;
    }
    
    public static String cleanPath(final URL url) {
        String path = url.getPath();
        try {
            path = URLDecoder.decode(path, "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {}
        if (path.startsWith("jar:")) {
            path = path.substring("jar:".length());
        }
        if (path.startsWith("file:")) {
            path = path.substring("file:".length());
        }
        if (path.endsWith("!/")) {
            path = path.substring(0, path.lastIndexOf("!/")) + "/";
        }
        return path;
    }
    
    private static String resourceName(final String name) {
        if (name != null) {
            String resourceName = name.replace(".", "/");
            resourceName = resourceName.replace("\\", "/");
            if (resourceName.startsWith("/")) {
                resourceName = resourceName.substring(1);
            }
            return resourceName;
        }
        return null;
    }
    
    private static Collection<URL> distinctUrls(final Collection<URL> urls) {
        final Map<String, URL> distinct = new LinkedHashMap<String, URL>(urls.size());
        for (final URL url : urls) {
            distinct.put(url.toExternalForm(), url);
        }
        return distinct.values();
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package javassist.scopedpool;

import javassist.CannotCompileException;
import java.security.ProtectionDomain;
import javassist.NotFoundException;
import java.util.Iterator;
import java.util.Map;
import javassist.CtClass;
import javassist.ClassPath;
import javassist.LoaderClassPath;
import java.lang.ref.WeakReference;
import javassist.ClassPool;

public class ScopedClassPool extends ClassPool
{
    protected ScopedClassPoolRepository repository;
    protected WeakReference classLoader;
    protected LoaderClassPath classPath;
    protected SoftValueHashMap softcache;
    boolean isBootstrapCl;
    
    @Deprecated
    protected ScopedClassPool(final ClassLoader cl, final ClassPool src, final ScopedClassPoolRepository repository) {
        this(cl, src, repository, false);
    }
    
    protected ScopedClassPool(final ClassLoader cl, final ClassPool src, final ScopedClassPoolRepository repository, final boolean isTemp) {
        super(src);
        this.softcache = new SoftValueHashMap();
        this.isBootstrapCl = true;
        this.repository = repository;
        this.classLoader = new WeakReference((T)cl);
        if (cl != null) {
            this.insertClassPath(this.classPath = new LoaderClassPath(cl));
        }
        this.childFirstLookup = true;
        if (!isTemp && cl == null) {
            this.isBootstrapCl = true;
        }
    }
    
    @Override
    public ClassLoader getClassLoader() {
        final ClassLoader cl = this.getClassLoader0();
        if (cl == null && !this.isBootstrapCl) {
            throw new IllegalStateException("ClassLoader has been garbage collected");
        }
        return cl;
    }
    
    protected ClassLoader getClassLoader0() {
        return (ClassLoader)this.classLoader.get();
    }
    
    public void close() {
        this.removeClassPath(this.classPath);
        this.classPath.close();
        this.classes.clear();
        this.softcache.clear();
    }
    
    public synchronized void flushClass(final String classname) {
        this.classes.remove(classname);
        this.softcache.remove(classname);
    }
    
    public synchronized void soften(final CtClass clazz) {
        if (this.repository.isPrune()) {
            clazz.prune();
        }
        this.classes.remove(clazz.getName());
        this.softcache.put(clazz.getName(), clazz);
    }
    
    public boolean isUnloadedClassLoader() {
        return false;
    }
    
    @Override
    protected CtClass getCached(final String classname) {
        CtClass clazz = this.getCachedLocally(classname);
        if (clazz == null) {
            boolean isLocal = false;
            final ClassLoader dcl = this.getClassLoader0();
            if (dcl != null) {
                final int lastIndex = classname.lastIndexOf(36);
                String classResourceName = null;
                if (lastIndex < 0) {
                    classResourceName = classname.replaceAll("[\\.]", "/") + ".class";
                }
                else {
                    classResourceName = classname.substring(0, lastIndex).replaceAll("[\\.]", "/") + classname.substring(lastIndex) + ".class";
                }
                isLocal = (dcl.getResource(classResourceName) != null);
            }
            if (!isLocal) {
                final Map registeredCLs = this.repository.getRegisteredCLs();
                synchronized (registeredCLs) {
                    for (final ScopedClassPool pool : registeredCLs.values()) {
                        if (pool.isUnloadedClassLoader()) {
                            this.repository.unregisterClassLoader(pool.getClassLoader());
                        }
                        else {
                            clazz = pool.getCachedLocally(classname);
                            if (clazz != null) {
                                return clazz;
                            }
                            continue;
                        }
                    }
                }
            }
        }
        return clazz;
    }
    
    @Override
    protected void cacheCtClass(final String classname, final CtClass c, final boolean dynamic) {
        if (dynamic) {
            super.cacheCtClass(classname, c, dynamic);
        }
        else {
            if (this.repository.isPrune()) {
                c.prune();
            }
            this.softcache.put(classname, c);
        }
    }
    
    public void lockInCache(final CtClass c) {
        super.cacheCtClass(c.getName(), c, false);
    }
    
    protected CtClass getCachedLocally(final String classname) {
        final CtClass cached = this.classes.get(classname);
        if (cached != null) {
            return cached;
        }
        synchronized (this.softcache) {
            return (CtClass)this.softcache.get(classname);
        }
    }
    
    public synchronized CtClass getLocally(final String classname) throws NotFoundException {
        this.softcache.remove(classname);
        CtClass clazz = this.classes.get(classname);
        if (clazz == null) {
            clazz = this.createCtClass(classname, true);
            if (clazz == null) {
                throw new NotFoundException(classname);
            }
            super.cacheCtClass(classname, clazz, false);
        }
        return clazz;
    }
    
    @Override
    public Class toClass(final CtClass ct, final ClassLoader loader, final ProtectionDomain domain) throws CannotCompileException {
        this.lockInCache(ct);
        return super.toClass(ct, this.getClassLoader0(), domain);
    }
    
    static {
        ClassPool.doPruning = false;
        ClassPool.releaseUnmodifiedClassFile = false;
    }
}

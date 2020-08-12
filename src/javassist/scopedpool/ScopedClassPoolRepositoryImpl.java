// 
// Decompiled by Procyon v0.5.36
// 

package javassist.scopedpool;

import java.util.Iterator;
import java.util.ArrayList;
import javassist.ClassPath;
import javassist.LoaderClassPath;
import java.util.Collections;
import java.util.WeakHashMap;
import javassist.ClassPool;
import java.util.Map;

public class ScopedClassPoolRepositoryImpl implements ScopedClassPoolRepository
{
    private static final ScopedClassPoolRepositoryImpl instance;
    private boolean prune;
    boolean pruneWhenCached;
    protected Map registeredCLs;
    protected ClassPool classpool;
    protected ScopedClassPoolFactory factory;
    
    public static ScopedClassPoolRepository getInstance() {
        return ScopedClassPoolRepositoryImpl.instance;
    }
    
    private ScopedClassPoolRepositoryImpl() {
        this.prune = true;
        this.registeredCLs = Collections.synchronizedMap(new WeakHashMap<Object, Object>());
        this.factory = new ScopedClassPoolFactoryImpl();
        this.classpool = ClassPool.getDefault();
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        this.classpool.insertClassPath(new LoaderClassPath(cl));
    }
    
    @Override
    public boolean isPrune() {
        return this.prune;
    }
    
    @Override
    public void setPrune(final boolean prune) {
        this.prune = prune;
    }
    
    @Override
    public ScopedClassPool createScopedClassPool(final ClassLoader cl, final ClassPool src) {
        return this.factory.create(cl, src, this);
    }
    
    @Override
    public ClassPool findClassPool(final ClassLoader cl) {
        if (cl == null) {
            return this.registerClassLoader(ClassLoader.getSystemClassLoader());
        }
        return this.registerClassLoader(cl);
    }
    
    @Override
    public ClassPool registerClassLoader(final ClassLoader ucl) {
        synchronized (this.registeredCLs) {
            if (this.registeredCLs.containsKey(ucl)) {
                return this.registeredCLs.get(ucl);
            }
            final ScopedClassPool pool = this.createScopedClassPool(ucl, this.classpool);
            this.registeredCLs.put(ucl, pool);
            return pool;
        }
    }
    
    @Override
    public Map getRegisteredCLs() {
        this.clearUnregisteredClassLoaders();
        return this.registeredCLs;
    }
    
    @Override
    public void clearUnregisteredClassLoaders() {
        ArrayList toUnregister = null;
        synchronized (this.registeredCLs) {
            final Iterator it = this.registeredCLs.values().iterator();
            while (it.hasNext()) {
                final ScopedClassPool pool = it.next();
                if (pool.isUnloadedClassLoader()) {
                    it.remove();
                    final ClassLoader cl = pool.getClassLoader();
                    if (cl == null) {
                        continue;
                    }
                    if (toUnregister == null) {
                        toUnregister = new ArrayList();
                    }
                    toUnregister.add(cl);
                }
            }
            if (toUnregister != null) {
                for (int i = 0; i < toUnregister.size(); ++i) {
                    this.unregisterClassLoader(toUnregister.get(i));
                }
            }
        }
    }
    
    @Override
    public void unregisterClassLoader(final ClassLoader cl) {
        synchronized (this.registeredCLs) {
            final ScopedClassPool pool = this.registeredCLs.remove(cl);
            if (pool != null) {
                pool.close();
            }
        }
    }
    
    public void insertDelegate(final ScopedClassPoolRepository delegate) {
    }
    
    @Override
    public void setClassPoolFactory(final ScopedClassPoolFactory factory) {
        this.factory = factory;
    }
    
    @Override
    public ScopedClassPoolFactory getClassPoolFactory() {
        return this.factory;
    }
    
    static {
        instance = new ScopedClassPoolRepositoryImpl();
    }
}

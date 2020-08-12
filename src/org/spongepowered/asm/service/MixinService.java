// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.service;

import org.apache.logging.log4j.LogManager;
import java.util.ServiceConfigurationError;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.util.ServiceLoader;
import org.apache.logging.log4j.Logger;

public final class MixinService
{
    private static final Logger logger;
    private static MixinService instance;
    private ServiceLoader<IMixinServiceBootstrap> bootstrapServiceLoader;
    private final Set<String> bootedServices;
    private ServiceLoader<IMixinService> serviceLoader;
    private IMixinService service;
    
    private MixinService() {
        this.bootedServices = new HashSet<String>();
        this.service = null;
        this.runBootServices();
    }
    
    private void runBootServices() {
        this.bootstrapServiceLoader = ServiceLoader.load(IMixinServiceBootstrap.class, this.getClass().getClassLoader());
        for (final IMixinServiceBootstrap bootService : this.bootstrapServiceLoader) {
            try {
                bootService.bootstrap();
                this.bootedServices.add(bootService.getServiceClassName());
            }
            catch (Throwable th) {
                MixinService.logger.catching(th);
            }
        }
    }
    
    private static MixinService getInstance() {
        if (MixinService.instance == null) {
            MixinService.instance = new MixinService();
        }
        return MixinService.instance;
    }
    
    public static void boot() {
        getInstance();
    }
    
    public static IMixinService getService() {
        return getInstance().getServiceInstance();
    }
    
    private synchronized IMixinService getServiceInstance() {
        if (this.service == null) {
            this.service = this.initService();
            if (this.service == null) {
                throw new ServiceNotAvailableError("No mixin host service is available");
            }
        }
        return this.service;
    }
    
    private IMixinService initService() {
        this.serviceLoader = ServiceLoader.load(IMixinService.class, this.getClass().getClassLoader());
        final Iterator<IMixinService> iter = this.serviceLoader.iterator();
        while (iter.hasNext()) {
            try {
                final IMixinService service = iter.next();
                if (this.bootedServices.contains(service.getClass().getName())) {
                    MixinService.logger.debug("MixinService [{}] was successfully booted in {}", new Object[] { service.getName(), this.getClass().getClassLoader() });
                }
                if (service.isValid()) {
                    return service;
                }
                continue;
            }
            catch (ServiceConfigurationError serviceError) {
                serviceError.printStackTrace();
            }
            catch (Throwable th) {
                th.printStackTrace();
            }
        }
        return null;
    }
    
    static {
        logger = LogManager.getLogger("mixin");
    }
}

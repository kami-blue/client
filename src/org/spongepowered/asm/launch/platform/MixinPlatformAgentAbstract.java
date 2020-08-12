// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.launch.platform;

import org.apache.logging.log4j.LogManager;
import java.io.File;
import java.net.URI;
import org.apache.logging.log4j.Logger;

public abstract class MixinPlatformAgentAbstract implements IMixinPlatformAgent
{
    protected static final Logger logger;
    protected final MixinPlatformManager manager;
    protected final URI uri;
    protected final File container;
    protected final MainAttributes attributes;
    
    public MixinPlatformAgentAbstract(final MixinPlatformManager manager, final URI uri) {
        this.manager = manager;
        this.uri = uri;
        this.container = ((this.uri != null) ? new File(this.uri) : null);
        this.attributes = MainAttributes.of(uri);
    }
    
    @Override
    public String toString() {
        return String.format("PlatformAgent[%s:%s]", this.getClass().getSimpleName(), this.uri);
    }
    
    @Override
    public String getPhaseProvider() {
        return null;
    }
    
    static {
        logger = LogManager.getLogger("mixin");
    }
}

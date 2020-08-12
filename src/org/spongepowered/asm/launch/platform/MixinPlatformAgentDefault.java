// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.launch.platform;

import java.net.URI;

public class MixinPlatformAgentDefault extends MixinPlatformAgentAbstract
{
    public MixinPlatformAgentDefault(final MixinPlatformManager manager, final URI uri) {
        super(manager, uri);
    }
    
    @Override
    public void prepare() {
        final String compatibilityLevel = this.attributes.get("MixinCompatibilityLevel");
        if (compatibilityLevel != null) {
            this.manager.setCompatibilityLevel(compatibilityLevel);
        }
        final String mixinConfigs = this.attributes.get("MixinConfigs");
        if (mixinConfigs != null) {
            for (final String config : mixinConfigs.split(",")) {
                this.manager.addConfig(config.trim());
            }
        }
        final String tokenProviders = this.attributes.get("MixinTokenProviders");
        if (tokenProviders != null) {
            for (final String provider : tokenProviders.split(",")) {
                this.manager.addTokenProvider(provider.trim());
            }
        }
    }
    
    @Override
    public void initPrimaryContainer() {
    }
    
    @Override
    public void inject() {
    }
    
    @Override
    public String getLaunchTarget() {
        return this.attributes.get("Main-Class");
    }
}

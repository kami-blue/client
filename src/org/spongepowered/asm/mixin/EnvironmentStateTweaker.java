// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin;

import org.spongepowered.asm.launch.MixinBootstrap;
import net.minecraft.launchwrapper.LaunchClassLoader;
import java.io.File;
import java.util.List;
import net.minecraft.launchwrapper.ITweaker;

public class EnvironmentStateTweaker implements ITweaker
{
    public void acceptOptions(final List<String> args, final File gameDir, final File assetsDir, final String profile) {
    }
    
    public void injectIntoClassLoader(final LaunchClassLoader classLoader) {
        MixinBootstrap.getPlatform().inject();
    }
    
    public String getLaunchTarget() {
        return "";
    }
    
    public String[] getLaunchArguments() {
        MixinEnvironment.gotoPhase(MixinEnvironment.Phase.DEFAULT);
        return new String[0];
    }
}

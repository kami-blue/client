// 
// Decompiled by Procyon v0.5.36
// 

package com.dazo66.shulkerboxshower.asm;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("")
public class CoreModBase implements IFMLLoadingPlugin
{
    private static boolean isDeo;
    
    public String[] getASMTransformerClass() {
        return new String[] { MainTransformer.class.getName() };
    }
    
    public String getModContainerClass() {
        return null;
    }
    
    @Nullable
    public String getSetupClass() {
        return null;
    }
    
    public void injectData(final Map<String, Object> data) {
        CoreModBase.isDeo = data.get("runtimeDeobfuscationEnabled");
    }
    
    public String getAccessTransformerClass() {
        return AccessTransformer.class.getName();
    }
    
    public static boolean isDeo() {
        return CoreModBase.isDeo;
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.player;

import me.zeroeightsix.kami.module.Module;

@Info(name = "AntiChunkLoadPatch", category = Category.PLAYER, description = "Prevents loading of overloaded chunks", showOnArray = ShowOnArray.OFF)
public class AntiChunkLoadPatch extends Module
{
    private static /* synthetic */ AntiChunkLoadPatch INSTANCE;
    
    public static boolean enabled() {
        return AntiChunkLoadPatch.INSTANCE.isEnabled();
    }
    
    static {
        AntiChunkLoadPatch.INSTANCE = new AntiChunkLoadPatch();
    }
    
    public AntiChunkLoadPatch() {
        AntiChunkLoadPatch.INSTANCE = this;
    }
}

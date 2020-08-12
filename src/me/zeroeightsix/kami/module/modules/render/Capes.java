// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.module.Module;

@Info(name = "Capes", description = "Show fancy Capes", category = Category.RENDER)
public class Capes extends Module
{
    private static Capes INSTANCE;
    
    public Capes() {
        Capes.INSTANCE = this;
    }
    
    public static boolean isActive() {
        return Capes.INSTANCE.isEnabled();
    }
}

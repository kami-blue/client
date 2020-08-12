// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.player;

import me.zeroeightsix.kami.module.Module;

@Info(name = "Fastplace", category = Category.PLAYER, description = "Nullifies block place delay")
public class Fastplace extends Module
{
    @Override
    public void onUpdate() {
        Fastplace.mc.field_71467_ac = 0;
    }
}

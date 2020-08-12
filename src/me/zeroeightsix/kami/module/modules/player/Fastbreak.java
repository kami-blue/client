// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.player;

import me.zeroeightsix.kami.module.Module;

@Info(name = "Fastbreak", category = Category.PLAYER, description = "Nullifies block hit delay")
public class Fastbreak extends Module
{
    @Override
    public void onUpdate() {
        Fastbreak.mc.field_71442_b.field_78781_i = 0;
    }
}

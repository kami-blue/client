// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.module.Module;

@Info(name = "Suicide (KILLS YOU!)", category = Category.MISC, description = "Kills self by running the /kill command")
public class Suicide extends Module
{
    public void onEnable() {
        Suicide.mc.field_71439_g.func_71165_d("/kill");
        this.disable();
    }
}

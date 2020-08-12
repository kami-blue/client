// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;

@Info(name = "AutoJump", category = Category.MOVEMENT, description = "Automatically jumps if possible")
public class AutoJump extends Module
{
    @Override
    public void onUpdate() {
        if (AutoJump.mc.field_71439_g.func_70090_H() || AutoJump.mc.field_71439_g.func_180799_ab()) {
            AutoJump.mc.field_71439_g.field_70181_x = 0.1;
        }
        else if (AutoJump.mc.field_71439_g.field_70122_E) {
            AutoJump.mc.field_71439_g.func_70664_aZ();
        }
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.movement;

import net.minecraft.client.entity.EntityPlayerSP;
import org.lwjgl.input.Keyboard;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Speed", category = Category.MOVEMENT)
public class Speed extends Module
{
    @Override
    public void onUpdate() {
        if (Keyboard.isKeyDown(Speed.mc.field_71474_y.field_74314_A.func_151463_i())) {
            if (Speed.mc.field_71439_g.func_180799_ab() || Speed.mc.field_71439_g.func_70090_H()) {
                final EntityPlayerSP field_71439_g;
                final EntityPlayerSP player = field_71439_g = Speed.mc.field_71439_g;
                field_71439_g.field_70181_x += 0.039000000804662704;
            }
            else if (Speed.mc.field_71439_g.field_70122_E) {
                Speed.mc.field_71439_g.func_70664_aZ();
            }
        }
    }
}

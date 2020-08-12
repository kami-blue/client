// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.render;

import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.module.Module;

@Info(name = "GlowESP", category = Category.RENDER, description = "Gives players glowing effect")
public class GlowESP extends Module
{
    @Override
    public void onUpdate() {
        if (Wrapper.getMinecraft().func_175598_ae().field_78733_k == null) {
            return;
        }
        for (final Entity entity : GlowESP.mc.field_71441_e.field_72996_f) {
            if (entity instanceof EntityPlayer && !entity.func_184202_aL()) {
                entity.func_184195_f(true);
            }
        }
    }
    
    public void onDisable() {
        for (final Entity entity : GlowESP.mc.field_71441_e.field_72996_f) {
            if (entity instanceof EntityPlayer && entity.func_184202_aL()) {
                entity.func_184195_f(false);
            }
        }
        super.onDisable();
    }
}

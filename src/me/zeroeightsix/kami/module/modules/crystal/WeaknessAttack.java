// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.crystal;

import net.minecraft.item.ItemStack;
import java.util.Iterator;
import net.minecraft.item.ItemSword;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.Entity;
import net.minecraft.init.MobEffects;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.Module;

@Info(name = "WeaknessAttack", category = Category.CRYSTAL, description = "Hits entities around you")
public class WeaknessAttack extends Module
{
    int slotBefore;
    int bestSlot;
    
    @Override
    public void onUpdate() {
        if (!this.isEnabled()) {
            return;
        }
        if (ModuleManager.getModuleByName("CrystalAura").isEnabled() && WeaknessAttack.mc.field_71439_g.func_70644_a(MobEffects.field_76437_t)) {
            for (final Entity e : WeaknessAttack.mc.field_71441_e.field_72996_f) {
                if (WeaknessAttack.mc.field_71439_g.func_70068_e(e) <= 36.0 && e instanceof EntityEnderCrystal) {
                    WeaknessAttack.mc.field_71439_g.field_71071_by.field_70461_c = this.bestSlot;
                }
            }
        }
        this.bestSlot = -1;
        final int PrevSlot = WeaknessAttack.mc.field_71439_g.field_71071_by.field_70461_c;
        for (int i = 0; i < 9; ++i) {
            final ItemStack item = WeaknessAttack.mc.field_71439_g.field_71071_by.func_70301_a(i);
            if (item != null && item.func_77973_b() instanceof ItemSword) {
                this.bestSlot = i;
            }
        }
        if (this.bestSlot == -1) {
            return;
        }
        this.slotBefore = WeaknessAttack.mc.field_71439_g.field_71071_by.field_70461_c;
        if (this.slotBefore == -1) {
            return;
        }
    }
}

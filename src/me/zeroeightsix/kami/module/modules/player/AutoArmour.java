// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AutoArmour", category = Category.PLAYER, description = "Automatically equips armour")
public class AutoArmour extends Module
{
    @Override
    public void onUpdate() {
        if (AutoArmour.mc.field_71439_g.field_70173_aa % 2 == 0) {
            return;
        }
        if (AutoArmour.mc.field_71462_r instanceof GuiContainer && !(AutoArmour.mc.field_71462_r instanceof InventoryEffectRenderer)) {
            return;
        }
        final int[] bestArmorSlots = new int[4];
        final int[] bestArmorValues = new int[4];
        for (int armorType = 0; armorType < 4; ++armorType) {
            final ItemStack oldArmor = AutoArmour.mc.field_71439_g.field_71071_by.func_70440_f(armorType);
            if (oldArmor != null && oldArmor.func_77973_b() instanceof ItemArmor) {
                bestArmorValues[armorType] = ((ItemArmor)oldArmor.func_77973_b()).field_77879_b;
            }
            bestArmorSlots[armorType] = -1;
        }
        for (int slot = 0; slot < 36; ++slot) {
            final ItemStack stack = AutoArmour.mc.field_71439_g.field_71071_by.func_70301_a(slot);
            if (stack.func_190916_E() <= 1) {
                if (stack != null) {
                    if (stack.func_77973_b() instanceof ItemArmor) {
                        final ItemArmor armor = (ItemArmor)stack.func_77973_b();
                        final int armorType2 = armor.field_77881_a.ordinal() - 2;
                        if (armorType2 != 2 || !AutoArmour.mc.field_71439_g.field_71071_by.func_70440_f(armorType2).func_77973_b().equals(Items.field_185160_cR)) {
                            final int armorValue = armor.field_77879_b;
                            if (armorValue > bestArmorValues[armorType2]) {
                                bestArmorSlots[armorType2] = slot;
                                bestArmorValues[armorType2] = armorValue;
                            }
                        }
                    }
                }
            }
        }
        for (int armorType = 0; armorType < 4; ++armorType) {
            int slot2 = bestArmorSlots[armorType];
            if (slot2 != -1) {
                final ItemStack oldArmor2 = AutoArmour.mc.field_71439_g.field_71071_by.func_70440_f(armorType);
                if (oldArmor2 == null || oldArmor2 != ItemStack.field_190927_a || AutoArmour.mc.field_71439_g.field_71071_by.func_70447_i() != -1) {
                    if (slot2 < 9) {
                        slot2 += 36;
                    }
                    AutoArmour.mc.field_71442_b.func_187098_a(0, 8 - armorType, 0, ClickType.QUICK_MOVE, (EntityPlayer)AutoArmour.mc.field_71439_g);
                    AutoArmour.mc.field_71442_b.func_187098_a(0, slot2, 0, ClickType.QUICK_MOVE, (EntityPlayer)AutoArmour.mc.field_71439_g);
                    break;
                }
            }
        }
    }
}

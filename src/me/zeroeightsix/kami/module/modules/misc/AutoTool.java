// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import net.minecraft.item.ItemSword;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.item.ItemTool;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityEnderCrystal;
import java.util.function.Predicate;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import me.zero.alpine.listener.EventHandler;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AutoTool", description = "Automatically switch to the best tools when mining or attacking", category = Category.MISC)
public class AutoTool extends Module
{
    @EventHandler
    private Listener<PlayerInteractEvent.LeftClickBlock> leftClickListener;
    @EventHandler
    private Listener<AttackEntityEvent> attackListener;
    
    public AutoTool() {
        this.leftClickListener = new Listener<PlayerInteractEvent.LeftClickBlock>(event -> this.equipBestTool(AutoTool.mc.field_71441_e.func_180495_p(event.getPos())), (Predicate<PlayerInteractEvent.LeftClickBlock>[])new Predicate[0]);
        this.attackListener = new Listener<AttackEntityEvent>(event -> {
            if (!(event.getTarget() instanceof EntityEnderCrystal)) {
                equipBestWeapon();
            }
        }, (Predicate<AttackEntityEvent>[])new Predicate[0]);
    }
    
    private void equipBestTool(final IBlockState blockState) {
        int bestSlot = -1;
        double max = 0.0;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = AutoTool.mc.field_71439_g.field_71071_by.func_70301_a(i);
            if (!stack.field_190928_g) {
                float speed = stack.func_150997_a(blockState);
                if (speed > 1.0f) {
                    final int eff;
                    speed += (float)(((eff = EnchantmentHelper.func_77506_a(Enchantments.field_185305_q, stack)) > 0) ? (Math.pow(eff, 2.0) + 1.0) : 0.0);
                    if (speed > max) {
                        max = speed;
                        bestSlot = i;
                    }
                }
            }
        }
        if (bestSlot != -1) {
            equip(bestSlot);
        }
    }
    
    public static void equipBestWeapon() {
        int bestSlot = -1;
        double maxDamage = 0.0;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = AutoTool.mc.field_71439_g.field_71071_by.func_70301_a(i);
            if (!stack.field_190928_g) {
                if (stack.func_77973_b() instanceof ItemTool) {
                    final double damage = ((ItemTool)stack.func_77973_b()).field_77865_bY + (double)EnchantmentHelper.func_152377_a(stack, EnumCreatureAttribute.UNDEFINED);
                    if (damage > maxDamage) {
                        maxDamage = damage;
                        bestSlot = i;
                    }
                }
                else if (stack.func_77973_b() instanceof ItemSword) {
                    final double damage = ((ItemSword)stack.func_77973_b()).func_150931_i() + (double)EnchantmentHelper.func_152377_a(stack, EnumCreatureAttribute.UNDEFINED);
                    if (damage > maxDamage) {
                        maxDamage = damage;
                        bestSlot = i;
                    }
                }
            }
        }
        if (bestSlot != -1) {
            equip(bestSlot);
        }
    }
    
    private static void equip(final int slot) {
        AutoTool.mc.field_71439_g.field_71071_by.field_70461_c = slot;
        AutoTool.mc.field_71442_b.func_78750_j();
    }
}

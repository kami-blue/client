// 
// Decompiled by Procyon v0.5.36
// 

package com.theundertaker11.enchgapplefinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import java.util.Iterator;
import net.minecraft.world.World;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.Entity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ChestDetection
{
    @SubscribeEvent
    public static void WorldTick(final TickEvent.WorldTickEvent event) {
        if (event.world.field_72995_K) {
            return;
        }
        final World world = event.world;
        EntityPlayer player = null;
        if (!world.field_73010_i.isEmpty()) {
            player = world.field_73010_i.get(0);
            for (final TileEntity tile : world.field_147482_g) {
                if (tile instanceof TileEntityLockableLoot) {
                    final TileEntityLockableLoot lockable = (TileEntityLockableLoot)tile;
                    if (lockable.func_184276_b() == null) {
                        continue;
                    }
                    lockable.func_184281_d(player);
                    for (int i = 0; i < lockable.func_70302_i_(); ++i) {
                        final ItemStack stack = lockable.func_70301_a(i);
                        if (stack.func_77973_b() == Items.field_151153_ao && stack.func_77952_i() == 1) {
                            writeToFile("Dungeon Chest with ench gapple at: " + lockable.func_174877_v().func_177958_n() + " " + lockable.func_174877_v().func_177956_o() + " " + lockable.func_174877_v().func_177952_p());
                        }
                        if (stack.func_77973_b() == Items.field_151134_bR && EnchantmentHelper.func_77506_a(Enchantments.field_185296_A, stack) > 0) {
                            writeToFile("Dungeon Chest with Mending Book: " + lockable.func_174877_v().func_177958_n() + " " + lockable.func_174877_v().func_177956_o() + " " + lockable.func_174877_v().func_177952_p());
                        }
                    }
                }
            }
            for (final Entity entity : world.field_72996_f) {
                if (entity instanceof EntityMinecartContainer) {
                    final EntityMinecartContainer cart = (EntityMinecartContainer)entity;
                    if (cart.func_184276_b() == null) {
                        continue;
                    }
                    cart.func_184288_f(player);
                    for (int i = 0; i < cart.itemHandler.getSlots(); ++i) {
                        final ItemStack stack = cart.itemHandler.getStackInSlot(i);
                        if (stack.func_77973_b() == Items.field_151153_ao && stack.func_77952_i() == 1) {
                            writeToFile("Minecart with ench gapple at: " + cart.field_70165_t + " " + cart.field_70163_u + " " + cart.field_70161_v);
                        }
                        if (stack.func_77973_b() == Items.field_151134_bR && EnchantmentHelper.func_77506_a(Enchantments.field_185296_A, stack) > 0) {
                            writeToFile("Minecart with Mending at: " + cart.field_70165_t + " " + cart.field_70163_u + " " + cart.field_70161_v);
                        }
                    }
                }
            }
        }
    }
    
    protected static void writeToFile(final String coords) {
        try (final FileWriter fw = new FileWriter("AstraMod_egaps.txt", true);
             final BufferedWriter bw = new BufferedWriter(fw);
             final PrintWriter out = new PrintWriter(bw)) {
            out.println(coords);
        }
        catch (IOException ex) {}
    }
}

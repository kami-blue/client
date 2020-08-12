// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.player;

import java.util.Iterator;
import net.minecraft.util.EnumHand;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import me.zeroeightsix.kami.module.Module;

@Info(name = "BannerAura", category = Category.PLAYER, description = "Makes irrelevant fags cry")
public class BannerAura extends Module
{
    public ArrayList<BlockPos> banners;
    int axeSlot;
    
    public BannerAura() {
        this.banners = new ArrayList<BlockPos>();
    }
    
    @Override
    public void onUpdate() {
        if (this.isEnabled()) {
            final Iterable<BlockPos> blocks = (Iterable<BlockPos>)BlockPos.func_177980_a(BannerAura.mc.field_71439_g.func_180425_c().func_177982_a(-5, -5, -5), BannerAura.mc.field_71439_g.func_180425_c().func_177982_a(5, 5, 5));
            for (final BlockPos pos : blocks) {
                if (BannerAura.mc.field_71441_e.func_180495_p(pos).func_177230_c() == Blocks.field_180393_cK) {
                    this.axeSlot = -1;
                    for (int i = 0; i < 9 && this.axeSlot == -1; ++i) {
                        final ItemStack stack = BannerAura.mc.field_71439_g.field_71071_by.func_70301_a(i);
                        if (stack != ItemStack.field_190927_a && stack.func_77973_b() instanceof ItemAxe) {
                            final ItemAxe axe = (ItemAxe)stack.func_77973_b();
                            this.axeSlot = i;
                        }
                    }
                    if (this.axeSlot != -1) {
                        BannerAura.mc.field_71439_g.field_71071_by.field_70461_c = this.axeSlot;
                    }
                    BannerAura.mc.field_71442_b.func_180512_c(pos, BannerAura.mc.field_71439_g.func_174811_aO());
                    BannerAura.mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
                }
            }
            for (final BlockPos pos : blocks) {
                if (BannerAura.mc.field_71441_e.func_180495_p(pos).func_177230_c() == Blocks.field_180394_cL) {
                    this.axeSlot = -1;
                    for (int i = 0; i < 9 && this.axeSlot == -1; ++i) {
                        final ItemStack stack = BannerAura.mc.field_71439_g.field_71071_by.func_70301_a(i);
                        if (stack != ItemStack.field_190927_a && stack.func_77973_b() instanceof ItemAxe) {
                            final ItemAxe axe = (ItemAxe)stack.func_77973_b();
                            this.axeSlot = i;
                        }
                    }
                    if (this.axeSlot != -1) {
                        BannerAura.mc.field_71439_g.field_71071_by.field_70461_c = this.axeSlot;
                    }
                    BannerAura.mc.field_71442_b.func_180512_c(pos, BannerAura.mc.field_71439_g.func_174811_aO());
                    BannerAura.mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
                }
            }
        }
    }
}

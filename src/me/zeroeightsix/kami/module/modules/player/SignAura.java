// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.player;

import java.util.Iterator;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumHand;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import me.zeroeightsix.kami.module.Module;

@Info(name = "SignAura", category = Category.PLAYER, description = "If youre a faggot use this.")
public class SignAura extends Module
{
    public ArrayList<BlockPos> banners;
    int axeSlot;
    
    public SignAura() {
        this.banners = new ArrayList<BlockPos>();
    }
    
    @Override
    public void onUpdate() {
        if (this.isEnabled()) {
            final Iterable<BlockPos> blocks = (Iterable<BlockPos>)BlockPos.func_177980_a(SignAura.mc.field_71439_g.func_180425_c().func_177982_a(-5, -5, -5), SignAura.mc.field_71439_g.func_180425_c().func_177982_a(5, 5, 5));
            for (final BlockPos pos : blocks) {
                if (SignAura.mc.field_71441_e.func_180495_p(pos).func_177230_c() == Blocks.field_150472_an) {
                    this.axeSlot = -1;
                    for (int i = 0; i < 9 && this.axeSlot == -1; ++i) {
                        final ItemStack stack = SignAura.mc.field_71439_g.field_71071_by.func_70301_a(i);
                        if (stack != ItemStack.field_190927_a && stack.func_77973_b() instanceof ItemAxe) {
                            final ItemAxe axe = (ItemAxe)stack.func_77973_b();
                            this.axeSlot = i;
                        }
                    }
                    if (this.axeSlot != -1) {
                        SignAura.mc.field_71439_g.field_71071_by.field_70461_c = this.axeSlot;
                    }
                    SignAura.mc.field_71442_b.func_180512_c(pos, SignAura.mc.field_71439_g.func_174811_aO());
                    SignAura.mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
                }
            }
            for (final BlockPos pos : blocks) {
                if (SignAura.mc.field_71441_e.func_180495_p(pos).func_177230_c() == Blocks.field_150444_as) {
                    this.axeSlot = -1;
                    for (int i = 0; i < 9 && this.axeSlot == -1; ++i) {
                        final ItemStack stack = SignAura.mc.field_71439_g.field_71071_by.func_70301_a(i);
                        if (stack != ItemStack.field_190927_a && stack.func_77973_b() instanceof ItemAxe) {
                            final ItemAxe axe = (ItemAxe)stack.func_77973_b();
                            this.axeSlot = i;
                        }
                    }
                    if (this.axeSlot != -1) {
                        SignAura.mc.field_71439_g.field_71071_by.field_70461_c = this.axeSlot;
                    }
                    SignAura.mc.field_71442_b.func_180512_c(pos, SignAura.mc.field_71439_g.func_174811_aO());
                    SignAura.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayerDigging());
                }
            }
        }
    }
}

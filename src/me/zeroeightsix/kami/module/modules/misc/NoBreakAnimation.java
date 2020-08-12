// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import net.minecraft.network.Packet;
import java.util.Iterator;
import java.util.function.Predicate;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Secret-Mine", category = Category.MISC, description = "Prevents block break animation server side")
public class NoBreakAnimation extends Module
{
    private boolean isMining;
    private BlockPos lastPos;
    private EnumFacing lastFacing;
    @EventHandler
    public Listener<PacketEvent.Send> listener;
    
    public NoBreakAnimation() {
        this.isMining = false;
        this.lastPos = null;
        this.lastFacing = null;
        final CPacketPlayerDigging[] cPacketPlayerDigging = { null };
        final Iterator<Entity> iterator = null;
        final Entity[] entity = { null };
        final Object o;
        final Iterator<Entity> iterator2;
        final Object o2;
        this.listener = new Listener<PacketEvent.Send>(event -> {
            if (event.getPacket() instanceof CPacketPlayerDigging) {
                o[0] = (CPacketPlayerDigging)event.getPacket();
                NoBreakAnimation.mc.field_71441_e.func_72839_b((Entity)null, new AxisAlignedBB(o[0].func_179715_a())).iterator();
                while (iterator2.hasNext()) {
                    o2[0] = (Entity)iterator2.next();
                    if (o2[0] instanceof EntityEnderCrystal) {
                        this.resetMining();
                        return;
                    }
                    else if (o2[0] instanceof EntityLivingBase) {
                        this.resetMining();
                        return;
                    }
                    else {
                        continue;
                    }
                }
                if (o[0].func_180762_c().equals((Object)CPacketPlayerDigging.Action.START_DESTROY_BLOCK)) {
                    this.isMining = true;
                    this.setMiningInfo(o[0].func_179715_a(), o[0].func_179714_b());
                }
                if (o[0].func_180762_c().equals((Object)CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK)) {
                    this.resetMining();
                }
            }
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
    }
    
    @Override
    public void onUpdate() {
        if (!NoBreakAnimation.mc.field_71474_y.field_74312_F.func_151470_d()) {
            this.resetMining();
            return;
        }
        if (this.isMining && this.lastPos != null && this.lastFacing != null) {
            NoBreakAnimation.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.lastPos, this.lastFacing));
        }
    }
    
    private void setMiningInfo(final BlockPos lastPos, final EnumFacing lastFacing) {
        this.lastPos = lastPos;
        this.lastFacing = lastFacing;
    }
    
    public void resetMining() {
        this.isMining = false;
        this.lastPos = null;
        this.lastFacing = null;
    }
}

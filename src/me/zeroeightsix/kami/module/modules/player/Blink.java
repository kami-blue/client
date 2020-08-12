// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import java.util.function.Predicate;
import java.util.LinkedList;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;
import java.util.Queue;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Blink", category = Category.PLAYER, description = "Cancels server side packets")
public class Blink extends Module
{
    Queue<CPacketPlayer> packets;
    @EventHandler
    public Listener<PacketEvent.Send> listener;
    private EntityOtherPlayerMP clonedPlayer;
    
    public Blink() {
        this.packets = new LinkedList<CPacketPlayer>();
        this.listener = new Listener<PacketEvent.Send>(event -> {
            if (this.isEnabled() && event.getPacket() instanceof CPacketPlayer) {
                event.cancel();
                this.packets.add((CPacketPlayer)event.getPacket());
            }
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
    }
    
    @Override
    protected void onEnable() {
        if (Blink.mc.field_71439_g != null) {
            (this.clonedPlayer = new EntityOtherPlayerMP((World)Blink.mc.field_71441_e, Blink.mc.func_110432_I().func_148256_e())).func_82149_j((Entity)Blink.mc.field_71439_g);
            this.clonedPlayer.field_70759_as = Blink.mc.field_71439_g.field_70759_as;
            Blink.mc.field_71441_e.func_73027_a(-100, (Entity)this.clonedPlayer);
        }
    }
    
    @Override
    protected void onDisable() {
        while (!this.packets.isEmpty()) {
            Blink.mc.field_71439_g.field_71174_a.func_147297_a((Packet)this.packets.poll());
        }
        final EntityPlayer localPlayer = (EntityPlayer)Blink.mc.field_71439_g;
        if (localPlayer != null) {
            Blink.mc.field_71441_e.func_73028_b(-100);
            this.clonedPlayer = null;
        }
    }
    
    @Override
    public String getHudInfo() {
        return String.valueOf(this.packets.size());
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.combat;

import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import me.zeroeightsix.kami.KamiMod;
import net.minecraft.world.World;
import net.minecraft.network.play.server.SPacketEntityStatus;
import java.util.function.Predicate;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.TotemPopEvent;
import me.zero.alpine.listener.Listener;
import java.util.HashMap;
import me.zeroeightsix.kami.module.Module;

@Info(name = "TotemPopCounter", description = "Counts the times your enemy pops", category = Category.COMBAT)
public class TotemPopCounter extends Module
{
    private HashMap<String, Integer> popList;
    @EventHandler
    public Listener<TotemPopEvent> totemPopEvent;
    @EventHandler
    public Listener<PacketEvent.Receive> totemPopListener;
    
    public TotemPopCounter() {
        this.popList = new HashMap<String, Integer>();
        int popCounter;
        int newPopCounter;
        this.totemPopEvent = new Listener<TotemPopEvent>(event -> {
            if (this.popList == null) {
                this.popList = new HashMap<String, Integer>();
            }
            if (this.popList.get(event.getEntity().func_70005_c_()) == null) {
                this.popList.put(event.getEntity().func_70005_c_(), 1);
                Command.sendChatMessage("&4" + event.getEntity().func_70005_c_() + " popped " + 1 + " totem!");
            }
            else if (this.popList.get(event.getEntity().func_70005_c_()) != null) {
                popCounter = this.popList.get(event.getEntity().func_70005_c_());
                newPopCounter = ++popCounter;
                this.popList.put(event.getEntity().func_70005_c_(), newPopCounter);
                Command.sendChatMessage("&4" + event.getEntity().func_70005_c_() + " popped " + newPopCounter + " totems!");
            }
            return;
        }, (Predicate<TotemPopEvent>[])new Predicate[0]);
        SPacketEntityStatus packet;
        Entity entity;
        this.totemPopListener = new Listener<PacketEvent.Receive>(event -> {
            if (TotemPopCounter.mc.field_71441_e != null && TotemPopCounter.mc.field_71439_g != null) {
                if (event.getPacket() instanceof SPacketEntityStatus) {
                    packet = (SPacketEntityStatus)event.getPacket();
                    if (packet.func_149160_c() == 35) {
                        entity = packet.func_149161_a((World)TotemPopCounter.mc.field_71441_e);
                        KamiMod.EVENT_BUS.post(new TotemPopEvent(entity));
                    }
                }
            }
        }, (Predicate<PacketEvent.Receive>[])new Predicate[0]);
    }
    
    @Override
    public void onUpdate() {
        for (final EntityPlayer player : TotemPopCounter.mc.field_71441_e.field_73010_i) {
            if (player.func_110143_aJ() <= 0.0f && this.popList.containsKey(player.func_70005_c_())) {
                Command.sendChatMessage("&4" + player.func_70005_c_() + " died after popping " + this.popList.get(player.func_70005_c_()) + " totems!");
                this.popList.remove(player.func_70005_c_(), this.popList.get(player.func_70005_c_()));
            }
        }
    }
}

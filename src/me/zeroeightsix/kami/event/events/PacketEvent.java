// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.event.events;

import net.minecraft.network.Packet;
import me.zeroeightsix.kami.event.KamiEvent;

public class PacketEvent extends KamiEvent
{
    private final Packet packet;
    
    public PacketEvent(final Packet packet) {
        this.packet = packet;
    }
    
    public Packet getPacket() {
        return this.packet;
    }
    
    public static class Receive extends PacketEvent
    {
        public Receive(final Packet packet) {
            super(packet);
        }
    }
    
    public static class Send extends PacketEvent
    {
        public Send(final Packet packet) {
            super(packet);
        }
    }
}

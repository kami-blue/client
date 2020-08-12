// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util.other;

import net.minecraft.network.Packet;

public class EventReceivePacket extends EventCancellable
{
    private Packet packet;
    
    public EventReceivePacket(final EventStage stage, final Packet packet) {
        super(stage);
        this.packet = packet;
    }
    
    public Packet getPacket() {
        return this.packet;
    }
    
    public void setPacket(final Packet packet) {
        this.packet = packet;
    }
}

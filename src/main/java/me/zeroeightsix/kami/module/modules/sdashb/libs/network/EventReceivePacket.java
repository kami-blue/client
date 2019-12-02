package me.zeroeightsix.kami.module.modules.sdashb.libs.network;

import me.zeroeightsix.kami.module.modules.sdashb.libs.EventCancellable;
import net.minecraft.network.Packet;

/**
 * Author Seth
 * 4/6/2019 @ 1:45 PM.
 * https://github.com/seppukudevelopment/seppuku
 */
public class EventReceivePacket extends EventCancellable {

    private Packet packet;

    public EventReceivePacket(EventStage stage, Packet packet) {
        super(stage);
        this.packet = packet;
    }

    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }
}

package org.kamiblue.client.event.events

import net.minecraft.network.Packet
import org.kamiblue.client.event.Cancellable
import org.kamiblue.client.event.Event
import org.kamiblue.client.event.ICancellable

abstract class PacketEvent(val packet: Packet<*>) : Event, ICancellable by Cancellable() {
    class Receive(packet: Packet<*>) : PacketEvent(packet)
    class PostReceive(packet: Packet<*>) : PacketEvent(packet)
    class Send(packet: Packet<*>) : PacketEvent(packet)
    class PostSend(packet: Packet<*>) : PacketEvent(packet)
}
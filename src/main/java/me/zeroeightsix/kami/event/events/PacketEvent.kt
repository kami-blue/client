package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Cancellable
import me.zeroeightsix.kami.event.Event
import me.zeroeightsix.kami.event.ICancellable
import net.minecraft.network.Packet

abstract class PacketEvent(val packet: Packet<*>) : Event, ICancellable by Cancellable() {
    class Receive(packet: Packet<*>) : PacketEvent(packet)
    class Send(packet: Packet<*>) : PacketEvent(packet)
    class PostSend(packet: Packet<*>) : PacketEvent(packet)
}
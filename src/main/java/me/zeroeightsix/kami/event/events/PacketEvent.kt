package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.network.Packet

/**
 * Created by 086 on 13/11/2017.
 */
open class PacketEvent(val packet: Packet<*>) : KamiEvent() {

    class Receive(packet: Packet<*>) : PacketEvent(packet)
    class Send(packet: Packet<*>) : PacketEvent(packet)
    class PostSend(packet: Packet<*>) : PacketEvent(packet)
}
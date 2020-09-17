package me.zeroeightsix.kami.module.modules.misc

import io.netty.buffer.Unpooled
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.CPacketCustomPayload

@Module.Info(
        name = "BeaconSelector",
        category = Module.Category.MISC,
        description = "Choose any of the 5 beacon effects regardless of beacon base height"
)
object BeaconSelector : Module() {
    private var doCancelPacket = true
    var effect = -1

    @EventHandler
    private val packetListener = Listener(EventHook { event: PacketEvent.Send ->
        if (event.packet is CPacketCustomPayload && event.packet.channelName == "MC|Beacon" && doCancelPacket) {
            doCancelPacket = false
            val data = event.packet.bufferData
            /* i1 is actually not unused, reading the int discards the bytes it read, allowing k1 to read the next bytes */
            val i1 = data.readInt() // primary
            val k1 = data.readInt() // secondary
            event.cancel()
            val buf = PacketBuffer(Unpooled.buffer())
            buf.writeInt(effect)
            buf.writeInt(k1)
            mc.player.connection.sendPacket(CPacketCustomPayload("MC|Beacon", buf))
            doCancelPacket = true
        }
    })
}
package me.zeroeightsix.kami.module.modules.misc

import io.netty.buffer.Unpooled
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.CPacketCustomPayload

/**
 * Created by 0x2E | PretendingToCode
 */
@Module.Info(
        name = "BeaconSelector",
        category = Module.Category.MISC,
        description = "Choose any of the 5 beacon effects regardless of beacon base height"
)
class BeaconSelector : Module() {
    private var doCancelPacket = true

    @EventHandler
    var packetListener = Listener(EventHook { event: PacketEvent.Send ->
        if (event.packet is CPacketCustomPayload && (event.packet as CPacketCustomPayload).channelName == "MC|Beacon" && doCancelPacket) {
            doCancelPacket = false
            val data = (event.packet as CPacketCustomPayload).bufferData
            /* i1 is actually not unused, reading the int discards the bytes it read, allowing k1 to read the next bytes */
            val i1 = data.readInt() // primary
            val k1 = data.readInt() // secondary
            event.cancel()
            val buf = PacketBuffer(Unpooled.buffer())
            buf.writeInt(effect)
            buf.writeInt(k1)
            Wrapper.getPlayer().connection.sendPacket(CPacketCustomPayload("MC|Beacon", buf))
            doCancelPacket = true
        }
    })

    companion object {
        @JvmField
        var effect = -1
    }
}
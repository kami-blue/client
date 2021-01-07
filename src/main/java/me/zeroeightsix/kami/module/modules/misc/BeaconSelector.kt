package me.zeroeightsix.kami.module.modules.misc

import io.netty.buffer.Unpooled
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.CPacketCustomPayload
import org.kamiblue.event.listener.listener

object BeaconSelector : Module(
    name = "BeaconSelector",
    category = Category.MISC,
    description = "Choose any of the 5 beacon effects regardless of beacon base height"
) {
    private var doCancelPacket = true
    var effect = -1

    init {
        listener<PacketEvent.Send> {
            if (it.packet !is CPacketCustomPayload || !doCancelPacket || it.packet.channelName != "MC|Beacon") return@listener
            doCancelPacket = false
            it.packet.bufferData.readInt() // primary
            val secondary = it.packet.bufferData.readInt() // secondary
            it.cancel()
            PacketBuffer(Unpooled.buffer()).apply {
                writeInt(effect)
                writeInt(secondary)
            }.also { buffer ->
                mc.player.connection.sendPacket(CPacketCustomPayload("MC|Beacon", buffer))
            }
            doCancelPacket = true
        }
    }
}
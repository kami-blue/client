package org.kamiblue.client.module.modules.player

import net.minecraft.network.play.client.*
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.event.listener.listener

internal object PacketCancel : Module(
    name = "PacketCancel",
    description = "Cancels specific packets used for various actions",
    category = Category.PLAYER
) {
    private val all by setting("All", false)
    private val packetInput by setting("CPacket Input", true, { !all })
    private val packetPlayer by setting("CPacket Player", true, { !all })
    private val packetEntityAction by setting("CPacket Entity Action", true, { !all })
    private val packetUseEntity by setting("CPacket Use Entity", true, { !all })
    private val packetVehicleMove by setting("CPacket Vehicle Move", true, { !all })

    private var numPackets = 0

    override fun getHudInfo(): String {
        return numPackets.toString()
    }

    init {
        listener<PacketEvent.Send> {
            if (all
                || it.packet is CPacketInput && packetInput
                || it.packet is CPacketPlayer && packetPlayer
                || it.packet is CPacketEntityAction && packetEntityAction
                || it.packet is CPacketUseEntity && packetUseEntity
                || it.packet is CPacketVehicleMove && packetVehicleMove
            ) {
                it.cancel()
                numPackets++
            }
        }

        onDisable {
            numPackets = 0
        }
    }
}
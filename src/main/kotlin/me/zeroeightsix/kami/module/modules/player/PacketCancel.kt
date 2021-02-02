package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import net.minecraft.network.play.client.*
import org.kamiblue.event.listener.listener

internal object PacketCancel : Module(
    name = "PacketCancel",
    description = "Cancels specific packets used for various actions",
    category = Category.PLAYER
) {
    private val all by setting("All", false)
    private val packetInput by setting("CPacketInput", true, { !all })
    private val packetPlayer by setting("CPacketPlayer", true, { !all })
    private val packetEntityAction by setting("CPacketEntityAction", true, { !all })
    private val packetUseEntity by setting("CPacketUseEntity", true, { !all })
    private val packetVehicleMove by setting("CPacketVehicleMove", true, { !all })

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
package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import net.minecraft.network.play.client.*
import org.kamiblue.event.listener.listener

object PacketCancel : Module(
    category = Category.PLAYER
) {
    private val all by setting(getTranslationKey("All"), false)
    private val packetInput by setting(getTranslationKey("CPacketInput"), true, { !all })
    private val packetPlayer by setting(getTranslationKey("CPacketPlayer"), true, { !all })
    private val packetEntityAction by setting(getTranslationKey("CPacketEntityAction"), true, { !all })
    private val packetUseEntity by setting(getTranslationKey("CPacketUseEntity"), true, { !all })
    private val packetVehicleMove by setting(getTranslationKey("CPacketVehicleMove"), true, { !all })

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
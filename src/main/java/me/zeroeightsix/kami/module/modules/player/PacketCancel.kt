package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.network.play.client.*

@Module.Info(
        name = "PacketCancel",
        description = "Cancels specific packets used for various actions",
        category = Module.Category.PLAYER
)
object PacketCancel : Module() {
    private val all = register(Settings.b("All", false))
    private val packetInput = register(Settings.booleanBuilder("CPacketInput").withValue(true).withVisibility { !all.value }.build())
    private val packetPlayer = register(Settings.booleanBuilder("CPacketPlayer").withValue(true).withVisibility { !all.value }.build())
    private val packetEntityAction = register(Settings.booleanBuilder("CPacketEntityAction").withValue(true).withVisibility { !all.value }.build())
    private val packetUseEntity = register(Settings.booleanBuilder("CPacketUseEntity").withValue(true).withVisibility { !all.value }.build())
    private val packetVehicleMove = register(Settings.booleanBuilder("CPacketVehicleMove").withValue(true).withVisibility { !all.value }.build())

    private var numPackets = 0

    init {
        listener<PacketEvent.Send> {
            if (mc.player == null) return@listener
            if (all.value
                    || it.packet is CPacketInput && packetInput.value
                    || it.packet is CPacketPlayer && packetPlayer.value
                    || it.packet is CPacketEntityAction && packetEntityAction.value
                    || it.packet is CPacketUseEntity && packetUseEntity.value
                    || it.packet is CPacketVehicleMove && packetVehicleMove.value
            ) {
                it.cancel()
                numPackets++
            }
        }
    }

    public override fun onDisable() {
        numPackets = 0
    }

    override fun getHudInfo(): String {
        return numPackets.toString()
    }
}
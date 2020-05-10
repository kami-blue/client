package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.network.play.client.*

/**
 * @author dominikaaaa
 */
@Module.Info(
        name = "PacketCancel",
        description = "Cancels specific packets used for various actions",
        category = Module.Category.PLAYER
)
class PacketCancel : Module() {
    private val all = register(Settings.b("All", false))
    private val packetInput = register(Settings.booleanBuilder("CPacketInput").withValue(true).withVisibility { !all.value }.build())
    private val packetPlayer = register(Settings.booleanBuilder("CPacketPlayer").withValue(true).withVisibility { !all.value }.build())
    private val packetEntityAction = register(Settings.booleanBuilder("CPacketEntityAction").withValue(true).withVisibility { !all.value }.build())
    private val packetUseEntity = register(Settings.booleanBuilder("CPacketUseEntity").withValue(true).withVisibility { !all.value }.build())
    private val packetVehicleMove = register(Settings.booleanBuilder("CPacketVehicleMove").withValue(true).withVisibility { !all.value }.build())
    private var numPackets = 0

    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (all.value
                ||
                packetInput.value && event.packet is CPacketInput
                ||
                packetPlayer.value && event.packet is CPacketPlayer
                ||
                packetEntityAction.value && event.packet is CPacketEntityAction
                ||
                packetUseEntity.value && event.packet is CPacketUseEntity
                ||
                packetVehicleMove.value && event.packet is CPacketVehicleMove) {
            event.cancel()
            numPackets++
        }
    })

    public override fun onDisable() {
        numPackets = 0
    }

    override fun getHudInfo(): String {
        return numPackets.toString()
    }
}
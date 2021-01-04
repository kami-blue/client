package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import net.minecraft.network.play.client.*
import org.kamiblue.event.listener.listener

@Module.Info(
        name = "PacketCancel",
        description = "Cancels specific packets used for various actions",
        category = Module.Category.PLAYER
)
object PacketCancel : Module() {
    private val all = setting("All", false)
    private val packetInput = setting("CPacketInput", true, { !all.value })
    private val packetPlayer = setting("CPacketPlayer", true, { !all.value })
    private val packetEntityAction = setting("CPacketEntityAction", true, { !all.value })
    private val packetUseEntity = setting("CPacketUseEntity", true, { !all.value })
    private val packetVehicleMove = setting("CPacketVehicleMove", true, { !all.value })

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
package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.network.play.server.SPacketPlayerPosLook

@Module.Info(
        name = "AntiForceLook",
        category = Module.Category.PLAYER,
        description = "Stops server packets from turning your head"
)

object AntiForceLook : Module() {
    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketPlayerPosLook || mc.player == null) return@listener
            val packet = it.packet
            packet.yaw = mc.player.rotationYaw
            packet.pitch = mc.player.rotationPitch
        }
    }
}
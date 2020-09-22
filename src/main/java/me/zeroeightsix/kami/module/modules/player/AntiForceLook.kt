package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import net.minecraft.network.play.server.SPacketPlayerPosLook

@Module.Info(
        name = "AntiForceLook",
        category = Module.Category.PLAYER,
        description = "Stops server packets from turning your head"
)

object AntiForceLook : Module() {
    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (mc.player == null) return@EventHook
        if (event.packet is SPacketPlayerPosLook) {
            val packet = event.packet
            packet.yaw = mc.player.rotationYaw
            packet.pitch = mc.player.rotationPitch
        }
    })
}
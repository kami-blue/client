package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.network.play.client.CPacketAnimation

@Module.Info(
        name = "NoSwing",
        category = Module.Category.PLAYER,
        description = "Cancels server and client swinging packets"
)
object NoSwing : Module() {
    init {
        listener<PacketEvent.Send> {
            if (it.packet is CPacketAnimation) it.cancel()
        }
    }
}
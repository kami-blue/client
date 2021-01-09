package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import net.minecraft.network.play.client.CPacketCloseWindow
import org.kamiblue.event.listener.listener

object XCarry : Module(
    category = Category.PLAYER,
) {
    init {
        listener<PacketEvent.Send> {
            if (it.packet is CPacketCloseWindow) it.cancel()
        }
    }
}
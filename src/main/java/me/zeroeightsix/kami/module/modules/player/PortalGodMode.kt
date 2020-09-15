package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import net.minecraft.network.play.client.CPacketConfirmTeleport

@Module.Info(
        name = "PortalGodMode",
        category = Module.Category.PLAYER,
        description = "Don't take damage in portals"
)
object PortalGodMode : Module() {
    @EventHandler
    private val listener = Listener(EventHook { event: PacketEvent.Send ->
        if (event.packet is CPacketConfirmTeleport) {
            event.cancel()
        }
    })
}
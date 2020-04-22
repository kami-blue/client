package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import net.minecraft.network.play.client.CPacketConfirmTeleport

/**
 * Created by GlowskiBroski on 10/14/2018.
 */
@Module.Info(
        name = "PortalGodMode",
        category = Module.Category.PLAYER,
        description = "Don't take damage in portals"
)
class PortalGodMode : Module() {
    @EventHandler
    var listener = Listener(EventHook { event: PacketEvent.Send ->
        if (isEnabled && event.packet is CPacketConfirmTeleport) {
            event.cancel()
        }
    })
}
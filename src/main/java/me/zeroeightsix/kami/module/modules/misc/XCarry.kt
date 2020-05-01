package me.zeroeightsix.kami.module.modules.misc

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import net.minecraft.network.play.client.CPacketCloseWindow

/**
 * @author Hamburger2k
 */
@Module.Info(
        name = "XCarry",
        category = Module.Category.PLAYER,
        description = "Store items in crafting slots",
        showOnArray = Module.ShowOnArray.OFF
)
class XCarry : Module() {
    @EventHandler
    private val l = Listener(EventHook { event: PacketEvent.Send ->
        if (event.packet is CPacketCloseWindow) {
            event.cancel()
        }
    })
}
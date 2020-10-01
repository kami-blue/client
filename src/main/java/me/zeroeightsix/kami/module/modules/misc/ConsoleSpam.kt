package me.zeroeightsix.kami.module.modules.misc

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.client.CPacketUpdateSign
import net.minecraft.tileentity.TileEntitySign

@Module.Info(
        name = "ConsoleSpam",
        description = "Spams Spigot consoles by sending invalid UpdateSign packets",
        category = Module.Category.MISC
)
object ConsoleSpam : Module() {
    override fun onEnable() {
        MessageSendHelper.sendChatMessage("$chatName Every time you right click a sign, a warning will appear in console.")
        MessageSendHelper.sendChatMessage("$chatName Use an autoclicker to automate this process.")
    }

    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (event.packet is CPacketPlayerTryUseItemOnBlock) {
            val location = event.packet.pos
            mc.player.connection.sendPacket(CPacketUpdateSign(location, TileEntitySign().signText))
        }
    })
}
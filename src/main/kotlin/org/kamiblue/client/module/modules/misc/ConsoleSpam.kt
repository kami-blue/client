package org.kamiblue.client.module.modules.misc

import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.client.CPacketUpdateSign
import net.minecraft.tileentity.TileEntitySign
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.event.listener.listener

internal object ConsoleSpam : Module(
    name = "ConsoleSpam",
    description = "Spams Spigot consoles by sending invalid UpdateSign packets",
    category = Category.MISC
) {
    init {
        onEnable {
            MessageSendHelper.sendChatMessage("$chatName Every time you right click a sign, a warning will appear in console.")
            MessageSendHelper.sendChatMessage("$chatName Use an auto clicker to automate this process.")
        }

        listener<PacketEvent.Send> {
            if (it.packet !is CPacketPlayerTryUseItemOnBlock) return@listener
            mc.player.connection.sendPacket(CPacketUpdateSign(it.packet.pos, TileEntitySign().signText))
        }
    }
}
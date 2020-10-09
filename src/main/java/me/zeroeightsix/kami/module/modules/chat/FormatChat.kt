package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.Minecraft
import net.minecraft.network.play.client.CPacketChatMessage

@Module.Info(
        name = "FormatChat",
        description = "Add colour and linebreak support to upstream chat packets",
        category = Module.Category.CHAT
)
object FormatChat : Module() {
    override fun onEnable() {
        if (Minecraft.getMinecraft().getCurrentServerData() == null) {
            MessageSendHelper.sendWarningMessage("$chatName &6&lWarning: &r&6This does not work in singleplayer")
            disable()
        } else {
            MessageSendHelper.sendWarningMessage("$chatName &6&lWarning: &r&6This will kick you on most servers!")
        }
    }

    init {
        listener<PacketEvent.Send> {
            if (it.packet !is CPacketChatMessage || mc.player == null) return@listener
            var message = it.packet.message

            if (message.contains("&") || message.contains("#n")) {
                message = message.replace("&".toRegex(), KamiMod.colour.toString() + "")
                message = message.replace("#n".toRegex(), "\n")
                mc.player.connection.sendPacket(CPacketChatMessage(message))
                it.cancel()
            }
        }
    }
}
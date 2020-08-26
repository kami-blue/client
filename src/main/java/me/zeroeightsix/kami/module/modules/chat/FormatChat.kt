package me.zeroeightsix.kami.module.modules.chat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.Minecraft
import net.minecraft.network.play.client.CPacketChatMessage

/**
 * Created on 16 December by 0x2E | PretendingToCode
 */
@Module.Info(
        name = "FormatChat",
        description = "Add colour and linebreak support to upstream chat packets",
        category = Module.Category.CHAT
)
class FormatChat : Module() {
    public override fun onEnable() {
        if (Minecraft.getMinecraft().getCurrentServerData() == null) {
            MessageSendHelper.sendWarningMessage("$chatName &6&lWarning: &r&6This does not work in singleplayer")
            disable()
        } else {
            MessageSendHelper.sendWarningMessage("$chatName &6&lWarning: &r&6This will kick you on most servers!")
        }
    }

    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (event.packet is CPacketChatMessage) {
            var message = (event.packet as CPacketChatMessage).message

            if (message.contains("&") || message.contains("#n")) {
                message = message.replace("&".toRegex(), KamiMod.colour.toString() + "")
                message = message.replace("#n".toRegex(), "\n")
                mc.player.connection.sendPacket(CPacketChatMessage(message))
                event.cancel()
            }
        }
    })
}
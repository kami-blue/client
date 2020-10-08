package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageDetectionHelper
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage
import net.minecraft.network.play.server.SPacketChat

@Module.Info(
        name = "AutoReply",
        description = "Automatically reply to direct messages",
        category = Module.Category.CHAT
)
object AutoReply : Module() {
    val customMessage = register(Settings.b("CustomMessage", false))
    val message = register(Settings.stringBuilder("CustomText").withValue("Use &7" + Command.getCommandPrefix() + "autoreply&r to modify this").withVisibility { customMessage.value })

    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketChat || !MessageDetectionHelper.isDirect(true, it.packet.getChatComponent().unformattedText)) return@listener
            if (customMessage.value) {
                sendServerMessage("/r " + message.value)
            } else {
                sendServerMessage("/r I just automatically replied, thanks to KAMI Blue's AutoReply module!")
            }
        }
    }
}
package me.zeroeightsix.kami.module.modules.chat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.misc.AntiAFK
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.text.MessageDetectionHelper
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.network.play.server.SPacketChat

@Module.Info(
        name = "AutoReply",
        description = "Automatically reply to direct messages",
        category = Module.Category.CHAT
)
object AutoReply : Module() {
    val customMessage = register(Settings.b("CustomMessage", false))
    val message = register(Settings.stringBuilder("CustomText").withValue("Use &7" + Command.getCommandPrefix() + "autoreply&r to modify this").withConsumer { _: String?, _: String? -> }.withVisibility { customMessage.value }.build())

    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (AntiAFK.isEnabled && AntiAFK.autoReply.value) return@EventHook

        if (event.packet is SPacketChat && MessageDetectionHelper.isDirect(true, event.packet.getChatComponent().unformattedText)) {
            if (customMessage.value) {
                MessageSendHelper.sendServerMessage("/r " + message.value)
            } else {
                MessageSendHelper.sendServerMessage("/r I just automatically replied, thanks to KAMI Blue's AutoReply module!")
            }
        }
    })
}